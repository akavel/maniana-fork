/*
 * Copyright (C) 2011 The original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.zapta.apps.maniana.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.controller.MainMenuEntry;
import com.zapta.apps.maniana.controller.StartupKind;
import com.zapta.apps.maniana.model.persistence.ModelLoadingResult;
import com.zapta.apps.maniana.model.persistence.ModelPersistence;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * The main activity of this app.
 * 
 * @author Tal Dayan
 */
public class MainActivity extends Activity {

    private AppContext mApp;

    /** Used to pass resume action from onNewIntent() to onResume(). */
    private ResumeAction mResumeAction = ResumeAction.NONE;

    /** Called by the Android framework to initialize the activity. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // App context Ties all the app pieces together.
        mApp = new AppContext(this);

        // Load model from file
        final ModelLoadingResult modelLoadResult = ModelPersistence.loadModelDataFile(
                mApp.context(), mApp.model());

        final StartupKind startupKind;
        switch (modelLoadResult.outcome) {
            case FILE_READ_OK: {
                final int oldVersionCode = modelLoadResult.metadata.writerVersionCode;
                final int newVersionCode = mApp.services().getAppVersionCode();
                final boolean isSameVersion = (oldVersionCode == newVersionCode);
                final boolean isSilentUpgrade = isSilentUpgrade(oldVersionCode, newVersionCode);
                startupKind = isSameVersion ? StartupKind.NORMAL
                        : (isSilentUpgrade ? StartupKind.NEW_VERSION_SILENT
                                : StartupKind.NEW_VERSION_ANNOUNCE);
                break;
            }
            case FILE_NOT_FOUND: {
                // If this returnes an error, the model is guaranteed to be cleared.
                final ModelLoadingResult sampleLoadingResult = ModelPersistence
                        .loadSampleModelAsset(mApp.context(), mApp.model());
                startupKind = (sampleLoadingResult.outcome.isOk()) ? StartupKind.NEW_USER
                        : StartupKind.SAMPLE_DATA_ERROR;
                // Prevent moving item from Tomorow to Today.
                mApp.model().setLastPushDateStamp(mApp.dateTracker().getDateStampString());
                break;
            }

            default:
                LogUtil.error("Unknown model loading outcome: " + modelLoadResult.outcome);
                // Falling through intentionally.
            case FILE_HAS_ERRORS:
                mApp.model().clear();
                mApp.model().setLastPushDateStamp(mApp.dateTracker().getDateStampString());
                startupKind = StartupKind.MODEL_DATA_ERROR;
        }

        // Inform the view about the model data change
        mApp.view().updatePages();

        // Set top view of this activity
        setContentView(mApp.view().getRootView());

        // Track resume action from the launch intent
        trackResumeAction(getIntent());

        // Tell the controller the app was just created.
        mApp.controller().onMainActivityCreated(startupKind);

    }

    /** Is this a minor upgrade that should supress the startup message? */
    private static final boolean isSilentUpgrade(int oldVersionCode, int newVersionCode) {
        // Return here true if combination of old and new version code does not warrant
        // bothering some users with the What's New popup.

        // By default, upgrdes are not silent.
        return false;
    }

    /** Called by the framework when the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Tell the controller the app is being destroyed.
        mApp.controller().onMainActivityDestroy();
        // Make sure we release the preferences listener.
        mApp.pref().release();
    }

    /** Called by the framework when user opened the app main menu. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // For the old style option menu
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

        // NOTE: alternative implementation to use the ICS popup menu instead. We don't
        // use it because it shows the menu at the bottom of the screen far from the
        // menu overlow button (bad user experience)
        // IcsMainMenuDialog.showMenu(mApp);
        // return false;
    }

    /** Called by the framework when the user make a selection in the app main menu. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainMenuEntry mainMenuEntry;

        switch (item.getItemId()) {
            case R.id.main_menu_settings:
                mainMenuEntry = MainMenuEntry.SETTINGS;
                break;
            case R.id.main_menu_help:
                mainMenuEntry = MainMenuEntry.HELP;
                break;
            case R.id.main_menu_about:
                mainMenuEntry = MainMenuEntry.ABOUT;
                break;
            default:
                LogUtil.error("Unknown option menu item id: " + item.getTitle());
                return true;
        }

        mApp.controller().onMainMenuSelection(mainMenuEntry);
        return true;
    }

    /** Called by the framework when this activity is paused. */
    @Override
    protected void onPause() {
        super.onPause();
        mResumeAction = ResumeAction.NONE;
        // Inform the controller.
        mApp.controller().onMainActivityPause();
    }

    /** Called by the framework when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();

        // Get the action for this resume
        final ResumeAction thisResumeAction = mResumeAction;
        mResumeAction = ResumeAction.NONE;

        // Inform the controller
        mApp.controller().onMainActivityResume(thisResumeAction);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean eventHandled = false;
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            eventHandled = mApp.controller().onBackButton();
        }
        return eventHandled || super.onKeyDown(keyCode, event);
    }

    /** Delegates sub sctivities result to the controller. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mApp.controller().onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Called when the activity receives an intent. Used to detect launches from list widget action
     * buttons.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        trackResumeAction(intent);
    }

    /** Update the resume action from the given launch intent. */
    private final void trackResumeAction(Intent launchIntent) {
        // TODO: should we condition test first that the intent is a launcher intent?
        mResumeAction = ResumeAction.fromIntent(launchIntent);
    }
}
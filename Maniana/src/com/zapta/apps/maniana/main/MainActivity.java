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

import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;

import com.zapta.apps.maniana.controller.MainActivityStartupKind;
import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.persistence.ModelReadingResult;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * The main activity of this app.
 * 
 * @author Tal Dayan
 */
public class MainActivity extends Activity {

    private MainActivityState mState;

    /** Used to pass resume action from onNewIntent() to onResume(). */
    private MainActivityResumeAction mResumeAction = MainActivityResumeAction.NONE;

    /** Contains the intent that triggered mResumeAction. Null FF action = NONE. */
    @Nullable
    private Intent mResumeIntent = null;

    /** Called by the Android framework to initialize the activity. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // App context Ties all the app pieces together.
        mState = new MainActivityState(this);

        // Load model from file
        final ModelReadingResult modelLoadResult = ModelPersistence.readModelFile(mState.context(),
                mState.model());

        final MainActivityStartupKind startupKind;
        switch (modelLoadResult.outcome) {
            case FILE_READ_OK: {
                final int oldVersionCode = modelLoadResult.metadata.writerVersionCode;
                final int newVersionCode = mState.services().getAppVersionCode();
                final boolean isSameVersion = (oldVersionCode == newVersionCode);
                final boolean isSilentUpgrade = isSilentUpgrade(oldVersionCode, newVersionCode);
                startupKind = isSameVersion ? MainActivityStartupKind.NORMAL
                        : (isSilentUpgrade ? MainActivityStartupKind.NEW_VERSION_SILENT
                                : MainActivityStartupKind.NEW_VERSION_ANNOUNCE);
                break;
            }
            case FILE_NOT_FOUND: {
                // Model should be empty here
                startupKind = MainActivityStartupKind.NEW_USER;
                // Prevent moving item from Tomorow to Today.
                mState.model().setLastPushDateStamp(mState.dateTracker().getDateStampString());
                break;
            }

            default:
                LogUtil.error("Unknown model loading outcome: " + modelLoadResult.outcome);
                // Falling through intentionally.
            case FILE_HAS_ERRORS:
                mState.model().clear();
                mState.model().setLastPushDateStamp(mState.dateTracker().getDateStampString());
                startupKind = MainActivityStartupKind.MODEL_DATA_ERROR;
        }

        // Inform the view about the model data change
        mState.view().updatePages();

        // Set top view of this activity
        setContentView(mState.view().getRootView());

        // Track resume action from the launch intent
        trackResumeAction(getIntent());

        // Tell the controller the app was just created.
        mState.controller().onMainActivityCreated(startupKind);

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
        mState.controller().onMainActivityDestroy();
        // Make sure we release the preferences listener.
        mState.prefTracker().release();
    }

    /** Called by the framework once when user opened the app main menu the first time. */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // For the old style option menu
//        final MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        return true;
//
//        // NOTE: alternative implementation to use the ICS popup menu instead. We don't
//        // use it because it shows the menu at the bottom of the screen far from the
//        // menu overlow button (bad user experience)
//        // IcsMainMenuDialog.showMenu(mApp);
//        // return false;
//    }

//    /** Called each time before the menu is shown. */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
////        final MenuItem debugItem = menu.findItem(R.id.main_menu_debug);
////        debugItem.setVisible(mState.debugController().isDebugMode());
////        return super.onPrepareOptionsMenu(menu);
//        LogUtil.debug("*** onPrepareOptionsMenu called");
//        mState.controller().onMenuButton();
//        return false;
//    }

//    /** Called by the framework when the user make a selection in the app main menu. */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        @Nullable
//        final MainMenuEntry mainMenuEntry = MainMenuEntry.byMainMenuId(item.getItemId());
//        
//        if (mainMenuEntry == null) {
//            LogUtil.error("Unknown option menu item id: " + item.getTitle());
//            return true;
//        }
//
//        mState.controller().onMainMenuSelection(mainMenuEntry);
//        return true;
//    }

    /** Called by the framework when this activity is paused. */
    @Override
    protected void onPause() {
        super.onPause();
        mResumeIntent = null;
        mResumeAction = MainActivityResumeAction.NONE;
        // Inform the controller.
        mState.controller().onMainActivityPause();
    }

    /** Called by the framework when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();

        // Get the action for this resume
        final Intent thisResumeIntent = mResumeIntent;
        final MainActivityResumeAction thisResumeAction = mResumeAction;

        mResumeIntent = null;
        mResumeAction = MainActivityResumeAction.NONE;

        // Inform the controller
        mState.controller().onMainActivityResume(thisResumeAction, thisResumeIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.debug("*** onKeyDown: %d", keyCode);
        
        boolean eventHandled = false;
        
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    eventHandled = mState.controller().onBackButton();
                    break;  
                case KeyEvent.KEYCODE_MENU:
                    eventHandled = mState.controller().onMenuButton();
                    break;
            }
        }

        return eventHandled || super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        LogUtil.debug("*** dispatchKeyEvent: %d", event.getKeyCode());
        return super.dispatchKeyEvent(event);
    }

    /** Delegates sub sctivities result to the controller. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mState.controller().onActivityResult(requestCode, resultCode, intent);
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
        mResumeIntent = launchIntent;
        mResumeAction = MainActivityResumeAction.fromIntent(mState, launchIntent);
    }
}

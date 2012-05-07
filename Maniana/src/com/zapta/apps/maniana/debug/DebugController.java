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

package com.zapta.apps.maniana.debug;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.zapta.apps.maniana.help.PopupMessageActivity;
import com.zapta.apps.maniana.help.PopupMessageActivity.MessageKind;
import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.settings.PreferenceKind;
import com.zapta.apps.maniana.util.NotificationUtil;
import com.zapta.apps.maniana.view.IcsMainMenuDialog;

/**
 * Controller for the debug functionality.
 * 
 * @author Tal Dayan
 */
public class DebugController {

    private final AppContext mApp;

    public DebugController(AppContext mApp) {
        this.mApp = mApp;
    }

    /** Call this one to allow the user to select a debug command. */
    public final void startMainDialog() {
        DebugDialog.startDialog(mApp, "Debug", DebugCommandMain.values(),
                new DebugDialogListener<DebugCommandMain>() {
                    @Override
                    public void onDebugCommand(DebugCommandMain command) {
                        onDebugCommandMain(command);
                    }
                });
    }

    private final void onDebugCommandMain(DebugCommandMain command) {
        switch (command) {
            case NOTIFICATIONS:
                startNotificationDialog();
                break;
            case NEW_USER:
                mApp.context().startActivity(
                        PopupMessageActivity.intentFor(mApp.context(), MessageKind.NEW_USER));
                break;
            case ICS_MENU:
                IcsMainMenuDialog.showMenu(mApp);
                break;
            case ON_SHAKE:
                mApp.controller().onShake();
                break;
            case EXIT:
                setDebugMode(false);
                break;
            default:
                mApp.services().toast("Not implemented: " + command);
        }
    }

    private final void startNotificationDialog() {
        DebugDialog.startDialog(mApp, "Debug Notifications", DebugCommandNotification.values(),
                new DebugDialogListener<DebugCommandNotification>() {
                    @Override
                    public void onDebugCommand(DebugCommandNotification command) {
                        onDebugCommandNotification(command);
                    }
                });
    }

    private final void onDebugCommandNotification(DebugCommandNotification command) {
        switch (command) {
            case NOTIFICATION_SINGLE:
                NotificationUtil.sendPendingItemsNotification(mApp.context(), 1);
                break;
            case NOTIFICATION_MULTI:
                NotificationUtil.sendPendingItemsNotification(mApp.context(), 17);
                break;
            case NOTIFICATION_CLEAR:
                NotificationUtil.clearPendingItemsNotification(mApp.context());
                break;
            default:
                mApp.services().toast("Not implemented: " + command);
        }
    }

    /** Write a persisted debug mode flag value */
    public final void setDebugMode(boolean flag) {
        mApp.services().toast("Debug mode: " + (flag ? "ON" : "OFF"));
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mApp.context());
        final Editor editor = sharedPreferences.edit();
        editor.putBoolean(PreferenceKind.DEBUG_MODE.getKey(), flag);
        editor.commit();
    }

    /** Read the persisted debug mode flag value. */
    public final boolean isDebugMode() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApp
                .context());
        return mSharedPreferences.getBoolean(PreferenceKind.DEBUG_MODE.getKey(), false);
    }
}

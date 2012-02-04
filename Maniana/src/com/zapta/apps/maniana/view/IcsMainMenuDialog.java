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

package com.zapta.apps.maniana.view;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.controller.MainMenuEntry;
import com.zapta.apps.maniana.main.AppContext;

/**
 * Main menu dialog for API > 10 (e.g. Ice Cream Sandwich). The old option menu
 * has been deprecated for these API levels.
 * 
 * @author Tal Dayan
 */
public class IcsMainMenuDialog {

    /** Do not instantiate */
    private IcsMainMenuDialog() {
    }

    public static final void showMenu(final AppContext app) {
        final Dialog dialog = new Dialog(app.context(), R.style.IcsMainMenuStyle);
        dialog.setContentView(R.layout.ics_menu_dialog_layout);

        for (MainMenuEntry entry : MainMenuEntry.values()) {
            final MainMenuEntry finalEntry = entry;
            final TextView textView = (TextView) dialog.findViewById(menuEntryViewId(entry));
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuEntryClicked(app, dialog, finalEntry);
                }
            });
        }

        dialog.show();
    }

    private static final void menuEntryClicked(AppContext app, Dialog dialog, MainMenuEntry entry) {
//        LogUtil.debug("Clicked on item: %s", entry);
        dialog.dismiss();
        app.controller().onMainMenuSelection(entry);
    }

    /** Map menu entry enum value to menu dialog text view ids. */
    private static final int menuEntryViewId(MainMenuEntry entry) {
        switch (entry) {
            case SETTINGS:
                return R.id.ics_menu_settings;
            case HELP:
                return R.id.ics_menu_help;
            case ABOUT:
                return R.id.ics_menu_about;
            default:
                throw new RuntimeException("Unknown menu entry: " + entry);
        }
    }

}

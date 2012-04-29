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

package com.zapta.apps.maniana.controller;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.R;

/**
 * Entries of the main menu.
 * 
 * @author Tal Dayan
 */
public enum MainMenuEntry {
    SETTINGS(R.id.main_menu_settings, R.id.ics_menu_settings),
    HELP(R.id.main_menu_help, R.id.ics_menu_help),
    ABOUT(R.id.main_menu_about, R.id.ics_menu_about),
    DEBUG(R.id.main_menu_debug, R.id.ics_menu_debug);

    public final int mainMenuEntryId;
    public final int icsMenuEntryId;

    private MainMenuEntry(int mainMenuEntryId, int icsMenuEntryId) {
        this.mainMenuEntryId = mainMenuEntryId;
        this.icsMenuEntryId = icsMenuEntryId;
    }

    @Nullable
    public static final MainMenuEntry byMainMenuId(int mainMenuEntryId) {
        for (MainMenuEntry entry : MainMenuEntry.values()) {
            if (mainMenuEntryId == entry.mainMenuEntryId) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static final MainMenuEntry byIcsMenuId(int icsMenuEntryId) {
        for (MainMenuEntry entry : MainMenuEntry.values()) {
            if (icsMenuEntryId == entry.icsMenuEntryId) {
                return entry;
            }
        }
        return null;
    }

}

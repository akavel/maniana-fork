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

package com.zapta.apps.maniana.settings;

/**
 * Default preference values. All values should match the default values in Preferences.xml
 * 
 * @author Tal Dayan
 */
public interface PreferenceConstants {
    // Sound
    public static final boolean DEFAULT_ALLOWS_SOUND_EFFECTS = true;
    public static final ApplauseLevel DEFAULT_APPPLAUSE_LEVEL = ApplauseLevel.ALWAYS;

    // Behavior
    public static final boolean DEFAULT_STARTUP_ANIMATION = true;
    public static final boolean DEFAULT_VERBOSE_MESSAGES = true;
    public static final boolean DEFAULT_AUTO_SORT = true;
    public static final boolean DEFAULT_AUTO_DAILY_CLEANUP = true;
    public static final LockExpirationPeriod DEFAULT_LOCK_PERIOD = LockExpirationPeriod.NEVER;

    // Page
    public static final boolean DEFAULT_PAGE_BACKGROUND_PAPER = true;
    public static final int DEFAULT_PAGE_PAPER_COLOR = 0xffffffff;
    public static final int DEFAULT_PAGE_BACKGROUND_SOLID_COLOR = 0xffffffc0;
    public static final ItemFontType DEFAULT_PAGE_FONT_TYPE = ItemFontType.CURSIVE;
    public static final int DEFAULT_PAGE_FONT_SIZE = 16;
    public static final int DEFAULT_ITEM_TEXT_COLOR = 0xff000000;
    public static final int DEFAULT_COMPLETED_ITEM_TEXT_COLOR = 0xff888888;
    public static final int DEFAULT_PAGE_ITEM_DIVIDER_COLOR = 0xffffddaa;

    // Widget
    public static final boolean DEFAULT_WIDGET_BACKGROUND_PAPER = true;
    public static final int DEFAULT_WIDGET_PAPER_COLOR = 0xffffffff;
    public static final int DEFAULT_WIDGET_BACKGROUND_COLOR = 0x44000000;
    public static final ItemFontType DEFAULT_WIDGET_FONT_TYPE = ItemFontType.CURSIVE;
    public static final int DEFAULT_WIDGET_ITEM_FONT_SIZE = 14;
    public static final boolean DEFAULT_WIDGET_AUTO_FIT = false;
    public static final int DEFAULT_WIDGET_TEXT_COLOR = 0xff444444;
    public static final boolean DEFAULT_WIDGET_SHOW_COMPLETED_ITEMS = false;
    public static final int DEFAULT_WIDGET_ITEM_COMPLETED_TEXT_COLOR = 0xff888888;
    public static final boolean DEFAULT_WIDGET_SHOW_TOOLBAR = true;
    public static final boolean DEFAULT_WIDGET_SINGLE_LINE = false;
    
    /** Android name sapce used in XML docs. */
    public static final String ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android";

}
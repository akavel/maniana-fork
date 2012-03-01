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

package com.zapta.apps.maniana.preferences;

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
    public static final boolean DEFAULT_AUTO_SORT = false;  
    public static final boolean DEFAULT_AUTO_DAILY_CLEANUP = true; 
    public static final LockExpirationPeriod DEFAULT_LOCK_PERIOD = LockExpirationPeriod.NEVER;
    
    // Page
    public static final PageBackgroundType DEFAULT_PAGE_BACKGROUND_TYPE = PageBackgroundType.PAPER;
    public static final int DEFAULT_PAGE_BACKGROUND_SOLID_COLOR = 0xffffffc0;    
    public static final ItemFontType DEFAULT_PAGE_FONT_TYPE = ItemFontType.CURSIVE;
    public static final ItemFontSize DEFAULT_PAGE_FONT_SIZE = ItemFontSize.NORMAL;  
    public static final int DEFAULT_ITEM_TEXT_COLOR = 0xff000000;
    public static final int DEFAULT_COMPLETED_ITEM_TEXT_COLOR = 0xff888888;   
    public static final int DEFAULT_PAGE_ITEM_DIVIDER_COLOR = 0xffffddaa;
    
    // Widget
    public static final WidgetBackgroundType DEFAULT_WIDGET_BACKGROUND_TYPE = WidgetBackgroundType.PAPER;
    public static final int DEFAULT_WIDGET_BACKGROUND_COLOR = 0x44000000;
    public static final ItemFontType DEFAULT_WIDGET_FONT_TYPE = ItemFontType.CURSIVE;
    public static final ItemFontSize DEFAULT_WIDGET_ITEM_FONT_SIZE = ItemFontSize.NORMAL;
    public static final int DEFAULT_WIDGET_TEXT_COLOR = 0xff000000;
    public static final boolean DEFAULT_WIDGET_SHOW_TOOLBAR = true;    
    public static final boolean DEFAULT_WIDGET_SINGLE_LINE = false;
}
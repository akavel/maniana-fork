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

import javax.annotation.Nullable;

import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;

/**
 * Represents the preference items.
 * 
 * DO NOT REUSE THESE OLD KEYS: - prefBackgroundKey - prefVoiceRecognitionKey
 * 
 * @author Tal Dayan
 */
public enum PreferenceKind implements KeyedEnum {
    // Sound
    SOUND_ENABLED("prefAllowSoundKey"),
    APPLAUSE_LEVEL("prefApplauseLevelKey"),

    // Behavior
    STARTUP_ANIMATION("prefStartupAnimationKey"),
    VERBOSE_MESSAGES("prefVerboseMessagesKey"),
    AUTO_SORT("prefAutoSortKey"),
    AUTO_DAILY_CLEANUP("prefAutoDailyCleanupKey"),
    LOCK_PERIOD("prefLockPeriodKey"),

    // Page
    PAGE_SELECT_THEME("prefPageSelectThemeKey"),
    PAGE_BACKGROUND_TYPE("prefPageBackgroundTypeKey"),
    PAGE_BACKGROUND_SOLID_COLOR("prefPageBackgroundSolidColorKey"),
    PAGE_ITEM_FONT_TYPE("prefItemFontKey"),
    PAGE_ITEM_FONT_SIZE("prefItemFontSizeKey"),
    PAGE_ITEM_ACTIVE_TEXT_COLOR("prefPageTextColorKey"),
    PAGE_ITEM_COMPLETED_TEXT_COLOR("prefPageCompletedTextColorKey"),
    PAGE_ITEM_DIVIDER_COLOR("prefPageItemDividerColorKey"), 

    // Widget
    WIDGET_SELECT_THEME("prefWidgetSelectThemeKey"),    
    WIDGET_BACKGROUND_TYPE("prefWidgetBackgroundTypeKey"),
    WIDGET_BACKGROUND_COLOR("prefWidgetBackgroundColorKey"),
    WIDGET_ITEM_FONT_TYPE("prefWidgetItemFontKey"),
    WIDGET_ITEM_TEXT_COLOR("prefWidgetTextColorKey"),
    WIDGET_ITEM_FONT_SIZE("prefWidgetItemFontSizeKey"),
    WIDGET_SHOW_TOOLBAR("prefWidgetShowToolbarKey"),
    WIDGET_SINGLE_LINE("prefWidgetSingleLineKey"),   
    WIDGET_PORTRAIT_WIDTH_ADJUST("prefWidgetPortraitWidthAdjustKey"),
    WIDGET_PORTRAIT_HEIGHT_ADJUST("prefWidgetPortraitHeightAdjustKey"),
    WIDGET_LANDSCAPE_WIDTH_ADJUST("prefWidgetLandscapeWidthAdjustKey"),
    WIDGET_LANDSCAPE_HEIGHT_ADJUST("prefWidgetLandscapeHeightAdjustKey"),
    
    // Miscellaneous
    VERSION_INFO("prefVersionInfoKey"),
    SHARE("prefShareKey"),
    FEEDBACK("prefFeedbackKey"),
    RESTORE_DEFAULTS("prefRestoreDefaultsKey");

    /** Preference item key. Persisted. Change only if must. Must match preferences XML definitions. */
    private final String mKey;

    private PreferenceKind(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, null if not found. */
    @Nullable
    public final static PreferenceKind fromKey(String key) {
        return EnumUtil.fromKey(key, PreferenceKind.values(), null);
    }
}

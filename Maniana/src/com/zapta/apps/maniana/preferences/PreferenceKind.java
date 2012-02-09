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
 * DO NOT REUSE THESE OLD KEYS: 
 *   - prefBackgroundKey
 *   - prefVoiceRecognitionKey
 *   
 * @author Tal Dayan
 */
public enum PreferenceKind implements KeyedEnum {
    SOUND_ENABLED("prefAllowSoundKey"),
    APPLAUSE_LEVEL("prefApplauseLevelKey"),
    AUTO_SORT("prefAutoSortKey"),
    ITEM_FONT_TYPE("prefItemFontKey"),
    ITEM_FONT_SIZE("prefItemFontSizeKey"),
    ITEM_ACTIVE_TEXT_COLOR("prefPageTextColorKey"),
    ITEM_COMPLETED_TEXT_COLOR("prefPageCompletedTextColorKey"),
    PAGE_BACKGROUND_TYPE("prefPageBackgroundTypeKey"),
    PAGE_BACKGROUND_SOLID_COLOR("prefPageBackgroundSolidColorKey"),
    PAGE_ITEM_DIVIDER_COLOR("prefPageItemDividerColorKey"),
    LOCK_PERIOD("prefLockPeriodKey"),
    VERBOSE_MESSAGES("prefVerboseMessagesKey"),
    STARTUP_ANIMATION("prefStartupAnimationKey"),
    WIDGET_SINGLE_LINE("prefWidgetSingleLineKey"),
    WIDGET_BACKGROUND_COLOR("prefWidgetBackgroundColorKey"),
    WIDGET_TEXT_COLOR("prefWidgetTextColorKey"),
    VERSION_INFO("prefVersionInfoKey"),
    SHARE("prefShareKey"),
    FEEDBACK("prefFeedbackKey"),
    RESTORE_DEFAULTS("prefRestoreDefaultsKey");

    /** Preference item key. Persisted. Change only if must. Must match preferences XML definitions. */
    private final String mKey;

    private PreferenceKind(String key) {
        this.mKey = key;
    }

    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, null if not found. */
    @Nullable
    public final static PreferenceKind fromKey(String key) {
        return EnumUtil.fromKey(key, PreferenceKind.values(), null);
    }
}

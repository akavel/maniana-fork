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

import javax.annotation.Nullable;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;

/**
 * Represents possible values of Font preference.
 * 
 * @author Tal Dayan
 */
public enum PageIconSet implements KeyedEnum {
    // NOTE: keys are persisted in preferences. Do not modify.
    HAND_DRAWN(
            "Hand Drawn",
            "handdrawn",
            R.drawable.button_undo1,
            R.drawable.button_add_by_text1,
            R.drawable.button_add_by_voice1,
            R.drawable.button_clean1),
    MODERN(
            "Modern",
            "modern",
            R.drawable.button_undo2,
            R.drawable.button_add_by_text2,
            R.drawable.button_add_by_voice2,
            R.drawable.button_clean2),

    PARTY(
            "Party",
            "party",
            R.drawable.button_undo3,
            R.drawable.button_add_by_text3,
            R.drawable.button_add_by_voice3,
            R.drawable.button_clean3),
    WHITE(
            "White Silhouette",
            "white",
            R.drawable.button_undo4,
            R.drawable.button_add_by_text4,
            R.drawable.button_add_by_voice4,
            R.drawable.button_clean4),

    BLACK(
            "Black Silhouette",
            "black",
            R.drawable.button_undo5,
            R.drawable.button_add_by_text5,
            R.drawable.button_add_by_voice5,
            R.drawable.button_clean5);

    /** User visible name. */
    public final String name;

    public final int buttonUndoResourceId;
    public final int buttonAddByTextResourceId;
    public final int buttonAddByVoiceResourceId;
    public final int buttonCleanResourceId;

    /**
     * Preference value key. Should match the values in preference xml. Persisted in user's
     * settings.
     */
    private final String mKey;

    private PageIconSet(String name, String key, int buttonUndoResourceId,
            int buttonAddByTextResourceId, int buttonAddByVoiceResourceId, int buttonCleanResourceId) {
        this.name = name;
        this.mKey = key;

        this.buttonUndoResourceId = buttonUndoResourceId;
        this.buttonAddByTextResourceId = buttonAddByTextResourceId;
        this.buttonAddByVoiceResourceId = buttonAddByVoiceResourceId;
        this.buttonCleanResourceId = buttonCleanResourceId;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static PageIconSet fromKey(String key, @Nullable PageIconSet fallBack) {
        return EnumUtil.fromKey(key, PageIconSet.values(), fallBack);
    }
}
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
 * Represents possible values of Font Size preference. 
 * 
 * @author Tal Dayan
 */
public enum FontSize implements KeyedEnum {
    SMALL1("small1", 0.8f),
    NORMAL("normal", 1.0f),
    LARGE1("large1", 1.3f),
    LARGE2("large2", 1.6f);

    /** Preference value key. Should match the values in preference xml. */
    private final String mKey;
    
    /** Magnification factor. 1.0f is normal. */
    private final float mFactor;

    private FontSize(String key, float factor) {
        this.mKey = key;
        this.mFactor = factor;
    }

    public final String getKey() {
        return mKey;
    }
    
    public final float getFactor() {
        return mFactor;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static FontSize fromKey(String key, @Nullable FontSize fallBack) {
        return EnumUtil.fromKey(key, FontSize.values(), fallBack);
    }
}
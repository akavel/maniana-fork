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
 * Represents possible values of Font preference. 
 * 
 * @author
 */
public enum PageItemFontType implements KeyedEnum {
    CURSIVE("cursive"),
    ELEGANT("elegant"),
    SAN_SERIF("sans"),
    SERIF("serif");

    /** Preference value key. Should match the values in preference xml. */
    private final String mKey;

    private PageItemFontType(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static PageItemFontType fromKey(String key, @Nullable PageItemFontType fallBack) {
        return EnumUtil.fromKey(key, PageItemFontType.values(), fallBack);
    }
}
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

package com.zapta.apps.maniana.model;

import javax.annotation.Nullable;

import android.graphics.Color;

/**
 * The colors set used to flag items.
 * 
 * @author Tal Dayan
 */
public enum ItemColor {
    // Item order determines the color sequence the user gets when tapping the screen.
    NONE("none", Color.TRANSPARENT),
    RED("red", 0xffff0000),
    BLUE("blue", 0xff0077ff),
    GREEN("green", 0xff00aa00);

    /** The key used for serialization. Not user visible. Should be consistent. */
    private final String mKey;
    
    // TODO: move this out of the model, to the view. 
    /** The android color of this enum value. */
    private final int mColor;

    private ItemColor(String key, int color) {
        mColor = color;
        mKey = key;
    }

    /**
     * Map a key to an ItemColor. Used for item deserialization.
     * 
     * @param key the input key.
     * @param fallBack the color to use if key did not match any color.
     * @return the matched color of falLBack if no match.
     */
    @Nullable
    public final static ItemColor fromKey(String key, @Nullable ItemColor fallBack) {
        // TODO: use map?
        for (ItemColor itemColor : ItemColor.values()) {
            if (key.equals(itemColor.mKey)) {
                return itemColor;
            }
        }
        return fallBack;
    }

    /** Return the cyclic next color. The next color of the last color is the first color. */
    public final ItemColor nextCyclicColor() {
        final int nextIndex = (this.ordinal() + 1) % ItemColor.values().length;
        return ItemColor.values()[nextIndex];
    }

    public final String getKey() {
        return mKey;
    }

    public final int getColor() {
        return mColor;
    }
    
    public final boolean isNone() {
        return (this == NONE);
    }
    
    
}

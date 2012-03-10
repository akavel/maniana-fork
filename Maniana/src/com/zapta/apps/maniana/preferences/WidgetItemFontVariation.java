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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Represents parameters of selected font for widget items text.
 * 
 * Instances of this class cache the various parameters needed to set the currently selected item
 * font. This class is immutable.
 * 
 * @author Tal Dayan
 */
public class WidgetItemFontVariation {
    // TODO: share code with PageItemFontVariation. E.g. using a common base class.

    /** Path to page title fonts relative to assets directory. */
    private static final String CURSIVE_ITEM_FONT_ASSET_PATH = "fonts/Vavont/Vavont-modified.ttf";

    private static final String ELEGANT_ITEM_FONT_ASSET_PATH = "fonts/Pompiere/Pompiere-Regular-modified.ttf";

    private final Typeface mTypeFace;
    private final int mColor;
    private final float mTextSize;
    private final float mLineSpacingMultiplier;

    /**
     * Construct a new variation.
     * 
     * @param typeFace
     *            the typeface t use
     * @param color
     *            the text color for non completed items.
     * @param colorCompleted
     *            the text color for completed items.
     * @param textSize
     *            the text size.
     * @param lineSpacingMultiplier
     *            The line spacing multiplier to use.
     * @param topBottomPadding
     *            padding (in dip) at top and bottom of text.
     */
    private WidgetItemFontVariation(Typeface typeFace, int color, float textSize,
            float lineSpacingMultiplier) {
        this.mTypeFace = typeFace;
        this.mColor = color;
        // this.mColorCompleted = colorCompleted;
        this.mTextSize = textSize;
        // this.mTopBottomPadding = topBottomPadding;
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
    }

    /**
     * Apply this font variation to given text view.
     * 
     * @param textView
     *            the item's text view.
     * @param isCompleted
     *            true if the item is completed.
     */
    public void apply(TextView textView) {
        textView.setTypeface(mTypeFace);
        textView.setTextColor(mColor);
        textView.setTextSize(mTextSize);
        textView.setLineSpacing(0.0f, mLineSpacingMultiplier);
    }

    public static final WidgetItemFontVariation newFromCurrentPreferences(Context context,
            SharedPreferences sharedPreferences) {
        final ItemFontType fontType = PreferencesTracker
                .readWidgetFontTypeFontTypePreference(sharedPreferences);
        final int color = PreferencesTracker.readWidgetTextColorPreference(sharedPreferences);

        final int rawFontSize = PreferencesTracker
                .readWidgetItemFontSizePreference(sharedPreferences);
        final int fontSize = (int) (rawFontSize * fontType.scale);


        switch (fontType) {
            case CURSIVE:
                return new WidgetItemFontVariation(Typeface.createFromAsset(context.getAssets(),
                        CURSIVE_ITEM_FONT_ASSET_PATH), color, fontSize, 0.9f);
            case ELEGANT:
                return new WidgetItemFontVariation(Typeface.createFromAsset(context.getAssets(),
                        ELEGANT_ITEM_FONT_ASSET_PATH), color, fontSize, 0.9f);
            case SAN_SERIF:
                return new WidgetItemFontVariation(Typeface.SANS_SERIF, color, fontSize, 1.1f);
            case SERIF:
                return new WidgetItemFontVariation(Typeface.SERIF, color, fontSize, 1.1f);
            default:
                throw new RuntimeException("Unknown widget font type: " + fontType);
        }
    }
}

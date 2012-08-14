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

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.view.ExtendedEditText;
import com.zapta.apps.maniana.view.ExtendedTextView;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Represents parameters of selected font for page items text.
 * 
 * Instances of this class cache the various parameters needed to set the currently selected item
 * font. This class is immutable.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ItemFontVariation {

    private final Typeface mTypeFace;
    private final int mColor;
    private final int mColorCompleted;
    private final int mTextSize;
    private final float mLineSpacingMultiplier;
    private final float mLastLineExtraSpacingFraction;

    /**
     * Construct a new variation.
     * 
     * @param typeFace the typeface t use
     * @param color the text color for non completed items.
     * @param colorCompleted the text color for completed items.
     * @param textSize the text size.
     * @param lineSpacingMultiplier The line spacing multiplier to use.
     * @param lastLineExtraSpacingFraction extra spacing, in fraction of line height, to add to last
     *        line. Used to avoid truncation if line spacing multiplier is smaller than 1.
     */
    private ItemFontVariation(Typeface typeFace, int color, int colorCompleted, int textSize,
            float lineSpacingMultiplier, float lastLineExtraSpacingFraction) {
        this.mTypeFace = typeFace;
        this.mColor = color;
        this.mColorCompleted = colorCompleted;
        this.mTextSize = textSize;
        this.mLineSpacingMultiplier = lineSpacingMultiplier;
        this.mLastLineExtraSpacingFraction = lastLineExtraSpacingFraction;
    }

    public void apply(ExtendedTextView extendedTextView, boolean isCompleted, boolean applyAlsoColor) {
        applyCommon(extendedTextView, isCompleted, applyAlsoColor);
        extendedTextView.setLastLineExtraSpacingFraction(mLastLineExtraSpacingFraction);
    }

    public void apply(ExtendedEditText extendedEditText, boolean isCompleted, boolean applyAlsoColor) {
        applyCommon(extendedEditText, isCompleted, applyAlsoColor);
        extendedEditText.setLastLineExtraSpacingFraction(mLastLineExtraSpacingFraction);
    }

    /**
     * Apply this font variation to given text/edit view. setLastLineExtraSpacingFraction()
     * is done latter by the caller.
     * 
     * @param textView the item's text view.
     * @param isCompleted true if the item is completed.
     * @param applyAlsoColor determines if color should be set.
     */
    private void applyCommon(TextView textView, boolean isCompleted, boolean applyAlsoColor) {
        textView.setTypeface(mTypeFace);
        if (applyAlsoColor) {
            textView.setTextColor(isCompleted ? mColorCompleted : mColor);
        }
        textView.setTextSize(mTextSize);
        textView.setLineSpacing(0.0f, mLineSpacingMultiplier);

        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public static final ItemFontVariation newFromPagePreferences(Context context,
            PreferencesTracker prefTracker) {
        final Font font = prefTracker.getItemFontPreference();
        final int color = prefTracker.getPageItemActiveTextColorPreference();
        final int completedColor = prefTracker.getPageItemCompletedTextColorPreference();

        final int rawFontSize = prefTracker.getItemFontSizePreference();
        final int fontSize = (int) (rawFontSize * font.scale);

        return new ItemFontVariation(font.getTypeface(context), color, completedColor, fontSize,
                font.lineSpacingMultipler, font.lastLineExtraSpacingFraction);
    }

    public static final ItemFontVariation newFromWidgetPreferences(Context context,
            PreferencesReader prefReader) {
        final Font font = prefReader.getWidgetFontPreference();
        final int color = prefReader.getWidgetTextColorPreference();
        final int completedColor = prefReader.getWidgetCompletedTextColorPreference();

        final int rawFontSize = prefReader.getWidgetItemFontSizePreference();
        final int fontSize = (int) (rawFontSize * font.scale);

        return new ItemFontVariation(font.getTypeface(context), color, completedColor, fontSize,
                font.lineSpacingMultipler, font.lastLineExtraSpacingFraction);
    }

    public final int getTextSize() {
        return mTextSize;
    }
}

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

package com.zapta.apps.maniana.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Provides few tweaks of the stock EditText. Similar to ExtendedTextView.
 * 
 * @author Tal Dayan
 */
public class ExtendedEditText extends EditText {

    private float mLastLineExtraSpacingFraction = 0.0f;

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedEditText(Context context) {
        super(context);
    }

    /**
     * Similar to ExtendedTextView.
     */
    public void setLastLineExtraSpacingFraction(float lastLineExtraSpacingFraction) {
        // TODO: exact comparisons of floats. Is is safe?
        if (mLastLineExtraSpacingFraction != lastLineExtraSpacingFraction) {
            mLastLineExtraSpacingFraction = lastLineExtraSpacingFraction;
            invalidate();
        }
    }

    public float getLastLineExtraSpacingFraction() {
        return mLastLineExtraSpacingFraction;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: exact comparisons of floats. Is is safe?
        if (mLastLineExtraSpacingFraction != 0) {
            final int extraHeightPixels = (int) (getTextSize() * mLastLineExtraSpacingFraction);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + extraHeightPixels);
        }
    }

}

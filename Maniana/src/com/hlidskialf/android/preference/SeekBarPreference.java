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

package com.hlidskialf.android.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * Custom preference for selecting an integer within range.
 * 
 * Downloaded from http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * 
 * @Author Matthew Wiggins
 * @author Tal Dayan
 */
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private final Context mContext;
    private final String mDialogMessage;
    private final String mSuffix;
    private final int mDefault;
    private final int mMin;
    private final int mMax;
    private int mValue;
    
    private SeekBar mSeekBar;
    private TextView mValueText;


    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns, "text");
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 50);
        mMin = attrs.getAttributeIntValue(androidns, "minLevel", 0);
        mMax = attrs.getAttributeIntValue(androidns, "maxLevel", 100);
        mValue = shouldPersist() ? getPersistedInt(mDefault) : mDefault;
    }

    /** Dialog preference */
    @Override
    protected View onCreateDialogView() {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        final TextView dialogMessageTextView = new TextView(mContext);
        if (mDialogMessage != null) {
            dialogMessageTextView.setText(mDialogMessage);
        }
        layout.addView(dialogMessageTextView);

        mValueText = new TextView(mContext);

        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setPadding(0, 40, 0, 0);
        mValueText.setTextSize(64);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setPadding(0, 40, 0, 40);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue - mMin);
        return layout;
    }

    /** DialogPreference */
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        // NOTE: SeekBar values are zero relative so we offset them accordingly
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue - mMin);
    }

    /** Preference */
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? getPersistedInt(mDefault) : mDefault;
        } else {
            mValue = (Integer) defaultValue;
        }
    }

    /** Called from mSeekBar. */
    @Override
    public void onProgressChanged(SeekBar seek, int seekBarValue, boolean fromTouch) {
        final int currentValue = seekBarValue + mMin;
        final String t = String.valueOf(currentValue);
        mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
    }

    /** Called from mSeekBar. */
    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // Nothing to do here
    }

    /** Called from mSeekBar. */
    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // Nothing to do here
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        LogUtil.debug("*** onDialogCloses(%s) called", positiveResult);
        super.onDialogClosed(positiveResult);
        // We accept changes only if the user clicked OK
        if (positiveResult) {
            final int newValue = mSeekBar.getProgress() + mMin;
            if (newValue != mValue) {
                mValue = newValue;
                if (shouldPersist()) {
                    persistInt(mValue);
                }
                // TODO: the javadoc of this method says it should be called before persisting
                // and its return value should control the persistence.
                callChangeListener(new Integer(mValue));
            }
        }
    }     
}

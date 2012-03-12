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

    /** Format string for running label in dialog. Should contain %d. Attribute: text */
    private final String mLabelFormat;

    /** Default value. Attribute: defaultValue */
    private final int mDefaultValue;

    /** Min value (inclusive). Attribute: minLevel */
    private final int mMinValue;

    /** Max value, (inclusive). Attribute: maxLevel */
    private final int mMaxValue;

    /** Current preference value. Updated when the user OKs a new value. */
    private int mValue;

    /**
     * Format string for preference summary string (when dialog is closed). Can contain a single %d
     * for current value.
     */
    private String mSummaryFormat;

    /** The SeekBar in the dialog. */
    private SeekBar mSeekBar;

    /** The running value string field in the dialog. */
    private TextView mValueTextView;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mLabelFormat = attrs.getAttributeValue(androidns, "text");
        mDefaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", 50);
        mMinValue = attrs.getAttributeIntValue(androidns, "minLevel", 0);
        mMaxValue = attrs.getAttributeIntValue(androidns, "maxLevel", 100);
        mValue = shouldPersist() ? getPersistedInt(mDefaultValue) : mDefaultValue;
        mSummaryFormat = attrs.getAttributeValue(androidns, "summary");
        updateSummaryWithCurrentValue();
    }

    /** Dialog preference */
    @Override
    protected View onCreateDialogView() {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        mValueTextView = new TextView(mContext);
        mValueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueTextView.setPadding(0, 15, 0, 0);
        mValueTextView.setTextSize(64);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueTextView, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setPadding(0, 40, 0, 40);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mValue - mMinValue);
        return layout;
    }

    /** DialogPreference */
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        // NOTE: SeekBar values are zero relative so we offset them accordingly
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mValue - mMinValue);
    }

    /** Preference */
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? getPersistedInt(mDefaultValue) : mDefaultValue;
        } else {
            mValue = (Integer) defaultValue;
        }
        updateSummaryWithCurrentValue();
    }

    /** Called from mSeekBar. */
    @Override
    public void onProgressChanged(SeekBar seek, int seekBarValue, boolean fromTouch) {
        final int currentValue = seekBarValue + mMinValue;
        mValueTextView.setText(String.format(mLabelFormat, currentValue));
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
        super.onDialogClosed(positiveResult);
        // We accept changes only if the user clicked OK
        if (positiveResult) {
            final int newValue = mSeekBar.getProgress() + mMinValue;
            setValue(newValue);
        }
    }

    public void setValue(int newValue) {
        if (newValue != mValue) {
            // Enforce range
            mValue = Math.min(mMaxValue, Math.max(mMinValue, newValue));
            updateSummaryWithCurrentValue();
            if (shouldPersist()) {
                persistInt(mValue);
            }
            // TODO: the javadoc of this method says it should be called before persisting
            // and its return value should control the persistence.
            callChangeListener(new Integer(mValue));
        }
    }

    private final void updateSummaryWithCurrentValue() {
        super.setSummary(String.format(mSummaryFormat, mValue));
    }

    @Override
    public void setSummary(CharSequence summary) {
        // TODO(tal): capture base summary and expand with current value.
        super.setSummary(summary + " (TBD)");
    }

    @Override
    public void setSummary(int summaryResId) {
        // TODO: read resource into a string and append current value.
        super.setSummary(summaryResId);
    }
}

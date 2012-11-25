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

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class TaskColorsPreference extends Preference implements
        Preference.OnPreferenceClickListener {

    public TaskColorsPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TaskColorsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public TaskColorsPreference(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

       // Log.i("Maniana", "*** TaskColorsPreferences clicked");

        // TODO(tal): create and show a TaskColorsDialog()

        final TaskColorsPreferenceDialog dialog = new TaskColorsPreferenceDialog(getContext());

        // ColorPickerDialog picker = new ColorPickerDialog(getContext(), getValue());
        // // TAL: added propagation of title to dialog
        // picker.setTitle(getTitle());
        //
        // picker.setOnColorChangedListener(this);
        // if (mAlphaSliderEnabled) {
        // picker.setAlphaSliderVisible(true);
        // }
        // if (mJustHsNoV) {
        // picker.setJustHsNoV(mMaxSaturation);
        // }
        // picker.show();

        dialog.show();

        return false;
    }

}

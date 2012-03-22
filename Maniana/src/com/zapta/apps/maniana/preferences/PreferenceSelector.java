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

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

/**
 * Switcher between two preferences. At any given time, exactly one is dispalyed based on a checkbox
 * preference state.
 * 
 * @author Tal Dayan
 */
public class PreferenceSelector {
    private final PreferenceGroup mGroup;
    private final CheckBoxPreference mCheckbox;
    private final Preference mPref1;
    private final Preference mPref2;

    private boolean lastUpdatedStatel;

    /** On construction, group contains both preferences. */
    public PreferenceSelector(PreferenceGroup group, CheckBoxPreference checkboxPrefernce,
            Preference prefernece1, Preference preference2) {
        checkNotNull(group);
        this.mGroup = group;
        this.mCheckbox = checkboxPrefernce;
        this.mPref1 = prefernece1;
        this.mPref2 = preference2;

        this.lastUpdatedStatel = checkboxPrefernce.isChecked();
        if (lastUpdatedStatel) {
            mGroup.removePreference(preference2);
        } else {
            mGroup.removePreference(prefernece1);
        }
    }

    public void update() {
        final boolean newState = mCheckbox.isChecked();
        if (newState == lastUpdatedStatel) {
            return;
        }
        lastUpdatedStatel = newState;
        if (lastUpdatedStatel) {
            mGroup.addPreference(mPref1);
            mGroup.removePreference(mPref2);
        } else {
            mGroup.removePreference(mPref1);
            mGroup.addPreference(mPref2);
        }
    }
}

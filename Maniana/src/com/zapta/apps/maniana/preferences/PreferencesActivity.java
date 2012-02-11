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

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.format.Time;
import android.widget.Toast;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.help.PopupMessageActivity;
import com.zapta.apps.maniana.help.PopupMessageActivity.MessageKind;
import com.zapta.apps.maniana.util.DateUtil;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.VisibleForTesting;

/**
 * Activity that shows the settings page.
 * <p>
 * The class includes logic to display the current selected values. This feature is not yet provided
 * by the Android framework (as of Dec 2011).
 * 
 * @author Tal Dayan
 */
public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private ListPreference mFontListPreference;
    private ListPreference mFontSizeListPreference;
    private ListPreference mPageBackgroundTypeListPreference;
    private ColorPickerPreference mPageSolidColorPickPreference;
    private ListPreference mLockPeriodListPreference;
    private ListPreference mApplauseLevelListPreference;
    private ListPreference mWidgetToolbarLocationPreference;
    private CheckBoxPreference mSoundEnablePreference;
    private Preference mVersionInfoPreference;
    private Preference mSharePreference;
    private Preference mFeedbackPreference;
    private Preference mRestoreDefaultsPreference;

    /** For temp time calculations. Avoiding new object creation. */
    private Time tempTime = new Time();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // TODO: take keys from PreferenceKind enum, not from literal strings.
        mFontListPreference = (ListPreference) findPreference(PreferenceKind.ITEM_FONT_TYPE);
        mFontSizeListPreference = (ListPreference) findPreference(PreferenceKind.ITEM_FONT_SIZE);
        mPageBackgroundTypeListPreference = (ListPreference) findPreference(PreferenceKind.PAGE_BACKGROUND_TYPE);
        mPageSolidColorPickPreference = (ColorPickerPreference) findPreference(PreferenceKind.PAGE_BACKGROUND_SOLID_COLOR);
        mLockPeriodListPreference = (ListPreference) findPreference(PreferenceKind.LOCK_PERIOD);
        mApplauseLevelListPreference = (ListPreference) findPreference(PreferenceKind.APPLAUSE_LEVEL);
        mSoundEnablePreference = (CheckBoxPreference) findPreference(PreferenceKind.SOUND_ENABLED);
        mWidgetToolbarLocationPreference = (ListPreference) findPreference(PreferenceKind.WIDGET_TOOLBAR_LOCATION);
        mVersionInfoPreference = findPreference(PreferenceKind.VERSION_INFO);
        mSharePreference = findPreference(PreferenceKind.SHARE);
        mFeedbackPreference = findPreference(PreferenceKind.FEEDBACK);
        mRestoreDefaultsPreference = findPreference(PreferenceKind.RESTORE_DEFAULTS);

        // We lookup also the preferences we don't use here to assert that the code and the xml
        // key strings match.
        findColorPickerPrerence(PreferenceKind.PAGE_ITEM_DIVIDER_COLOR).setAlphaSliderEnabled(true);
        findColorPickerPrerence(PreferenceKind.ITEM_ACTIVE_TEXT_COLOR);
        findColorPickerPrerence(PreferenceKind.ITEM_COMPLETED_TEXT_COLOR);
        findColorPickerPrerence(PreferenceKind.WIDGET_BACKGROUND_COLOR).setAlphaSliderEnabled(true);
        findColorPickerPrerence(PreferenceKind.WIDGET_TEXT_COLOR);
        findPreference(PreferenceKind.AUTO_SORT);

        mRestoreDefaultsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onResetSettingsClick();
                return true;
            }
        });

        mVersionInfoPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onVersionInfoSettingsClick();
                return true;
            }
        });

        mSharePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onShareClick();
                return true;
            }
        });

        mFeedbackPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onFeedbackClick();
                return true;
            }
        });

    }

    /** Hanlde user selecting reset settings in the settings activity */
    private final void onResetSettingsClick() {
        // TODO: *** what does this do? Do we need it? Look like a left over.
        // PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        onConfirmedResetSettingsClick();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Doing nothing
                        break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Revert all settings to default values?\n\n(Does not affect task data)")
                .setPositiveButton("Yes!", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private final void onConfirmedResetSettingsClick() {
        // public void onClick(DialogInterface dialog, int which) {
        // switch (which) {
        // case DialogInterface.BUTTON_POSITIVE:
        Editor editor = getPreferenceScreen().getEditor();
        editor.clear();

        // TODO: this is a hack to force notifying the main activity and widget
        // about the change. For some reason they are not notified about it
        // otherwise as the the non custom preference types do.
        editor.putInt(PreferenceKind.ITEM_ACTIVE_TEXT_COLOR.getKey(),
                PreferenceConstants.DEFAULT_ITEM_TEXT_COLOR);
        editor.putInt(PreferenceKind.ITEM_COMPLETED_TEXT_COLOR.getKey(),
                PreferenceConstants.DEFAULT_COMPLETED_ITEM_TEXT_COLOR);
        editor.putInt(PreferenceKind.PAGE_BACKGROUND_SOLID_COLOR.getKey(),
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_SOLID_COLOR);
        editor.putInt(PreferenceKind.PAGE_ITEM_DIVIDER_COLOR.getKey(),
                PreferenceConstants.DEFAULT_PAGE_ITEM_DIVIDER_COLOR);
        editor.putInt(PreferenceKind.WIDGET_BACKGROUND_COLOR.getKey(),
                PreferenceConstants.DEFAULT_WIDGET_BACKGROUND_COLOR);
        editor.putInt(PreferenceKind.WIDGET_TEXT_COLOR.getKey(),
                PreferenceConstants.DEFAULT_WIDGET_TEXT_COLOR);

        // We also need to set boolean preferences whose default value is false.
        // Otherwise their change is not broadcast to the tracker for some reason.
        editor.putBoolean(PreferenceKind.AUTO_SORT.getKey(), false);

        editor.commit();

        // Hack per http://tinyurl.com/c44gl4r We close this activity and restart
        // it using the same intent that created it. This causes it to reload
        // the preferences.
        finish();
        startActivity(getIntent());
        Toast.makeText(PreferencesActivity.this, "All settings restored to defaut",
                Toast.LENGTH_SHORT).show();
    }

    private final void onVersionInfoSettingsClick() {
        final Intent intent = PopupMessageActivity.intentFor(this, MessageKind.WHATS_NEW);
        startActivity(intent);
    }

    private final void onShareClick() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String message = "Check out Maniana, a free fun and easy to use todo "
                + "list app on the Android Market:\n \n"
                + "https://market.android.com/details?id=com.zapta.apps.maniana";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Check out \"Maniana To Do List\"");
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private final void onFeedbackClick() {
        // NOTE: based on http://stackoverflow.com/questions/3312438
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri data = Uri.parse("mailto:maniana@zapta.com?subject=Maniana feedback&body=");
        intent.setData(data);
        startActivity(intent);
    }

    private final ColorPickerPreference findColorPickerPrerence(PreferenceKind kind) {
        return (ColorPickerPreference) findPreference(kind);
    }

    private final Preference findPreference(PreferenceKind kind) {
        final Preference result = getPreferenceScreen().findPreference(kind.getKey());
        if (result == null) {
            throw new RuntimeException("Preference key not found for kind: " + kind);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSummaries();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // NOTE(tal): since settings change is a rare operation, we don't bother to
        // switch of key and always update all the lists. Code is simpler this way.
        updateSummaries();
    }

    private void updateSummaries() {
        updateListSummary(mFontListPreference, R.array.itemFontSummaries, null);
        updateListSummary(mFontSizeListPreference, R.array.itemFontSizeSummaries, null);
        updateListSummary(mPageBackgroundTypeListPreference, R.array.pageBackgroundTypeSummaries,
                null);
        updateListSummary(mWidgetToolbarLocationPreference, R.array.widgetToolbarLocationSummaries,
                null);

        // Disable applause if voice is disabled
        if (mSoundEnablePreference.isChecked()) {
            updateListSummary(mApplauseLevelListPreference, R.array.applauseLevelSummaries, null);
        } else {
            mApplauseLevelListPreference.setSummary("(sound is off)");
        }

        // Disable solid background color picker if background type is stained paper
        if (PageBackgroundType.SOLID.getKey().equals(mPageBackgroundTypeListPreference.getValue())) {
            mPageSolidColorPickPreference.setEnabled(true);
            mPageSolidColorPickPreference.setSummary("Solid background color");
        } else {
            mPageSolidColorPickPreference.setEnabled(false);
            mPageSolidColorPickPreference.setSummary("Solid background color not used");
        }

        // For lock expiration preference, also show the time until next expiration. This require
        // some computation.
        {
            final String key = mLockPeriodListPreference.getValue();

            final LockExpirationPeriod selection = LockExpirationPeriod.fromKey(key, null);

            final int wholeHoursLeft;
            if (selection == LockExpirationPeriod.WEEKLY) {
                tempTime.setToNow();
                wholeHoursLeft = DateUtil.hoursToEndOfWeek(tempTime);
            } else if (selection == LockExpirationPeriod.MONTHLY) {
                tempTime.setToNow();
                wholeHoursLeft = DateUtil.hoursToEndOfMonth(tempTime);
            } else {
                wholeHoursLeft = -1;
            }

            final String suffix = (wholeHoursLeft >= 0) ? construtLockTimeLeftMessageSuffix(wholeHoursLeft)
                    : "";
            updateListSummary(mLockPeriodListPreference, R.array.lockPeriodSummaries, suffix);
        }
    }

    @VisibleForTesting
    static String construtLockTimeLeftMessageSuffix(int wholeHoursLeft) {
        if (wholeHoursLeft < 16) {
            return "  (tonight)";
        }
        if (wholeHoursLeft < 48) {
            return String.format("  (in %d hours)", wholeHoursLeft);
        }
        // Rounding up
        return String.format("  (in %d days)", (wholeHoursLeft + 23) / 24);
    }

    private void updateListSummary(ListPreference listPreference, int stringArrayId,
            @Nullable String suffix) {
        final String value = listPreference.getValue();
        // -1 if not found.
        final int index = listPreference.findIndexOfValue(value);
        String summary;
        if (index < 0) {
            LogUtil.error("Could not find index of preference value [%s]", value);
            // Fallback
            summary = "";
        } else {
            summary = getResources().getStringArray(stringArrayId)[index];
            if (suffix != null) {
                summary += suffix;
            }
        }
        listPreference.setSummary(summary);
    }
}
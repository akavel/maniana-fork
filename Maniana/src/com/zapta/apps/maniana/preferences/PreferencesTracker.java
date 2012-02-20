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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Manages the app preferences (settings).
 * 
 * @author Tal Dayan
 */
public class PreferencesTracker implements PreferenceConstants {

    private final AppContext mApp;

    private final SharedPreferences mSharedPreferences;
    
    private PageItemFontVariation mCachedItemFontVariation;

    private boolean mCachedAllowSoundsPreference;

    private PageItemFontType mCachedPageFontTypePreference;

    private PageItemFontSize mCachedPageFontSizePreference;

    private int mCachedPageItemActiveTextColorPreference;

    private int mCachedPageItemCompletedTextColorPreference;

    private PageBackgroundType mCachedPageBackgroundTypePreference;

    private int mCachedPageBackgroundSolidColorPreference;

    private int mCachedPageItemDividerColorPreference;

    private LockExpirationPeriod mCachedLockExpirationPeriodPrefernece;

    private ApplauseLevel mCachedApplauseLevelPreference;

    private boolean mCachedVerboseMessagesPreference;

    private boolean mCachedStartupAnimationPreference;

    private boolean mCachedAutoSortPreference;
    
    private boolean mCachedAutoDailyCleanupPreference;

    // This is a hack to keep the listener from being garbage collected per
    // http://tinyurl.com/blkycrk. Should be unregistered explicitly when main activity is
    // destroyed.
    private final OnSharedPreferenceChangeListener mListener;

    public PreferencesTracker(AppContext app) {
        mApp = app;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApp.context());

        updateCachedAllowSoundsPreference();
        updateCachedApplauseLevelPreference();
        updateCachedAutoSortPreference();
        updateCachedAutoDailyCleanupPreference();
        updateCachedPageFontTypePreference();
        updateCachedPageFontSizePreference();
        updateCachedPageItemActiveTextColorPreference();
        updateCachedPageItemCompletedTextColorPreference();
        updateCachedPageBackgroundTypePreference();
        updateCachedPageBackgroundSolidColorPreference();
        updateCachedPageItemDividerColorPreference();
        updateCachedLockExpierationPeriodPreference();
        updateCachedVerboseMessagesPreference();
        updateCachedStartupAnimationPreference();

        mListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                onPreferenceChange(key);
            }
        };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this.mListener);
        
        onItemFontVariationPreferenceChange();
    }

    private final void updateCachedAllowSoundsPreference() {
        mCachedAllowSoundsPreference = mSharedPreferences.getBoolean(
                PreferenceKind.SOUND_ENABLED.getKey(), DEFAULT_ALLOWS_SOUND_EFFECTS);
    }

    private final void updateCachedPageFontTypePreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_ITEM_FONT_TYPE.getKey(),
                DEFAULT_PAGE_FONT_TYPE.getKey());
        mCachedPageFontTypePreference = PageItemFontType.fromKey(key, DEFAULT_PAGE_FONT_TYPE);
    }

    private final void updateCachedPageFontSizePreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_ITEM_FONT_SIZE.getKey(),
                DEFAULT_PAGE_FONT_SIZE.getKey());
        mCachedPageFontSizePreference = PageItemFontSize.fromKey(key, DEFAULT_PAGE_FONT_SIZE);
    }

    private final void updateCachedPageBackgroundTypePreference() {
        final String key = mSharedPreferences
                .getString(PreferenceKind.PAGE_BACKGROUND_TYPE.getKey(),
                        DEFAULT_PAGE_BACKGROUND_TYPE.getKey());
        mCachedPageBackgroundTypePreference = PageBackgroundType.fromKey(key,
                DEFAULT_PAGE_BACKGROUND_TYPE);
    }

    private final void updateCachedPageBackgroundSolidColorPreference() {
        mCachedPageBackgroundSolidColorPreference = mSharedPreferences.getInt(
                PreferenceKind.PAGE_BACKGROUND_SOLID_COLOR.getKey(),
                DEFAULT_PAGE_BACKGROUND_SOLID_COLOR);
    }

    private final void updateCachedPageItemDividerColorPreference() {
        mCachedPageItemDividerColorPreference = mSharedPreferences.getInt(
                PreferenceKind.PAGE_ITEM_DIVIDER_COLOR.getKey(), DEFAULT_PAGE_ITEM_DIVIDER_COLOR);
    }

    private final void updateCachedPageItemActiveTextColorPreference() {
        mCachedPageItemActiveTextColorPreference = mSharedPreferences.getInt(
                PreferenceKind.PAGE_ITEM_ACTIVE_TEXT_COLOR.getKey(), DEFAULT_ITEM_TEXT_COLOR);
    }

    private final void updateCachedPageItemCompletedTextColorPreference() {
        mCachedPageItemCompletedTextColorPreference = mSharedPreferences.getInt(
                PreferenceKind.PAGE_ITEM_COMPLETED_TEXT_COLOR.getKey(),
                DEFAULT_COMPLETED_ITEM_TEXT_COLOR);
    }

    private final void updateCachedLockExpierationPeriodPreference() {
        mCachedLockExpirationPeriodPrefernece = readLockExpierationPeriodPreference(mSharedPreferences);
    }

    /**
     * Read current lock expiration period preference. Can be called from widgets. Main activity
     * functionality should use the cached value instead.
     */
    public static final LockExpirationPeriod readLockExpierationPeriodPreference(
            SharedPreferences sharedPreferences) {
        final String key = sharedPreferences.getString(PreferenceKind.LOCK_PERIOD.getKey(),
                DEFAULT_LOCK_PERIOD.getKey());
        return LockExpirationPeriod.fromKey(key, DEFAULT_LOCK_PERIOD);
    }

    /** Read widget background type preference. */
    public static final WidgetBackgroundType readWidgetBackgroundTypePreference(SharedPreferences sharedPreferences) {
        final String key = sharedPreferences.getString(PreferenceKind.WIDGET_BACKGROUND_TYPE.getKey(),
                DEFAULT_WIDGET_BACKGROUND_TYPE.getKey());
        return WidgetBackgroundType.fromKey(key, DEFAULT_WIDGET_BACKGROUND_TYPE);
    }
    
    /** Read widget item text size preference. */
    public static final WidgetItemFontSize readWidgetItemFontSizePreference(SharedPreferences sharedPreferences) {
        final String key = sharedPreferences.getString(PreferenceKind.WIDGET_ITEM_FONT_SIZE.getKey(),
                DEFAULT_WIDGET_ITEM_FONT_SIZE.getKey());
        return WidgetItemFontSize.fromKey(key, DEFAULT_WIDGET_ITEM_FONT_SIZE);
    }

    /** 
     * Read widget background color preference. 
     * Used by the list widget only. Should be used only of 
     * background type is SOLID
     */
    public static final int readWidgetBackgroundColorPreference(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(PreferenceKind.WIDGET_BACKGROUND_COLOR.getKey(),
                DEFAULT_WIDGET_BACKGROUND_COLOR);
    }

    /** Read widget text color preference. Used by the list widget only. */
    public static final int readWidgetTextColorPreference(SharedPreferences sharedPreferences) {

        return sharedPreferences.getInt(PreferenceKind.WIDGET_ITEM_TEXT_COLOR.getKey(),
                DEFAULT_WIDGET_TEXT_COLOR);
    }

    /** Read widget line wrapping preference. Used by the list widget only. */
    public static final boolean readWidgetSingleLinePreference(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(PreferenceKind.WIDGET_SINGLE_LINE.getKey(),
                DEFAULT_WIDGET_SINGLE_LINE);
    }

    /** Read widget show toolbar preference. Used by the list widget only. */
    public static final boolean readWidgetShowToolbarPreference(
            SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(PreferenceKind.WIDGET_SHOW_TOOLBAR.getKey(),
                DEFAULT_WIDGET_SHOW_TOOLBAR);
    }

    private final void updateCachedApplauseLevelPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.APPLAUSE_LEVEL.getKey(),
                DEFAULT_APPPLAUSE_LEVEL.getKey());
        mCachedApplauseLevelPreference = ApplauseLevel.fromKey(key, DEFAULT_APPPLAUSE_LEVEL);
    }

    private final void updateCachedVerboseMessagesPreference() {
        mCachedVerboseMessagesPreference = mSharedPreferences.getBoolean(
                PreferenceKind.VERBOSE_MESSAGES.getKey(), DEFAULT_VERBOSE_MESSAGES);
    }

    private final void updateCachedStartupAnimationPreference() {
        mCachedStartupAnimationPreference = mSharedPreferences.getBoolean(
                PreferenceKind.STARTUP_ANIMATION.getKey(), DEFAULT_STARTUP_ANIMATION);
    }

    private final void updateCachedAutoSortPreference() {
        mCachedAutoSortPreference = mSharedPreferences.getBoolean(
                PreferenceKind.AUTO_SORT.getKey(), DEFAULT_AUTO_SORT);
    }
    
    private final void updateCachedAutoDailyCleanupPreference() {
        mCachedAutoDailyCleanupPreference = mSharedPreferences.getBoolean(
                PreferenceKind.AUTO_DAILY_CLEANUP.getKey(), DEFAULT_AUTO_DAILY_CLEANUP);
    }

    public final boolean getSoundEnabledPreference() {
        return mCachedAllowSoundsPreference;
    }

    /** Should be ignored if sound is disabled */
    public final ApplauseLevel getApplauseLevelPreference() {
        return mCachedApplauseLevelPreference;
    }

    public final PageItemFontType getItemFontTypePreference() {
        return mCachedPageFontTypePreference;
    }

    public final PageItemFontSize getItemFontSizePreference() {
        return mCachedPageFontSizePreference;
    }

    public int getPageItemActiveTextColorPreference() {
        return mCachedPageItemActiveTextColorPreference;
    }

    public int getPageItemCompletedTextColorPreference() {
        return mCachedPageItemCompletedTextColorPreference;
    }

    public final PageBackgroundType getBackgroundTypePreference() {
        return mCachedPageBackgroundTypePreference;
    }

    public final int getPageBackgroundSolidColorPreference() {
        return mCachedPageBackgroundSolidColorPreference;
    }

    public final int getPageItemDividerColorPreference() {
        return mCachedPageItemDividerColorPreference;
    }

    public final LockExpirationPeriod getLockExpirationPeriodPrefernece() {
        return mCachedLockExpirationPeriodPrefernece;
    }

    public final boolean getVerboseMessagesEnabledPreference() {
        return mCachedVerboseMessagesPreference;
    }

    public final boolean getStartupAnimationPreference() {
        return mCachedStartupAnimationPreference;
    }

    public final boolean getAutoSortPreference() {
        return mCachedAutoSortPreference;
    }
    
    public final boolean getAutoDailyCleanupPreference() {
        return mCachedAutoDailyCleanupPreference;
    }

    /**
     * Handle preferences change.
     * 
     * @param key
     *            the preference key string as defined in preferences.xml.
     */
    private final void onPreferenceChange(String key) {
        // Null if not found.
        @Nullable
        final PreferenceKind id = PreferenceKind.fromKey(key);

        if (id == null) {
            LogUtil.error("Unknown setting key: " + key);
            return;
        }

        switch (id) {
            case SOUND_ENABLED:
                updateCachedAllowSoundsPreference();
                break;
            case APPLAUSE_LEVEL:
                updateCachedApplauseLevelPreference();
                break;
            case AUTO_SORT:
                updateCachedAutoSortPreference();
                break;
            case AUTO_DAILY_CLEANUP:
                updateCachedAutoDailyCleanupPreference();
                break;
            case PAGE_ITEM_FONT_TYPE:
                updateCachedPageFontTypePreference();
                break;
            case PAGE_ITEM_FONT_SIZE:
                updateCachedPageFontSizePreference();
                break;
            case PAGE_ITEM_ACTIVE_TEXT_COLOR:
                updateCachedPageItemActiveTextColorPreference();
                break;
            case PAGE_ITEM_COMPLETED_TEXT_COLOR:
                updateCachedPageItemCompletedTextColorPreference();
                break;
            case PAGE_BACKGROUND_TYPE:
                updateCachedPageBackgroundTypePreference();
                break;
            case PAGE_BACKGROUND_SOLID_COLOR:
                updateCachedPageBackgroundSolidColorPreference();
                break;
            case PAGE_ITEM_DIVIDER_COLOR:
                updateCachedPageItemDividerColorPreference();
                break;
            case LOCK_PERIOD:
                updateCachedLockExpierationPeriodPreference();
                break;
            case VERBOSE_MESSAGES:
                updateCachedVerboseMessagesPreference();
                break;
            case STARTUP_ANIMATION:
                updateCachedStartupAnimationPreference();
                break;
            case WIDGET_BACKGROUND_TYPE:
            case WIDGET_BACKGROUND_COLOR:
            case WIDGET_ITEM_TEXT_COLOR:
            case WIDGET_ITEM_FONT_SIZE:
            case WIDGET_SINGLE_LINE:
            case WIDGET_SHOW_TOOLBAR:
                // These ones are not cached or used here. Just reported to controller to
                // trigger the widget update and backup service.
                break;
            default:
                // Report and ignore this call.
                LogUtil.error("Unknown changed preference key: %s", key);
                return;
        }

        // Inform the controller about the prefernce change. At this point, this object already
        // cached the new values.
        mApp.controller().onPreferenceChange(id);
    }
    
    
    /**
     * Update cached item font variation using current preferences. Should be called whenever the
     * item font preference changes
     */
    public final void onItemFontVariationPreferenceChange() {
        mCachedItemFontVariation = PageItemFontVariation.newFromCurrentPreferences(mApp.context(), this);
    }

    /** Get current item font variation */
    public final PageItemFontVariation getItemFontVariation() {
        return mCachedItemFontVariation;
    }

    /** Relase resources. This is the last call to this instance. */
    public void release() {
        // Per http://tinyurl.com/blkycrk
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.mListener);
    }
}

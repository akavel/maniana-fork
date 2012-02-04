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

    private boolean mCachedAllowSoundsPreference;

    private FontType mCachedFontTypePreference;
    
    private FontSize mCachedFontSizePreference;
    
    private int mCachedPageItemActiveTextColorPreference;
    
    private int mCachedPageItemCompletedTextColorPreference;

    private PageBackgroundType mCachedPageBackgroundTypePreference;
    
    private int mCachedPageBackgroundSolidColorPreference;
    
    private int mCachedPageItemDividerColorPreference;

    private LockExpirationPeriod mCachedLockExpirationPeriodPrefernece;

    private ApplauseLevel mCachedApplauseLevelPreference;

    private boolean mCachedVerboseMessagesPreference;

    private boolean mCachedStartupAnimationPreference;

//    private boolean mCachedVoiceRecognitionPreference;
    
    private boolean mCachedAutoSortPreference;

//    private final boolean mDeviceSupportsVoiceRecognition;

    // This is a hack to keep the listener from being garbage collected per
    // http://tinyurl.com/blkycrk. Should be unregistered explicitly when main activity is
    // destroyed.
    private final OnSharedPreferenceChangeListener listener;

    public PreferencesTracker(AppContext app) {
        mApp = app;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApp.context());

//        mDeviceSupportsVoiceRecognition = AppServices.isVoiceRecognitionSupported(mApp.context());

        updateCachedAllowSoundsPreference();
        updateCachedApplauseLevelPreference();
        updateCachedAutoSortPreference();
        updateCachedFontTypePreference();
        updateCachedFontSizePreference();
        updateCachedPageItemActiveTextColorPreference();
        updateCachedPageItemCompletedTextColorPreference();
        updateCachedPageBackgroundTypePreference();
        updateCachedPageBackgroundSolidColorPreference();
        updateCachedPageItemDividerColorPreference();
        updateCachedLockExpierationPeriodPreference();
        updateCachedVerboseMessagesPreference();
        updateCachedStartupAnimationPreference();
//        updateCachedVoiceRecognitionPreference();

        listener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                onPreferenceChange(key);
            }
        };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this.listener);
    }

    private final void updateCachedAllowSoundsPreference() {
        mCachedAllowSoundsPreference = mSharedPreferences.getBoolean(PreferenceKind.SOUND_ENABLED
                        .getKey(), DEFAULT_ALLOWS_SOUND_EFFECTS);
    }

    private final void updateCachedFontTypePreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.ITEM_FONT_TYPE.getKey(),
                        DEFAULT_FONT_TYPE.getKey());
        mCachedFontTypePreference = FontType.fromKey(key, DEFAULT_FONT_TYPE);
    }
    
    private final void updateCachedFontSizePreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.ITEM_FONT_SIZE.getKey(),
                        DEFAULT_FONT_SIZE.getKey());
        mCachedFontSizePreference = FontSize.fromKey(key, DEFAULT_FONT_SIZE);
    }

    private final void updateCachedPageBackgroundTypePreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_BACKGROUND_TYPE.getKey(),
                        DEFAULT_PAGE_BACKGROUND_TYPE.getKey());
        mCachedPageBackgroundTypePreference = PageBackgroundType.fromKey(key,
                        DEFAULT_PAGE_BACKGROUND_TYPE);
    }
    
    private final void updateCachedPageBackgroundSolidColorPreference() {
        mCachedPageBackgroundSolidColorPreference = mSharedPreferences.getInt(PreferenceKind.PAGE_BACKGROUND_SOLID_COLOR.getKey(),
                        DEFAULT_PAGE_BACKGROUND_SOLID_COLOR);
    }
    
    private final void updateCachedPageItemDividerColorPreference() {
        mCachedPageItemDividerColorPreference = mSharedPreferences.getInt(PreferenceKind.PAGE_ITEM_DIVIDER_COLOR.getKey(),
                        DEFAULT_PAGE_ITEM_DIVIDER_COLOR);
    }
        
    private final void updateCachedPageItemActiveTextColorPreference() {
        mCachedPageItemActiveTextColorPreference = mSharedPreferences.getInt(PreferenceKind.ITEM_ACTIVE_TEXT_COLOR.getKey(),
                        DEFAULT_ITEM_TEXT_COLOR);
    }
    
    private final void updateCachedPageItemCompletedTextColorPreference() {
        mCachedPageItemCompletedTextColorPreference = mSharedPreferences.getInt(PreferenceKind.ITEM_COMPLETED_TEXT_COLOR.getKey(),
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

    /** Read current widget background color. Used by the widget only. */
    public static final int readWidgetBackgroundColorPreference(
                    SharedPreferences sharedPreferences) {       
        return sharedPreferences.getInt(PreferenceKind.WIDGET_BACKGROUND_COLOR.getKey(),
                        DEFAULT_WIDGET_BACKGROUND_COLOR);
    }

    /** Read current widget text color preference. Used by the widget only. */
    public static final int readWidgetTextColorPreference(
                    SharedPreferences sharedPreferences) {

         return sharedPreferences.getInt(PreferenceKind.WIDGET_TEXT_COLOR.getKey(),
                        DEFAULT_WIDGET_TEXT_COLOR);
    }
    
    /** Read current widget line wrapping preference. Used by the widget only. */
    public static final boolean readWidgetSingleLinePreference(SharedPreferences sharedPreferences) {
         return sharedPreferences.getBoolean(PreferenceKind.WIDGET_SINGLE_LINE.getKey(),
                        DEFAULT_WIDGET_SINGLE_LINE);
    }

    private final void updateCachedApplauseLevelPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.APPLAUSE_LEVEL.getKey(),
                        DEFAULT_APPPLAUSE_LEVEL.getKey());
        mCachedApplauseLevelPreference = ApplauseLevel.fromKey(key, DEFAULT_APPPLAUSE_LEVEL);
    }

    private final void updateCachedVerboseMessagesPreference() {
        mCachedVerboseMessagesPreference = mSharedPreferences.getBoolean(
                        PreferenceKind.VERBOSE_MESSAGES.getKey(), DEFAULT_VERBOSE_MESSAGES);
//        LogUtil.debug("*** updateCachedVerboseMessagesPreference() -> %s", mCachedVerboseMessagesPreference);
    }

    private final void updateCachedStartupAnimationPreference() {
        mCachedStartupAnimationPreference = mSharedPreferences.getBoolean(
                        PreferenceKind.STARTUP_ANIMATION.getKey(), DEFAULT_STARTUP_ANIMATION);
    }

//    private final void updateCachedVoiceRecognitionPreference() {
//        mCachedVoiceRecognitionPreference = mDeviceSupportsVoiceRecognition
//                        && mSharedPreferences.getBoolean(PreferenceKind.VOICE_RECOGNITION.getKey(),
//                                        DEFAULT_VOICE_RECOGNITION);
//    }
    
    private final void updateCachedAutoSortPreference() {
        mCachedAutoSortPreference = mSharedPreferences.getBoolean(PreferenceKind.AUTO_SORT.getKey(),
                                        DEFAULT_AUTO_SORT);
//        LogUtil.debug("*** updateCachedAutoSortPreference() -> %s", mCachedAutoSortPreference);
    }

    public final boolean getSoundEnabledPreference() {
        return mCachedAllowSoundsPreference;
    }

    /** Should be ignored if sound is disabled */
    public final ApplauseLevel getApplauseLevelPreference() {
        return mCachedApplauseLevelPreference;
    }

    public final FontType getItemFontTypePreference() {
        return mCachedFontTypePreference;
    }
    
    public final FontSize getItemFontSizePreference() {
        return mCachedFontSizePreference;
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
//        LogUtil.debug("*** getVerboseMessagesEnabledPreference = %s", mCachedVerboseMessagesPreference);
        return mCachedVerboseMessagesPreference;
    }

    public final boolean getStartupAnimationPreference() {
        return mCachedStartupAnimationPreference;
    }

//    // TODO: move to services class
//    public final boolean getDeviceSupportsVoiceRecognition() {
//        return mDeviceSupportsVoiceRecognition;
//    }
    
    public final boolean getAutoSortPreference() {
//        LogUtil.debug("*** getAutoSortPreference = %s", mCachedAutoSortPreference);
        return mCachedAutoSortPreference;
    }

    /**
     * Handle preferences change.
     * 
     * @param key the preference key string as defined in preferences.xml.
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
            case ITEM_FONT_TYPE:
                updateCachedFontTypePreference();
                break;
            case ITEM_FONT_SIZE:
                updateCachedFontSizePreference();
                break;
            case ITEM_ACTIVE_TEXT_COLOR:
                updateCachedPageItemActiveTextColorPreference();
                break;
            case ITEM_COMPLETED_TEXT_COLOR:
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
             case WIDGET_BACKGROUND_COLOR:
             case WIDGET_TEXT_COLOR:
             case WIDGET_SINGLE_LINE:
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

    /** Relase resources. This is the last call to this instance. */
    public void release() {
        // Per http://tinyurl.com/blkycrk
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.listener);
        // LogUtil.("Unregistered prefernces listener");
    }
}

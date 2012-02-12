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

package com.zapta.apps.maniana.services;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;

import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.preferences.ItemFontVariation;

/**
 * Provides access and caching of the app resources.
 * 
 * @author Tal Dayan
 */
public class AppResources {
    /** Path to page title fonts relative to assets directory. */
    private static final String TITLE_FONT_ASSET_PATH = "fonts/Damion/Damion-Regular.ttf";

    /** App context. */
    private final AppContext mApp;

    // TODO: @@@ move this to PreferenceTracker
    /** Cached item text font variation */
    private ItemFontVariation mItemFontVariation;

    /** Cached page title typeface. */
    private final Typeface mTitleTypeFace;

    /** Cached autio manager . */
    private final AudioManager mAudioManager;

    /** Cached long press min time in millies. */
// private final int mLongPressMinMillis;

    /** Cached click max length in millis. */
//    private final int mClickMaxMillis;

    public AppResources(AppContext app) {
        this.mApp = app;

        onItemFontVariationPreferenceChange();

        mTitleTypeFace = Typeface
                        .createFromAsset(mApp.context().getAssets(), TITLE_FONT_ASSET_PATH);
        mAudioManager = (AudioManager) mApp.context().getSystemService(Context.AUDIO_SERVICE);

        // TODO: *** removed unused resource integer values        
//        mLongPressMinMillis = mApp.context().getResources().getInteger(
//                        R.integer.long_press_min_millis_id);
//        mClickMaxMillis = mApp.context().getResources().getInteger(R.integer.click_max_millis_id);
    }

    /**
     * TODO: move to preferences
     * 
     * Update cached item font variation using current preferences. Should be called whenever the
     * item font preference changes
     */
    public final void onItemFontVariationPreferenceChange() {
        mItemFontVariation = ItemFontVariation.newFromCurrentPreferences(mApp);
    }

    /** Get current item font variation */
    public final ItemFontVariation getItemFontVariation() {
        return mItemFontVariation;
    }

    /** Get typeface to use for the pages title font. */
    public final Typeface getTitleTypeFace() {
        return mTitleTypeFace;
    }

    /** Get the underlying audio manager. */
    public final AudioManager getAudioManager() {
        return mAudioManager;
    }

    /**
     * Get a drawable from a drawable id.
     * 
     * @param drawableResourceId id of the drawable resource to use.
     */
    public final Drawable getDrawable(int drawableResourceId) {
        return mApp.context().getResources().getDrawable(drawableResourceId);
    }

    /**
     * Get color from color resources.
     * 
     * @param colorResourceId id of color resource to use.
     */
    public final int getColor(int colorResourceId) {
        return mApp.context().getResources().getColor(colorResourceId);
    }
}

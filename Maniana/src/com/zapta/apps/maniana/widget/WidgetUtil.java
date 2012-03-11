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

package com.zapta.apps.maniana.widget;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.text.format.Time;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.util.Orientation;

/**
 * Common widget related utilities.
 * 
 * @author Tal Dayan
 */
public abstract class WidgetUtil {

    /** Do not instantiate */
    private WidgetUtil() {
    }

    /** Return a list of TODAY's active items subject to time based push. */
    public static final List<ItemModelReadOnly> selectTodaysActiveItemsByTime(AppModel model,
            Time timeNow, LockExpirationPeriod lockExpirationPeriod) {
        final PushScope pushScope = ModelUtil.computePushScope(model.getLastPushDateStamp(),
                timeNow, lockExpirationPeriod);
        return selectTodaysActiveItemsByPushScope(model, pushScope);
    }

    /** Return a list of active TODAY's items subject given push scope */
    public static final List<ItemModelReadOnly> selectTodaysActiveItemsByPushScope(AppModel model,
            PushScope pushScope) {
        final List<ItemModelReadOnly> result = new ArrayList<ItemModelReadOnly>(
                model.getItemCount());

        // If needed, collect items from Tomorow
        if (pushScope != PushScope.NONE) {
            final boolean pushAlsoLocked = (pushScope == PushScope.ALL);
            final int n = model.getPageItemCount(PageKind.TOMOROW);
            for (int i = 0; i < n; i++) {
                final ItemModelReadOnly item = model.getItemReadOnly(PageKind.TOMOROW, i);
                // We ignore completed items since the widget does not show them anyway
                if (!item.isCompleted()) {
                    if (!item.isLocked() || pushAlsoLocked) {
                        // NOTE: we don't change the item to unlocked. The widget ignore that
                        // state. The unlock will happen at the main activity when the user
                        // will open it the next time.
                        result.add(item);
                    }
                }
            }
        }

        // Append today's active items
        {
            final int n = model.getPageItemCount(PageKind.TODAY);
            for (int i = 0; i < n; i++) {
                final ItemModelReadOnly item = model.getItemReadOnly(PageKind.TODAY, i);
                if (!item.isCompleted()) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    /**
     * Compute the widget size in pixels.
     * 
     * Based on 
     * http://osdir.com/ml/Android-Developers/2011-01/msg02879.html
     * 
     * See also (2 dip inset)
     * http://blog.doityourselfandroid.com/2011/05/24/developing-android-home-screenwidgets/
     * 
     */
    public static Point widgetGrossPixelSize(float density, Orientation orientation,
            int widthCells, int heightCells) {
        final int widthPixels;
        final int heightPixels;
        if (orientation.isPortrait) {
            widthPixels = (int) ((80 * widthCells - 4) * density + .5f);
            heightPixels = (int) ((100 * heightCells - 4) * density + .5f);
        } else {
            widthPixels = (int) ((106 * widthCells - 4) * density + .5f);
            heightPixels = (int) ((74 * heightCells - 4) * density + .5f);
        }
        return new Point(widthPixels, heightPixels);
    }
    
    /** Compute the list widget TODAY title text size. */ 
    public static int titleTextSize(ListWidgetSize listWidgetSize, int itemTextSize) {
        // A monotonic function of text size and widget height that provides good results.
        final int t = (int)( 2 + (itemTextSize * 0.5f) + (listWidgetSize.heightCells * 2));
        // Clip on min/max
        return Math.max(10, Math.min(22, t));
    }
}

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

import android.text.format.Time;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.VisibleForTesting;

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
            Time timeNow, LockExpirationPeriod lockExpirationPeriod,
            boolean cleanCompletedTasksOnPush, boolean includeCompletedItems, boolean sortItems) {
        final PushScope pushScope = ModelUtil.computePushScope(model.getLastPushDateStamp(),
                timeNow, lockExpirationPeriod);
        final boolean activeCleanup = pushScope.isActive() && cleanCompletedTasksOnPush;
        final boolean completedItemsIncluded = includeCompletedItems && !activeCleanup;
        final boolean needToSort = completedItemsIncluded && sortItems;
        return selectTodaysActiveItemsByPushScope(model, pushScope, completedItemsIncluded,
                needToSort);
    }

    /** Return a list of active TODAY's items subject given push scope */
    @VisibleForTesting
    static final List<ItemModelReadOnly> selectTodaysActiveItemsByPushScope(AppModel model,
            PushScope pushScope, boolean completedItemsIncluded, boolean sortItems) {
        // If sorting, we collect the active and completed items in seperate list and contact
        // them at the end. Otherwise, we collect them in a single list.
        final List<ItemModelReadOnly> mainList = new ArrayList<ItemModelReadOnly>(
                model.getItemCount());
        final List<ItemModelReadOnly> secondaryList = sortItems ? new ArrayList<ItemModelReadOnly>(
                model.getItemCount()) : mainList;

        // If needed, collect items from Tomorrow
        if (pushScope != PushScope.NONE) {
            final boolean pushAlsoLocked = (pushScope == PushScope.ALL);
            final int n = model.getPageItemCount(PageKind.TOMOROW);
            for (int i = 0; i < n; i++) {
                final ItemModelReadOnly item = model.getItemReadOnly(PageKind.TOMOROW, i);
                if ((!item.isLocked() || pushAlsoLocked)
                        && (!item.isCompleted() || completedItemsIncluded)) {
                    // NOTE: we don't change the item to unlocked. The widget ignore that
                    // state. The unlock will happen at the main activity when the user
                    // will open it the next time.
                    if (item.isCompleted()) {
                        secondaryList.add(item);
                    } else {
                        mainList.add(item);
                    }
                }
            }
        }

        // Append today's active items
        {
            final int n = model.getPageItemCount(PageKind.TODAY);
            for (int i = 0; i < n; i++) {
                final ItemModelReadOnly item = model.getItemReadOnly(PageKind.TODAY, i);
                if (!item.isCompleted() || completedItemsIncluded) {
                    if (item.isCompleted()) {
                        secondaryList.add(item);
                    } else {
                        mainList.add(item);
                    }
                }
            }
        }

        if (sortItems) {
            mainList.addAll(secondaryList);
        }

        return mainList;
    }

    /** Compute the list widget TODAY title text size. */
    public static int titleTextSize(ListWidgetSize listWidgetSize, int itemTextSize) {
        // A monotonic function of text size and widget height that provides good results.
        final int t = (int) (3.5f + (itemTextSize * 0.5f) + (listWidgetSize.heightCells * 0.5f));
        // Clip on min/max
        return Math.max(9, Math.min(22, t));
    }
}

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

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;

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
    public static final List<ItemModelReadOnly> selectTodaysItems(AppModel model,
            boolean includeCompletedItems) {
        final int n = model.getPageItemCount(PageKind.TODAY);
        final List<ItemModelReadOnly> result = new ArrayList<ItemModelReadOnly>(
              n);
        for (int i = 0; i < n; i++) {
            final ItemModelReadOnly item = model.getItemReadOnly(PageKind.TODAY, i);
            if (includeCompletedItems || !item.isCompleted()) {
                result.add(item);
            }
        }
        return result;
    }

    /** Compute the list widget TODAY title text size. */
    public static int titleTextSize(ListWidgetSize listWidgetSize, int itemTextSize) {
        // A monotonic function of text size and widget height that provides good results.
        final int t = (int) (3.5f + (itemTextSize * 0.5f) + (listWidgetSize.heightCells * 0.5f));
        // Clip on min/max
        return Math.max(9, Math.min(22, t));
    }
}

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

import com.zapta.apps.maniana.util.Orientation;

import android.graphics.Point;

/**
 * Descriptor of supproted list widget sizes.
 * 
 * @author Tal Dayan
 */
public class ListWidgetSize {

    protected static final ListWidgetSize LIST_WIDGET_SIZE1 = new ListWidgetSize(
            ListWidgetProvider1.class, 4, 1);
    protected static final ListWidgetSize LIST_WIDGET_SIZE2 = new ListWidgetSize(
            ListWidgetProvider2.class, 4, 2);
    protected static final ListWidgetSize LIST_WIDGET_SIZE3 = new ListWidgetSize(
            ListWidgetProvider3.class, 4, 3);
    protected static final ListWidgetSize LIST_WIDGET_SIZE4 = new ListWidgetSize(
            ListWidgetProvider4.class, 2, 2);
    protected static final ListWidgetSize LIST_WIDGET_SIZE5 = new ListWidgetSize(
            ListWidgetProvider5.class, 4, 4);

    /** List of all list widget sizes. */
    static final ListWidgetSize[] LIST_WIDGET_SIZES = new ListWidgetSize[] { LIST_WIDGET_SIZE1,
            LIST_WIDGET_SIZE2, LIST_WIDGET_SIZE3, LIST_WIDGET_SIZE4, LIST_WIDGET_SIZE5 };

    /** The actual concrete provider class for this widget size. */
    final Class<? extends ListWidgetProvider> widgetProviderClass;
    
    /** Widget width in home launcher cells. */
    final int widthCells;
    
    /** Widget height in home launcher cells. */
    final int heightCells;

    ListWidgetSize(Class<? extends ListWidgetProvider> widgetProviderClass, int widthCells,
            int heightCells) {
        this.widgetProviderClass = widgetProviderClass;
        this.widthCells = widthCells;
        this.heightCells = heightCells;
    }

    /** Compute widget gross area size in pixels based on current orientation */
    Point grossPixelSizeForOrientation(float density, Orientation orientation) {
        return WidgetUtil.widgetGrossPixelSize(density, orientation, widthCells, heightCells);
    }
}

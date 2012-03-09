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

import java.util.List;

import junit.framework.TestCase;

import android.graphics.Point;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.model.ItemModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.Orientation;

/**
 * Unit test for WidgetlUtil.
 * 
 * @author Tal Dayan
 */
public class WidgetUtilTest extends TestCase {

    private static final ItemModel ITEMS[] = new ItemModel[] {
        // Today
        new ItemModel("item0", false, false, ItemColor.NONE),
        new ItemModel("item1", true, false, ItemColor.NONE),

        // Tommorow
        new ItemModel("item2", false, false, ItemColor.NONE),
        new ItemModel("item3", true, false, ItemColor.NONE),
        new ItemModel("item4", false, true, ItemColor.NONE),
        new ItemModel("item5", true, true, ItemColor.NONE) };

    public void testComputePushScope() {
        AppModel model = new AppModel();

        for (int i = 0; i < ITEMS.length; i++) {
            model.appendItem((i < 2) ? PageKind.TODAY : PageKind.TOMOROW, ITEMS[i]);
        }

        for (PushScope pushScope : PushScope.values()) {
            LogUtil.info("*** %s ****", pushScope);
            final int[] expectedItemIndexes;
            switch (pushScope) {
                case NONE:
                    expectedItemIndexes = new int[] { 0 };
                    break;
                case UNLOCKED_ONLY:
                    // Tomorow's items are before today's
                    expectedItemIndexes = new int[] {
                        2,
                        0 };
                    break;
                case ALL:
                    // Tomorow's items preserve order
                    expectedItemIndexes = new int[] {
                        2,
                        4,
                        0 };
                    break;
                default:
                    throw new RuntimeException("Unknown push scope kind: " + pushScope);
            }

            List<ItemModelReadOnly> actualItems = WidgetUtil.selectTodaysActiveItemsByPushScope(
                            model, pushScope);

            assertEquals(expectedItemIndexes.length, actualItems.size());
            for (int i = 0; i < actualItems.size(); i++) {
                final ItemModel expected = ITEMS[expectedItemIndexes[i]];
                final ItemModelReadOnly actual = actualItems.get(i);
                final String message = String.format("%s/%d: expected: %s, actual %s", pushScope,
                                i, expected.getText(), actual.getText());
                LogUtil.info("--- " + message);
                assertSame(message, expected, actual);
            }
        }
    }
    
    public void testWidgetGrossPixelSize() {      
        // Portrait. Density = 1.0
        assertEquals(new Point(316, 96), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.PORTRAIT, 4, 1));
        assertEquals(new Point(316, 196), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.PORTRAIT, 4, 2));
        assertEquals(new Point(316, 296), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.PORTRAIT, 4, 3));
        assertEquals(new Point(316, 396), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.PORTRAIT, 4, 4));
        assertEquals(new Point(156, 196), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.PORTRAIT, 2, 2));

        // Portrait. Density = 1.5
        assertEquals(new Point(474, 144), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.PORTRAIT, 4, 1));
        assertEquals(new Point(474, 294), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.PORTRAIT, 4, 2));
        assertEquals(new Point(474, 444), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.PORTRAIT, 4, 3));
        assertEquals(new Point(474, 594), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.PORTRAIT, 4, 4));
        assertEquals(new Point(234, 294), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.PORTRAIT, 2, 2));

        // Landscape. Density = 1.0
        assertEquals(new Point(420, 70), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.LANDSCAPE, 4, 1));
        assertEquals(new Point(420, 144), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.LANDSCAPE, 4, 2));
        assertEquals(new Point(420, 218), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.LANDSCAPE, 4, 3));
        assertEquals(new Point(420, 292), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.LANDSCAPE, 4, 4));
        assertEquals(new Point(208, 144), WidgetUtil.widgetGrossPixelSize(1.0f, Orientation.LANDSCAPE, 2, 2));

        // Landscape. Density = 1.5
        assertEquals(new Point(630, 105), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.LANDSCAPE, 4, 1));
        assertEquals(new Point(630, 216), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.LANDSCAPE, 4, 2));
        assertEquals(new Point(630, 327), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.LANDSCAPE, 4, 3));
        assertEquals(new Point(630, 438), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.LANDSCAPE, 4, 4));
        assertEquals(new Point(312, 216), WidgetUtil.widgetGrossPixelSize(1.5f, Orientation.LANDSCAPE, 2, 2));
    }
}

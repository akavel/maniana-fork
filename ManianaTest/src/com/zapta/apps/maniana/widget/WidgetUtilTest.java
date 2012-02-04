// Copyright 2011, the original author or authors.

package com.zapta.apps.maniana.widget;

import java.util.List;

import junit.framework.TestCase;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.model.ItemModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.util.LogUtil;

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

    public void testcomputePushScope() {
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
}

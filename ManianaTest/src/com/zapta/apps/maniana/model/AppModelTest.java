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

package com.zapta.apps.maniana.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import junit.framework.TestCase;

import com.zapta.apps.maniana.model.AppModel.ProjectedImportStats;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for AppModel
 * 
 * @author Tal Dayan
 */
public class AppModelTest extends TestCase {

    private ItemModel[] mItems;
    private AppModel mModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mItems = new ItemModel[10];
        mModel = new AppModel();
        final long ts = 1234567;
        for (int i = 0; i < 10; i++) {
            mItems[i] = new ItemModel(ts, "id-" + i, "Item" + i, false, false, ItemColor.NONE);
            final PageKind pageKind = i < 5 ? PageKind.TODAY : PageKind.TOMOROW;
            mModel.appendItem(pageKind, mItems[i]);
        }
    }

    /** Additional setup for push tests. */
    private void setModelForPushTest() {
        // Today
        mItems[1].setIsCompleted(true);
        // Tomorrow
        mItems[6].setIsCompleted(true);
        mItems[7].setIsLocked(true);
        mItems[8].setIsCompleted(true);
        mItems[8].setIsLocked(true);
        mItems[9].setIsCompleted(true);
    }

    public void testGetItem() {
        for (int i = 0; i < 5; i++) {
            LogUtil.info("--- testGetItem, i = %d", i);
            // Today's items
            assertEquals(mItems[i], mModel.getItemReadOnly(PageKind.TODAY, i));
            assertEquals(mItems[i], mModel.getItemForMutation(PageKind.TODAY, i));
            // Tomorow's items
            assertEquals(mItems[i + 5], mModel.getItemReadOnly(PageKind.TOMOROW, i));
            assertEquals(mItems[i + 5], mModel.getItemForMutation(PageKind.TOMOROW, i));
        }
    }

    public void testLastPushTimestamp() {
        // Default value
        assertEquals("", mModel.getLastPushDateStamp());

        // Actual timestamp format we use
        mModel.setLastPushDateStamp("20120213");
        assertEquals("20120213", mModel.getLastPushDateStamp());

        // Arbitrary string
        mModel.setLastPushDateStamp("@#\n7 8");
        assertEquals("@#\n7 8", mModel.getLastPushDateStamp());

        // Empty string
        mModel.setLastPushDateStamp("");
        assertEquals("", mModel.getLastPushDateStamp());
    }

    public void testPush_expireAndClean() {
        setModelForPushTest();
        mModel.pushToToday(true, true);
        assertPageItems(PageKind.TODAY, 5, 7, 0, 2, 3, 4);
        assertUndoItems(PageKind.TODAY, 1);
        assertPageItems(PageKind.TOMOROW);
        assertUndoItems(PageKind.TOMOROW, 6, 8, 9);
    }

    public void testPush_expireOnly() {
        setModelForPushTest();
        mModel.pushToToday(true, false);
        assertPageItems(PageKind.TODAY, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4);
        assertUndoItems(PageKind.TODAY);
        assertPageItems(PageKind.TOMOROW);
        assertUndoItems(PageKind.TOMOROW);
    }

    public void testPush_cleanOnly() {
        setModelForPushTest();
        mModel.pushToToday(false, true);
        assertPageItems(PageKind.TODAY, 5, 0, 2, 3, 4);
        assertUndoItems(PageKind.TODAY, 1);
        assertPageItems(PageKind.TOMOROW, 7);
        assertUndoItems(PageKind.TOMOROW, 6, 8, 9);
    }

    public void testPush_none() {
        setModelForPushTest();
        mModel.pushToToday(false, false);
        assertPageItems(PageKind.TODAY, 5, 6, 9, 0, 1, 2, 3, 4);
        assertUndoItems(PageKind.TODAY);
        assertPageItems(PageKind.TOMOROW, 7, 8);
        assertUndoItems(PageKind.TOMOROW);
    }

    /**
     * Assert that a model page contains given items in given order.
     * 
     * @param pageKind page to assert on
     * @param expectedItemIndexes indexes in items[] of expected items.
     */
    private void assertPageItems(PageKind pageKind, int... expectedItemIndexes) {
        dumpModel(mModel);
        assertEquals(expectedItemIndexes.length, mModel.getPageItemCount(pageKind));
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(mItems[expectedItemIndex], mModel.getItemReadOnly(pageKind, i));
        }
    }

    /**
     * Assert that a model page undo buffer contains given items in given order.
     * 
     * @param pageKind page to assert on
     * @param expectedItemIndexes indexes in items[] of expected items
     */
    private void assertUndoItems(PageKind pageKind, int... expectedItemIndexes) {
        dumpModel(mModel);
        final List<ItemModel> undoItems = mModel.getPageModel(pageKind)
                .getUndoItemsCloneForTesting();
        assertEquals(expectedItemIndexes.length, undoItems.size());
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(mItems[expectedItemIndex], undoItems.get(i));
        }
    }

    public void testProjectedImportStats() {
        final AppModel model1 = new AppModel();
        final long ts = 1234567;
        model1.appendItem(PageKind.TODAY, new ItemModel(ts, "id-a", "aaa", false, false,
                ItemColor.GREEN));
        model1.appendItem(PageKind.TODAY, new ItemModel(ts, "id-b", "bbb", true, false,
                ItemColor.RED));
        model1.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-c", "ccc", false, true,
                ItemColor.NONE));

        final AppModel model2 = new AppModel();
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-a", "aaa", true, false,
                ItemColor.NONE));
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-c", "ccc", true, false,
                ItemColor.BLUE));
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-x", "xxx", false, false,
                ItemColor.RED));
        model2.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-y", "yyy", false, true,
                ItemColor.NONE));
        model2.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-a", "aaa", false, true,
                ItemColor.NONE));

        final ProjectedImportStats actual = model1.projectedImportStats(model2);
        assertEquals(3, actual.mergeKeep);
        assertEquals(0, actual.mergeDelete);
        assertEquals(2, actual.mergeAdd);

        assertEquals(2, actual.replaceKeep);
        assertEquals(1, actual.replaceDelete);
        assertEquals(3, actual.replaceAdd);
    }

    public void testMergeFrom() {
        final AppModel model1 = new AppModel();
        final long ts = 1234567;
        model1.appendItem(PageKind.TODAY, new ItemModel(ts, "id-a", "aaa", false, false,
                ItemColor.GREEN));
        model1.appendItem(PageKind.TODAY, new ItemModel(ts, "id-b", "bbb", true, false,
                ItemColor.RED));
        model1.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-c", "ccc", false, true,
                ItemColor.NONE));

        final AppModel model2 = new AppModel();
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-a", "aaa", true, false,
                ItemColor.NONE));
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-c", "ccc", true, false,
                ItemColor.BLUE));
        model2.appendItem(PageKind.TODAY, new ItemModel(ts, "id-x", "xxx", false, false,
                ItemColor.RED));
        model2.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-y", "yyy", false, true,
                ItemColor.NONE));
        model2.appendItem(PageKind.TOMOROW, new ItemModel(ts, "id-a", "aaa", false, true,
                ItemColor.NONE));

        model1.mergeFrom(model2);
        dumpModel(model1);

        assertEquals(4, model1.getPageItemCount(PageKind.TODAY));
        assertEquals(1, model1.getPageItemCount(PageKind.TOMOROW));

        // NOTE: this may fail because the merge order currently is not deterministic and depends
        // on hash table order.
        assertItem(model1.getItemReadOnly(PageKind.TODAY, 0), "yyy", false, false, ItemColor.NONE);
        assertItem(model1.getItemReadOnly(PageKind.TODAY, 1), "xxx", false, false, ItemColor.RED);
        assertItem(model1.getItemReadOnly(PageKind.TODAY, 2), "aaa", false, false, ItemColor.GREEN);
        assertItem(model1.getItemReadOnly(PageKind.TODAY, 3), "bbb", true, false, ItemColor.RED);

        assertItem(model1.getItemReadOnly(PageKind.TOMOROW, 0), "ccc", false, false, ItemColor.BLUE);
    }

    private void assertItem(ItemModelReadOnly item, String text, boolean isCompleted,
            boolean isLocked, ItemColor color) {
        assertEquals(text, item.getText());
        assertEquals(isCompleted, item.isCompleted());
        assertEquals(isLocked, item.isLocked());
        assertEquals(color, item.getColor());
    }

    private void dumpModel(AppModel model) {
        final StringWriter buffer = new StringWriter();
        final PrintWriter p = new PrintWriter(buffer);
        p.print("Model:\n");
        for (PageKind pageKind : PageKind.values()) {
            p.printf("  %s:\n", pageKind);
            p.print("    Items:\n");
            for (int i = 0; i < model.getPageItemCount(pageKind); i++) {
                p.printf("      %d: %s\n", i, model.getItemReadOnly(pageKind, i).getText());
            }
            p.print("    Undo:\n");
            final List<ItemModel> undoItems = model.getPageModel(pageKind)
                    .getUndoItemsCloneForTesting();
            for (int i = 0; i < undoItems.size(); i++) {
                p.printf("      %d: %s\n", i, undoItems.get(i).getText());
            }
        }
        p.flush();
        LogUtil.info(buffer.toString());
    }
}

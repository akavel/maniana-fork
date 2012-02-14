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
import android.text.format.Time;

import com.zapta.apps.maniana.model.persistence.ModelSerialization;
import com.zapta.apps.maniana.model.persistence.PersistenceMetadata;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for AppModel
 * 
 * @author Tal Dayan
 */
public class AppModelTest extends TestCase {

    private ItemModel[] items;
    private AppModel model;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        items = new ItemModel[10];
        model = new AppModel();
        for (int i = 0; i < 10; i++) {
            items[i] = new ItemModel("Item" + i, false, false, ItemColor.NONE);
            final PageKind pageKind = i < 5 ? PageKind.TODAY : PageKind.TOMOROW;
            model.appendItem(pageKind, items[i]);
        }
    }
    
    /** Additional setup for push tests. */
    private void setModelForPushTest() {
        // Today
        items[1].setIsCompleted(true);
        // Tomorrow
        items[6].setIsCompleted(true);
        items[7].setIsLocked(true);
        items[8].setIsCompleted(true);
        items[8].setIsLocked(true);
        items[9].setIsCompleted(true);
    }

    public void testGetItem() {
        for (int i = 0; i < 5; i++) {
            LogUtil.info("--- testGetItem, i = %d", i);
            // Today's items
            assertEquals(items[i], model.getItemReadOnly(PageKind.TODAY, i));
            assertEquals(items[i], model.getItemForMutation(PageKind.TODAY, i));
            // Tomorow's items
            assertEquals(items[i + 5], model.getItemReadOnly(PageKind.TOMOROW, i));
            assertEquals(items[i + 5], model.getItemForMutation(PageKind.TOMOROW, i));
        }
    }

    public void testLastPushTimestamp() {
        // Default value
        assertEquals("", model.getLastPushDateStamp());

        // Actual timestamp format we use
        model.setLastPushDateStamp("20120213");
        assertEquals("20120213", model.getLastPushDateStamp());

        // Arbitrary string
        model.setLastPushDateStamp("@#\n7 8");
        assertEquals("@#\n7 8", model.getLastPushDateStamp());

        // Empty string
        model.setLastPushDateStamp("");
        assertEquals("", model.getLastPushDateStamp());
    }

    public void testPush_expireAndClean() {
        setModelForPushTest();
        model.pushToToday(true, true);
        assertPageItems(PageKind.TODAY, 5, 7, 0, 2, 3, 4);
        assertUndoItems(PageKind.TODAY, 1);
        assertPageItems(PageKind.TOMOROW);
        assertUndoItems(PageKind.TOMOROW, 6, 8, 9);
    }

    public void testPush_expireOnly() {
        setModelForPushTest();
        model.pushToToday(true, false);
        assertPageItems(PageKind.TODAY, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4);
        assertUndoItems(PageKind.TODAY);
        assertPageItems(PageKind.TOMOROW);
        assertUndoItems(PageKind.TOMOROW);
    }
    
    public void testPush_cleanOnly() {
        setModelForPushTest();
        model.pushToToday(false, true);
        assertPageItems(PageKind.TODAY,  5, 0, 2, 3, 4);
        assertUndoItems(PageKind.TODAY, 1);
        assertPageItems(PageKind.TOMOROW, 7);
        assertUndoItems(PageKind.TOMOROW, 6, 8, 9);
    }
    
    public void testPush_none() {
        setModelForPushTest();
        model.pushToToday(false, false);
        assertPageItems(PageKind.TODAY,  5, 6, 9, 0, 1, 2, 3, 4);
        assertUndoItems(PageKind.TODAY);
        assertPageItems(PageKind.TOMOROW, 7, 8);
        assertUndoItems(PageKind.TOMOROW);
    }

    /**
     * Assert that a model page contains given items in given order.
     * 
     * @param pageKind
     *            page to assert on
     * @param expectedItemIndexes
     *            indexes in items[] of expected items.
     */
    private void assertPageItems(PageKind pageKind, int... expectedItemIndexes) {
        dumpModel();
        assertEquals(expectedItemIndexes.length, model.getPageItemCount(pageKind));
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(items[expectedItemIndex], model.getItemReadOnly(pageKind, i));
        }
    }
    
    /**
     * Assert that a model page undo buffer contains given items in given order.
     * 
     * @param pageKind
     *            page to assert on
     * @param expectedItemIndexes
     *            indexes in items[] of expected items
     */
    private void assertUndoItems(PageKind pageKind, int... expectedItemIndexes) {
        dumpModel();      
        final List<ItemModel> undoItems = model.getPageModel(pageKind).getUndoItemsCloneForTesting();       
        assertEquals(expectedItemIndexes.length, undoItems.size());
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(items[expectedItemIndex], undoItems.get(i));
        }
    }  

    private void dumpModel() {
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
            final List<ItemModel> undoItems = model.getPageModel(pageKind).getUndoItemsCloneForTesting();
            for (int i = 0; i < undoItems.size(); i++) {
                p.printf("      %d: %s\n", i, undoItems.get(i).getText());
            }           
        }       
        p.flush();
        LogUtil.info(buffer.toString());
    }
}

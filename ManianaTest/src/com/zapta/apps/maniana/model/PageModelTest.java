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

import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for PageModel
 * 
 * @author Tal Dayan
 */
public class PageModelTest extends TestCase {

    private ItemModel[] items;
    private PageModel pageModel;
    private ItemModel extraItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        items = new ItemModel[5];
        pageModel = new PageModel();
        final long ts = 1234567;
        for (int i = 0; i < 5; i++) {
            items[i] = new ItemModel(ts, "id-" + i, "Item" + i, false, false, 0, ItemColor.NONE);
            pageModel.appendItem(items[i]);
        }
        
        extraItem = new ItemModel(ts, "id-extra", "Extra1", false, false, 0, ItemColor.NONE);
        pageModel.appendUndoItemForTesting(extraItem);
    }

    public void testOrganizePageWithUndo_withDeletion() {
        items[0].setIsCompleted(true);
        items[1].setIsLocked(true);
        items[3].setIsCompleted(true);
        items[3].setIsLocked(true);
        
        OrganizePageSummary summary = new OrganizePageSummary();
        pageModel.organizePageWithUndo(true, 1, summary);

        dumpPageModel();
        
        assertPageItems(2, 4, 1);
        assertUndoItems(0, 3);

        assertTrue(summary.orderChanged);
        assertEquals(2, summary.completedItemsFound);
        assertEquals(2, summary.completedItemsDeleted);     
        assertEquals(2, summary.itemOfInterestNewIndex);
    }
    
    public void testOrganizePageWithUndo_noDeletion() {
        items[0].setIsCompleted(true);
        items[1].setIsLocked(true);
        items[3].setIsCompleted(true);
        items[3].setIsLocked(true);

        OrganizePageSummary summary = new OrganizePageSummary();
        pageModel.organizePageWithUndo(false, 2, summary);
        
        dumpPageModel();

        assertPageItems(2, 4, 0, 3, 1);
        
        // Since nothing was deleted, the undo buffer should be preserved
        assertEquals(1, pageModel.getUndoItemsCloneForTesting().size());
        assertEquals(extraItem, pageModel.getUndoItemsCloneForTesting().get(0));

        assertTrue(summary.orderChanged);
        assertEquals(2, summary.completedItemsFound);
        assertEquals(0, summary.completedItemsDeleted);     
        assertEquals(0, summary.itemOfInterestNewIndex);
    }
    
    public void testOrganizePageWithUndo_noChange() {
        items[2].setIsCompleted(true);
        items[3].setIsCompleted(true);
        items[3].setIsLocked(true);
        items[4].setIsLocked(true);

        OrganizePageSummary summary = new OrganizePageSummary();
        pageModel.organizePageWithUndo(false, -1, summary);
        
        dumpPageModel();

        assertPageItems(0, 1, 2, 3, 4);
        
        // Since nothing was deleted, the undo buffer should be preserved
        assertEquals(1, pageModel.getUndoItemsCloneForTesting().size());
        assertEquals(extraItem, pageModel.getUndoItemsCloneForTesting().get(0));

        assertFalse(summary.orderChanged);
        assertEquals(2, summary.completedItemsFound);
        assertEquals(0, summary.completedItemsDeleted);     
        assertEquals(-1, summary.itemOfInterestNewIndex);
    }

    /**
     * Assert that the page model contains given items in given order.
     * 
     * @param expectedItemIndexes
     *            indexes in items[] of expected items.
     */
    private void assertPageItems(int... expectedItemIndexes) {
        assertEquals(expectedItemIndexes.length, pageModel.itemCount());
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(items[expectedItemIndex], pageModel.getItem(i));
        }
    }

    /**
     * Assert that the page model undo buffer contains given items in given order.
     * 
     * @param expectedItemIndexes
     *            indexes in items[] of expected items
     */
    private void assertUndoItems(int... expectedItemIndexes) {
        final List<ItemModel> undoItems = pageModel.getUndoItemsCloneForTesting();
        assertEquals(expectedItemIndexes.length, undoItems.size());
        for (int i = 0; i < expectedItemIndexes.length; i++) {
            final int expectedItemIndex = expectedItemIndexes[i];
            assertEquals(items[expectedItemIndex], undoItems.get(i));
        }
    }

    private void dumpPageModel() {
        final StringWriter buffer = new StringWriter();
        final PrintWriter p = new PrintWriter(buffer);
        p.printf("Page:\n");
        p.print("  Items:\n");
        for (int i = 0; i < pageModel.itemCount(); i++) {
            p.printf("  %d: %s\n", i, pageModel.getItem(i).getText());
        }
        p.print("  Undo:\n");
        final List<ItemModel> undoItems = pageModel.getUndoItemsCloneForTesting();
        for (int i = 0; i < undoItems.size(); i++) {
            p.printf("    %d: %s\n", i, undoItems.get(i).getText());
        }
        p.flush();
        LogUtil.info(buffer.toString());
    }
}

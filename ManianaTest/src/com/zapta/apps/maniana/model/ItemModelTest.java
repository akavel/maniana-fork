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

import junit.framework.TestCase;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for PageModel
 * 
 * @author Tal Dayan
 */
public class ItemModelTest extends TestCase {

    private static class MergeTestCase {
        final ItemModelReadOnly item1;
        final ItemModelReadOnly item2;
        final ItemModelReadOnly expected;

        public MergeTestCase(ItemModelReadOnly item1, ItemModelReadOnly item2,
                ItemModelReadOnly expected) {
            this.item1 = item1;
            this.item2 = item2;
            this.expected = expected;
        }
    }

    final static long ts = 1234567;

    private static final MergeTestCase MERGE_TEST_CASES[] = {

        new MergeTestCase(new ItemModel(ts, "id11", "a", false, false, ItemColor.BLUE),
                new ItemModel(ts, "id12", "a", true, true, ItemColor.RED), new ItemModel(ts,
                        "id13", "a", false, false, ItemColor.RED)),
        new MergeTestCase(new ItemModel(ts, "id21", "a", true, true, ItemColor.RED), new ItemModel(
                ts, "id22", "a", true, true, ItemColor.BLUE), new ItemModel(ts, "id23", "a", true,
                true, ItemColor.RED)),
        new MergeTestCase(new ItemModel(ts, "id31", "a", false, false, ItemColor.NONE),
                new ItemModel(ts, "id32", "a", false, false, ItemColor.BLUE), new ItemModel(ts,
                        "id33", "a", false, false, ItemColor.BLUE)),
        new MergeTestCase(new ItemModel(ts, "id41", "a", false, false, ItemColor.NONE),
                new ItemModel(ts, "id42", "a", false, false, ItemColor.NONE), new ItemModel(ts,
                        "id43", "a", false, false, ItemColor.NONE)),
    };

    public void testMergePropertiesFrom() {
        for (int i = 0; i < MERGE_TEST_CASES.length; i++) {
            LogUtil.info("*** --- MergeTestCase %s ---", i);
            final MergeTestCase testCase = MERGE_TEST_CASES[i];
            ItemModel i1 = new ItemModel(testCase.item1);
            ItemModel i2 = new ItemModel(testCase.item2);
            i1.mergePropertiesFrom(i2);
            assertItemsEqual(testCase.expected, i1);
        }
    }

    private void assertItemsEqual(ItemModelReadOnly item1, ItemModelReadOnly item2) {
        assertEquals(item1.getText(), item2.getText());
        assertEquals(item1.isCompleted(), item2.isCompleted());
        assertEquals(item1.isLocked(), item2.isLocked());
        assertEquals(item1.getColor(), item2.getColor());
    }
}

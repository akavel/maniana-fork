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

package com.zapta.apps.maniana.util;

import junit.framework.TestCase;

/**
 * Unit test for ColorUtil.
 * 
 * @author Tal Dayan
 */
public class ColorUtilTest extends TestCase {

    /** Data for a single time period test case. */
    private class CompositeColorTestCase {
        private final int argb1;
        private final int argb2;
        private final int expectedResult;

        public CompositeColorTestCase(int argb1, int argb2, int expectedResult) {
            this.argb1 = argb1;
            this.argb2 = argb2;
            this.expectedResult = expectedResult;
        }
    }

    public void testCompositeColor() {
        CompositeColorTestCase testCases[] = {
                new CompositeColorTestCase(0x12345678, 0x00223344, 0x12345678),
                new CompositeColorTestCase(0x00556677, 0x00223344, 0x00000000),
                new CompositeColorTestCase(0x12345678, 0xff223344, 0xff223344),
                new CompositeColorTestCase(0x40606060, 0x40808080, 0x70727272),
                new CompositeColorTestCase(0x40223344, 0x40000000, 0x700e151d), };

        for (CompositeColorTestCase testCase : testCases) {
            LogUtil.debug("--- Composite color test: %08x, %08x", testCase.argb1, testCase.argb2);
            final int actualResult = ColorUtil.compositeColor(testCase.argb1, testCase.argb2);
            LogUtil.debug("Expected: %08x, actual: %08x", testCase.expectedResult, actualResult);
            assertEquals(testCase.expectedResult, actualResult);
        }
    }
}

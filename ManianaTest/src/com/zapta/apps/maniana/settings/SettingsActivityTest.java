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

package com.zapta.apps.maniana.settings;

import junit.framework.TestCase;
import android.content.Context;
import android.content.res.Resources;
import android.test.mock.MockContext;
import android.test.mock.MockResources;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for SettingsActivity.
 * 
 * @author Tal Dayan
 */
public class SettingsActivityTest extends TestCase {

    /** Data for an int to string mapping testcase. */
    private class intStringTestCase {
        private final int input;
        private String expectedResult;

        public intStringTestCase(int data, String expectedResult) {
            this.input = data;
            this.expectedResult = expectedResult;
        }
    }
    
    private static final Context mockContext = new MockContext() {
        private final Resources mockResources = new MockResources() {
            @Override
            public String getString(int id) throws NotFoundException {
                if (id == R.string.time_string_tonight) {
                    return "tonight";
                }
                if (id == R.string.time_string_in_d_hours) {
                    return "in %d hours";
                }
                if (id == R.string.time_string_in_d_days) {
                    return "in %d days";
                }
                throw new NotFoundException();
            }           
        };
        
        @Override
        public Resources getResources() {
            return mockResources;
        } 
    };
    
    // TODO: make to work. How do we access a context?
    public void testDatestamps() {
        intStringTestCase testCases[] = new intStringTestCase[] {
            new intStringTestCase(0, "tonight"),
            new intStringTestCase(1, "tonight"),
            new intStringTestCase(20, "in 20 hours"),
            new intStringTestCase(24, "in 24 hours"),
            new intStringTestCase(100, "in 5 days"),
            new intStringTestCase(24 * 7 - 1, "in 7 days"),
            new intStringTestCase(24 * 30, "in 30 days"), };

        for (intStringTestCase testCase : testCases) {
            LogUtil.debug("--- Time left message test case: in: %s, expected: %s", testCase.input,
                            testCase.expectedResult);
            assertEquals(testCase.expectedResult, SettingsActivity
                            .construtLockTimeLeftMessageSuffix(mockContext, testCase.input));
        }
    }
}

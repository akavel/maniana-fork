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
import android.text.format.Time;

import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for ModelUtil.
 * 
 * @author Tal Dayan
 */
public class ModelUtilTest extends TestCase {

    private class PushScopeTestCase {
        private final String timestamp;
        private final Time someTimeToday;

        private final PushScope expectedNeverPushScope;
        private final PushScope expectedMonthlyPushScope;
        private final PushScope expectedWeeklyPushScope;

        public PushScopeTestCase(String timestamp, String someTimeToday,
                        PushScope expectedNeverPushScope, PushScope expectedMonthlyPushScope,
                        PushScope expectedWeeklyPushScope) {
            this.timestamp = timestamp;
            this.someTimeToday = new Time();
            this.someTimeToday.parse(someTimeToday);

            this.expectedNeverPushScope = expectedNeverPushScope;
            this.expectedMonthlyPushScope = expectedMonthlyPushScope;
            this.expectedWeeklyPushScope = expectedWeeklyPushScope;
        }
    }

    public void testcomputePushScope() {
        PushScopeTestCase testCases[] = {
            new PushScopeTestCase("20111220", "20111130T235959", PushScope.UNLOCKED_ONLY,
                            PushScope.ALL, PushScope.ALL),
            new PushScopeTestCase("20111220", "20111217T235959", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.ALL),
            new PushScopeTestCase("20111220", "20111218T000000", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.UNLOCKED_ONLY),
            new PushScopeTestCase("20111220", "20111220T000000", PushScope.NONE, PushScope.NONE,
                            PushScope.NONE),
            new PushScopeTestCase("20111220", "20111224T000000", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.UNLOCKED_ONLY),
            new PushScopeTestCase("20111220", "20111224T235959", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.UNLOCKED_ONLY),
            new PushScopeTestCase("20111220", "20120101T000000", PushScope.UNLOCKED_ONLY,
                            PushScope.ALL, PushScope.ALL),
            new PushScopeTestCase("???", "20120101T000000", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.UNLOCKED_ONLY),
            new PushScopeTestCase(null, "20120101T000000", PushScope.UNLOCKED_ONLY,
                            PushScope.UNLOCKED_ONLY, PushScope.UNLOCKED_ONLY),
        };

        for (PushScopeTestCase testCase : testCases) {
            LogUtil.debug("--- test case: %s, %s", testCase.someTimeToday.toString(),
                            testCase.timestamp);
            {
                final PushScope pushScope = ModelUtil.computePushScope(testCase.timestamp,
                                testCase.someTimeToday, LockExpirationPeriod.NEVER);
                assertEquals(testCase.expectedNeverPushScope, pushScope);
            }
            {
                final PushScope pushScope = ModelUtil.computePushScope(testCase.timestamp,
                                testCase.someTimeToday, LockExpirationPeriod.MONTHLY);
                assertEquals(testCase.expectedMonthlyPushScope, pushScope);
            }
            {
                final PushScope pushScope = ModelUtil.computePushScope(testCase.timestamp,
                                testCase.someTimeToday, LockExpirationPeriod.WEEKLY);
                assertEquals(testCase.expectedWeeklyPushScope, pushScope);
            }
        }
    }

}

// Copyright 2011, the original author or authors.

package com.zapta.apps.maniana.preferences;

import junit.framework.TestCase;

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

    public void testDatestamps() {
        intStringTestCase testCases[] = new intStringTestCase[] {
            new intStringTestCase(0, "  (tonight)"),
            new intStringTestCase(1, "  (tonight)"),
            new intStringTestCase(20, "  (in 20 hours)"),
            new intStringTestCase(24, "  (in 24 hours)"),
            new intStringTestCase(100, "  (in 5 days)"),
            new intStringTestCase(24 * 7 - 1, "  (in 7 days)"),
            new intStringTestCase(24 * 30, "  (in 30 days)"), };

        for (intStringTestCase testCase : testCases) {
            LogUtil.debug("--- Time left message test case: in: %s, expected: %s", testCase.input,
                            testCase.expectedResult);
            assertEquals(testCase.expectedResult, PreferencesActivity
                            .construtLockTimeLeftMessageSuffix(testCase.input));
        }
    }
}

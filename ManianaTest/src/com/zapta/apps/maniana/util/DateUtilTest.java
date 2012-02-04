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

import javax.annotation.Nullable;

import junit.framework.TestCase;
import android.text.format.Time;

/**
 * Unit test for DateUtil.
 * 
 * @author Tal Dayan
 */
public class DateUtilTest extends TestCase {

    // private static final SimpleDateFormat TIME_FORMAT = new
    // SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    /** Data for a single time period test case. */
    private class DaysLeftTestCase {
        private final int year;
        private final int month;
        private final int day;
        private final int hours;
        private final int minutes;
        private final int seconds;
        private final int daysLeftInWeek;
        private final int daysLeftInMonth;

        public DaysLeftTestCase(int year, int month, int day, int hours, int minutes, int seconds,
                        int daysLeftInWeek, int daysLeftInMonth) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
            this.daysLeftInWeek = daysLeftInWeek;
            this.daysLeftInMonth = daysLeftInMonth;
        }
    }

    /** Data for a single string to string mapping testcase. */
    private class StringTestCase {
        private final String input;
        private String expectedResult;

        public StringTestCase(String data, String expectedResult) {
            this.input = data;
            this.expectedResult = expectedResult;
        }
    }

    /** Data for a single time period test case. */
    private class InSameTimePeriodTestCase {
        private final int year1;
        private final int month1;
        private final int day1;
        private final int year2;
        private final int month2;
        private final int day2;
        private final boolean isSameWeek;
        private final boolean isSameMonth;

        public InSameTimePeriodTestCase(int year1, int month1, int day1, int year2, int month2,
                        int day2, boolean isSameWeek, boolean isSameMonth) {
            this.year1 = year1;
            this.month1 = month1;
            this.day1 = day1;
            this.year2 = year2;
            this.month2 = month2;
            this.day2 = day2;
            this.isSameWeek = isSameWeek;
            this.isSameMonth = isSameMonth;
        }

    }

    public void testIsSameTimePeriods() {
        InSameTimePeriodTestCase testCases[] = {
            new InSameTimePeriodTestCase(2011, 12, 06, 2011, 12, 06, true, true),
            new InSameTimePeriodTestCase(2011, 12, 05, 2011, 12, 06, true, true),
            new InSameTimePeriodTestCase(2011, 12, 04, 2011, 12, 06, true, true),
            new InSameTimePeriodTestCase(2011, 12, 03, 2011, 12, 06, false, true),
            new InSameTimePeriodTestCase(2011, 12, 01, 2011, 12, 06, false, true),
            new InSameTimePeriodTestCase(2011, 11, 30, 2011, 12, 06, false, false),

            new InSameTimePeriodTestCase(2011, 12, 11, 2011, 12, 12, true, true) };

        for (InSameTimePeriodTestCase testCase : testCases) {
            final Time t1 = new Time();
            t1.set(testCase.day1, testCase.month1 - 1, testCase.year1);
            final Time t2 = new Time();
            t2.set(testCase.day2, testCase.month2 - 1, testCase.year2);

            LogUtil.debug("--- Time Periods test case: t1= %s, t2=%s", dateToString(t1),
                            dateToString(t2));

            assertEquals(testCase.isSameWeek, DateUtil.isSameWeek(t1, t2));
            assertEquals(testCase.isSameWeek, DateUtil.isSameWeek(t2, t1));

            assertEquals(testCase.isSameMonth, DateUtil.isSameMonth(t1, t2));
            assertEquals(testCase.isSameMonth, DateUtil.isSameMonth(t2, t1));
        }
    }

    public void testDatestamps() {
        StringTestCase testCases[] = new StringTestCase[] {
            // Bad datestamps
            new StringTestCase("", "(null)"),
            new StringTestCase("  ", "(null)"),
            new StringTestCase("null", "(null)"),
            new StringTestCase(null, "(null)"),
            new StringTestCase("xyz", "(null)"),
            new StringTestCase("-20111204", "(null)"),

            // Good datestamps
            new StringTestCase("20111223", "2011.12.23"),
            new StringTestCase("20111231", "2011.12.31"),
            new StringTestCase("20120101", "2012.01.01"),
            new StringTestCase("10000101", "1000.01.01"),
            new StringTestCase("20111211", "2011.12.11"),
            new StringTestCase("20111212", "2011.12.12"),
            new StringTestCase("22001231", "2200.12.31"), };

        for (StringTestCase testCase : testCases) {
            LogUtil.debug("--- Parse Datestamp test case: in: %s, expected: %s", testCase.input,
                            testCase.expectedResult);

            Time t = new Time();
            final boolean parsedOk = DateUtil.setFromString(t, testCase.input);
            if (!parsedOk) {
                t = null;
            }

            assertEquals(testCase.expectedResult, dateToString(t));

            // Test timestamp
            if (t != null) {
                assertEquals(testCase.input, DateUtil.dateToString(t));
            }
        }
    }

    public void testHoursLeftInPeriod() {
        DaysLeftTestCase testCases[] = {
            // Whole days
            new DaysLeftTestCase(2011, 12, 1, 0, 0, 0, 3 * 24, 31 * 24),
            new DaysLeftTestCase(2011, 12, 10, 0, 0, 0, 1 * 24, 22 * 24),
            new DaysLeftTestCase(2011, 12, 30, 0, 0, 0, 2 * 24, 2 * 24),
            new DaysLeftTestCase(2011, 12, 31, 0, 0, 0, 1 * 24, 1 * 24),
            new DaysLeftTestCase(2010, 12, 26, 0, 0, 0, 7 * 24, 6 * 24),

            // Partial days
            new DaysLeftTestCase(2011, 12, 01, 20, 0, 0, 2 * 24 + 4, 31 * 24 - 20),
            new DaysLeftTestCase(2011, 12, 01, 20, 1, 1, 2 * 24 + 4, 31 * 24 - 20),
            new DaysLeftTestCase(2011, 12, 01, 20, 59, 59, 2 * 24 + 4, 31 * 24 - 20),
            new DaysLeftTestCase(2011, 12, 31, 23, 59, 59, 1, 1), };

        for (DaysLeftTestCase testCase : testCases) {
            final Time t = new Time();
            t.set(testCase.seconds, testCase.minutes, testCase.hours, testCase.day,
                            testCase.month - 1, testCase.year);

            // testCase.year, testCase.month - 1,
            // testCase.day, testCase.hours, testCase.minutes, testCase.seconds);

            LogUtil.debug("--- Hours Left test case: cal: %s", timeToString(t));
            assertEquals(testCase.daysLeftInWeek, DateUtil.hoursToEndOfWeek(t));
            assertEquals(testCase.daysLeftInMonth, DateUtil.hoursToEndOfMonth(t));
        }
    }

    /** Utility for testing. */
    private static String timeToString(@Nullable Time t) {
        return (t == null) ? "(null)" : String.format("%04d.%02d.%02d:%02d.%02d.%02d", t.year,
                        t.month + 1, t.monthDay, t.hour, t.minute, t.second);
    }
    
    /** Utility for testing. */
    private static String dateToString(@Nullable Time t) {
        return (t == null) ? "(null)" : String.format("%04d.%02d.%02d", t.year,
                        t.month + 1, t.monthDay);
    }

    // /** Utility for testing. */
    // private static String timeToString(@Nullable GregorianCalendar cal) {
    // return (cal == null) ? "(null)" : TIME_FORMAT.format(cal.getTimeInMillis());
    // }
}

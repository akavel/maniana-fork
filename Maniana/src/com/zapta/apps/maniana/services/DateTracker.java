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

package com.zapta.apps.maniana.services;

import android.text.format.Time;

import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.util.DateUtil;

/**
 * Tracks the current date. Provide current date information and detection of date changes for
 * pushing Tomorow items to Today page.
 * 
 * @author Tal Dayan
 */
public class DateTracker {

    /** Caching of current user visible day of week. E.g. "Sunday" */
    private String mUserDayOfWeekString;

    /** Caching of current user visible current month/day date */
    private String mUserMonthDayString;

    /** Caching of the last updated date. */
    private Time mCachedDate = new Time();
    
    /** Used to avoid object instantiation. */
    private Time mTempTime = new Time();

    /** Caching of date stamp of mCachedDate. Not user visible. Persisted. */
    private String mCachedDateString;

    public DateTracker() {
        updateDate();
    }

    /** Read today's date and cache values. */
    public void updateDate() {
        // TODO: could cache the time of end of day and skip this update unless current
        // time passed that limit (performance).
        //final GregorianCalendar now = new GregorianCalendar();
        
        mTempTime.setToNow();

        if (!DateUtil.isSameDate(mTempTime, mCachedDate)) {
            mCachedDate.set(mTempTime);
            mCachedDateString = DateUtil.dateToString(mCachedDate);
            //final long time = now.getTimeInMillis();
            mUserDayOfWeekString = mCachedDate.format("%A");
            mUserMonthDayString = mCachedDate.format("%b %d");
        }
    }

    /** Get day of week string. User visible */
    public final String getUserDayOfWeekString() {
        return mUserDayOfWeekString;
    }

    /** Get month/day week. User visible. */
    public final String getUserMonthDayString() {
        return mUserMonthDayString;
    }

    /** Get year.month.day datestamp. Non user visible. Persisted with model. */
    public final String getDateStampString() {
        return mCachedDateString;
    }
    
    public PushScope computePushScope(String lastPushTimestamp,
                    LockExpirationPeriod lockExpirationPeriod) {
        return ModelUtil.computePushScope(lastPushTimestamp, mCachedDate, lockExpirationPeriod);
    }
}
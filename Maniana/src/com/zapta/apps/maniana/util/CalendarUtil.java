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

import android.content.Intent;
import android.net.Uri;

import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * Google Calendar related utility methods.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public final class CalendarUtil {

    /** Do not instantiate */
    private CalendarUtil() {
    }

    
    /** Returned true if ok. Result depends on time now. Should not be cached for too long. */
    public static Intent constructGoogleCalendarIntent() {
        final int OS_VERSION = android.os.Build.VERSION.SDK_INT;

        // Construct the intent
        final Intent intent = new Intent();
        if (OS_VERSION <= 14) {
            intent.setClassName("com.android.calendar", "com.android.calendar.AgendaActivity");
        } else {
            // ICS added API for launching the google calendar. See documentation at:
            // http://developer.android.com/guide/topics/providers/calendar-provider.html#intents
            final long startTimeMillis = System.currentTimeMillis();
            final String url = "content://com.android.calendar/time/" + startTimeMillis;
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        }

        return intent;
    }
}

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

import android.content.Context;
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

    /** 
     * Try constructing an intent to launch google calendar for time = now.
     * <p>
     * Note: we could cache the intent or the intent type for better performance.
     */
    @Nullable
    public static Intent maybeConstructGoogleCalendarIntent(Context context) {
        // Try variant 1. Older.
        {
            final Intent intent = constructGoogleCalendarIntentVariant1();
            if (IntentUtil.isIntentAvailable(context, intent)) {
                LogUtil.info("Using google calendar intent variant 1");
                return intent;
            }
        }

        // Try variant 2. Newer.
        // See // http://developer.android.com/guide/topics/providers/calendar-provider.html#intents
        {
            final Intent intent = constructGoogleCalendarIntentVariant2();
            if (IntentUtil.isIntentAvailable(context, intent)) {
                LogUtil.info("Using google calendar intent variant 1");
                return intent;
            }
        }

        // Fail
        LogUtil.info("No functional google calendar intent variant");
        return null;
    }

    // For debugging only.
    public static String debugGoogleCalendarVariants(Context context) {
        final StringBuilder sb = new StringBuilder();
        
        if (IntentUtil.isIntentAvailable(context, constructGoogleCalendarIntentVariant1())) {
            sb.append("V1");
        }

        if (IntentUtil.isIntentAvailable(context, constructGoogleCalendarIntentVariant2())) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append("V2");
        }

        if (sb.length() == 0) {
            sb.append("NONE");
        }

        return sb.toString();
    }

    /** Google calendar intent variant 1 */
    private static Intent constructGoogleCalendarIntentVariant1() {
        final Intent intent = new Intent();
        return intent.setClassName("com.android.calendar", "com.android.calendar.AgendaActivity");
    }

    /** Google calendar intent variant 1 */
    private static Intent constructGoogleCalendarIntentVariant2() {
        final Intent intent = new Intent();

        // Newer calendar have API for launching the google calendar. See documentation at:
        // http://developer.android.com/guide/topics/providers/calendar-provider.html#intents
        final long startTimeMillis = System.currentTimeMillis();
        final String url = "content://com.android.calendar/time/" + startTimeMillis;
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        return intent;
    }

}

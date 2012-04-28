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

package com.zapta.apps.maniana.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * Provides midnight trigger for updating the widgets and issuing notifications.
 * 
 * @author Tal Dayan
 */
// TODO: have this receiver responding also to date or time zone change by user. Since they may
// also affect the widgets.
public class WidgetMidnightTicker extends BroadcastReceiver {

    // Should match AndroidManifest.xml.
    public static final String WIDGET_UPDATE_ACTION = "com.zapta.apps.maniana.widget.WIDGET_UPDATE_ACTION";

    /** Trigger slightly after midnight to avoid truncation and timing errors, etc. */
    private static final int MIDNIGHT_MARGIN_MILLIS = 60000;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.info("WidgetMidnightTicker onRecieve: " + intent);
        BaseWidgetProvider.updateAllWidgetsFromContext(context);
    }

    /**
     * Schedule or reschedule midnight widget update.
     * 
     * Called from few hooks to make sure we still have a pending midnight alarm.
     */
    public static final void scheduleMidnightUpdates(Context context) {
        Intent intent = new Intent(WIDGET_UPDATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        final long startTimeUtcMillis = utcMillisNextMidnight() + MIDNIGHT_MARGIN_MILLIS;

        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, startTimeUtcMillis, AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    /** Get time of next midnight in UTC millis. */
    public static final long utcMillisNextMidnight() {
        Time t = new Time();
        t.setToNow();
        t.monthDay++;
        t.hour = 0;
        t.minute = 0;
        t.second = 0;
        return t.normalize(true);
    }

}

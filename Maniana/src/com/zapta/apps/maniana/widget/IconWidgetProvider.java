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

import java.util.List;

import javax.annotation.Nullable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.RemoteViews;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.MainActivity;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.preferences.PreferencesTracker;

/**
 * Implemnets the Maniana icon widgets.
 * 
 * @author Tal Dayan
 */
public class IconWidgetProvider extends BaseWidgetProvider {

    public IconWidgetProvider() {
    }

    /** Called by the widget host. */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {        
        update(context, appWidgetManager, appWidgetIds, loadModel(context));
    }

    /** Internal widget update method. */
    private static final void update(Context context, AppWidgetManager appWidgetManager,
                    int[] appWidgetIds, @Nullable AppModel model) {
        if (appWidgetIds.length == 0) {
            return;
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(context);

        final String label;
        if (model == null) {
            label = "??";
        } else {
            final LockExpirationPeriod lockExpirationPeriod = PreferencesTracker
                            .readLockExpierationPeriodPreference(sharedPreferences);
            // TODO: reorganize the code. No need to read lock preference if date now is same as
            // model.
            // TODO: could reuse a temp Time member.
            Time now = new Time();
            now.setToNow();
            List<ItemModelReadOnly> items = WidgetUtil.selectTodaysActiveItemsByTime(model, now,
                            lockExpirationPeriod);
            
            label = String.format("%d", items.size());
        }
        

        // Provides access to the remote view hosted by the home launcher.
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_icon_layout);

        // Set widget on click to trigger the Manian app
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_icon_top_view, pendingIntent);

        remoteViews.setTextViewText(R.id.widget_icon_label_text_view, label);

        // Tell the app widget manager to replace the views with the new views. This is not a 
        // partial update.
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
    
    public static void updateAllIconWidgetsFromModel(Context context, @Nullable AppModel model) {
        // Get list of all widget ids
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] widgetIds =  appWidgetManager.getAppWidgetIds(new ComponentName(context,
                      IconWidgetProvider.class));
        
        // Update (ignores silently if widgetIds is empty)
        update(context, appWidgetManager, widgetIds, model);
    }
}

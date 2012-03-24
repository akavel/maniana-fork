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

import javax.annotation.Nullable;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.model.OrganizePageSummary;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.persistence.ModelLoadingResult;
import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.preferences.PreferencesTracker;

/**
 * Base class widget providers.
 * 
 * @author Tal Dayan
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {

    /** Model is already pushed and sorted according to current settings. */
    public static void updateAllWidgetsFromModel(Context context, @Nullable AppModel model) {
        IconWidgetProvider.updateAllIconWidgetsFromModel(context, model);
        ListWidgetProvider.updateAllListWidgetsFromModel(context, model);
    }

    public static void updateAllWidgetsFromContext(Context context) {
        updateAllWidgetsFromModel(context, loadModelForWidgets(context));
    }

    /** Load model. Return null if error. The model is pushed and sorted based on current settings */
    @Nullable
    protected static AppModel loadModelForWidgets(Context context) {
        // Load model
        final AppModel model = new AppModel();
        final ModelLoadingResult modelLoadingResult = ModelPersistence.loadModelDataFile(context,
                model);
        if (!modelLoadingResult.outcome.isOk()) {
            return null;
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        final LockExpirationPeriod lockExpirationPeriod = PreferencesTracker
                .readLockExpierationPeriodPreference(sharedPreferences);
        final boolean removeCompletedOnPush = PreferencesTracker
                .readAutoDailyCleanupPreference(sharedPreferences);
        final boolean includeCompletedItems = PreferencesTracker
                .readWidgetShowCompletedItemsPreference(sharedPreferences);
        final boolean sortItems = includeCompletedItems ? PreferencesTracker
                .readAutoSortPreference(sharedPreferences) : false;

        Time timeNow = new Time();
        timeNow.setToNow();

        final PushScope pushScope = ModelUtil.computePushScope(model.getLastPushDateStamp(),
                timeNow, lockExpirationPeriod);

        if (pushScope.isActive()) {
            final boolean unlockAllLocks = (pushScope == PushScope.ALL);
            model.pushToToday(unlockAllLocks, removeCompletedOnPush);
            
            // NOTE: if not pushing, model is assumed to already be consistent with the
            // current auto sorting setting.
            if (sortItems) {
                OrganizePageSummary summary = new OrganizePageSummary();
                model.organizePageWithUndo(PageKind.TODAY, false, -1, summary);
                // NOTE: we don't bother to sort Maniana page since it does not affect the widgets
            }
        } 

        return model;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        WidgetMidnightTicker.scheduleMidnightUpdates(context);
    }
}

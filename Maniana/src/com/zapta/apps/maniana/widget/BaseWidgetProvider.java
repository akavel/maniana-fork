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

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.persistence.ModelLoadingResult;
import com.zapta.apps.maniana.persistence.ModelPersistence;

/**
 * Base class widget providers.
 * 
 * @author Tal Dayan
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider {

    public static void updateAllWidgetsFromModel(Context context, @Nullable AppModel model) {
        IconWidgetProvider.updateAllIconWidgetsFromModel(context, model);
        ListWidgetProvider.updateAllListWidgetsFromModel(context, model);
    }

    public static void updateAllWidgetsFromContext(Context context) {
        updateAllWidgetsFromModel(context, loadModel(context));
    }

    /** Load model. Return null if error. */
    @Nullable
    protected static AppModel loadModel(Context context) {
        // Load model
        final AppModel model = new AppModel();
        final ModelLoadingResult modelLoadingResult = ModelPersistence.loadModelDataFile(context,
                model);
        return modelLoadingResult.outcome.isOk() ? model : null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        WidgetMidnightTicker.scheduleMidnightUpdates(context);
    }

}

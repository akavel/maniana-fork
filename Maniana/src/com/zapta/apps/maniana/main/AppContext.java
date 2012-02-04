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

package com.zapta.apps.maniana.main;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
import android.content.Context;

import com.zapta.apps.maniana.controller.Controller;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.preferences.PreferencesTracker;
import com.zapta.apps.maniana.services.AppResources;
import com.zapta.apps.maniana.services.AppServices;
import com.zapta.apps.maniana.services.DateTracker;
import com.zapta.apps.maniana.view.AppView;

/**
 * Central references to all parts of the application. Initialized once upon main activity
 * creation.
 * 
 * @author Tal Dayan.
 */
public class AppContext {
    /** The main activity of this app. */
    private final MainActivity mMainActivity;

    /** The preferences tracker of this app. */
    private PreferencesTracker mAppPreferences;

    /** The data tracker of this app. */
    private DateTracker mDateTracker = new DateTracker();

    /** Access to resources used by this app */
    private AppResources mResources;

    /** Common services used by this app */
    private AppServices mServices;

    /** The app data model. Contains the items' data. */
    private AppModel mModel;

    /** The app controller. Contains the main app logic. */
    private Controller mController;

    /** The app view. */
    private AppView mView;

    AppContext(MainActivity mainActivity) {
        mMainActivity = checkNotNull(mainActivity);
        mModel = new AppModel(); 
        mAppPreferences = new PreferencesTracker(this);
        mResources = new AppResources(this);
        mServices = new AppServices(this);
        mController = new Controller(this);       
        mView = new AppView(this);

    }

    public final MainActivity mainActivity() {
        return mMainActivity;
    }

    public final Context context() {
        // The main activity is also the context.
        return (Context) mMainActivity;
    }

    public PreferencesTracker pref() {
        return mAppPreferences;
    }

    public final DateTracker dateTracker() {
        return mDateTracker;
    }

    public final AppResources resources() {
        return mResources;
    }

    public final AppServices services() {
        return mServices;
    }

    public final AppModel model() {
        return mModel;
    }

    public final Controller controller() {
        return mController;
    }

    public final AppView view() {
        return mView;
    }
}

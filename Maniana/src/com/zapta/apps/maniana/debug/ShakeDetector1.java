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

package com.zapta.apps.maniana.debug;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.util.LogUtil;

// Experimental implementation
public class ShakeDetector1 implements ShakeDetector {

    private final AppContext mApp;

    private final SensorManager mSensorManager;

    private boolean mIsEnabled = false;

    private float mAccelCurrent;

    private float mAccelLast;

    // false -> waiting for on, true -> waiting for false;
    private boolean mState;

    private final float history[] = new float[4];

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent se) {
            handleSensorChanged(se);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public ShakeDetector1(AppContext app) {
        /* do this in onCreate */
        this.mApp = app;
        mSensorManager = (SensorManager) app.context().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    private final void historyFill(float value) {
        Arrays.fill(history, value);
    }

    private final void historyPush(float newValue) {
        for (int i = history.length - 1; i >= 1; i--) {
            history[i] = history[i - 1];
        }
        history[0] = newValue;
    }

    private final float historyAvgAbs() {
        float result = 0;
        for (int i = 0; i < history.length; i++) {
            result += Math.abs(history[i]);
        }
        return result / history.length;
    }

    private final float historyMin() {
        float result = history[0];
        for (int i = 1; i < history.length; i++) {
            result = Math.min(result, history[i]);
        }
        return result;
    }

    private final float historyMax() {
        float result = history[0];
        for (int i = 1; i < history.length; i++) {
            result = Math.max(result, history[i]);
        }
        return result;
    }

    @Override
    public void enable() {
        if (!mIsEnabled) {
            // Reset state. Waiting for a quite period first.
            mState = true;
            historyFill(0);
            // mStateCounter = 0;

            mIsEnabled = true;
            mSensorManager.registerListener(mSensorListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void disable() {
        if (mIsEnabled) {
            mSensorManager.unregisterListener(mSensorListener);
            mIsEnabled = false;
        }
    }

    private void handleSensorChanged(SensorEvent se) {
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];

        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));

        float delta = mAccelCurrent - mAccelLast;

        final boolean lastState = mState;

        historyPush(delta);
        final float avg = historyAvgAbs();
        final float min = historyMin();
        final float max = historyMax();

        if (mState) {
            if (avg < 0.5f && min > -0.5f && max < 0.5f) {
                mState = false;
            }
        } else {
            if (avg >= 4 && min < -2f && max > 2) {
                mState = true;
            }
        }

        LogUtil.debug("Delta: %s, state: %s, avg: %s, min: %s, max: %s", delta, mState, avg, min,
                max);

        if (mState && !lastState) {
            mApp.controller().onAddItemByTextButton(mApp.view().getCurrentPage());
            LogUtil.debug("*** Shake!!!");
        }
    }

}

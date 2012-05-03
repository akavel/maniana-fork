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

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * An implementation of ShakerDetector. This implementation computes for each accelerometer event
 * the accelerator change magnitude |da=<dx, dy, dz>| where da the acceleration difference vector
 * from previous acceleration event (da represents the first derivative of the acceleration). An
 * shake event is generated if (da1 - da2) > threshold, where da1 (da2) is the average da over the
 * last N1 (N2) events, where N1 < N2. da1 represent the recent value of da while da2 represents the
 * longer term background noise level.
 * 
 * @author Tal Dayan.
 */
public class ShakeImpl implements Shaker {

    /** Number of events in the short term time window */
    private final static int N1 = 1;

    /** Number of events in the longer term time window (background noise) */
    private final static int N2 = 5;

    private final ShakerListener mListener;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;

    /** Indicates if the shaker is currently resumed or paused. */
    private boolean mIsResumed = false;

    /** Acceleration from previous event. */
    private float lastX;
    private float lastY;
    private float lastZ;

    /** System time of previous event. */
    private long mLastTimeMillis;

    /** Magnitude from last N2 events. */
    private final int[] history = new int[N2];

    /** Next insertion point in history, [0..N1) */
    private int nextIndex;

    /** Sum of the last N1 history points. */
    private int sum1;

    /** Sum of the last N2 history points. */
    private int sum2;

    /** If greater than zero, do not allow a shake event for this number of sensor events. */
    private int blackout = 0;

    // TODO: remove this field.
    /** Used to log a heat beat when shaker is active. */
    private int mEventCounter = 0;

    /** Shake event is triggered when signal > this value. Set later. */
    private int threshold;

    /** Event handling adapter. */
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent se) {
            handleSensorChanged(se);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Ignored
        }
    };

    /** Constructor. Leaves the shaker in paused state. */
    public ShakeImpl(Context context, ShakerListener listner) {
        this.mListener = listner;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    
    private final void resetHistory() {
        LogUtil.debug("Reseting history");
        
        Arrays.fill(history, 0);
        nextIndex = 0;

        sum1 = 0;
        sum2 = 0;
        
        // TODO: have a more explicit 'history full' condition.
        //
        // Suppress shake events until the history buffer will get refilled
        blackout = N2 - 1;
    }

    private final void resetState() {
        LogUtil.debug("Reseting state");
        resetHistory();       

        lastX = 0f;
        lastY = 0f;
        lastZ = 0f;

        mLastTimeMillis = 0;     
    }

    /**
     * Accelerometer change event. Called periodically when in resume state.
     */
    private void handleSensorChanged(SensorEvent se) {
        if ((mEventCounter++ % 200) == 0) {
            LogUtil.info("Shake detector is active");
        }

        // Accelerations in X,Y,Z direction
        final float x = se.values[SensorManager.DATA_X];
        final float y = se.values[SensorManager.DATA_Y];
        final float z = se.values[SensorManager.DATA_Z];

        // Calculate acceleration change (first derivative of acceleration vector).
        final float dX = x - lastX;
        final float dY = y - lastY;
        final float dZ = z - lastZ;      

        final long currentTimeMillis = System.currentTimeMillis();
        
        // If no previous sample than skip this event.       
        if (mLastTimeMillis == 0) {
            LogUtil.info("No prev sample, skipping this one");
            mLastTimeMillis = currentTimeMillis;
            return;
        }
        
        // If delta time is way too long, reset state. Sensing has paused
        // for some reason.       
        final long deltaTimeMillis = (currentTimeMillis - mLastTimeMillis);
        if (deltaTimeMillis > 5000) {
            // TODO: reseting here loose the current event data. This will require
            // one more event to settle down. Can we preserve it in the lastXYZT?
            LogUtil.info("Reseting history, dt: %sms", deltaTimeMillis);
            resetHistory();
            return;
        }

        mLastTimeMillis = currentTimeMillis;

        // Calculate change magnitude |<dx, dy, dz>|. Scaled by an arbitrary scale
        // to provide enough int bits of accuracy. We use ints to avoid accomulating
        // error in the incremental tracking of sum1, sum2.
        final int newValue = (int) (Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ)) * 500);

        // Push to history queue and update incrementally the N1 and N2 sums.
        final int droppedValue1 = history[(nextIndex + N2 - N1) % N2];
        final int droppedValue2 = history[nextIndex];

        history[nextIndex++] = newValue;
        if (nextIndex >= N2) {
            nextIndex = 0;
        }

        sum1 += (newValue - droppedValue1);
        sum2 += (newValue - droppedValue2);

        // Save for next iteration
        lastX = x;
        lastY = y;
        lastZ = z;

        // If in blackout, don't issue a shake event in this cycle.
        if (blackout > 0) {
            blackout--;
            return;
        }

        // The monitored signal is the difference between the short term average and the long
        // term average (noise level)
        final int avg1 = sum1 / N1;
        final int avg2 = sum2 / N2;
        final int signal = (avg1 - avg2);

        // LogUtil.debug("signal: %s", signal);

        // Compare signal to the detection threshold
        if (signal > threshold) {
            mListener.onShake();
            // Debouncing. Avoid successive shake event for the next N2 cycles.
            blackout = N2;
        }
    }

    @Override
    public boolean resume(int force) {
        if (!mIsResumed) {
            resetState();
            // Using NORMAL (low) rate for better battery life.
            mIsResumed = mSensorManager.registerListener(mSensorListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Force sensitivity to [1..10]
        final int actualForce = Math.max(1, Math.min(10, force));

        // Map sensitivity to threshold. Values are based on trial and error..
        threshold = 1300 + (force * 700);

        LogUtil.debug("Shaker resumed, force: %s, threshold: %s", actualForce, threshold);
        return mIsResumed;
    }

    @Override
    public void pause() {
        if (mIsResumed) {
            mSensorManager.unregisterListener(mSensorListener);
            mIsResumed = false;
        }
    }
}

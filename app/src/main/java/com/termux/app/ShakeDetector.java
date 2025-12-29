package com.termux.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detects phone shaking. If more than 75% of the samples taken in the past 0.5s are
 * accelerating, the device is a) shaking, or b) free falling 18+ inches. TODO: distinguish
 * these two cases.
 * 
 * Adapted from http://code.google.com/p/android-shake-detect/
 */
public class ShakeDetector implements SensorEventListener {
    /**
     * When the magnitude of total acceleration exceeds this
     * value, the phone is accelerating.
     */
    private static final float ACCELERATION_THRESHOLD = 13f;

    /**
     * Number of acceleration samples to require before reporting shake.
     */
    private static final int MIN_ACCELERATION_SAMPLES = 15;

    /**
     * Portion of MIN_ACCELERATION_SAMPLES that must be accelerating to constitute a shake.
     */
    private static final float MIN_PORTION_ACCELERATING = 0.75f;

    private final ShakeCallback callback;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    // Circular history of acceleration samples.
    private final boolean[] accelerating = new boolean[MIN_ACCELERATION_SAMPLES];
    private int accelerationIdx = 0;
    private int numAccelerating = 0;

    // Debouncing to prevent rapid repeated callbacks
    private long lastShakeTime = 0;
    private static final long SHAKE_DEBOUNCE_MS = 2000; // 2 seconds between shakes

    public interface ShakeCallback {
        void onShake();
    }

    public ShakeDetector(Context context, ShakeCallback callback) {
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];

        float total = ax * ax + ay * ay + az * az;
        boolean acceleratingNow = total > ACCELERATION_THRESHOLD * ACCELERATION_THRESHOLD;

        if (acceleratingNow) {
            numAccelerating++;
        }
        if (accelerating[accelerationIdx]) {
            numAccelerating--;
        }
        accelerating[accelerationIdx] = acceleratingNow;
        accelerationIdx = (accelerationIdx + 1) % accelerating.length;

        if (numAccelerating > MIN_PORTION_ACCELERATING * MIN_ACCELERATION_SAMPLES) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShakeTime > SHAKE_DEBOUNCE_MS) {
                lastShakeTime = currentTime;
                callback.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignored
    }
}
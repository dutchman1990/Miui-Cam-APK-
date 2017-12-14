package com.android.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

public class SensorStateManager {
    private static final int CAPTURE_POSTURE_DEGREE = SystemProperties.getInt("capture_degree", 45);
    private static final double GYROSCOPE_MOVING_THRESHOLD = ((double) (((float) SystemProperties.getInt("camera_moving_threshold", 15)) / 10.0f));
    private static final double GYROSCOPE_STABLE_THRESHOLD = ((double) (((float) SystemProperties.getInt("camera_stable_threshold", 9)) / 10.0f));
    private final Sensor mAccelerometerSensor;
    private SensorEventListener mAccelerometerSensorEventListenerImpl = new C00913();
    private int mAccelerometerTag = 0;
    private long mAccelerometerTimeStamp = 0;
    private double[] mAngleSpeed = new double[]{GYROSCOPE_STABLE_THRESHOLD, GYROSCOPE_STABLE_THRESHOLD, GYROSCOPE_STABLE_THRESHOLD, GYROSCOPE_STABLE_THRESHOLD, GYROSCOPE_STABLE_THRESHOLD};
    private int mAngleSpeedIndex = -1;
    private double mAngleTotal = 0.0d;
    private int mCapturePosture = 0;
    private boolean mDeviceStable;
    private boolean mEdgeTouchEnabled;
    private boolean mFocusSensorEnabled;
    private boolean mGradienterEnabled;
    private final Sensor mGyroscope;
    private SensorEventListener mGyroscopeListener = new C00891();
    private long mGyroscopeTimeStamp = 0;
    private Handler mHandler;
    private boolean mIsLying = false;
    private SensorEventListener mLinearAccelerationListener = new C00902();
    private final Sensor mLinearAccelerometer;
    private float mOrientation = -1.0f;
    private final Sensor mOrientationSensor;
    private SensorEventListener mOrientationSensorEventListener;
    private int mRate;
    private boolean mRotationFlagEnabled;
    private HandlerThread mSensorListenerThread;
    private final SensorManager mSensorManager;
    private int mSensorRegister;
    private SensorStateListener mSensorStateListener;
    private Handler mThreadHandler;

    class C00891 implements SensorEventListener {
        C00891() {
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            long abs = Math.abs(sensorEvent.timestamp - SensorStateManager.this.mGyroscopeTimeStamp);
            if (SensorStateManager.this.mSensorStateListener != null && SensorStateManager.this.mSensorStateListener.isWorking() && abs >= 100000000) {
                if (SensorStateManager.this.mGyroscopeTimeStamp == 0 || abs > 1000000000) {
                    SensorStateManager.this.mGyroscopeTimeStamp = sensorEvent.timestamp;
                    return;
                }
                float f = ((float) abs) * 1.0E-9f;
                double sqrt = Math.sqrt((double) (((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1])) + (sensorEvent.values[2] * sensorEvent.values[2])));
                SensorStateManager.this.mGyroscopeTimeStamp = sensorEvent.timestamp;
                if (SensorStateManager.GYROSCOPE_MOVING_THRESHOLD < sqrt) {
                    SensorStateManager.this.deviceBeginMoving();
                }
                SensorStateManager sensorStateManager = SensorStateManager.this;
                SensorStateManager sensorStateManager2 = SensorStateManager.this;
                sensorStateManager.mAngleSpeedIndex = sensorStateManager2.mAngleSpeedIndex = sensorStateManager2.mAngleSpeedIndex + 1 % SensorStateManager.this.mAngleSpeed.length;
                SensorStateManager.this.mAngleSpeed[SensorStateManager.this.mAngleSpeedIndex] = sqrt;
                if (sqrt >= 0.05000000074505806d) {
                    sensorStateManager = SensorStateManager.this;
                    sensorStateManager.mAngleTotal = sensorStateManager.mAngleTotal + (((double) f) * sqrt);
                    if (SensorStateManager.this.mAngleTotal > 0.5235987755982988d) {
                        SensorStateManager.this.mAngleTotal = 0.0d;
                        SensorStateManager.this.deviceKeepMoving(10000.0d);
                    }
                }
            }
        }
    }

    class C00902 implements SensorEventListener {
        C00902() {
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            long abs = Math.abs(sensorEvent.timestamp - SensorStateManager.this.mAccelerometerTimeStamp);
            if (SensorStateManager.this.mSensorStateListener != null && SensorStateManager.this.mSensorStateListener.isWorking() && abs >= 100000000) {
                if (SensorStateManager.this.mAccelerometerTimeStamp == 0 || abs > 1000000000) {
                    SensorStateManager.this.mAccelerometerTimeStamp = sensorEvent.timestamp;
                    return;
                }
                double sqrt = Math.sqrt((double) (((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1])) + (sensorEvent.values[2] * sensorEvent.values[2])));
                SensorStateManager.this.mAccelerometerTimeStamp = sensorEvent.timestamp;
                if (sqrt > 1.0d) {
                    SensorStateManager.this.deviceKeepMoving(sqrt);
                }
            }
        }
    }

    class C00913 implements SensorEventListener {
        private float[] finalFilter = new float[3];
        private float[] firstFilter = new float[3];

        C00913() {
        }

        private void clearFilter() {
            for (int i = 0; i < this.firstFilter.length; i++) {
                this.firstFilter[i] = 0.0f;
                this.finalFilter[i] = 0.0f;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.v("SensorStateManager", "onAccuracyChanged accuracy=" + i);
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (SensorStateManager.this.mSensorStateListener != null) {
                this.firstFilter[0] = (this.firstFilter[0] * 0.8f) + (sensorEvent.values[0] * 0.19999999f);
                this.firstFilter[1] = (this.firstFilter[1] * 0.8f) + (sensorEvent.values[1] * 0.19999999f);
                this.firstFilter[2] = (this.firstFilter[2] * 0.8f) + (sensorEvent.values[2] * 0.19999999f);
                this.finalFilter[0] = (this.finalFilter[0] * 0.7f) + (this.firstFilter[0] * 0.3f);
                this.finalFilter[1] = (this.finalFilter[1] * 0.7f) + (this.firstFilter[1] * 0.3f);
                this.finalFilter[2] = (this.finalFilter[2] * 0.7f) + (this.firstFilter[2] * 0.3f);
                Log.m5v("SensorStateManager", "finalFilter=" + this.finalFilter[0] + " " + this.finalFilter[1] + " " + this.finalFilter[2] + " event.values=" + sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2]);
                float f = -1.0f;
                float f2 = -this.finalFilter[0];
                float f3 = -this.finalFilter[1];
                float f4 = -this.finalFilter[2];
                if (4.0f * ((f2 * f2) + (f3 * f3)) >= f4 * f4) {
                    f = SensorStateManager.this.normalizeDegree(90.0f - (((float) Math.atan2((double) (-f3), (double) f2)) * 57.29578f));
                }
                if (f != SensorStateManager.this.mOrientation) {
                    if (Math.abs(SensorStateManager.this.mOrientation - f) > 3.0f) {
                        clearFilter();
                    }
                    SensorStateManager.this.mOrientation = f;
                    Log.m5v("SensorStateManager", "SensorEventListenerImpl TYPE_ACCELEROMETER mOrientation=" + SensorStateManager.this.mOrientation + " mIsLying=" + SensorStateManager.this.mIsLying);
                    SensorStateManager.this.mSensorStateListener.onDeviceOrientationChanged(SensorStateManager.this.mOrientation, SensorStateManager.this.mIsLying);
                }
            }
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    SensorStateManager.this.deviceBecomeStable();
                    return;
                case 2:
                    SensorStateManager sensorStateManager = SensorStateManager.this;
                    int i = message.arg1;
                    if (message.arg2 != 1) {
                        z = false;
                    }
                    sensorStateManager.update(i, z);
                    return;
                default:
                    return;
            }
        }
    }

    class OrientationSensorEventListenerImpl implements SensorEventListener {
        OrientationSensorEventListenerImpl() {
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.v("SensorStateManager", "onAccuracyChanged accuracy=" + i);
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (SensorStateManager.this.mSensorStateListener != null) {
                boolean z;
                float f = -1.0f;
                float f2 = sensorEvent.values[1];
                float f3 = sensorEvent.values[2];
                float abs = Math.abs(f2);
                float abs2 = Math.abs(f3);
                int i = SensorStateManager.this.mIsLying ? 5 : 0;
                int i2 = i + 26;
                int i3 = 153 - i;
                if (abs <= ((float) i2) || abs >= ((float) i3)) {
                    boolean z2 = abs2 <= ((float) i2) || abs2 >= ((float) i3);
                    z = z2;
                } else {
                    z = false;
                }
                if (z && Math.abs(abs - abs2) > 1.0f) {
                    if (abs > abs2) {
                        f = (float) (f2 < 0.0f ? 0 : 180);
                    } else if (abs < abs2) {
                        f = (float) (f3 < 0.0f ? 90 : 270);
                    }
                }
                if (Math.abs(abs2 - 90.0f) < ((float) SensorStateManager.CAPTURE_POSTURE_DEGREE)) {
                    SensorStateManager.this.changeCapturePosture(f3 < 0.0f ? 1 : 2);
                } else {
                    SensorStateManager.this.changeCapturePosture(0);
                }
                if (z != SensorStateManager.this.mIsLying || (z && f != SensorStateManager.this.mOrientation)) {
                    SensorStateManager.this.mIsLying = z;
                    if (SensorStateManager.this.mIsLying) {
                        SensorStateManager.this.mOrientation = f;
                    }
                    Log.m5v("SensorStateManager", "SensorEventListenerImpl TYPE_ORIENTATION mOrientation=" + SensorStateManager.this.mOrientation + " mIsLying=" + SensorStateManager.this.mIsLying);
                    SensorStateManager.this.mSensorStateListener.onDeviceOrientationChanged(SensorStateManager.this.mOrientation, SensorStateManager.this.mIsLying);
                }
            }
        }
    }

    public interface SensorStateListener {
        boolean isWorking();

        void notifyDevicePostureChanged();

        void onDeviceBecomeStable();

        void onDeviceBeginMoving();

        void onDeviceKeepMoving(double d);

        void onDeviceOrientationChanged(float f, boolean z);
    }

    public SensorStateManager(Context context, Looper looper) {
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mLinearAccelerometer = this.mSensorManager.getDefaultSensor(10);
        this.mGyroscope = this.mSensorManager.getDefaultSensor(4);
        this.mOrientationSensor = this.mSensorManager.getDefaultSensor(3);
        this.mAccelerometerSensor = this.mSensorManager.getDefaultSensor(1);
        this.mHandler = new MainHandler(looper);
        this.mRate = 30000;
        if (canDetectOrientation()) {
            this.mOrientationSensorEventListener = new OrientationSensorEventListenerImpl();
        }
        this.mSensorListenerThread = new HandlerThread("SensorListenerThread");
        this.mSensorListenerThread.start();
    }

    private void changeCapturePosture(int i) {
        if (this.mCapturePosture != i) {
            this.mCapturePosture = i;
            if (this.mSensorStateListener != null) {
                this.mSensorStateListener.notifyDevicePostureChanged();
            }
        }
    }

    private void deviceBecomeStable() {
        if (this.mFocusSensorEnabled) {
            this.mSensorStateListener.onDeviceBecomeStable();
        }
    }

    private void deviceBeginMoving() {
        this.mSensorStateListener.onDeviceBeginMoving();
    }

    private void deviceKeepMoving(double d) {
        if (this.mFocusSensorEnabled) {
            this.mSensorStateListener.onDeviceKeepMoving(d);
        }
    }

    private int filterUnregistSensor(int i) {
        if (this.mEdgeTouchEnabled) {
            i = (i & -3) & -5;
        }
        if (this.mRotationFlagEnabled) {
            i &= -5;
        }
        if (this.mFocusSensorEnabled) {
            i = (i & -2) & -3;
        }
        return this.mGradienterEnabled ? (i & -9) & -5 : i;
    }

    private boolean isContains(int i, int i2) {
        return (i & i2) == i2;
    }

    private boolean isPartialContains(int i, int i2) {
        return (i & i2) != 0;
    }

    private float normalizeDegree(float f) {
        while (f >= 360.0f) {
            f -= 360.0f;
        }
        while (f < 0.0f) {
            f += 360.0f;
        }
        return f;
    }

    private void update(int i, boolean z) {
        if (!z && isPartialContains(this.mSensorRegister, i)) {
            unregister(i);
        } else if (z && !isContains(this.mSensorRegister, i)) {
            register(i);
        }
    }

    public boolean canDetectOrientation() {
        return this.mOrientationSensor != null;
    }

    public int getCapturePosture() {
        return this.mCapturePosture;
    }

    public boolean isDeviceLying() {
        return this.mIsLying;
    }

    public void onDestory() {
        this.mSensorListenerThread.quit();
    }

    public void register() {
        int i = 0;
        if (this.mFocusSensorEnabled) {
            i = 1 | 2;
        }
        if (this.mEdgeTouchEnabled) {
            i = (i | 2) | 4;
        }
        if (this.mGradienterEnabled) {
            i = (i | 8) | 4;
        }
        if (this.mRotationFlagEnabled) {
            i |= 4;
        }
        register(i);
    }

    public void register(int i) {
        if (!isContains(this.mSensorRegister, i)) {
            if (this.mThreadHandler == null && isPartialContains(i, 12)) {
                this.mThreadHandler = new Handler(this.mSensorListenerThread.getLooper());
            }
            if (this.mFocusSensorEnabled) {
                this.mDeviceStable = true;
                i = (i | 1) | 2;
                this.mHandler.removeMessages(2);
            }
            if (isContains(i, 2) && !isContains(this.mSensorRegister, 2)) {
                this.mSensorManager.registerListener(this.mGyroscopeListener, this.mGyroscope, 2);
                this.mSensorRegister |= 2;
            }
            if (isContains(i, 1) && !isContains(this.mSensorRegister, 1)) {
                this.mSensorManager.registerListener(this.mLinearAccelerationListener, this.mLinearAccelerometer, 2);
                this.mSensorRegister |= 1;
            }
            if (canDetectOrientation() && isContains(i, 4) && !isContains(this.mSensorRegister, 4)) {
                this.mSensorManager.registerListener(this.mOrientationSensorEventListener, this.mOrientationSensor, this.mRate, this.mThreadHandler);
                this.mSensorRegister |= 4;
            }
            if (isContains(i, 8) && !isContains(this.mSensorRegister, 8)) {
                this.mSensorManager.registerListener(this.mAccelerometerSensorEventListenerImpl, this.mAccelerometerSensor, this.mRate, this.mThreadHandler);
                this.mSensorRegister |= 8;
            }
        }
    }

    public void reset() {
        this.mHandler.removeMessages(1);
        this.mAngleTotal = 0.0d;
        this.mDeviceStable = true;
        this.mAccelerometerTag = 0;
    }

    public void setEdgeTouchEnabled(boolean z) {
        if (this.mEdgeTouchEnabled != z) {
            this.mEdgeTouchEnabled = z;
            int i = 6;
            if (!this.mEdgeTouchEnabled) {
                if (this.mGradienterEnabled) {
                    i = 2;
                }
                if (this.mFocusSensorEnabled) {
                    i &= -3;
                }
            }
            update(i, this.mEdgeTouchEnabled);
        }
    }

    public void setFocusSensorEnabled(boolean z) {
        if (this.mFocusSensorEnabled != z) {
            this.mFocusSensorEnabled = z;
            this.mHandler.removeMessages(2);
            int i = 3;
            if (!this.mFocusSensorEnabled) {
                i = filterUnregistSensor(3);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, i, z ? 1 : 0), 1000);
        }
    }

    public void setGradienterEnabled(boolean z) {
        if (this.mGradienterEnabled != z) {
            this.mGradienterEnabled = z;
            int i = 12;
            if (!this.mGradienterEnabled) {
                i = filterUnregistSensor(12);
            }
            update(i, this.mGradienterEnabled);
        }
    }

    public void setRotationIndicatorEnabled(boolean z) {
        if (Device.isOrientationIndicatorEnabled() && canDetectOrientation() && this.mRotationFlagEnabled != z) {
            this.mRotationFlagEnabled = z;
            int i = 4;
            if (!this.mRotationFlagEnabled) {
                i = filterUnregistSensor(4);
            }
            update(i, this.mRotationFlagEnabled);
        }
    }

    public void setSensorStateListener(SensorStateListener sensorStateListener) {
        this.mSensorStateListener = sensorStateListener;
    }

    public void unregister(int i) {
        if (this.mSensorRegister != 0) {
            if (!this.mFocusSensorEnabled || i == 15) {
                if (!this.mFocusSensorEnabled && this.mHandler.hasMessages(2)) {
                    i |= 1;
                    if (!this.mEdgeTouchEnabled) {
                        i |= 2;
                    }
                }
                reset();
                this.mHandler.removeMessages(2);
            }
            if (isContains(i, 2) && isContains(this.mSensorRegister, 2)) {
                this.mSensorManager.unregisterListener(this.mGyroscopeListener);
                this.mSensorRegister &= -3;
            }
            if (isContains(i, 1) && isContains(this.mSensorRegister, 1)) {
                this.mSensorManager.unregisterListener(this.mLinearAccelerationListener);
                this.mSensorRegister &= -2;
            }
            if (isContains(i, 4) && isContains(this.mSensorRegister, 4)) {
                this.mSensorManager.unregisterListener(this.mOrientationSensorEventListener);
                this.mSensorRegister &= -5;
                this.mIsLying = false;
                changeCapturePosture(0);
            }
            if (isContains(i, 8) && isContains(this.mSensorRegister, 8)) {
                this.mSensorManager.unregisterListener(this.mAccelerometerSensorEventListenerImpl);
                this.mSensorRegister &= -9;
            }
        }
    }
}

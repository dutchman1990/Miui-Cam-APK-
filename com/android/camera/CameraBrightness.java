package com.android.camera;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Spline;
import android.view.WindowManager.LayoutParams;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.internal.R.array;
import com.android.internal.R.bool;
import com.android.internal.R.integer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CameraBrightness {
    private static Spline mPositiveScreenManualBrightnessSpline;
    private static Spline mScreenManualBrightnessSpline;
    private int mBrightness;
    private AsyncTask<Void, Void, Integer> mCameraBrightnessTask;
    private Activity mCurrentActivity;
    private boolean mFirstFocusChanged = false;
    private boolean mPaused;
    private boolean mUseDefaultValue = true;

    private class CameraBrightnessTask extends AsyncTask<Void, Void, Integer> {
        private CameraBrightnessTask() {
        }

        private String execCommand(String str) {
            InterruptedException e;
            IOException e2;
            long currentTimeMillis = System.currentTimeMillis();
            String str2 = "";
            try {
                Process exec = Runtime.getRuntime().exec(str);
                if (exec.waitFor() != 0) {
                    Log.e("CameraBrightness", "exit value = " + exec.exitValue());
                    return str2;
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                BufferedReader bufferedReader2;
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    }
                    bufferedReader.close();
                    str2 = stringBuffer.toString();
                    Log.v("CameraBrightness", "execCommand lcd value=" + str2 + " cost=" + (System.currentTimeMillis() - currentTimeMillis));
                    bufferedReader2 = bufferedReader;
                } catch (InterruptedException e3) {
                    e = e3;
                    bufferedReader2 = bufferedReader;
                    Log.e("CameraBrightness", "execCommand InterruptedException");
                    e.printStackTrace();
                    return str2;
                } catch (IOException e4) {
                    e2 = e4;
                    bufferedReader2 = bufferedReader;
                    Log.e("CameraBrightness", "execCommand IOException");
                    e2.printStackTrace();
                    return str2;
                }
                return str2;
            } catch (InterruptedException e5) {
                e = e5;
                Log.e("CameraBrightness", "execCommand InterruptedException");
                e.printStackTrace();
                return str2;
            } catch (IOException e6) {
                e2 = e6;
                Log.e("CameraBrightness", "execCommand IOException");
                e2.printStackTrace();
                return str2;
            }
        }

        private int getCurrentBackLight() {
            Object obj = null;
            int i = 0;
            while (true) {
                if (("0".equals(obj) || obj == null) && i < 3 && !isCancelled()) {
                    obj = execCommand("cat sys/class/leds/lcd-backlight/brightness");
                    if ("0".equals(obj) || obj == null) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            Log.e("CameraBrightness", e.getMessage());
                        }
                        i++;
                    }
                }
            }
            Log.v("CameraBrightness", "getCurrentBackLight currentSetting=" + obj);
            if (TextUtils.isEmpty(obj)) {
                return -1;
            }
            int -wrap0 = CameraBrightness.this.getAndroidIntResource("config_backlightBits");
            int parseFloat = (int) Float.parseFloat(obj);
            if (-wrap0 > 8) {
                parseFloat >>= -wrap0 - 8;
                Log.v("CameraBrightness", "getCurrentBackLight convert to " + parseFloat);
            }
            return parseFloat;
        }

        private void updateBrightness(int i) {
            if (i != -1 || CameraBrightness.this.mUseDefaultValue || CameraBrightness.this.mPaused) {
                LayoutParams attributes = CameraBrightness.this.mCurrentActivity.getWindow().getAttributes();
                if (CameraBrightness.this.mUseDefaultValue || CameraBrightness.this.mPaused) {
                    attributes.screenBrightness = -1.0f;
                } else {
                    attributes.screenBrightness = ((float) i) / 255.0f;
                }
                Log.v("CameraBrightness", "updateBrightness setting=" + i + " useDefaultValue=" + CameraBrightness.this.mUseDefaultValue + " screenBrightness=" + attributes.screenBrightness);
                CameraBrightness.this.mCurrentActivity.getWindow().setAttributes(attributes);
                CameraBrightness.this.mBrightness = i;
            }
        }

        protected Integer doInBackground(Void... voidArr) {
            Log.v("CameraBrightness", "doInBackground useDefaultValue=" + CameraBrightness.this.mUseDefaultValue + " paused=" + CameraBrightness.this.mPaused);
            if (CameraBrightness.this.mUseDefaultValue || CameraBrightness.this.mPaused) {
                return Integer.valueOf(-1);
            }
            int currentBackLight = getCurrentBackLight();
            if (currentBackLight <= 0) {
                return null;
            }
            CameraBrightness.this.createSpline();
            LayoutParams attributes = CameraBrightness.this.mCurrentActivity.getWindow().getAttributes();
            if (attributes.screenBrightness > 0.0f) {
                float f = attributes.screenBrightness * 255.0f;
                if (Math.abs((CameraBrightness.mPositiveScreenManualBrightnessSpline != null ? Math.round(CameraBrightness.mPositiveScreenManualBrightnessSpline.interpolate(f)) : Math.round(f)) - currentBackLight) <= 1) {
                    Log.v("CameraBrightness", "doInBackground brightness unchanged");
                    return null;
                }
            }
            int i = currentBackLight;
            if (CameraBrightness.mScreenManualBrightnessSpline != null) {
                i = (int) CameraBrightness.mScreenManualBrightnessSpline.interpolate((float) currentBackLight);
            }
            return Integer.valueOf(Util.clamp((int) (((double) i) * (((double) (0.1f + ((((float) Util.clamp(i, 0, 185)) / 185.0f) * 0.3f))) + 1.0d)), 0, 255));
        }

        protected void onPostExecute(Integer num) {
            if (!isCancelled() && num != null) {
                updateBrightness(num.intValue());
            }
        }
    }

    public CameraBrightness(Activity activity) {
        this.mCurrentActivity = activity;
    }

    private void adjustBrightness() {
        if (Device.adjustScreenLight() && this.mCurrentActivity != null) {
            cancelLastTask();
            this.mCameraBrightnessTask = new CameraBrightnessTask().execute(new Void[0]);
        }
    }

    private void cancelLastTask() {
        if (this.mCameraBrightnessTask != null) {
            this.mCameraBrightnessTask.cancel(true);
        }
    }

    private static Spline createManualBrightnessSpline(int[] iArr, int[] iArr2) {
        try {
            int length = iArr.length;
            float[] fArr = new float[length];
            float[] fArr2 = new float[length];
            for (int i = 0; i < length; i++) {
                fArr[i] = (float) iArr[i];
                fArr2[i] = (float) iArr2[i];
            }
            return Spline.createMonotoneCubicSpline(fArr, fArr2);
        } catch (Throwable e) {
            Log.e("CameraBrightness", "Could not create manual-brightness spline.", e);
            return null;
        }
    }

    private void createSpline() {
        if ((mScreenManualBrightnessSpline == null || mPositiveScreenManualBrightnessSpline == null) && getAndroidBoolRes("config_manual_spline_available", true)) {
            int[] androidArrayRes = getAndroidArrayRes("config_manualBrightnessRemapIn");
            int[] androidArrayRes2 = getAndroidArrayRes("config_manualBrightnessRemapOut");
            mScreenManualBrightnessSpline = createManualBrightnessSpline(androidArrayRes2, androidArrayRes);
            mPositiveScreenManualBrightnessSpline = createManualBrightnessSpline(androidArrayRes, androidArrayRes2);
            if (mScreenManualBrightnessSpline == null || mPositiveScreenManualBrightnessSpline == null) {
                Log.e("CameraBrightness", "Error to create manual brightness spline");
            }
        }
    }

    private int[] getAndroidArrayRes(String str) {
        int fieldInt = ReflectUtil.getFieldInt(array.class, null, str, 0);
        return fieldInt != 0 ? CameraAppImpl.getAndroidContext().getResources().getIntArray(fieldInt) : new int[]{0, 255};
    }

    private boolean getAndroidBoolRes(String str, boolean z) {
        int fieldInt = ReflectUtil.getFieldInt(bool.class, null, str, 0);
        return fieldInt == 0 ? z : CameraAppImpl.getAndroidContext().getResources().getBoolean(fieldInt);
    }

    private int getAndroidIntResource(String str) {
        int fieldInt = ReflectUtil.getFieldInt(integer.class, null, str, 0);
        return fieldInt == 0 ? 0 : CameraAppImpl.getAndroidContext().getResources().getInteger(fieldInt);
    }

    public int getCurrentBrightness() {
        return this.mBrightness;
    }

    public void onPause() {
        this.mFirstFocusChanged = false;
        this.mPaused = true;
        cancelLastTask();
    }

    public void onResume() {
        this.mUseDefaultValue = this.mCurrentActivity instanceof BasePreferenceActivity;
        this.mPaused = false;
        Log.v("CameraBrightness", "onResume adjustBrightness");
        adjustBrightness();
    }

    public void onWindowFocusChanged(boolean z) {
        boolean z2 = true;
        Log.v("CameraBrightness", "onWindowFocusChanged hasFocus=" + z + " mFirstFocusChanged=" + this.mFirstFocusChanged);
        if (!this.mFirstFocusChanged && z) {
            this.mFirstFocusChanged = true;
        } else if (!this.mPaused) {
            if (!(this.mCurrentActivity instanceof BasePreferenceActivity) && z) {
                z2 = false;
            }
            this.mUseDefaultValue = z2;
            adjustBrightness();
        }
    }
}

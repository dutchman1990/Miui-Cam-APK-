package com.android.camera;

import android.media.MediaRecorder;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.File;

public class Clapper {
    public static final int AMPLITUDE_ABSOLUTE_THRESHOLD = (SCALE_FACTOR * 5000);
    public static final int AMPLITUDE_INIT = (SCALE_FACTOR * 2000);
    private static final int DEFAULT_AMPLITUDE_DIFF = (SCALE_FACTOR * 2000);
    public static final int SCALE_FACTOR = getScaleFactor();
    private boolean mContinueRecording;
    private ClapperListener mListener;
    private MediaRecorder mRecorder;

    public interface ClapperListener {
        void heard(float f);

        void releaseShutter();
    }

    class C00831 implements Runnable {
        C00831() {
        }

        public void run() {
            Clapper.this.threadRecordClap();
        }
    }

    public Clapper(ClapperListener clapperListener) {
        this.mListener = clapperListener;
    }

    public static int getScaleFactor() {
        return (Device.IS_MI2A || Device.IS_C3A || Device.IS_PAD1) ? 1 : (Device.IS_MI4 || Device.IS_X5 || Device.IS_A9 || (Device.IS_H2XLTE && 21 <= VERSION.SDK_INT)) ? 6 : 3;
    }

    private boolean startRecorder() {
        this.mRecorder = new MediaRecorder();
        try {
            this.mRecorder.setAudioSource(1);
            this.mRecorder.setOutputFormat(1);
            this.mRecorder.setAudioEncoder(1);
            this.mRecorder.setOutputFile(CameraAppImpl.getAndroidContext().getFilesDir() + File.separator + "camera_claaper_recorder.3gp");
            this.mRecorder.prepare();
            this.mRecorder.start();
            return true;
        } catch (Exception e) {
            Log.e("Clapper", "Failed to start media recorder. Maybe it is used by other app.");
            e.printStackTrace();
            return false;
        }
    }

    private void threadRecordClap() {
        int i = AMPLITUDE_INIT;
        int i2 = 3;
        do {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e("Clapper", "Thread.sleep() interrupted");
            }
            int maxAmplitude = this.mRecorder.getMaxAmplitude();
            i = maxAmplitude > i ? (int) ((((double) i) * 0.9d) + (((double) maxAmplitude) * 0.09999999999999998d)) : (int) ((((double) i) * 0.8d) + (((double) maxAmplitude) * 0.19999999999999996d));
            if (i2 > 0) {
                i2--;
            } else {
                int i3 = maxAmplitude - i;
                int i4 = i > AMPLITUDE_INIT ? (int) (((double) DEFAULT_AMPLITUDE_DIFF) * (((((double) i) * 0.5d) / ((double) AMPLITUDE_INIT)) + 0.5d)) : DEFAULT_AMPLITUDE_DIFF;
                if (this.mListener != null) {
                    if (maxAmplitude > AMPLITUDE_ABSOLUTE_THRESHOLD || i3 >= i4) {
                        this.mListener.heard(1.0f);
                        this.mListener.releaseShutter();
                    } else {
                        this.mListener.heard(Math.max(Math.abs(((float) maxAmplitude) / ((float) AMPLITUDE_ABSOLUTE_THRESHOLD)), Math.abs(((float) i3) / ((float) i4))));
                    }
                }
            }
        } while (this.mContinueRecording);
        stopRecorder();
    }

    public boolean start() {
        boolean startRecorder = startRecorder();
        if (startRecorder) {
            this.mContinueRecording = true;
            new Thread(new C00831()).start();
        }
        return startRecorder;
    }

    public void stop() {
        this.mContinueRecording = false;
    }

    public void stopRecorder() {
        if (this.mRecorder != null) {
            try {
                this.mContinueRecording = false;
                this.mRecorder.stop();
                this.mRecorder.release();
                this.mRecorder = null;
            } catch (Exception e) {
                Log.e("Clapper", "Failed to stop media recorder.");
                e.printStackTrace();
            }
        }
    }
}

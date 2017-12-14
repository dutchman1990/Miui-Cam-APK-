package com.android.camera;

import android.support.v7.recyclerview.C0049R;
import android.widget.Toast;
import com.android.camera.AudioCapture.Callback;
import com.android.camera.module.CameraModule;
import com.android.camera.ui.V6ShutterButton;

public class AudioCaptureManager implements Callback {
    private ActivityBase mActivity;
    private AudioCapture mAudioCapture;
    private CameraModule mCameraModule;

    public AudioCaptureManager(CameraModule cameraModule, ActivityBase activityBase) {
        this.mCameraModule = cameraModule;
        this.mActivity = activityBase;
        if (this.mCameraModule == null) {
            throw new IllegalArgumentException("CameraModule == null");
        }
        this.mAudioCapture = new AudioCapture(this);
    }

    public void close() {
        if (this.mAudioCapture.isRunning()) {
            this.mAudioCapture.pause();
            this.mCameraModule.tryRemoveCountDownMessage();
            this.mCameraModule.getUIController().getShutterButton().setImageResource(C0049R.drawable.camera_shutter_button_bg);
            getShutterButton().setAudioProgress(-1.0f);
        }
    }

    public V6ShutterButton getShutterButton() {
        return this.mCameraModule.getUIController().getShutterButton();
    }

    public void hideDelayNumber() {
        this.mCameraModule.getUIController().getPreviewPanel().hideDelayNumber();
    }

    public boolean isRunning() {
        return this.mAudioCapture.isRunning();
    }

    public boolean onBackPressed() {
        if (!this.mAudioCapture.isRunning()) {
            return false;
        }
        close();
        return true;
    }

    public void onClick() {
        if (this.mAudioCapture.isRunning()) {
            close();
        } else {
            open();
        }
        this.mActivity.getUIController().getSettingsStatusBar().updateStatus();
    }

    public void onPause() {
        if (this.mAudioCapture.isRunning()) {
            close();
        }
    }

    public void onResume() {
        if (CameraSettings.isAudioCaptureOpen()) {
            open();
        }
    }

    public void open() {
        if (this.mAudioCapture.start()) {
            this.mCameraModule.tryRemoveCountDownMessage();
            this.mActivity.loadCameraSound(5);
            this.mCameraModule.getUIController().getShutterButton().setImageResource(C0049R.drawable.audio_shutter_button_bg);
            return;
        }
        this.mCameraModule.getUIController().getShutterButton().setImageResource(C0049R.drawable.camera_shutter_button_bg);
        Toast.makeText(this.mActivity, C0049R.string.error_fail_to_start_audio_capture, 0).show();
    }

    public boolean readyToAudioCapture() {
        return this.mCameraModule.readyToAudioCapture();
    }

    public void releaseShutter() {
        this.mCameraModule.sendDelayedCaptureMessage(700, 3);
    }

    public void setDelayStep(int i) {
        if (i >= 0) {
            this.mCameraModule.getUIController().getPreviewPanel().showDelayNumber(String.valueOf(i + 1));
        }
    }
}

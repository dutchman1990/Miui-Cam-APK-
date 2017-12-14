package com.android.camera.camera_adapter;

import com.android.camera.CameraSettings;
import com.android.camera.module.VideoModule;

public class VideoLC extends VideoModule {
    protected void updateVideoParametersPreference() {
        super.updateVideoParametersPreference();
        int[] maxPreviewFpsRange = CameraSettings.getMaxPreviewFpsRange(this.mParameters);
        if (maxPreviewFpsRange != null) {
            this.mParameters.setPreviewFpsRange(maxPreviewFpsRange[1], maxPreviewFpsRange[1]);
        }
    }
}

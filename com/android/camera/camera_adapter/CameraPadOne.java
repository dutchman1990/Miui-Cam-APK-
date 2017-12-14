package com.android.camera.camera_adapter;

import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import com.android.camera.Camera;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.effect.EffectController;
import com.android.camera.module.CameraModule;
import java.util.ArrayList;
import java.util.List;

public class CameraPadOne extends CameraModule {
    private final String KEY_AUTO_ROTATE = "jpeg-auto-rotate";
    private final String KEY_FLIP = "jpeg-flip";
    private final String TAG = "Camera";

    public static List<String> getLayoutModeKeys(Camera camera, boolean z, boolean z2) {
        List<String> arrayList = new ArrayList();
        if (z) {
            arrayList.add("pref_camera_face_beauty_key");
        } else {
            arrayList.add("pref_camera_face_beauty_key");
        }
        return arrayList;
    }

    private void updateCameraParametersPreference(Parameters parameters) {
        parameters.set("jpeg-auto-rotate", "true");
        if ((EffectController.getInstance().hasEffect() ? Device.isEffectWatermarkFilted() : false) || !CameraSettings.isTimeWaterMarkOpen(this.mPreferences)) {
            sProxy.setTimeWatermark(parameters, "off");
        } else {
            sProxy.setTimeWatermark(parameters, "on");
        }
        Log.i("Camera", "SetTimeWatermark =" + sProxy.getTimeWatermark(parameters));
        String string = this.mPreferences.getString("pref_camera_face_beauty_key", getString(C0049R.string.pref_face_beauty_default));
        sProxy.setStillBeautify(parameters, string);
        Log.i("Camera", "SetStillBeautify =" + string);
        String string2 = this.mPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.pref_camera_show_gender_age_default));
        getUIController().getFaceView().setShowGenderAndAge(string2);
        Log.i("Camera", "SetShowGenderAndAge =" + string2);
        sProxy.setMultiFaceBeautify(parameters, "on");
        Log.i("Camera", "SetMultiFaceBeautify =on");
        if (isFrontMirror()) {
            parameters.set("jpeg-flip", "true");
        } else {
            parameters.set("jpeg-flip", "false");
        }
        Log.i("Camera", "Set JPEG horizontal flip = " + parameters.get("jpeg-flip"));
    }

    protected boolean isZeroShotMode() {
        return true;
    }

    protected void updateCameraParametersPreference() {
        super.updateCameraParametersPreference();
        updateCameraParametersPreference(this.mParameters);
    }
}

package com.android.camera;

import com.android.camera.camera_adapter.CameraMTK.FBLevel;
import com.android.camera.camera_adapter.CameraMTK.FBParams;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;

public class MtkFBParamsUtil {
    private static final StringBuilder ADJUSTMENTS = new StringBuilder();
    private static final int[] BASE_VALUES = new int[]{-10, -11, -7, -12, -8, -8, -2, -12, -4, 0, 0, -9};

    static {
        ADJUSTMENTS.append("1200,1200,1201,1201,1200,1210,").append("2301,2411,2412,2412,2311,2421,").append("3411,3522,3623,3623,3512,3622,").append("1200,1211,1311,1311,1211,1311,").append("2301,2512,2522,2522,2412,2522,").append("3511,3723,3734,3734,3623,3733,");
    }

    private static void adjustValue(FBParams fBParams, FBLevel fBLevel, CameraHardwareFace cameraHardwareFace) {
        int i = 0;
        if (fBParams != null && cameraHardwareFace != null) {
            int genderIndex = getGenderIndex(cameraHardwareFace.gender);
            if (genderIndex != 2) {
                float f = genderIndex == 0 ? cameraHardwareFace.ageMale : cameraHardwareFace.ageFemale;
                if (genderIndex != 0) {
                    i = 1;
                }
                int ordinal = (((i * 5) * 6) * 3) + ((fBLevel.ordinal() * 5) * 6);
                int i2 = ordinal + 10;
                ordinal += getAgeIndex(f) * 5;
                int i3 = ordinal + 1;
                int i4 = i2 + 1;
                fBParams.skinColor = trimValue((ADJUSTMENTS.charAt(ordinal) - ADJUSTMENTS.charAt(i2)) + fBParams.skinColor);
                ordinal = i3 + 1;
                i2 = i4 + 1;
                fBParams.smoothLevel = trimValue((ADJUSTMENTS.charAt(i3) - ADJUSTMENTS.charAt(i4)) + fBParams.smoothLevel);
                i3 = ordinal + 1;
                i4 = i2 + 1;
                fBParams.slimFace = trimValue((ADJUSTMENTS.charAt(ordinal) - ADJUSTMENTS.charAt(i2)) + fBParams.slimFace);
                ordinal = i3 + 1;
                i2 = i4 + 1;
                fBParams.enlargeEye = trimValue((ADJUSTMENTS.charAt(i3) - ADJUSTMENTS.charAt(i4)) + fBParams.enlargeEye);
            }
        }
    }

    public static void getAdvancedValue(FBParams fBParams) {
        fBParams.skinColor = Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_color_key"));
        fBParams.smoothLevel = Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_skin_smooth_key"));
        fBParams.slimFace = Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_slim_face_key"));
        fBParams.enlargeEye = Integer.parseInt(CameraSettings.getBeautifyDetailValue("pref_skin_beautify_enlarge_eye_key"));
    }

    private static int getAgeIndex(float f) {
        return f <= 7.0f ? 0 : f <= 17.0f ? 1 : f <= 30.0f ? 2 : f <= 44.0f ? 3 : f <= 60.0f ? 4 : 5;
    }

    private static void getBaseValue(FBParams fBParams, FBLevel fBLevel) {
        if (fBParams != null) {
            int ordinal = fBLevel.ordinal() * 4;
            fBParams.skinColor = BASE_VALUES[ordinal];
            fBParams.smoothLevel = BASE_VALUES[ordinal + 1];
            fBParams.slimFace = BASE_VALUES[ordinal + 2];
            fBParams.enlargeEye = BASE_VALUES[ordinal + 3];
        }
    }

    private static int getGenderIndex(float f) {
        return f < 0.4f ? 1 : f > 0.6f ? 0 : 2;
    }

    public static void getIntelligentValue(FBParams fBParams, FBLevel fBLevel, CameraHardwareFace cameraHardwareFace) {
        getBaseValue(fBParams, fBLevel);
        adjustValue(fBParams, fBLevel, cameraHardwareFace);
    }

    private static int trimValue(int i) {
        return i < -12 ? -12 : i > 12 ? 12 : i;
    }
}

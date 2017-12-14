package com.android.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.input.InputManager;
import android.media.CamcorderProfile;
import android.provider.Settings.System;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.camera.effect.EffectController;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.hardware.QcomCameraProxy;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.ListPreference;
import com.android.camera.ui.V6ModulePicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraSettings {
    public static final int BOTTOM_CONTROL_HEIGHT = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.bottom_control_height);
    private static final int MMS_VIDEO_DURATION = (CamcorderProfile.get(0) != null ? CamcorderProfile.get(0).duration : 30);
    public static final int PREVIEW_FRAME_TOP_MARGIN = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.preview_frame_top_margin);
    public static final int SURFACE_LEFT_MARGIN_MDP_QUALITY_480P = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.v6_surface_view_left_margin_mdp_render_quality_480p);
    public static final int SURFACE_LEFT_MARGIN_MDP_QUALITY_LOW = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.v6_surface_view_left_margin_mdp_render_quality_low);
    public static final int TOP_CONTROL_HEIGHT = CameraAppImpl.getAndroidContext().getResources().getDimensionPixelSize(C0049R.dimen.bottom_control_upper_panel_height);
    public static final ChangeManager sCameraChangeManager = new ChangeManager();
    public static boolean sCroppedIfNeeded = false;
    private static boolean sEdgePhotoEnable = false;
    public static ArrayList<String> sRemindMode = new ArrayList();
    private static HashMap<String, String> sSceneToFlash = new HashMap(11);

    static {
        sSceneToFlash.put("auto", null);
        sSceneToFlash.put("portrait", null);
        sSceneToFlash.put("landscape", "off");
        sSceneToFlash.put("sports", null);
        sSceneToFlash.put("night", "off");
        sSceneToFlash.put("night-portrait", "on");
        sSceneToFlash.put("beach", "off");
        sSceneToFlash.put("snow", "off");
        sSceneToFlash.put("sunset", "off");
        sSceneToFlash.put("fireworks", "off");
        sSceneToFlash.put("backlight", "off");
        sSceneToFlash.put("flowers", "off");
        sRemindMode.add("pref_camera_mode_settings_key");
        sRemindMode.add("pref_camera_magic_mirror_key");
        if (Device.isSupportGroupShot()) {
            sRemindMode.add("pref_camera_groupshot_mode_key");
        }
    }

    public static void cancelFrontCameraFirstUseHint(CameraSettingPreferences cameraSettingPreferences) {
        Editor edit = cameraSettingPreferences.edit();
        edit.putBoolean("pref_front_camera_first_use_hint_shown_key", false);
        edit.apply();
    }

    public static void cancelRemind(String str) {
        if (isNeedRemind(str)) {
            Editor edit = CameraSettingPreferences.instance().edit();
            edit.putBoolean(str + "_remind", false);
            edit.apply();
        }
    }

    private static void changePreviewFrameLayoutParams(ActivityBase activityBase, int i) {
        RelativeLayout previewFrame = activityBase.getUIController().getPreviewFrame();
        LayoutParams layoutParams = (LayoutParams) previewFrame.getLayoutParams();
        if (i == 0) {
            layoutParams.setMargins(0, PREVIEW_FRAME_TOP_MARGIN, 0, 0);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }
        previewFrame.requestLayout();
    }

    private static void changePreviewPanelLayoutParams(ActivityBase activityBase, int i) {
        RelativeLayout previewPanel = activityBase.getUIController().getPreviewPanel();
        LayoutParams layoutParams = (LayoutParams) previewPanel.getLayoutParams();
        if (i == 0) {
            layoutParams.setMargins(0, 0, 0, BOTTOM_CONTROL_HEIGHT);
            activityBase.getUIController().getBottomControlPanel().setBackgroundVisible(false);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
            activityBase.getUIController().getBottomControlPanel().setBackgroundVisible(true);
        }
        previewPanel.requestLayout();
    }

    private static void changeSettingStatusBarLayoutParams(ActivityBase activityBase, int i) {
        RelativeLayout settingsStatusBar = activityBase.getUIController().getSettingsStatusBar();
        LayoutParams layoutParams = (LayoutParams) settingsStatusBar.getLayoutParams();
        if (i == 0) {
            layoutParams.setMargins(0, 0, 0, BOTTOM_CONTROL_HEIGHT);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }
        settingsStatusBar.requestLayout();
    }

    private static void changeSurfaceViewFrameLayoutParams(ActivityBase activityBase, int i, int i2, int i3) {
        if (Device.isMDPRender() && V6ModulePicker.isVideoModule()) {
            FrameLayout surfaceViewFrame = activityBase.getUIController().getSurfaceViewFrame();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) surfaceViewFrame.getLayoutParams();
            if (i == 0) {
                int i4 = (i2 <= 0 || i3 <= 0 || isAspectRatio4_3(i2, i3)) ? 0 : SURFACE_LEFT_MARGIN_MDP_QUALITY_LOW;
                layoutParams.setMargins(i4, 0, i4, BOTTOM_CONTROL_HEIGHT);
            } else if (i == 2) {
                layoutParams.setMargins(SURFACE_LEFT_MARGIN_MDP_QUALITY_480P, 0, SURFACE_LEFT_MARGIN_MDP_QUALITY_480P, 0);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            surfaceViewFrame.requestLayout();
        }
    }

    public static void changeUIByPreviewSize(ActivityBase activityBase, int i) {
        changeUIByPreviewSize(activityBase, i, -1, -1);
    }

    public static void changeUIByPreviewSize(ActivityBase activityBase, int i, int i2, int i3) {
        if (!Device.isPad()) {
            changeSettingStatusBarLayoutParams(activityBase, i);
            changePreviewPanelLayoutParams(activityBase, i);
            changePreviewFrameLayoutParams(activityBase, i);
            changeSurfaceViewFrameLayoutParams(activityBase, i, i2, i3);
        }
    }

    private static void filterPreference(Map<String, ?> map, String str, Editor editor, int i) {
        if (editor != null && !TextUtils.isEmpty(str) && i != 0) {
            Object obj = map.get(str);
            if (obj != null && !isStringValueContained(obj, i)) {
                editor.remove(str);
            }
        }
    }

    public static int get4kProfile() {
        return !Device.isSupportedVideoQuality4kUHD() ? -1 : Integer.parseInt(getString(C0049R.string.pref_video_quality_entry_value_4kuhd));
    }

    public static int getAspectRatio(int i, int i2) {
        return isNearRatio16_9(i, i2) ? 1 : 0;
    }

    public static String getBeautifyDetailValue(String str) {
        int i = 0;
        if ("pref_skin_beautify_skin_color_key".equals(str)) {
            i = C0049R.string.pref_skin_beautify_color_default;
        } else if ("pref_skin_beautify_slim_face_key".equals(str)) {
            i = C0049R.string.pref_skin_beautify_slim_default;
        } else if ("pref_skin_beautify_skin_smooth_key".equals(str)) {
            i = C0049R.string.pref_skin_beautify_smooth_default;
        } else if ("pref_skin_beautify_enlarge_eye_key".equals(str)) {
            i = C0049R.string.pref_skin_beautify_eye_default;
        }
        String str2 = "0";
        if (i == 0 || !Device.isSupportedSkinBeautify()) {
            return str2;
        }
        if (!isFrontCamera() && !isSwitchOn("pref_camera_face_beauty_mode_key")) {
            return str2;
        }
        return CameraSettingPreferences.instance().getString(str, getString(i));
    }

    public static int getCameraId() {
        return Integer.parseInt(CameraSettingPreferences.instance().getString("pref_camera_id_key", String.valueOf(CameraHolder.instance().getBackCameraId())));
    }

    public static String getCameraZoomMode() {
        return CameraSettingPreferences.instance().getString("pref_camera_zoom_mode_key", getString(C0049R.string.pref_camera_zoom_mode_default));
    }

    public static int getCountDownTimes() {
        return Integer.parseInt(CameraSettingPreferences.instance().getString("pref_delay_capture_key", getString(C0049R.string.pref_camera_delay_capture_default)));
    }

    public static int getDefaultPreferenceId(int i) {
        switch (i) {
            case C0049R.bool.pref_camera_auto_chroma_flash_default:
                if (Device.IS_X5 || Device.IS_X7) {
                    return C0049R.bool.pref_camera_auto_chroma_flash_virgo_default;
                }
            case C0049R.string.pref_video_quality_default:
                if (CameraSettingPreferences.instance().isFrontCamera() && Device.isFrontVideoQualityShouldBe1080P()) {
                    return C0049R.string.pref_mi_front_video_quality_default;
                }
            case C0049R.string.pref_camera_antibanding_default:
                if (Util.isAntibanding60()) {
                    return C0049R.string.pref_camera_antibanding_60;
                }
                break;
        }
        return i;
    }

    public static int getExitText(String str) {
        return ("pref_camera_coloreffect_key".equals(str) || "pref_camera_shader_coloreffect_key".equals(str)) ? C0049R.string.simple_mode_button_text_color_effect : "pref_camera_hand_night_key".equals(str) ? C0049R.string.simple_mode_button_text_hand_night : "pref_camera_panoramamode_key".equals(str) ? C0049R.string.simple_mode_button_text_panorama : "pref_video_speed_key".equals(str) ? C0049R.string.simple_mode_button_text_slow_video : "pref_camera_face_beauty_mode_key".equals(str) ? C0049R.string.simple_mode_button_text_face_beauty : "pref_delay_capture_mode".equals(str) ? C0049R.string.simple_mode_button_text_delay_capture : "pref_video_speed_fast_key".equals(str) ? C0049R.string.simple_mode_button_text_slow_video : "pref_video_speed_slow_key".equals(str) ? C0049R.string.simple_mode_button_text_fast_video : "pref_camera_ubifocus_key".equals(str) ? C0049R.string.simple_mode_button_text_ubifocus : "pref_camera_manual_mode_key".equals(str) ? C0049R.string.simple_mode_button_text_manual : "pref_camera_burst_shooting_key".equals(str) ? C0049R.string.burst_shoot_exit_button_text : "pref_audio_focus_mode_key".equals(str) ? C0049R.string.audio_focus_exit_button_text : "pref_camera_scenemode_setting_key".equals(str) ? C0049R.string.simple_mode_button_text_scene : "pref_camera_gradienter_key".equals(str) ? C0049R.string.simple_mode_button_text_gradienter : "pref_camera_tilt_shift_mode".equals(str) ? C0049R.string.simple_mode_button_text_tilt_shift : "pref_camera_magic_mirror_key".equals(str) ? C0049R.string.simple_mode_button_text_magic_mirror : "pref_audio_capture".equals(str) ? C0049R.string.simple_mode_button_text_audio_capture : "pref_camera_stereo_mode_key".equals(str) ? C0049R.string.simple_mode_button_text_stereo_mode : "pref_camera_square_mode_key".equals(str) ? C0049R.string.simple_mode_button_text_square : "pref_camera_groupshot_mode_key".equals(str) ? C0049R.string.simple_mode_button_text_groupshot : -1;
    }

    public static String getFaceBeautifyValue() {
        String string = getString(C0049R.string.pref_face_beauty_close);
        if (!Device.isSupportedSkinBeautify()) {
            return string;
        }
        String string2 = getString(C0049R.string.pref_face_beauty_default);
        if (!isFrontCamera()) {
            return isSwitchOn("pref_camera_face_beauty_mode_key") ? CameraSettingPreferences.instance().getString("pref_camera_face_beauty_key", string2) : string;
        } else {
            string = CameraSettingPreferences.instance().getString("pref_camera_face_beauty_key", string2);
            String string3 = CameraSettingPreferences.instance().getString("pref_camera_face_beauty_switch_key", "pref_camera_face_beauty_key");
            return string3.equals("pref_camera_face_beauty_advanced_key") ? getString(C0049R.string.pref_face_beauty_advanced) : string3.equals(getString(C0049R.string.pref_face_beauty_close)) ? getString(C0049R.string.pref_face_beauty_close) : string;
        }
    }

    public static String getFlashModeByScene(String str) {
        return (String) sSceneToFlash.get(str);
    }

    public static String getFocusMode() {
        return CameraSettingPreferences.instance().getString("pref_camera_focus_mode_key", getString(C0049R.string.pref_camera_focusmode_value_default));
    }

    public static int getFocusPosition() {
        return Integer.parseInt(CameraSettingPreferences.instance().getString("pref_focus_position_key", String.valueOf(1000)));
    }

    public static String getFrontMirror(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("pref_front_mirror_key", getString(C0049R.string.pref_front_mirror_default));
    }

    public static String getJpegQuality(CameraSettingPreferences cameraSettingPreferences, boolean z) {
        String string = cameraSettingPreferences.getString("pref_camera_jpegquality_key", getString(C0049R.string.pref_camera_jpegquality_default));
        String str = "high";
        if (z && Device.IS_HM3LTE) {
            str = "normal";
        } else if (z && Device.IS_HONGMI) {
            str = "low";
        }
        return JpegEncodingQualityMappings.getQualityNumber(string) < JpegEncodingQualityMappings.getQualityNumber(str) ? string : str;
    }

    public static int getKValue() {
        return CameraSettingPreferences.instance().getInt("pref_qc_manual_whitebalance_k_value_key", 5500);
    }

    public static String getManualFocusName(Context context, int i) {
        return i == 1000 ? context.getString(C0049R.string.pref_camera_focusmode_entry_auto) : ((double) i) >= 600.0d ? context.getString(C0049R.string.pref_camera_focusmode_entry_macro) : ((double) i) >= 200.0d ? context.getString(C0049R.string.pref_camera_focusmode_entry_normal) : context.getString(C0049R.string.pref_camera_focusmode_entry_infinity);
    }

    public static int getMaxExposureTimes(Context context) {
        return (Device.IS_XIAOMI && Device.isQcomPlatform()) ? ((QcomCameraProxy) CameraHardwareProxy.getDeviceProxy()).getMaxExposureTimeValue(CameraManager.instance().getStashParameters()) : 0;
    }

    public static int[] getMaxPreviewFpsRange(Parameters parameters) {
        List supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        return (supportedPreviewFpsRange == null || supportedPreviewFpsRange.size() <= 0) ? new int[0] : (int[]) supportedPreviewFpsRange.get(supportedPreviewFpsRange.size() - 1);
    }

    public static int getMinExposureTimes(Context context) {
        return (Device.IS_XIAOMI && Device.isQcomPlatform()) ? ((QcomCameraProxy) CameraHardwareProxy.getDeviceProxy()).getMinExposureTimeValue(CameraManager.instance().getStashParameters()) : 0;
    }

    public static String getMiuiSettingsKeyForStreetSnap(String str) {
        return str.equals(getString(getDefaultPreferenceId(C0049R.string.pref_camera_snap_value_take_picture))) ? "Street-snap-picture" : str.equals(getString(getDefaultPreferenceId(C0049R.string.pref_camera_snap_value_take_movie))) ? "Street-snap-movie" : "none";
    }

    public static int[] getPhotoPreviewFpsRange(Parameters parameters) {
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRange == null || supportedPreviewFpsRange.isEmpty()) {
            Log.e("CameraSettings", "No supported frame rates returned!");
            return null;
        }
        int i = 400000;
        for (int[] iArr : supportedPreviewFpsRange) {
            int[] iArr2;
            int i2 = iArr2[0];
            if (iArr2[1] >= 30000 && i2 <= 30000 && i2 < i) {
                i = i2;
            }
        }
        int i3 = -1;
        int i4 = 0;
        for (int i5 = 0; i5 < supportedPreviewFpsRange.size(); i5++) {
            iArr2 = (int[]) supportedPreviewFpsRange.get(i5);
            i2 = iArr2[0];
            int i6 = iArr2[1];
            if (i2 == i && r1 < i6) {
                i4 = i6;
                i3 = i5;
            }
        }
        if (i3 >= 0) {
            return (int[]) supportedPreviewFpsRange.get(i3);
        }
        Log.e("CameraSettings", "Can't find an appropriate frame rate range!");
        return null;
    }

    public static int getPreferVideoQuality() {
        String string = getString(getDefaultPreferenceId(C0049R.string.pref_video_quality_default));
        int parseInt = Integer.parseInt(string);
        if (CameraSettingPreferences.instance().contains("pref_video_quality_key")) {
            return Integer.parseInt(CameraSettingPreferences.instance().getString("pref_video_quality_key", string));
        }
        if (!CamcorderProfile.hasProfile(getCameraId(), Integer.parseInt(string))) {
            string = Integer.toString(1);
        }
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putString("pref_video_quality_key", string);
        edit.apply();
        return Integer.parseInt(string);
    }

    public static float getPreviewAspectRatio(int i, int i2) {
        return Math.abs((((double) i) / ((double) i2)) - 1.3333333333333333d) > Math.abs((((double) i) / ((double) i2)) - 1.7777777777777777d) ? 1.7777778f : 1.3333334f;
    }

    public static int getRenderAspectRatio(int i, int i2) {
        return (V6ModulePicker.isCameraModule() && isSwitchOn("pref_camera_square_mode_key")) ? 2 : getAspectRatio(i, i2);
    }

    public static int getShaderEffect() {
        if (isSwitchOn("pref_camera_gradienter_key")) {
            return EffectController.sGradienterIndex;
        }
        if (isSwitchOn("pref_camera_tilt_shift_mode")) {
            String string = CameraSettingPreferences.instance().getString("pref_camera_tilt_shift_key", getString(C0049R.string.pref_camera_tilt_shift_default));
            if (string.equals(getString(C0049R.string.pref_camera_tilt_shift_entryvalue_circle))) {
                return EffectController.sGaussianIndex;
            }
            if (string.equals(getString(C0049R.string.pref_camera_tilt_shift_entryvalue_parallel))) {
                return EffectController.sTiltShiftIndex;
            }
        } else if (isSwitchOn("pref_camera_magic_mirror_key")) {
            return 0;
        }
        try {
            return Integer.parseInt(CameraSettingPreferences.instance().getString("pref_camera_shader_coloreffect_key", getString(C0049R.string.pref_camera_shader_coloreffect_default)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getSkinBeautifyHumanReadableValue(Context context, ListPreference listPreference) {
        int findIndexOfValue = listPreference.findIndexOfValue(listPreference.getValue());
        int length = listPreference.getEntryValues().length;
        return findIndexOfValue > (length * 2) / 3 ? context.getString(C0049R.string.pref_camera_beautify_entry_high) : findIndexOfValue > length / 3 ? context.getString(C0049R.string.pref_camera_beautify_entry_normal) : findIndexOfValue > 0 ? context.getString(C0049R.string.pref_camera_beautify_entry_low) : context.getString(C0049R.string.pref_camera_beautify_entry_close);
    }

    public static String getSmartShutterPosition() {
        return CameraSettingPreferences.instance().getString("pref_key_camera_smart_shutter_position", "");
    }

    public static int getStrictAspectRatio(int i, int i2) {
        return isAspectRatio16_9(i, i2) ? 1 : isAspectRatio4_3(i, i2) ? 0 : isAspectRatio1_1(i, i2) ? 2 : -1;
    }

    public static String getString(int i) {
        return CameraAppImpl.getAndroidContext().getString(i);
    }

    public static ArrayList<String> getSupportedVideoQuality(int i) {
        ArrayList<String> arrayList = new ArrayList();
        int i2 = get4kProfile();
        if (Device.isSupportedVideoQuality4kUHD() && CamcorderProfile.hasProfile(i, i2)) {
            arrayList.add(Integer.toString(i2));
        }
        if (CamcorderProfile.hasProfile(i, 6)) {
            arrayList.add(Integer.toString(6));
        }
        if (CamcorderProfile.hasProfile(i, 5)) {
            arrayList.add(Integer.toString(5));
        }
        if (CamcorderProfile.hasProfile(i, 4)) {
            arrayList.add(Integer.toString(4));
        }
        if (CamcorderProfile.hasProfile(i, 11)) {
            arrayList.add(Integer.toString(11));
        }
        if (CamcorderProfile.hasProfile(i, 10)) {
            arrayList.add(Integer.toString(10));
        }
        if (CamcorderProfile.hasProfile(i, 9)) {
            arrayList.add(Integer.toString(9));
        }
        return arrayList;
    }

    public static int getSystemEdgeMode(Context context) {
        return (Device.isSupportedEdgeTouch() && (((System.getInt(context.getContentResolver(), "edge_handgrip", 0) | System.getInt(context.getContentResolver(), "edge_handgrip_clean", 0)) | System.getInt(context.getContentResolver(), "edge_handgrip_back", 0)) | System.getInt(context.getContentResolver(), "edge_handgrip_screenshot", 0)) == 1) ? 2 : 0;
    }

    public static int getUIStyleByPreview(int i, int i2) {
        if (Device.isPad()) {
            return 0;
        }
        if (sCroppedIfNeeded) {
            return 1;
        }
        int i3 = 0;
        double d = ((double) i) / ((double) i2);
        if (Device.isMDPRender() && Math.abs(d - 1.5d) < 0.02d) {
            i3 = 2;
        } else if (Math.abs(d - 1.3333333333333333d) > Math.abs(d - 1.7777777777777777d) || Math.abs(d - 1.5d) < 0.02d) {
            i3 = 1;
        }
        return i3;
    }

    public static int getVideoQuality() {
        int i = 6;
        if (isSwitchOn("pref_camera_stereo_mode_key")) {
            return 6;
        }
        int preferVideoQuality = getPreferVideoQuality();
        if (isSwitchOn("pref_video_speed_slow_key")) {
            if (!Device.isSupportFHDHFR()) {
                i = 5;
            }
            if (preferVideoQuality > i) {
                preferVideoQuality = i;
            }
        }
        return preferVideoQuality;
    }

    public static String getVideoSpeed(CameraSettingPreferences cameraSettingPreferences) {
        return "on".equals(cameraSettingPreferences.getString("pref_video_speed_fast_key", "off")) ? "fast" : "on".equals(cameraSettingPreferences.getString("pref_video_speed_slow_key", "off")) ? "slow" : "normal";
    }

    public static boolean is4KHigherVideoQuality(int i) {
        boolean z = false;
        if (!Device.isSupportedVideoQuality4kUHD()) {
            return false;
        }
        if (get4kProfile() <= i) {
            z = true;
        }
        return z;
    }

    public static boolean isAsdMotionEnable() {
        return Device.isSupportedAsdMotion() ? CameraSettingPreferences.instance().getBoolean("pref_camera_asd_night_key", Boolean.valueOf(getString(C0049R.bool.pref_camera_asd_night_default)).booleanValue()) : false;
    }

    public static boolean isAsdNightEnable() {
        return Device.isSupportedAsdNight() ? CameraSettingPreferences.instance().getBoolean("pref_camera_asd_night_key", Boolean.valueOf(getString(C0049R.bool.pref_camera_asd_night_default)).booleanValue()) : false;
    }

    public static boolean isAsdPopupEnable() {
        return Device.isSupportedAsdFlash() ? CameraSettingPreferences.instance().getBoolean("pref_camera_asd_popup_key", Boolean.valueOf(getString(C0049R.bool.pref_camera_asd_popup_default)).booleanValue()) : false;
    }

    public static boolean isAspectRatio16_9(int i, int i2) {
        if (i < i2) {
            int i3 = i;
            i = i2;
            i2 = i3;
        }
        return Math.abs((((double) i) / ((double) i2)) - 1.7777777777777777d) < 0.02d;
    }

    public static boolean isAspectRatio1_1(int i, int i2) {
        return i == i2;
    }

    public static boolean isAspectRatio4_3(int i, int i2) {
        if (i < i2) {
            int i3 = i;
            i = i2;
            i2 = i3;
        }
        return Math.abs((((double) i) / ((double) i2)) - 1.3333333333333333d) < 0.02d;
    }

    public static boolean isAudioCaptureOpen() {
        return isSwitchOn("pref_audio_capture");
    }

    public static boolean isBackCamera() {
        return CameraSettingPreferences.instance().isBackCamera();
    }

    public static boolean isBurstShootingEnable(SharedPreferences sharedPreferences) {
        return Device.isSupportedLongPressBurst() ? "burst".equals(sharedPreferences.getString("pref_camera_long_press_shutter_feature_key", getString(C0049R.string.pref_camera_long_press_shutter_feature_default))) : false;
    }

    public static boolean isCameraPortraitWithFaceBeauty() {
        return CameraSettingPreferences.instance().getBoolean("pref_camera_portrait_with_facebeauty_key", CameraAppImpl.getAndroidContext().getResources().getBoolean(C0049R.bool.pref_camera_portrait_with_facebeauty_default));
    }

    public static boolean isCameraPortraitWithFaceBeautyOptionVisible() {
        return CameraSettingPreferences.instance().getBoolean("pref_camera_portrait_with_facebeauty_key_visible", false);
    }

    public static boolean isCameraSoundOpen(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("pref_camerasound_key", true) || !Device.isSupportedMuteCameraSound();
    }

    public static boolean isDualCameraHintShown(CameraSettingPreferences cameraSettingPreferences) {
        boolean z = true;
        if (!Device.IS_H3C) {
            return true;
        }
        int i = cameraSettingPreferences.getInt("pref_dual_camera_use_hint_shown_times_key", 0);
        Editor edit = cameraSettingPreferences.edit();
        edit.putInt("pref_dual_camera_use_hint_shown_times_key", i + 1);
        edit.apply();
        if (i >= 5) {
            z = false;
        }
        return z;
    }

    public static boolean isDualCameraWaterMarkOpen(SharedPreferences sharedPreferences) {
        return (isSupportedOpticalZoom() && isBackCamera()) ? sharedPreferences.getBoolean("pref_dualcamera_watermark", CameraAppImpl.getAndroidContext().getResources().getBoolean(C0049R.bool.pref_dualcamera_watermark_default)) : false;
    }

    public static boolean isEdgePhotoEnable() {
        return Device.isSupportedEdgeTouch() ? sEdgePhotoEnable : false;
    }

    public static boolean isFaceBeautyOn(String str) {
        return (str == null || str.equals(getString(C0049R.string.pref_face_beauty_close))) ? false : true;
    }

    public static boolean isFaceWaterMarkOpen(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("pref_face_info_watermark_key", false);
    }

    public static boolean isFocusModeSwitching() {
        return CameraSettingPreferences.instance().getBoolean("pref_qc_focus_mode_switching_key", false);
    }

    public static boolean isFrontCamera() {
        return CameraSettingPreferences.instance().isFrontCamera();
    }

    public static boolean isMovieSolidOn(SharedPreferences sharedPreferences) {
        return Device.isSupportedMovieSolid() ? sharedPreferences.getBoolean("pref_camera_movie_solid_key", Boolean.valueOf(getString(C0049R.string.pref_camera_movie_solid_default)).booleanValue()) : false;
    }

    public static boolean isNearAspectRatio(int i, int i2, int i3, int i4) {
        return getAspectRatio(i, i2) == getAspectRatio(i3, i4);
    }

    public static boolean isNearRatio16_9(int i, int i2) {
        if (i < i2) {
            int i3 = i;
            i = i2;
            i2 = i3;
        }
        double d = ((double) i) / ((double) i2);
        return Math.abs(d - 1.3333333333333333d) > Math.abs(d - 1.7777777777777777d) || Math.abs(d - 1.5d) < 0.02d;
    }

    public static boolean isNeedFrontCameraFirstUseHint(CameraSettingPreferences cameraSettingPreferences) {
        boolean z = Device.IS_A8 || Device.IS_D5;
        return cameraSettingPreferences.getBoolean("pref_front_camera_first_use_hint_shown_key", z);
    }

    public static boolean isNeedRemind(String str) {
        return sRemindMode.contains(str) ? CameraSettingPreferences.instance().getBoolean(str + "_remind", true) : false;
    }

    public static boolean isNoCameraModeSelected(Context context) {
        for (String isSwitchOn : ((ActivityBase) context).getCurrentModule().getSupportedSettingKeys()) {
            if (isSwitchOn(isSwitchOn)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPressDownCapture() {
        return Device.isSupportedFastCapture() ? isFrontCamera() || !"focus".equals(CameraSettingPreferences.instance().getString("pref_camera_long_press_shutter_feature_key", getString(C0049R.string.pref_camera_long_press_shutter_feature_default))) : false;
    }

    public static boolean isRecordLocation(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("pref_camera_recordlocation_key", false) ? Device.isSupportedGPS() : false;
    }

    public static boolean isScanQRCode(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("pref_scan_qrcode_key", Boolean.valueOf(getString(C0049R.string.pref_scan_qrcode_default)).booleanValue()) && !isFrontCamera();
    }

    private static boolean isStringValueContained(Object obj, int i) {
        return isStringValueContains(obj, CameraAppImpl.getAndroidContext().getResources().getStringArray(i));
    }

    public static boolean isStringValueContains(Object obj, CharSequence[] charSequenceArr) {
        if (charSequenceArr == null || obj == null) {
            return false;
        }
        for (Object equals : charSequenceArr) {
            if (equals.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedMetadata() {
        return !Device.isSupportedASD() ? isSupportedPortrait() : true;
    }

    public static boolean isSupportedOpticalZoom() {
        return Device.isSupportedOpticalZoom() ? CameraHolder.instance().hasAuxCamera() : false;
    }

    public static boolean isSupportedPortrait() {
        return Device.isSupportedPortrait() ? CameraHolder.instance().hasAuxCamera() : false;
    }

    public static boolean isSwitchCameraZoomMode() {
        return !V6ModulePicker.isPanoramaModule() ? isSwitchOn("pref_camera_manual_mode_key") : true;
    }

    public static boolean isSwitchOn(String str) {
        return "on".equals(CameraSettingPreferences.instance().getString(str, "off"));
    }

    public static boolean isTimeWaterMarkOpen(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("pref_watermark_key", false);
    }

    public static boolean isVideoCaptureVisible() {
        CameraSettingPreferences instance = CameraSettingPreferences.instance();
        return (!instance.getBoolean("pref_video_captrue_ability_key", false) || ((Device.isMTKPlatform() && isSwitchOn("pref_camera_stereo_mode_key")) || (!Device.isHFRVideoCaptureSupported() && "slow".equals(getVideoSpeed(instance))))) ? false : Device.isSupportedVideoQuality4kUHD() ? !(Device.IS_MI4 || Device.IS_X5) || getVideoQuality() <= 6 : true;
    }

    public static void readEdgePhotoSetting(Context context) {
        boolean z = true;
        if (Device.isSupportedEdgeTouch()) {
            if (System.getInt(context.getContentResolver(), "edge_handgrip_photo", 0) != 1) {
                z = false;
            }
            sEdgePhotoEnable = z;
        }
    }

    public static int readExposure(CameraSettingPreferences cameraSettingPreferences) {
        String string = cameraSettingPreferences.getString("pref_camera_exposure_key", "0");
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            Log.e("CameraSettings", "Invalid exposure: " + string);
            return 0;
        }
    }

    public static int readPreferredCameraId(SharedPreferences sharedPreferences) {
        return Integer.parseInt(sharedPreferences.getString("pref_camera_id_key", String.valueOf(CameraHolder.instance().getBackCameraId())));
    }

    public static int readZoom(CameraSettingPreferences cameraSettingPreferences) {
        String string = cameraSettingPreferences.getString("pref_camera_zoom_key", "0");
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            Log.e("CameraSettings", "Invalid zoom: " + string);
            return 0;
        }
    }

    public static void resetCameraZoomMode() {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.remove("pref_camera_zoom_mode_key");
        edit.apply();
    }

    public static void resetExposure() {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.remove("pref_camera_exposure_key");
        edit.apply();
    }

    public static void resetOpenCameraFailTimes() {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putLong("open_camera_fail_key", 0);
        edit.apply();
    }

    public static void resetPreference(String str) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.remove(str);
        edit.apply();
    }

    public static void resetSettingsNoNeedToSave(CameraSettingPreferences cameraSettingPreferences, int i) {
        Object obj;
        Editor edit = cameraSettingPreferences.edit();
        edit.remove("pref_camera_exposure_key");
        edit.remove("pref_camera_coloreffect_key");
        edit.remove("pref_camera_shader_coloreffect_key");
        edit.remove("pref_camera_focus_mode_key");
        edit.remove("pref_camera_ae_bracket_hdr_key");
        edit.remove("pref_camera_hand_night_key");
        edit.remove("pref_camera_scenemode_key");
        edit.remove("pref_camera_scenemode_setting_key");
        edit.remove("pref_video_speed_key");
        edit.remove("pref_video_hdr_key");
        edit.remove("pref_camera_face_beauty_key");
        edit.remove("pref_camera_face_beauty_mode_key");
        edit.remove("pref_camera_id_key");
        edit.remove("pref_delay_capture_mode");
        edit.remove("pref_delay_capture_key");
        edit.remove("pref_audio_capture");
        edit.remove("pref_video_speed_fast_key");
        edit.remove("pref_video_speed_slow_key");
        edit.remove("pref_camera_ubifocus_key");
        edit.remove("pref_camera_manual_mode_key");
        edit.remove("pref_camera_panoramamode_key");
        edit.remove("pref_camera_burst_shooting_key");
        edit.remove("pref_audio_focus_mode_key");
        edit.remove("pref_camera_gradienter_key");
        edit.remove("pref_camera_tilt_shift_mode");
        edit.remove("pref_camera_magic_mirror_key");
        edit.remove("pref_camera_stereo_mode_key");
        edit.remove("pref_camera_groupshot_mode_key");
        edit.remove("pref_camera_zoom_key");
        edit.remove("pref_camera_zoom_mode_key");
        edit.remove("pref_camera_portrait_mode_key");
        edit.remove("pref_camera_square_mode_key");
        Map all = cameraSettingPreferences.getAll();
        for (String str : Arrays.asList(new String[]{"pref_camerasound_key", "pref_scan_qrcode_key", "pref_watermark_key", "pref_camera_referenceline_key", "pref_camera_facedetection_key", "pref_camera_movie_solid_key"})) {
            obj = all.get(str);
            if (obj != null && (obj instanceof String)) {
                edit.remove(str);
                if (!"pref_camera_facedetection_key".equals(str) || !Device.isThirdDevice()) {
                    edit.putBoolean(str, "on".equalsIgnoreCase((String) obj));
                }
            }
        }
        obj = all.get("pref_video_quality_key");
        if (obj != null && (obj instanceof String)) {
            if (is4KHigherVideoQuality(Integer.parseInt((String) obj))) {
                edit.remove("pref_video_quality_key");
            } else if (!getSupportedVideoQuality(i).contains(obj)) {
                Log.d("CameraSettings", "Remove unsupported video quality " + obj + " for camera " + i);
                edit.remove("pref_video_quality_key");
            }
        }
        filterPreference(all, "pref_camera_skin_beautify_key", edit, C0049R.array.pref_camera_skin_beautify_entryvalues);
        filterPreference(all, "pref_qc_camera_saturation_key", edit, C0049R.array.pref_camera_saturation_entryvalues);
        filterPreference(all, "pref_qc_camera_contrast_key", edit, C0049R.array.pref_camera_contrast_entryvalues);
        filterPreference(all, "pref_qc_camera_sharpness_key", edit, C0049R.array.pref_camera_sharpness_entryvalues);
        filterPreference(all, "pref_video_quality_key", edit, C0049R.array.pref_video_quality_entryvalues);
        obj = all.get("pref_front_mirror_key");
        if (!(obj == null || (obj instanceof String))) {
            edit.remove("pref_front_mirror_key");
        }
        obj = all.get("pref_camera_restored_flashmode_key");
        if (obj != null && (obj instanceof String)) {
            edit.putString("pref_camera_flashmode_key", (String) obj);
            edit.remove("pref_camera_restored_flashmode_key");
        }
        obj = all.get("pref_camera_hdr_key");
        if (!(obj == null || "auto".equals(obj) || "off".equals(obj))) {
            edit.remove("pref_camera_hdr_key");
        }
        if (all.get("pref_camera_confirm_location_shown_key") == null) {
            Object obj2 = all.get("pref_camera_recordlocation_key");
            if (Device.isSupportedGPS() && (obj2 == null || ((obj2 instanceof Boolean) && !((Boolean) obj2).booleanValue()))) {
                edit.remove("pref_camera_first_use_hint_shown_key");
                edit.remove("pref_camera_recordlocation_key");
            }
            edit.putBoolean("pref_camera_confirm_location_shown_key", false);
        }
        edit.apply();
    }

    public static void resetZoom(CameraSettingPreferences cameraSettingPreferences) {
        Editor edit = cameraSettingPreferences.edit();
        edit.remove("pref_camera_zoom_key");
        edit.apply();
    }

    public static void restorePreferences(Context context, CameraSettingPreferences cameraSettingPreferences) {
        int readPreferredCameraId = readPreferredCameraId(cameraSettingPreferences);
        Editor edit = cameraSettingPreferences.edit();
        edit.clear();
        edit.apply();
        upgradeGlobalPreferences(cameraSettingPreferences);
        writePreferredCameraId(cameraSettingPreferences, readPreferredCameraId);
    }

    public static void setAutoExposure(CameraHardwareProxy cameraHardwareProxy, Parameters parameters, String str) {
        if (str != null) {
            int i;
            List arrayList = new ArrayList();
            Rect rect = new Rect();
            if (str.equals(getString(C0049R.string.pref_camera_autoexposure_value_spotmetering))) {
                rect.left = -250;
                rect.top = -250;
                rect.right = 250;
                rect.bottom = 250;
                i = 1;
            } else if (str.equals(getString(C0049R.string.pref_camera_autoexposure_value_centerweighted))) {
                rect.left = 0;
                rect.top = 0;
                rect.right = 0;
                rect.bottom = 0;
                i = 0;
            } else {
                rect.left = -1000;
                rect.top = -1000;
                rect.right = 1000;
                rect.bottom = 1000;
                i = 1;
            }
            arrayList.add(new Area(rect, i));
            cameraHardwareProxy.setMeteringAreas(parameters, arrayList);
        }
    }

    public static void setCameraPortraitWithFaceBeautyOptionVisible(boolean z) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putBoolean("pref_camera_portrait_with_facebeauty_key_visible", z);
        edit.apply();
    }

    public static void setDualCameraWaterMarkOpen(SharedPreferences sharedPreferences, boolean z) {
        if (isSupportedOpticalZoom() && isBackCamera()) {
            sharedPreferences.edit().putBoolean("pref_dualcamera_watermark", z).apply();
        }
    }

    public static void setEdgeMode(Context context, boolean z) {
        int i = 1;
        if (context != null) {
            if (z) {
                readEdgePhotoSetting(context);
            }
            if (isEdgePhotoEnable()) {
                InputManager inputManager = (InputManager) context.getSystemService("input");
                Class cls = InputManager.class;
                String str = "switchTouchEdgeMode";
                String str2 = "(I)V";
                Object[] objArr = new Object[1];
                if (!z) {
                    i = getSystemEdgeMode(context);
                }
                objArr[0] = Integer.valueOf(i);
                ReflectUtil.callMethod(cls, inputManager, str, str2, objArr);
            }
        }
    }

    public static void setFocusMode(String str) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putString("pref_camera_focus_mode_key", str);
        edit.apply();
    }

    public static void setFocusModeSwitching(boolean z) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putBoolean("pref_qc_focus_mode_switching_key", z);
        edit.apply();
    }

    public static void setFocusPosition(int i) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putString("pref_focus_position_key", String.valueOf(i));
        edit.apply();
    }

    public static void setKValue(int i) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putInt("pref_qc_manual_whitebalance_k_value_key", i);
        edit.apply();
    }

    public static void setPriorityStoragePreference(boolean z) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putBoolean("pref_priority_storage", z);
        edit.apply();
    }

    public static void setShaderEffect(int i) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putString("pref_camera_shader_coloreffect_key", String.valueOf(i));
        edit.apply();
    }

    public static void setSmartShutterPosition(String str) {
        Editor edit = CameraSettingPreferences.instance().edit();
        edit.putString("pref_key_camera_smart_shutter_position", str);
        edit.apply();
    }

    public static boolean showGenderAge(SharedPreferences sharedPreferences) {
        return !sharedPreferences.getString("pref_camera_show_gender_age_key", getString(C0049R.string.face_beauty_intelligent_level_3)).equalsIgnoreCase("off");
    }

    public static void updateFocusMode() {
        String focusMode = getFocusMode();
        String str = (!isSwitchOn("pref_camera_manual_mode_key") || getFocusPosition() == 1000) ? "continuous-picture" : "manual";
        if (!str.equals(focusMode)) {
            setFocusModeSwitching(true);
            setFocusMode(str);
        }
    }

    public static long updateOpenCameraFailTimes() {
        Editor edit = CameraSettingPreferences.instance().edit();
        long j = CameraSettingPreferences.instance().getLong("open_camera_fail_key", 0) + 1;
        edit.putLong("open_camera_fail_key", j);
        edit.apply();
        return j;
    }

    public static void upgradeGlobalPreferences(CameraSettingPreferences cameraSettingPreferences) {
        Editor edit = cameraSettingPreferences.edit();
        if (cameraSettingPreferences.getInt("pref_version_key", 1) < 1 && !cameraSettingPreferences.getBoolean("pref_camera_first_use_hint_shown_key", true)) {
            edit.putBoolean("pref_camera_first_touch_toast_shown_key", false);
        }
        edit.putInt("pref_version_key", 1);
        edit.apply();
    }

    public static void upgradeLocalPreferences(SharedPreferences sharedPreferences) {
        Editor edit = sharedPreferences.edit();
        int i = sharedPreferences.getInt("pref_local_version_key", 0);
        if (i == 0) {
            i = 1;
        }
        edit.putInt("pref_local_version_key", i);
        edit.apply();
    }

    public static void writeExposure(CameraSettingPreferences cameraSettingPreferences, int i) {
        Editor edit = cameraSettingPreferences.edit();
        edit.putString("pref_camera_exposure_key", Integer.toString(i));
        edit.apply();
    }

    public static void writePreferredCameraId(SharedPreferences sharedPreferences, int i) {
        Editor edit = sharedPreferences.edit();
        edit.putString("pref_camera_id_key", Integer.toString(i));
        edit.apply();
    }

    public static void writeZoom(CameraSettingPreferences cameraSettingPreferences, int i) {
        Editor edit = cameraSettingPreferences.edit();
        edit.putString("pref_camera_zoom_key", Integer.toString(i));
        edit.apply();
    }
}

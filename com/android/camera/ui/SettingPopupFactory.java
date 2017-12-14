package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class SettingPopupFactory {
    public static V6AbstractSettingPopup createSettingPopup(String str, ViewGroup viewGroup, Context context) {
        int i;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        if ("pref_qc_camera_iso_key".equals(str)) {
            i = C0049R.layout.manual_iso_popup;
        } else if ("pref_delay_capture_mode".equals(str)) {
            i = C0049R.layout.v6_seek_bar_popup;
        } else if ("pref_skin_beautify_skin_color_key".equals(str) || "pref_skin_beautify_slim_face_key".equals(str) || "pref_skin_beautify_skin_smooth_key".equals(str) || "pref_skin_beautify_enlarge_eye_key".equals(str)) {
            i = C0049R.layout.v6_skin_beautify_seek_bar_popup;
        } else if ("pref_camera_whitebalance_key".equals(str)) {
            i = C0049R.layout.grid_setting_popup_whitebalance;
        } else if ("pref_focus_position_key".equals(str)) {
            i = C0049R.layout.manual_focus_popup;
        } else if ("pref_camera_manual_mode_key".equals(str) || "pref_camera_face_beauty_advanced_key".equals(str)) {
            i = C0049R.layout.sub_screen_setting_popup;
        } else if ("pref_qc_camera_exposuretime_key".equals(str)) {
            i = C0049R.layout.manual_exposure_popup;
        } else if ("pref_camera_scenemode_setting_key".equals(str)) {
            i = C0049R.layout.grid_setting_popup_scene;
        } else if ("pref_camera_stereo_mode_key".equals(str)) {
            i = C0049R.layout.stereo_view_popup;
        } else if ("pref_audio_focus_mode_key".equals(str) || "pref_delay_capture_key".equals(str) || "pref_camera_tilt_shift_mode".equals(str) || "pref_camera_face_beauty_key".equals(str)) {
            i = C0049R.layout.grid_setting_text_popup;
        } else if ("pref_camera_flashmode_key".equals(str) || "pref_camera_video_flashmode_key".equals(str) || "pref_camera_hdr_key".equals(str) || "pref_video_hdr_key".equals(str) || "pref_camera_face_beauty_switch_key".equals(str)) {
            i = C0049R.layout.grid_setting_expanded_text_popup;
        } else if ("pref_camera_shader_coloreffect_key".equals(str)) {
            i = C0049R.layout.effect_popup;
        } else if ("pref_camera_face_beauty_mode_key".equals(str)) {
            i = C0049R.layout.v6_seek_bar_popup;
        } else if (!"pref_camera_zoom_mode_key".equals(str)) {
            return null;
        } else {
            i = C0049R.layout.zoom_popup;
        }
        return (V6AbstractSettingPopup) layoutInflater.inflate(i, viewGroup, false);
    }
}

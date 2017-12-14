package com.android.camera.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import com.android.camera.CameraAppImpl;
import com.android.camera.CameraHolder;
import com.android.camera.CameraSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CameraSettingPreferences implements SharedPreferences {
    private static CameraSettingPreferences sPreferences;
    private SharedPreferences mPrefGlobal;
    private SharedPreferences mPrefLocal;
    private SharedPreferences mPrefModeGlobal;
    private int mPreferencesLocalId;

    private class MyEditor implements Editor {
        private Editor mEditorGlobal;
        private Editor mEditorLocal;
        private Editor mEditorModeGlobal;

        MyEditor() {
            this.mEditorGlobal = CameraSettingPreferences.this.mPrefGlobal.edit();
            this.mEditorModeGlobal = CameraSettingPreferences.this.mPrefModeGlobal.edit();
            this.mEditorLocal = CameraSettingPreferences.this.mPrefLocal.edit();
        }

        public void apply() {
            this.mEditorGlobal.apply();
            this.mEditorModeGlobal.apply();
            this.mEditorLocal.apply();
        }

        public Editor clear() {
            this.mEditorGlobal.clear();
            this.mEditorModeGlobal.clear();
            this.mEditorLocal.clear();
            return this;
        }

        public boolean commit() {
            return (this.mEditorGlobal.commit() && this.mEditorModeGlobal.commit()) ? this.mEditorLocal.commit() : false;
        }

        public Editor putBoolean(String str, boolean z) {
            if (CameraSettingPreferences.isGlobal(str)) {
                this.mEditorGlobal.putBoolean(str, z);
            } else if (CameraSettingPreferences.isModeGlobal(str)) {
                this.mEditorModeGlobal.putBoolean(str, z);
            } else {
                this.mEditorLocal.putBoolean(str, z);
            }
            return this;
        }

        public Editor putFloat(String str, float f) {
            if (CameraSettingPreferences.isGlobal(str)) {
                this.mEditorGlobal.putFloat(str, f);
            } else if (CameraSettingPreferences.isModeGlobal(str)) {
                this.mEditorModeGlobal.putFloat(str, f);
            } else {
                this.mEditorLocal.putFloat(str, f);
            }
            return this;
        }

        public Editor putInt(String str, int i) {
            if (CameraSettingPreferences.isGlobal(str)) {
                this.mEditorGlobal.putInt(str, i);
            } else if (CameraSettingPreferences.isModeGlobal(str)) {
                this.mEditorModeGlobal.putInt(str, i);
            } else {
                this.mEditorLocal.putInt(str, i);
            }
            return this;
        }

        public Editor putLong(String str, long j) {
            if (CameraSettingPreferences.isGlobal(str)) {
                this.mEditorGlobal.putLong(str, j);
            } else if (CameraSettingPreferences.isModeGlobal(str)) {
                this.mEditorModeGlobal.putLong(str, j);
            } else {
                this.mEditorLocal.putLong(str, j);
            }
            return this;
        }

        public Editor putString(String str, String str2) {
            if (CameraSettingPreferences.isGlobal(str)) {
                this.mEditorGlobal.putString(str, str2);
            } else if (CameraSettingPreferences.isModeGlobal(str)) {
                this.mEditorModeGlobal.putString(str, str2);
            } else {
                this.mEditorLocal.putString(str, str2);
            }
            return this;
        }

        public Editor putStringSet(String str, Set<String> set) {
            throw new UnsupportedOperationException();
        }

        public Editor remove(String str) {
            this.mEditorGlobal.remove(str);
            this.mEditorModeGlobal.remove(str);
            this.mEditorLocal.remove(str);
            return this;
        }
    }

    private CameraSettingPreferences(Context context) {
        this.mPrefGlobal = context.getSharedPreferences("camera_settings_global", 0);
    }

    private int getCameraId() {
        return Integer.parseInt(this.mPrefGlobal.getString("pref_camera_id_key", "0"));
    }

    public static synchronized CameraSettingPreferences instance() {
        CameraSettingPreferences cameraSettingPreferences;
        synchronized (CameraSettingPreferences.class) {
            if (sPreferences == null) {
                sPreferences = new CameraSettingPreferences(CameraAppImpl.getAndroidContext());
                sPreferences.setLocalIdInternal(sPreferences.getCameraId());
            }
            cameraSettingPreferences = sPreferences;
        }
        return cameraSettingPreferences;
    }

    private static boolean isGlobal(String str) {
        return (str.equals("pref_camera_id_key") || str.equals("pref_camera_recordlocation_key") || str.equals("pref_camera_volumekey_function_key") || str.equals("pref_version_key") || str.equals("pref_camerasound_key") || str.equals("pref_camera_referenceline_key") || str.equals("pref_watermark_key") || str.equals("pref_dualcamera_watermark") || str.equals("pref_face_info_watermark_key") || str.equals("pref_camera_antibanding_key") || str.equals("pref_front_mirror_key") || str.equals("pref_camera_show_gender_age_key") || str.equals("open_camera_fail_key") || str.equals("pref_camera_first_use_hint_shown_key") || str.equals("pref_camera_first_portrait_use_hint_shown_key") || str.equals("pref_camera_confirm_location_shown_key") || str.equals("pref_front_camera_first_use_hint_shown_key") || str.equals("pref_key_camera_smart_shutter_position") || str.equals("pref_priority_storage") || str.equals("pref_camera_snap_key") || str.equals("pref_groupshot_with_primitive_picture_key") || str.equals("pref_camera_mode_settings_key_remind")) ? true : str.equals("panorama_last_start_direction_key");
    }

    private static boolean isModeGlobal(String str) {
        return str.equals("pref_video_captrue_ability_key");
    }

    private CameraSettingPreferences setLocalIdInternal(int i) {
        this.mPreferencesLocalId = i;
        this.mPrefModeGlobal = CameraAppImpl.getAndroidContext().getSharedPreferences("camera_settings_simple_mode_global", 0);
        this.mPrefLocal = CameraAppImpl.getAndroidContext().getSharedPreferences("camera_settings_simple_mode_local_" + i, 0);
        CameraSettings.upgradeLocalPreferences(this.mPrefLocal);
        return sPreferences;
    }

    public boolean contains(String str) {
        return this.mPrefLocal.contains(str) || this.mPrefModeGlobal.contains(str) || this.mPrefGlobal.contains(str);
    }

    public Editor edit() {
        return new MyEditor();
    }

    public Map<String, ?> getAll() {
        Map<String, ?> hashMap = new HashMap();
        hashMap.putAll(this.mPrefLocal.getAll());
        hashMap.putAll(this.mPrefModeGlobal.getAll());
        hashMap.putAll(this.mPrefGlobal.getAll());
        return hashMap;
    }

    public boolean getBoolean(String str, boolean z) {
        return isGlobal(str) ? this.mPrefGlobal.getBoolean(str, z) : isModeGlobal(str) ? this.mPrefModeGlobal.getBoolean(str, z) : this.mPrefLocal.getBoolean(str, z);
    }

    public float getFloat(String str, float f) {
        return isGlobal(str) ? this.mPrefGlobal.getFloat(str, f) : isModeGlobal(str) ? this.mPrefModeGlobal.getFloat(str, f) : this.mPrefLocal.getFloat(str, f);
    }

    public int getInt(String str, int i) {
        return isGlobal(str) ? this.mPrefGlobal.getInt(str, i) : isModeGlobal(str) ? this.mPrefModeGlobal.getInt(str, i) : this.mPrefLocal.getInt(str, i);
    }

    public long getLong(String str, long j) {
        return isGlobal(str) ? this.mPrefGlobal.getLong(str, j) : isModeGlobal(str) ? this.mPrefModeGlobal.getLong(str, j) : this.mPrefLocal.getLong(str, j);
    }

    public String getString(String str, String str2) {
        return isGlobal(str) ? this.mPrefGlobal.getString(str, str2) : isModeGlobal(str) ? this.mPrefModeGlobal.getString(str, str2) : this.mPrefLocal.getString(str, str2);
    }

    public Set<String> getStringSet(String str, Set<String> set) {
        throw new UnsupportedOperationException();
    }

    public boolean isBackCamera() {
        return instance().getCameraId() == CameraHolder.instance().getBackCameraId();
    }

    public boolean isFrontCamera() {
        return instance().getCameraId() == CameraHolder.instance().getFrontCameraId();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    }

    public CameraSettingPreferences setLocalId(int i) {
        return i != this.mPreferencesLocalId ? setLocalIdInternal(i) : this;
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
    }
}

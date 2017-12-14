package com.android.camera;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings.Secure;
import android.support.v7.recyclerview.C0049R;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.storage.PriorityStorageBroadcastReceiver;
import com.android.camera.storage.Storage;
import com.android.camera.ui.PreviewListPreference;
import com.android.camera.ui.V6ModulePicker;
import java.util.List;

public abstract class BasePreferenceActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = BasePreferenceActivity.class.getSimpleName();
    private int mFaceDetectionHitCountDown;
    private int mFromWhere;
    private Preference mPortraitWithFaceBeautyPreference;
    protected PreferenceScreen mPreferenceGroup;
    protected CameraSettingPreferences mPreferences;

    class C00791 implements Runnable {
        C00791() {
        }

        public void run() {
            BasePreferenceActivity.this.restorePreferences();
        }
    }

    private void filterByCameraID() {
        if (this.mPreferences.isFrontCamera()) {
            removePreference(this.mPreferenceGroup, "pref_camera_hfr_key");
            removePreference(this.mPreferenceGroup, "pref_video_focusmode_key");
            removePreference(this.mPreferenceGroup, "pref_camera_skinToneEnhancement_key");
            removePreference(this.mPreferenceGroup, "pref_scan_qrcode_key");
            removePreference(this.mPreferenceGroup, "pref_camera_autoexposure_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_sharpness_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_contrast_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_saturation_key");
            removePreference(this.mPreferenceGroup, "pref_auto_chroma_flash_key");
            removePreference(this.mPreferenceGroup, "pref_capture_when_stable_key");
            removePreference(this.mPreferenceGroup, "pref_video_time_lapse_frame_interval_key");
            removePreference(this.mPreferenceGroup, "pref_camera_long_press_shutter_feature_key");
            removePreference(this.mPreferenceGroup, "pref_camera_asd_night_key");
            removePreference(this.mPreferenceGroup, "pref_camera_asd_popup_key");
            removePreference(this.mPreferenceGroup, "pref_camera_movie_solid_key");
            removePreference(this.mPreferenceGroup, "pref_camera_portrait_with_facebeauty_key");
            return;
        }
        removePreference(this.mPreferenceGroup, "pref_front_mirror_key");
    }

    private void filterByDeviceID() {
        if (!Device.isSupportedHFR()) {
            removePreference(this.mPreferenceGroup, "pref_camera_hfr_key");
        }
        if (!Device.isSupportedIntelligentBeautify()) {
            removePreference(this.mPreferenceGroup, "pref_camera_show_gender_age_key");
        }
        if (!Device.isSupportedSkinBeautify()) {
            removePreference(this.mPreferenceGroup, "pref_camera_show_gender_age_key");
        }
        removePreference(this.mPreferenceGroup, "pref_camera_long_press_shutter_key");
        if (!Device.isSupportedMovieSolid()) {
            removePreference(this.mPreferenceGroup, "pref_camera_movie_solid_key");
        }
        if (!Device.isSupportedTimeWaterMark()) {
            removePreference(this.mPreferenceGroup, "pref_watermark_key");
        }
        if (!Device.isSupportedFaceInfoWaterMark()) {
            removePreference(this.mPreferenceGroup, "pref_face_info_watermark_key");
        }
        if (!Device.isSupportedMuteCameraSound()) {
            removePreference(this.mPreferenceGroup, "pref_camerasound_key");
        }
        if (!Device.isSupportedGPS()) {
            removePreference(this.mPreferenceGroup, "pref_camera_recordlocation_key");
        }
        if (Device.isPad() || (Device.IS_MI3TD && this.mPreferences.isFrontCamera())) {
            removePreference(this.mPreferenceGroup, "pref_camera_picturesize_key");
        }
        if (!Storage.secondaryStorageMounted()) {
            removePreference(this.mPreferenceGroup, "pref_priority_storage");
        }
        if (!Device.isSupportedChromaFlash()) {
            removePreference(this.mPreferenceGroup, "pref_auto_chroma_flash_key");
        }
        if (!Device.isSupportedLongPressBurst()) {
            removePreference(this.mPreferenceGroup, "pref_camera_long_press_shutter_feature_key");
        }
        if (!Device.isSupportedObjectTrack()) {
            removePreference(this.mPreferenceGroup, "pref_capture_when_stable_key");
        }
        if (!Device.isSupportedAsdNight()) {
            removePreference(this.mPreferenceGroup, "pref_camera_asd_night_key");
        }
        if (!Device.isSupportedAsdFlash()) {
            removePreference(this.mPreferenceGroup, "pref_camera_asd_popup_key");
        }
        if (!Device.isSupportedQuickSnap()) {
            removePreference(this.mPreferenceGroup, "pref_camera_snap_key");
        }
        if (!Device.isSupportGroupShot()) {
            removePreference(this.mPreferenceGroup, "pref_groupshot_with_primitive_picture_key");
        }
        if (!CameraSettings.isSupportedPortrait()) {
            removePreference(this.mPreferenceGroup, "pref_camera_portrait_with_facebeauty_key");
        }
        if (Device.isThirdDevice()) {
            removePreference(this.mPreferenceGroup, "pref_camera_facedetection_key");
            removePreference(this.mPreferenceGroup, "pref_front_mirror_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_sharpness_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_contrast_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_saturation_key");
            removePreference(this.mPreferenceGroup, "pref_camera_autoexposure_key");
        }
        if (Device.IS_D2A) {
            removePreference(this.mPreferenceGroup, "pref_scan_qrcode_key");
        }
    }

    private void filterByFrom() {
        if (this.mFromWhere == 1) {
            removePreference(this.mPreferenceGroup, "category_camcorder_setting");
        } else if (this.mFromWhere == 2) {
            removePreference(this.mPreferenceGroup, "category_camera_setting");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_sharpness_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_contrast_key");
            removePreference(this.mPreferenceGroup, "pref_qc_camera_saturation_key");
            removePreference(this.mPreferenceGroup, "pref_camera_facedetection_key");
            removePreference(this.mPreferenceGroup, "pref_camera_show_gender_age_key");
            removePreference(this.mPreferenceGroup, "pref_camera_autoexposure_key");
            removePreference(this.mPreferenceGroup, "pref_scan_qrcode_key");
            removePreference(this.mPreferenceGroup, "pref_camera_portrait_with_facebeauty_key");
        }
    }

    private void filterByPreference() {
        PreviewListPreference previewListPreference = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_video_quality_key");
        if (previewListPreference != null) {
            filterUnsupportedOptions(this.mPreferenceGroup, previewListPreference, CameraSettings.getSupportedVideoQuality(CameraSettings.getCameraId()));
        }
        String videoSpeed = CameraSettings.getVideoSpeed(this.mPreferences);
        if (Device.IS_X9 && !"normal".equals(videoSpeed)) {
            removePreference(this.mPreferenceGroup, "pref_camera_movie_solid_key");
        }
        if (!Device.isHFRVideoCaptureSupported() && "slow".equals(videoSpeed)) {
            removePreference(this.mPreferenceGroup, "pref_video_captrue_ability_key");
        }
        if (!CameraSettings.isCameraPortraitWithFaceBeautyOptionVisible()) {
            removePreference(this.mPreferenceGroup, "pref_camera_portrait_with_facebeauty_key");
        }
    }

    private void filterGroup() {
        filterGroupIfEmpty("category_device_setting");
        filterGroupIfEmpty("category_camcorder_setting");
        filterGroupIfEmpty("category_camera_setting");
        filterGroupIfEmpty("category_advance_setting");
    }

    private void filterGroupIfEmpty(String str) {
        Preference findPreference = this.mPreferenceGroup.findPreference(str);
        if (findPreference != null && (findPreference instanceof PreferenceGroup) && ((PreferenceGroup) findPreference).getPreferenceCount() == 0) {
            removePreference(this.mPreferenceGroup, str);
        }
    }

    private String getFilterValue(PreviewListPreference previewListPreference, SharedPreferences sharedPreferences) {
        String value = previewListPreference.getValue();
        if (sharedPreferences == null) {
            return value;
        }
        CharSequence string = sharedPreferences.getString(previewListPreference.getKey(), value);
        if (!CameraSettings.isStringValueContains(string, previewListPreference.getEntryValues())) {
            string = value;
            if (!CameraSettings.isStringValueContains(value, previewListPreference.getEntryValues())) {
                CharSequence[] entryValues = previewListPreference.getEntryValues();
                if (entryValues != null && entryValues.length >= 1) {
                    string = entryValues[0];
                }
            }
            Editor edit = sharedPreferences.edit();
            edit.putString(previewListPreference.getKey(), string.toString());
            edit.apply();
        }
        return string.toString();
    }

    private void initializeActivity() {
        this.mPreferenceGroup = getPreferenceScreen();
        if (this.mPreferenceGroup != null) {
            this.mPreferenceGroup.removeAll();
        }
        addPreferencesFromResource(getPreferenceXml());
        this.mPreferenceGroup = getPreferenceScreen();
        if (this.mPreferenceGroup == null) {
            finish();
        }
        this.mPortraitWithFaceBeautyPreference = this.mPreferenceGroup.findPreference("pref_camera_portrait_with_facebeauty_key");
        registerListener();
        filterByPreference();
        filterByFrom();
        filterByDeviceID();
        filterByCameraID();
        filterByIntent();
        filterGroup();
        updateEntries();
        updatePreferences(this.mPreferenceGroup, this.mPreferences);
        updateConflictPreference(null);
    }

    private void registerListener() {
        registerListener(this.mPreferenceGroup, this);
        Preference findPreference = this.mPreferenceGroup.findPreference("pref_restore");
        if (findPreference != null) {
            findPreference.setOnPreferenceClickListener(this);
        }
        Preference findPreference2 = this.mPreferenceGroup.findPreference("pref_priority_storage");
        if (findPreference2 != null) {
            findPreference2.setOnPreferenceClickListener(this);
        }
        Preference findPreference3 = this.mPreferenceGroup.findPreference("pref_camera_facedetection_key");
        if (findPreference3 != null) {
            findPreference3.setOnPreferenceClickListener(this);
        }
    }

    private void registerListener(PreferenceGroup preferenceGroup, OnPreferenceChangeListener onPreferenceChangeListener) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                registerListener((PreferenceGroup) preference, onPreferenceChangeListener);
            } else {
                preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
            }
        }
    }

    private void resetIfInvalid(ListPreference listPreference) {
        if (listPreference.findIndexOfValue(listPreference.getValue()) == -1) {
            listPreference.setValueIndex(0);
        }
    }

    private void restorePreferences() {
        CameraSettings.restorePreferences(this, this.mPreferences);
        initializeActivity();
        PriorityStorageBroadcastReceiver.setPriorityStorage(getResources().getBoolean(C0049R.bool.priority_storage));
        onSettingChanged(3);
    }

    private void updateConflictPreference(Preference preference) {
        if (Device.IS_X9 && !this.mPreferences.isFrontCamera() && CameraSettings.isMovieSolidOn(this.mPreferences) && 6 <= CameraSettings.getPreferVideoQuality()) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) this.mPreferenceGroup.findPreference("pref_camera_movie_solid_key");
            PreviewListPreference previewListPreference = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_video_quality_key");
            Editor edit;
            if (preference == null || !"pref_camera_movie_solid_key".equals(preference.getKey())) {
                edit = this.mPreferences.edit();
                edit.putBoolean("pref_camera_movie_solid_key", false);
                edit.apply();
                checkBoxPreference.setChecked(false);
                return;
            }
            String string = getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_video_quality_default));
            edit = this.mPreferences.edit();
            edit.putString("pref_video_quality_key", string);
            edit.apply();
            previewListPreference.setValue(string);
        }
    }

    private void updateEntries() {
        PreviewListPreference previewListPreference = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_camera_picturesize_key");
        PreviewListPreference previewListPreference2 = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_camera_antibanding_key");
        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) this.mPreferenceGroup.findPreference("pref_auto_chroma_flash_key");
        PreviewListPreference previewListPreference3 = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_video_quality_key");
        PreviewListPreference previewListPreference4 = (PreviewListPreference) this.mPreferenceGroup.findPreference("pref_camera_snap_key");
        if (previewListPreference != null) {
            previewListPreference.setEntries(PictureSizeManager.getEntries());
            previewListPreference.setEntryValues(PictureSizeManager.getEntryValues());
            previewListPreference.setDefaultValue(PictureSizeManager.getDefaultValue());
            previewListPreference.setValue(PictureSizeManager.getDefaultValue());
        }
        if (previewListPreference2 != null && Util.isAntibanding60()) {
            previewListPreference2.setValue(getString(C0049R.string.pref_camera_antibanding_60));
            previewListPreference2.setDefaultValue(getString(C0049R.string.pref_camera_antibanding_60));
        }
        if (checkBoxPreference != null) {
            checkBoxPreference.setChecked(getResources().getBoolean(CameraSettings.getDefaultPreferenceId(C0049R.bool.pref_camera_auto_chroma_flash_default)));
        }
        if (previewListPreference3 != null) {
            String string = getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_video_quality_default));
            previewListPreference3.setDefaultValue(string);
            previewListPreference3.setValue(string);
        }
        if (previewListPreference4 != null && Device.isSupportedQuickSnap()) {
            String string2 = getString(C0049R.string.pref_camera_snap_default);
            previewListPreference4.setDefaultValue(string2);
            previewListPreference4.setValue(string2);
            String string3 = Secure.getString(getContentResolver(), "key_long_press_volume_down");
            if ("public_transportation_shortcuts".equals(string3) || "none".equals(string3)) {
                previewListPreference4.setValue(getString(C0049R.string.pref_camera_snap_value_off));
                return;
            }
            String string4 = CameraSettingPreferences.instance().getString("pref_camera_snap_key", null);
            if (string4 != null) {
                Secure.putString(getContentResolver(), "key_long_press_volume_down", CameraSettings.getMiuiSettingsKeyForStreetSnap(string4));
                CameraSettingPreferences.instance().edit().remove("pref_camera_snap_key").apply();
                previewListPreference4.setValue(string4);
            } else if ("Street-snap-picture".equals(string3)) {
                previewListPreference4.setValue(getString(C0049R.string.pref_camera_snap_value_take_picture));
            } else if ("Street-snap-movie".equals(string3)) {
                previewListPreference4.setValue(getString(C0049R.string.pref_camera_snap_value_take_movie));
            }
        }
    }

    private void updatePreferences(PreferenceGroup preferenceGroup, SharedPreferences sharedPreferences) {
        if (preferenceGroup != null) {
            int preferenceCount = preferenceGroup.getPreferenceCount();
            for (int i = 0; i < preferenceCount; i++) {
                Preference preference = preferenceGroup.getPreference(i);
                if (preference instanceof PreviewListPreference) {
                    PreviewListPreference previewListPreference = (PreviewListPreference) preference;
                    if ("pref_camera_picturesize_key".equals(previewListPreference.getKey())) {
                        previewListPreference.setValue(PictureSizeManager.getPictureSize(true).toString());
                    } else {
                        previewListPreference.setValue(getFilterValue(previewListPreference, sharedPreferences));
                    }
                    preference.setPersistent(false);
                } else if (preference instanceof CheckBoxPreference) {
                    CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                    checkBoxPreference.setChecked(sharedPreferences.getBoolean(checkBoxPreference.getKey(), checkBoxPreference.isChecked()));
                    preference.setPersistent(false);
                } else if (preference instanceof PreferenceGroup) {
                    updatePreferences((PreferenceGroup) preference, sharedPreferences);
                } else {
                    Log.v(TAG, "no need update preference for " + preference.getKey());
                }
            }
        }
    }

    protected boolean addPreference(String str, Preference preference) {
        Preference findPreference = this.mPreferenceGroup.findPreference(str);
        if (!(findPreference instanceof PreferenceGroup)) {
            return false;
        }
        ((PreferenceGroup) findPreference).addPreference(preference);
        return true;
    }

    protected void filterByIntent() {
        Iterable<String> stringArrayListExtra = getIntent().getStringArrayListExtra("remove_keys");
        if (stringArrayListExtra != null) {
            for (String removePreference : stringArrayListExtra) {
                removePreference(this.mPreferenceGroup, removePreference);
            }
        }
    }

    public void filterUnsupportedOptions(PreferenceGroup preferenceGroup, PreviewListPreference previewListPreference, List<String> list) {
        if (list == null || list.size() <= 1) {
            removePreference(preferenceGroup, previewListPreference.getKey());
            return;
        }
        previewListPreference.filterUnsupported(list);
        if (previewListPreference.getEntries().length <= 1) {
            removePreference(preferenceGroup, previewListPreference.getKey());
        } else {
            resetIfInvalid(previewListPreference);
        }
    }

    protected abstract int getPreferenceXml();

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Util.updateCountryIso(this);
        this.mFromWhere = getIntent().getIntExtra("from_where", 0);
        this.mPreferences = CameraSettingPreferences.instance();
        CameraSettings.upgradeGlobalPreferences(this.mPreferences);
        Storage.initStorage(this);
        initializeActivity();
        if (getIntent().getBooleanExtra("StartActivityWhenLocked", false)) {
            getWindow().addFlags(524288);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        onSettingChanged(1);
        Editor edit = this.mPreferences.edit();
        String key = preference.getKey();
        if (obj instanceof String) {
            edit.putString(key, (String) obj);
        } else if (obj instanceof Boolean) {
            edit.putBoolean(key, ((Boolean) obj).booleanValue());
        } else if (obj instanceof Integer) {
            edit.putInt(key, ((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            edit.putLong(key, ((Long) obj).longValue());
        } else if (obj instanceof Float) {
            edit.putFloat(key, ((Float) obj).floatValue());
        } else {
            throw new IllegalStateException("unhandled new value with type=" + obj.getClass().getName());
        }
        edit.apply();
        updateConflictPreference(preference);
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_restore")) {
            RotateDialogController.showSystemAlertDialog(this, getString(C0049R.string.confirm_restore_title), getString(C0049R.string.confirm_restore_message), getString(17039370), new C00791(), getString(17039360), null);
            return true;
        }
        if ("pref_priority_storage".equals(preference.getKey())) {
            PriorityStorageBroadcastReceiver.setPriorityStorage(((CheckBoxPreference) preference).isChecked());
        } else if ("pref_camera_facedetection_key".equals(preference.getKey()) && CameraSettings.isSupportedPortrait() && CameraSettings.isBackCamera() && V6ModulePicker.isCameraModule() && this.mFaceDetectionHitCountDown > 0) {
            this.mFaceDetectionHitCountDown--;
            if (this.mFaceDetectionHitCountDown == 0) {
                Toast.makeText(this, C0049R.string.portrait_with_facebeauty_hint, 1).show();
                CameraSettings.setCameraPortraitWithFaceBeautyOptionVisible(true);
                addPreference("category_advance_setting", this.mPortraitWithFaceBeautyPreference);
            }
        }
        return false;
    }

    public void onResume() {
        super.onResume();
        if (CameraSettings.isCameraPortraitWithFaceBeautyOptionVisible()) {
            this.mFaceDetectionHitCountDown = -1;
        } else {
            this.mFaceDetectionHitCountDown = 8;
        }
    }

    protected abstract void onSettingChanged(int i);

    protected void onStop() {
        super.onStop();
        finish();
    }

    protected boolean removePreference(PreferenceGroup preferenceGroup, String str) {
        Preference findPreference = preferenceGroup.findPreference(str);
        if (findPreference != null && preferenceGroup.removePreference(findPreference)) {
            return true;
        }
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            findPreference = preferenceGroup.getPreference(i);
            if ((findPreference instanceof PreferenceGroup) && removePreference((PreferenceGroup) findPreference, str)) {
                return true;
            }
        }
        return false;
    }
}

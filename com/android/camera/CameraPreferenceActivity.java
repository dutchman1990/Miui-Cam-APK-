package com.android.camera;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.recyclerview.C0049R;
import android.widget.Toast;
import com.android.camera.ui.PreviewListPreference;

public class CameraPreferenceActivity extends BasePreferenceActivity {
    private AlertDialog mDoubleConfirmActionChooseDialog = null;

    private void bringUpDoubleConfirmDlg(final PreviewListPreference previewListPreference, final String str) {
        if (this.mDoubleConfirmActionChooseDialog == null) {
            OnClickListener c00821 = new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == -1) {
                        CameraPreferenceActivity.this.mDoubleConfirmActionChooseDialog = null;
                        previewListPreference.setValue(str);
                        Secure.putString(CameraPreferenceActivity.this.getContentResolver(), "key_long_press_volume_down", CameraSettings.getMiuiSettingsKeyForStreetSnap(str));
                    } else if (i == -2) {
                        CameraPreferenceActivity.this.mDoubleConfirmActionChooseDialog.dismiss();
                        CameraPreferenceActivity.this.mDoubleConfirmActionChooseDialog = null;
                    }
                }
            };
            this.mDoubleConfirmActionChooseDialog = new Builder(this).setTitle(C0049R.string.title_snap_double_confirm).setMessage(C0049R.string.message_snap_double_confirm).setPositiveButton(C0049R.string.snap_confirmed, c00821).setNegativeButton(C0049R.string.snap_cancel, c00821).setCancelable(false).create();
            this.mDoubleConfirmActionChooseDialog.show();
        }
    }

    protected void filterByIntent() {
        super.filterByIntent();
        if (getIntent().getBooleanExtra("IsCaptureIntent", false)) {
            removePreference(this.mPreferenceGroup, "pref_capture_when_stable_key");
            removePreference(this.mPreferenceGroup, "pref_groupshot_with_primitive_picture_key");
        }
    }

    protected int getPreferenceXml() {
        return C0049R.xml.camera_other_preferences;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent().getCharSequenceExtra(":miui:starting_window_label") != null) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(C0049R.string.pref_camera_settings_category);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (!preference.getKey().equals("pref_camera_snap_key") || obj == null) {
            return super.onPreferenceChange(preference, obj);
        }
        if (System.getInt(getContentResolver(), "volumekey_wake_screen", 0) == 1) {
            Toast.makeText(this, C0049R.string.pref_camera_snap_toast_when_volume_can_wake_screen, 0).show();
            return false;
        } else if ((obj.equals(getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_camera_snap_value_take_picture))) || obj.equals(getString(CameraSettings.getDefaultPreferenceId(C0049R.string.pref_camera_snap_value_take_movie)))) && "public_transportation_shortcuts".equals(Secure.getString(getContentResolver(), "key_long_press_volume_down"))) {
            bringUpDoubleConfirmDlg((PreviewListPreference) preference, (String) obj);
            return false;
        } else {
            Secure.putString(getContentResolver(), "key_long_press_volume_down", CameraSettings.getMiuiSettingsKeyForStreetSnap((String) obj));
            return true;
        }
    }

    protected void onSettingChanged(int i) {
        CameraSettings.sCameraChangeManager.request(i);
    }
}

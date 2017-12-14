package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraHolder;
import com.android.camera.preferences.ListPreference;
import com.android.camera.preferences.PreferenceInflater;
import java.util.ArrayList;

public class V6CameraPicker extends AnimationImageView implements OnClickListener {
    private static final String TAG = V6CameraPicker.class.getSimpleName();
    private int mCameraFacing;
    private boolean mEnabled;
    private boolean mInitEntryValues;
    private ListPreference mPreference = ((ListPreference) new PreferenceInflater(this.mContext).inflate((int) C0049R.xml.v6_camera_picker_preferences));
    private boolean mVisible = true;

    public V6CameraPicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    private void initEntryValues() {
        if (!this.mInitEntryValues) {
            ArrayList arrayList = new ArrayList(2);
            if (CameraHolder.instance().getBackCameraId() != -1) {
                arrayList.add(Math.min(0, arrayList.size()), String.valueOf(CameraHolder.instance().getBackCameraId()));
            }
            if (CameraHolder.instance().getFrontCameraId() != -1) {
                arrayList.add(Math.min(1, arrayList.size()), String.valueOf(CameraHolder.instance().getFrontCameraId()));
            }
            this.mPreference.setEntryValues((CharSequence[]) arrayList.toArray(new CharSequence[arrayList.size()]));
            this.mInitEntryValues = true;
        }
    }

    private boolean isNeedShow() {
        return (this.mPreference == null || this.mPreference.getEntryValues() == null) ? false : this.mPreference.getEntryValues().length > 1;
    }

    private void reloadPreference() {
        if (isNeedShow()) {
            if (TextUtils.equals(this.mPreference.getEntryValues()[1], this.mPreference.getValue())) {
                this.mCameraFacing = 1;
            } else {
                this.mCameraFacing = 0;
            }
        }
    }

    private void updateVisible() {
        if (isNeedShow()) {
            this.mVisible = true;
            setVisibility(0);
            return;
        }
        this.mVisible = false;
        setVisibility(8);
    }

    public void enableControls(boolean z) {
        setEnabled(z);
        this.mEnabled = z;
    }

    public void onCameraOpen() {
        super.onCameraOpen();
        initEntryValues();
        updateVisible();
        reloadPreference();
    }

    public void onClick(View view) {
        if (this.mVisible && this.mEnabled) {
            Log.v(TAG, "click switch camera button");
            int i = this.mCameraFacing == 0 ? 1 : 0;
            int i2 = this.mCameraFacing;
            this.mCameraFacing = i;
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
            if (!this.mMessageDispacher.dispacherMessage(0, C0049R.id.v6_camera_picker, 2, Integer.valueOf(Integer.parseInt((String) this.mPreference.getEntryValues()[this.mCameraFacing])), null)) {
                this.mCameraFacing = i2;
            }
        }
    }

    public void onResume() {
        super.onResume();
        setVisibility(0);
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }
}

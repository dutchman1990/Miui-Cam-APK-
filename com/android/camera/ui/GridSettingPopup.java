package com.android.camera.ui;

import android.content.Context;
import android.media.AudioSystem;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridSettingPopup extends V6AbstractSettingPopup implements OnClickListener {
    private final String TAG = "GridSettingPopup";
    protected int mCurrentIndex = -1;
    protected int mDisplayColumnNum = 5;
    protected GridView mGridView;
    protected int mGridViewHeight;
    protected boolean mHasImage = true;
    protected boolean mIgnoreSameItemClick = true;

    protected class MySimpleAdapter extends SimpleAdapter {
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> list, int i, String[] strArr, int[] iArr) {
            super(context, list, i, strArr, iArr);
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = super.getView(i, view, viewGroup);
            view2.setOnClickListener(GridSettingPopup.this);
            view2.setTag(new Integer(i));
            if (view2 instanceof Rotatable) {
                ((Rotatable) view2).setOrientation(0, false);
            }
            GridSettingPopup.this.updateItemView(i, view2);
            return view2;
        }
    }

    public GridSettingPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected int getItemResId() {
        return C0049R.layout.grid_setting_item;
    }

    protected void initGridViewLayoutParam(int i) {
        this.mGridView.setLayoutParams(new LayoutParams(i * ((int) (((float) Util.sWindowWidth) / (i == this.mDisplayColumnNum ? (float) this.mDisplayColumnNum : ((float) this.mDisplayColumnNum) + 0.5f))), this.mGridViewHeight));
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        String[] strArr;
        int[] iArr;
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        Context context = getContext();
        CharSequence[] entries = this.mPreference.getEntries();
        int[] imageIds = this.mPreference.getImageIds();
        if (imageIds == null) {
            imageIds = this.mPreference.getIconIds();
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < entries.length; i++) {
            HashMap hashMap = new HashMap();
            hashMap.put("text", entries[i].toString());
            if (imageIds != null) {
                hashMap.put("image", Integer.valueOf(imageIds[i]));
            }
            arrayList.add(hashMap);
        }
        if (imageIds == null || !this.mHasImage) {
            strArr = new String[]{"text"};
            iArr = new int[]{C0049R.id.text};
        } else {
            strArr = new String[]{"image", "text"};
            iArr = new int[]{C0049R.id.image, C0049R.id.text};
        }
        ListAdapter mySimpleAdapter = new MySimpleAdapter(context, arrayList, getItemResId(), strArr, iArr);
        this.mDisplayColumnNum = arrayList.size() < 5 ? arrayList.size() : 5;
        this.mGridView.setAdapter(mySimpleAdapter);
        this.mGridView.setNumColumns(entries.length);
        this.mGridView.setChoiceMode(1);
        initGridViewLayoutParam(entries.length);
        reloadPreference();
    }

    protected void notifyToDispatcher(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(6, 0, 3, this.mPreference.getKey(), this);
        }
    }

    public void onClick(View view) {
        if (this.mGridView.isEnabled()) {
            int intValue = ((Integer) view.getTag()).intValue();
            if (this.mCurrentIndex != intValue || !this.mIgnoreSameItemClick) {
                boolean z = this.mCurrentIndex == intValue;
                this.mCurrentIndex = intValue;
                this.mGridView.setItemChecked(intValue, true);
                this.mPreference.setValueIndex(intValue);
                if ("pref_camera_scenemode_key".equals(this.mPreference.getKey())) {
                    CameraSettings.setFocusModeSwitching(true);
                } else if ("pref_audio_focus_key".equals(this.mPreference.getKey()) && ((ActivityBase) this.mContext).getCurrentModule().isVideoRecording()) {
                    AudioSystem.setParameters("camcorder_mode=" + this.mPreference.getValue());
                }
                notifyToDispatcher(z);
                AutoLockManager.getInstance(this.mContext).onUserInteraction();
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGridView = (GridView) findViewById(C0049R.id.settings_grid);
        this.mGridViewHeight = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.manual_popup_layout_height);
    }

    public void reloadPreference() {
        this.mCurrentIndex = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        if (this.mCurrentIndex != -1) {
            this.mGridView.setItemChecked(this.mCurrentIndex, true);
            return;
        }
        Log.e("GridSettingPopup", "Invalid preference value.");
        this.mPreference.print();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mGridView != null) {
            this.mGridView.setEnabled(z);
        }
    }

    public void setOrientation(int i, boolean z) {
    }

    public void show(boolean z) {
        super.show(z);
        if ("pref_camera_scenemode_key".equals(this.mPreference.getKey()) && !"auto".equals(this.mPreference.getValue())) {
            CameraSettings.setFocusModeSwitching(true);
        }
    }

    protected void updateItemView(int i, View view) {
    }
}

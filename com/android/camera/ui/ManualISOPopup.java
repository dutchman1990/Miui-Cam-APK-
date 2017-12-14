package com.android.camera.ui;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.camera.CameraManager;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.List;

public class ManualISOPopup extends V6AbstractSettingPopup implements OnItemClickListener {
    private static final String TAG = ManualISOPopup.class.getSimpleName();
    private int mCurrentIndex = -1;
    private int mItemHeight;
    private int mItemWidth;
    private HorizontalListView mListView;

    class HorizontalListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private class ViewHolder {
            private TextView mTitle;

            private ViewHolder() {
            }
        }

        public HorizontalListViewAdapter() {
            this.mInflater = (LayoutInflater) ManualISOPopup.this.getContext().getSystemService("layout_inflater");
        }

        public int getCount() {
            return ManualISOPopup.this.mPreference.getEntries().length;
        }

        public Object getItem(int i) {
            return ManualISOPopup.this.mPreference.getEntries()[i];
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = this.mInflater.inflate(C0049R.layout.horizontal_list_text_item, null);
                TextView textView = (TextView) view.findViewById(C0049R.id.text_item_title);
                viewHolder.mTitle = textView;
                textView.setWidth(ManualISOPopup.this.mItemWidth);
                textView.setHeight(ManualISOPopup.this.mItemHeight);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.mTitle.setText(ManualISOPopup.this.mPreference.getEntries()[i]);
            Util.setNumberText(viewHolder.mTitle, (String) getItem(i));
            return view;
        }
    }

    public ManualISOPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void notifyToDispatcher(boolean z, boolean z2) {
        if (this.mMessageDispacher == null) {
            return;
        }
        if (!z || !z2) {
            this.mMessageDispacher.dispacherMessage(7, 0, 2, this.mPreference.getKey(), Boolean.valueOf(z2));
        }
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        Parameters stashParameters = CameraManager.instance().getStashParameters();
        if (!(Device.isNvPlatform() || stashParameters == null)) {
            List supportedIsoValues = CameraHardwareProxy.getDeviceProxy().getSupportedIsoValues(stashParameters);
            if (supportedIsoValues != null) {
                this.mPreference.filterUnsupported(supportedIsoValues);
            }
        }
        this.mPreference.filterValue();
        this.mListView.setAdapter(new HorizontalListViewAdapter());
        this.mListView.setItemWidth(this.mItemWidth);
        reloadPreference();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mListView = (HorizontalListView) findViewById(C0049R.id.horizon_listview);
        this.mItemWidth = getResources().getDimensionPixelSize(C0049R.dimen.iso_item_width);
        this.mItemHeight = getResources().getDimensionPixelSize(C0049R.dimen.manual_popup_layout_height);
        this.mListView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        boolean z = this.mCurrentIndex == i;
        this.mPreference.setValueIndex(i);
        this.mCurrentIndex = i;
        notifyToDispatcher(z, this.mListView.isScrolling());
    }

    public void reloadPreference() {
        this.mCurrentIndex = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        if (this.mCurrentIndex != -1) {
            this.mListView.setSelection(this.mCurrentIndex);
            return;
        }
        Log.m2e(TAG, "Invalid preference value.");
        this.mPreference.print();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mListView.setEnabled(z);
    }

    public void setOrientation(int i, boolean z) {
    }
}

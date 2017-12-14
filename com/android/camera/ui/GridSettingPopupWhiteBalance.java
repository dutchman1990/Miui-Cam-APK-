package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraManager;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraSettings;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class GridSettingPopupWhiteBalance extends GridSettingPopup implements OnItemClickListener {
    private static String sWhiteBalanceManual;
    private static String sWhiteBalanceMeasure;
    private View mContentView;
    private int mCurrentKValue = -1;
    private int mItemHeight;
    private int mItemWidth;
    private NumericListAdapter mKItemAdapter;
    private HorizontalListView mListView;
    private OnClickListener mOnBackListener = new C01321();

    class C01321 implements OnClickListener {
        C01321() {
        }

        public void onClick(View view) {
            if (GridSettingPopupWhiteBalance.this.mListView.getVisibility() == 0) {
                GridSettingPopupWhiteBalance.this.reloadPreference();
                GridSettingPopupWhiteBalance.this.mListView.setVisibility(8);
                GridSettingPopupWhiteBalance.this.mGridView.setVisibility(0);
            }
        }
    }

    class HorizontalListViewAdapter extends BaseAdapter {
        private CharSequence[] mEntries;
        private LayoutInflater mInflater;
        private NumericListAdapter mNumAdapter;

        private class ViewHolder {
            private TextView mTitle;

            private ViewHolder() {
            }
        }

        public HorizontalListViewAdapter(NumericListAdapter numericListAdapter) {
            this.mInflater = (LayoutInflater) GridSettingPopupWhiteBalance.this.getContext().getSystemService("layout_inflater");
            this.mNumAdapter = numericListAdapter;
        }

        public int getCount() {
            return this.mEntries != null ? this.mEntries.length : this.mNumAdapter != null ? this.mNumAdapter.getItemsCount() : 0;
        }

        public Object getItem(int i) {
            return this.mEntries != null ? this.mEntries[i] : this.mNumAdapter != null ? this.mNumAdapter.getItem(i) : null;
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
                textView.setWidth(GridSettingPopupWhiteBalance.this.mItemWidth);
                textView.setHeight(GridSettingPopupWhiteBalance.this.mItemHeight);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.mTitle.setText((CharSequence) getItem(i));
            return view;
        }
    }

    public GridSettingPopupWhiteBalance(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (sWhiteBalanceManual == null || "".equals(sWhiteBalanceManual)) {
            sWhiteBalanceManual = this.mContext.getString(C0049R.string.pref_camera_whitebalance_entryvalue_manual);
            sWhiteBalanceMeasure = this.mContext.getString(C0049R.string.pref_camera_whitebalance_entryvalue_measure);
        }
    }

    private void notifyToDispatcher(boolean z, boolean z2) {
        if (this.mMessageDispacher == null) {
            return;
        }
        if (!z || !z2) {
            this.mMessageDispacher.dispacherMessage(7, 0, 3, "pref_qc_manual_whitebalance_k_value_key", Boolean.valueOf(z2));
        }
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        this.mKItemAdapter = new NumericListAdapter(2000, 8000, 100);
        this.mListView.setAdapter(new HorizontalListViewAdapter(this.mKItemAdapter));
        this.mListView.setItemWidth(this.mItemWidth);
    }

    public void onClick(View view) {
        if (this.mGridView.isEnabled()) {
            int intValue = ((Integer) view.getTag()).intValue();
            this.mGridView.setItemChecked(intValue, true);
            this.mPreference.setValueIndex(intValue);
            Object obj = this.mCurrentIndex != intValue ? 1 : null;
            if (sWhiteBalanceManual.equals(this.mPreference.getValue())) {
                this.mCurrentKValue = CameraSettings.getKValue();
                try {
                    CameraProxy cameraProxy = CameraManager.instance().getCameraProxy();
                    if (cameraProxy != null) {
                        int wbct = cameraProxy.getWBCT();
                        if (wbct != 0) {
                            this.mCurrentKValue = wbct;
                            Log.v("Camera", " Current WB CCT = " + this.mCurrentKValue);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Camera", "Can't get current WB CCT");
                }
                int itemIndexByValue = this.mKItemAdapter.getItemIndexByValue(Integer.valueOf(this.mCurrentKValue));
                if (itemIndexByValue != -1) {
                    this.mListView.setSelection(itemIndexByValue);
                    this.mCurrentKValue = this.mKItemAdapter.getItemValue(itemIndexByValue);
                }
                this.mListView.setVisibility(0);
                this.mGridView.setVisibility(4);
                CameraSettings.setKValue(this.mCurrentKValue);
                CameraDataAnalytics.instance().trackEvent("manual_whitebalance_key");
            } else if (sWhiteBalanceMeasure.equals(this.mPreference.getValue())) {
                this.mListView.setVisibility(8);
                this.mGridView.setVisibility(0);
                this.mCurrentIndex = intValue;
                return;
            } else {
                this.mListView.setVisibility(8);
                this.mGridView.setVisibility(0);
            }
            if (obj != null) {
                this.mCurrentIndex = intValue;
                if (this.mMessageDispacher != null) {
                    this.mMessageDispacher.dispacherMessage(6, 0, 3, this.mPreference.getKey(), this);
                }
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mListView = (HorizontalListView) findViewById(C0049R.id.horizon_listview);
        this.mItemWidth = getResources().getDimensionPixelSize(C0049R.dimen.whitebalance_item_width);
        this.mItemHeight = getResources().getDimensionPixelSize(C0049R.dimen.manual_popup_layout_height);
        this.mListView.setOnItemClickListener(this);
        this.mContentView = findViewById(C0049R.id.content_layout);
        this.mContentView.setOnClickListener(this.mOnBackListener);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        int itemValue = this.mKItemAdapter.getItemValue(i);
        boolean z = itemValue == this.mCurrentKValue;
        this.mCurrentKValue = itemValue;
        CameraSettings.setKValue(this.mCurrentKValue);
        notifyToDispatcher(z, this.mListView.isScrolling());
        this.mListView.setSelection(i);
        AutoLockManager.getInstance(this.mContext).onUserInteraction();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mListView != null) {
            this.mListView.setEnabled(z);
        }
    }

    public void show(boolean z) {
        super.show(z);
        if (this.mListView.getVisibility() == 0) {
            this.mListView.setVisibility(8);
            this.mGridView.setVisibility(0);
        }
    }
}

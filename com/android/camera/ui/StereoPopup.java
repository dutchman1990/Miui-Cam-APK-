package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.camera.Log;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class StereoPopup extends V6AbstractSettingPopup implements OnItemClickListener {
    private static final String TAG = StereoPopup.class.getSimpleName();
    private int mCurrentIndex = -1;
    private Animation mHidenAnimation;
    private int mItemHeight;
    private int mItemWidth;
    private HorizontalListView mListView;
    private Animation mShownAnimation;

    private class AnimationListener extends SimpleAnimationListener {
        public AnimationListener(View view, boolean z) {
            super(view, z);
        }

        public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            StereoPopup.this.getParent().requestLayout();
            int i = 0;
            if (StereoPopup.this.mShownAnimation == animation) {
                i = 11;
            } else if (StereoPopup.this.mHidenAnimation == animation) {
                i = 12;
            }
            if (StereoPopup.this.mMessageDispacher != null && i > 0) {
                StereoPopup.this.mMessageDispacher.dispacherMessage(i, 0, 0, null, null);
            }
        }
    }

    class HorizontalListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private class ViewHolder {
            private TextView mTitle;

            private ViewHolder() {
            }
        }

        public HorizontalListViewAdapter() {
            this.mInflater = (LayoutInflater) StereoPopup.this.getContext().getSystemService("layout_inflater");
        }

        public int getCount() {
            return StereoPopup.this.mPreference.getEntries().length;
        }

        public Object getItem(int i) {
            return StereoPopup.this.mPreference.getEntries()[i];
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
                textView.setWidth(StereoPopup.this.mItemWidth);
                textView.setHeight(StereoPopup.this.mItemHeight);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.mTitle.setText(StereoPopup.this.mPreference.getEntries()[i]);
            return view;
        }
    }

    public StereoPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private Animation initAnimation(boolean z) {
        if (z) {
            if (this.mShownAnimation == null) {
                this.mShownAnimation = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.slide_up);
                this.mShownAnimation.setAnimationListener(new AnimationListener(this, true));
            }
            return this.mShownAnimation;
        }
        if (this.mHidenAnimation == null) {
            this.mHidenAnimation = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.slide_down);
            this.mHidenAnimation.setAnimationListener(new AnimationListener(this, false));
        }
        return this.mHidenAnimation;
    }

    private void notifyToDispatcher(boolean z, boolean z2) {
        if (this.mMessageDispacher == null) {
            return;
        }
        if (!z || !z2) {
            this.mMessageDispacher.dispacherMessage(7, 0, 2, this.mPreference.getKey(), Boolean.valueOf(z2));
            reloadPreference();
        }
    }

    public void dismiss(boolean z) {
        clearAnimation();
        if (z) {
            startAnimation(initAnimation(false));
        } else {
            setVisibility(8);
        }
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, (IconListPreference) preferenceGroup.findPreference("pref_camera_stereo_key"), messageDispacher);
        this.mPreference.filterValue();
        this.mListView.setAdapter(new HorizontalListViewAdapter());
        this.mListView.setItemWidth(this.mItemWidth);
        reloadPreference();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mListView = (HorizontalListView) findViewById(C0049R.id.horizon_listview);
        this.mItemWidth = getResources().getDimensionPixelSize(C0049R.dimen.stereo_item_width);
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

    public void show(boolean z) {
        clearAnimation();
        setVisibility(0);
        if (z) {
            startAnimation(initAnimation(true));
        }
    }
}

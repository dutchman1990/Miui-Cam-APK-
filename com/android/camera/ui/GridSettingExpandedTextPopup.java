package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;

public class GridSettingExpandedTextPopup extends GridSettingPopup {
    private int mLeftMargin;
    private int mRightMargin;

    public GridSettingExpandedTextPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIgnoreSameItemClick = false;
    }

    private Animation initAnimation(boolean z) {
        Animation animation = getAnimation(z);
        animation.setAnimationListener(new SimpleAnimationListener(this, z));
        return animation;
    }

    public void dismiss(boolean z) {
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(false));
        } else {
            setVisibility(8);
        }
        notifyPopupVisibleChange(false);
    }

    public Animation getAnimation(boolean z) {
        if (this.mLeftMargin != 0) {
            return AnimationUtils.loadAnimation(this.mContext, z ? C0049R.anim.expand_right : C0049R.anim.shrink_left);
        }
        return AnimationUtils.loadAnimation(this.mContext, z ? C0049R.anim.expand_left : C0049R.anim.shrink_right);
    }

    protected int getItemResId() {
        return C0049R.layout.grid_setting_expanded_text_item;
    }

    protected void initGridViewLayoutParam(int i) {
        LayoutParams layoutParams = (LayoutParams) this.mGridView.getLayoutParams();
        layoutParams.width = i * getResources().getDimensionPixelSize(C0049R.dimen.expanded_text_item_width);
        layoutParams.leftMargin = this.mLeftMargin;
        layoutParams.rightMargin = this.mRightMargin;
        if ("pref_camera_hdr_key".equals(this.mPreference.getKey()) || "pref_camera_face_beauty_switch_key".equals(this.mPreference.getKey())) {
            layoutParams.addRule(11, -1);
        } else {
            layoutParams.addRule(9, -1);
        }
        this.mGridView.setLayoutParams(layoutParams);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        this.mHasImage = false;
        this.mIgnoreSameItemClick = false;
        if ("pref_camera_flashmode_key".equals(iconListPreference.getKey())) {
            this.mLeftMargin = ((ActivityBase) this.mContext).getUIController().getFlashButton().getWidth();
            this.mRightMargin = 0;
        } else if ("pref_camera_hdr_key".equals(iconListPreference.getKey())) {
            this.mRightMargin = ((ActivityBase) this.mContext).getUIController().getHdrButton().getWidth();
            this.mLeftMargin = 0;
        } else if ("pref_camera_face_beauty_switch_key".equals(iconListPreference.getKey())) {
            this.mRightMargin = ((ActivityBase) this.mContext).getUIController().getSkinBeautyButton().getWidth();
            this.mLeftMargin = 0;
        }
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
    }

    protected void notifyToDispatcher(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(6, 0, 3, this.mPreference.getKey(), Boolean.valueOf(z));
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGridViewHeight = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.expanded_text_popup_height);
    }

    public void show(boolean z) {
        setVisibility(0);
        if (z) {
            clearAnimation();
            startAnimation(initAnimation(true));
        }
        notifyPopupVisibleChange(true);
    }

    public void updateBackground() {
    }

    protected void updateItemView(int i, View view) {
        TextView textView = (TextView) view.findViewById(C0049R.id.text);
        if (textView == null) {
            return;
        }
        if (this.mCurrentIndex == i) {
            textView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
        } else {
            textView.setShadowLayer(4.0f, 0.0f, 0.0f, -1073741824);
        }
    }
}

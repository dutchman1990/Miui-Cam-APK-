package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.CameraAppImpl;
import com.android.camera.Log;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.ui.PopupManager.OnOtherPopupShowedListener;

public abstract class V6AbstractIndicator extends RelativeLayout implements Rotatable, OnOtherPopupShowedListener {
    public static final int TEXT_COLOR_DEFAULT = CameraAppImpl.getAndroidContext().getResources().getColor(C0049R.color.text_color_default);
    public static final int TEXT_COLOR_SELECTED = CameraAppImpl.getAndroidContext().getResources().getColor(C0049R.color.text_color_selected);
    protected TextView mContent;
    protected V6ModeExitView mExitView;
    protected TwoStateImageView mImage;
    protected MessageDispacher mMessageDispacher;
    protected int mOrientation;
    protected V6AbstractSettingPopup mPopup;
    protected ViewGroup mPopupRoot;
    protected IconListPreference mPreference;
    protected PreferenceGroup mPreferenceGroup;
    protected TextView mTitle;

    public V6AbstractIndicator(Context context) {
        super(context);
    }

    public V6AbstractIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean dismissPopup() {
        return false;
    }

    public String getKey() {
        return this.mPreference == null ? "" : this.mPreference.getKey();
    }

    public V6AbstractSettingPopup getPopup() {
        return this.mPopup;
    }

    protected int getShowedColor() {
        return isPressed() ? TEXT_COLOR_SELECTED : TEXT_COLOR_DEFAULT;
    }

    public void initialize(IconListPreference iconListPreference, MessageDispacher messageDispacher, ViewGroup viewGroup, int i, int i2, PreferenceGroup preferenceGroup) {
        this.mPreferenceGroup = preferenceGroup;
        this.mPreference = iconListPreference;
        this.mMessageDispacher = messageDispacher;
        this.mPopupRoot = viewGroup;
        Object obj = null;
        LayoutParams layoutParams = getLayoutParams();
        if (-10 != i) {
            layoutParams.width = i;
            obj = 1;
        }
        if (-10 != i2) {
            layoutParams.height = i2;
            obj = 1;
        }
        this.mExitView = ((ActivityBase) this.mContext).getUIController().getModeExitView();
        if (this.mImage != null) {
            this.mImage.setImageResource(this.mPreference.getSingleIcon());
        }
        setContentDescription(this.mPreference.getTitle());
        updateImage();
        updateTitle();
        updateContent();
        if (obj != null) {
            setLayoutParams(layoutParams);
            requestLayout();
        }
    }

    protected boolean isIndicatorSelected() {
        return false;
    }

    public void onDestroy() {
    }

    public void onDismiss() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mImage = (TwoStateImageView) findViewById(C0049R.id.image);
        this.mTitle = (TextView) findViewById(C0049R.id.text);
        this.mContent = (TextView) findViewById(C0049R.id.indicator_content);
        if (this.mImage != null) {
            this.mImage.enableFilter(true);
        }
    }

    public boolean onOtherPopupShowed(int i) {
        dismissPopup();
        return false;
    }

    public void recoverIfNeeded() {
        showPopup();
    }

    public void reloadPreference() {
    }

    public void setActivated(boolean z) {
        super.setActivated(z);
        if (this.mImage != null) {
            this.mImage.setActivated(z);
        }
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mPopup != null) {
            this.mPopup.setEnabled(z);
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        i = -i;
        int rotation = (int) getRotation();
        int i2 = (i >= 0 ? i % 360 : (i % 360) + 360) - (rotation >= 0 ? rotation % 360 : (rotation % 360) + 360);
        if (i2 == 0) {
            animate().cancel();
            return;
        }
        if (Math.abs(i2) > 180) {
            i2 = i2 >= 0 ? i2 - 360 : i2 + 360;
        }
        if (z) {
            animate().withLayer().rotation((float) (rotation + i2)).setDuration((long) ((Math.abs(i2) * 1000) / 270));
        } else {
            animate().withLayer().rotation((float) (rotation + i2)).setDuration(0);
        }
    }

    public void setPressed(boolean z) {
        super.setPressed(z);
        updateTitle();
    }

    public void showPopup() {
    }

    protected void updateContent() {
    }

    protected void updateImage() {
        Log.m5v("Camera5", "updateImage= " + this.mPreference.getSingleIcon() + " default=" + this.mPreference.isDefaultValue() + " value=" + this.mPreference.getValue() + " isIndicatorSelected=" + isIndicatorSelected() + " this=" + this.mPreference.getKey());
        if (this.mImage != null) {
            if (this.mPreference.getSingleIcon() != 0) {
                this.mImage.setVisibility(0);
                if (this.mPreference.getEnable()) {
                    this.mImage.setSelected(this.mPreference.isDefaultValue() ? isIndicatorSelected() : true);
                    this.mTitle.setEnabled(true);
                } else {
                    this.mImage.setEnabled(false);
                    this.mTitle.setEnabled(false);
                }
                this.mImage.setContentDescription(this.mPreference.getTitle());
            } else {
                this.mImage.setVisibility(8);
            }
        }
    }

    protected void updateTitle() {
        if (this.mTitle != null) {
            CharSequence title = this.mPreference.getTitle();
            this.mTitle.setText(title);
            if (!(this instanceof V6IndicatorButton)) {
                if (title != null) {
                    this.mTitle.setVisibility(0);
                    this.mTitle.setTextColor(getShowedColor());
                } else {
                    this.mTitle.setVisibility(8);
                }
            }
        }
    }
}

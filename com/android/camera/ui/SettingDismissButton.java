package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.AutoLockManager;

public class SettingDismissButton extends RotateImageView implements OnClickListener {
    private MessageDispacher mMessageDispacher;
    private boolean mVisible = true;

    public SettingDismissButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.mVisible) {
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
            this.mMessageDispacher.dispacherMessage(0, C0049R.id.dismiss_setting, 3, null, null);
        }
    }

    public void setMessageDispatcher(MessageDispacher messageDispacher) {
        this.mMessageDispacher = messageDispacher;
    }

    public void setVisibility(int i) {
        if (!this.mVisible) {
            i = 8;
        }
        super.setVisibility(i);
    }
}

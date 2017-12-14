package com.android.zxing.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.Util;

public class QRCodeFragmentLayout extends RelativeLayout {
    private boolean mDispatchTouchEvent = false;
    private Animation mFadeHide;
    private Animation mFadeShow;
    private TextView mViewFinderButton;

    class C01791 implements OnLayoutChangeListener {
        C01791() {
        }

        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            if (((ActivityBase) QRCodeFragmentLayout.this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
                QRCodeFragmentLayout.this.mViewFinderButton.setBackgroundResource(C0049R.drawable.btn_camera_mode_exit_full_screen);
            } else {
                QRCodeFragmentLayout.this.mViewFinderButton.setBackgroundResource(C0049R.drawable.btn_camera_mode_exit);
            }
            Util.expandViewTouchDelegate(QRCodeFragmentLayout.this.mViewFinderButton);
        }
    }

    public QRCodeFragmentLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return this.mDispatchTouchEvent ? true : super.dispatchTouchEvent(motionEvent);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFadeShow = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.qrcode_frament_layout_show);
        this.mFadeHide = AnimationUtils.loadAnimation(this.mContext, C0049R.anim.qrcode_frament_layout_hide);
        this.mViewFinderButton = (TextView) findViewById(C0049R.id.qrcode_viewfinder_button);
        this.mViewFinderButton.addOnLayoutChangeListener(new C01791());
    }
}

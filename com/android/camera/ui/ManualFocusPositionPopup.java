package com.android.camera.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.ui.HorizontalSlideView.HorizontalDrawAdapter;
import com.android.camera.ui.HorizontalSlideView.OnItemSelectListener;

public class ManualFocusPositionPopup extends V6AbstractSettingPopup implements OnItemSelectListener {
    private static final String TAG = ManualFocusPositionPopup.class.getSimpleName();
    private static final int[] sTextActivatedColorState = new int[]{16843518};
    private static final int[] sTextDefaultColorState = new int[]{0};
    private int mCurrentIndex = -1;
    private HorizontalSlideView mHorizontalSlideView;
    private int mLineColorDefault;
    private float mLineHalfHeight;
    private int mLineLineGap;
    private int mLineTextGap;
    private int mLineWidth;
    private ColorStateList mTextColor;
    private int mTextSize;

    class HorizontalSlideViewAdapter extends HorizontalDrawAdapter {
        private CharSequence[] mEntries;
        Paint mPaint = new Paint();

        public HorizontalSlideViewAdapter(CharSequence[] charSequenceArr) {
            this.mEntries = charSequenceArr;
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStrokeWidth((float) ManualFocusPositionPopup.this.mLineWidth);
            this.mPaint.setTextSize((float) ManualFocusPositionPopup.this.mTextSize);
            this.mPaint.setTextAlign(Align.LEFT);
        }

        private void drawText(int i, Canvas canvas) {
            canvas.drawText(Util.getLocalizedNumberString(this.mEntries[i].toString()), 0.0f, (-(this.mPaint.ascent() + this.mPaint.descent())) / 2.0f, this.mPaint);
        }

        public void draw(int i, Canvas canvas, boolean z) {
            this.mPaint.setColor(z ? -65536 : -1);
            if (i % 10 == 0) {
                this.mPaint.setColor(z ? ManualFocusPositionPopup.this.mTextColor.getColorForState(ManualFocusPositionPopup.sTextActivatedColorState, 0) : ManualFocusPositionPopup.this.mTextColor.getColorForState(ManualFocusPositionPopup.sTextDefaultColorState, 0));
                drawText(i / 10, canvas);
                return;
            }
            this.mPaint.setColor(z ? ManualFocusPositionPopup.this.mTextColor.getColorForState(ManualFocusPositionPopup.sTextActivatedColorState, 0) : ManualFocusPositionPopup.this.mLineColorDefault);
            canvas.drawLine(0.0f, -ManualFocusPositionPopup.this.mLineHalfHeight, 0.0f, ManualFocusPositionPopup.this.mLineHalfHeight, this.mPaint);
        }

        public Align getAlign(int i) {
            return Align.LEFT;
        }

        public int getCount() {
            return 101;
        }

        public float measureGap(int i) {
            return (i % 10 == 0 || (i + 1) % 10 == 0) ? (float) ManualFocusPositionPopup.this.mLineTextGap : (float) ManualFocusPositionPopup.this.mLineLineGap;
        }

        public float measureWidth(int i) {
            return i % 10 == 0 ? this.mPaint.measureText(this.mEntries[i / 10].toString()) : (float) ManualFocusPositionPopup.this.mLineWidth;
        }
    }

    public ManualFocusPositionPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(C0049R.style.SingeTextItemTextStyle, new int[]{16842901, 16842904});
        this.mTextSize = obtainStyledAttributes.getDimensionPixelSize(obtainStyledAttributes.getIndex(0), this.mTextSize);
        this.mTextColor = obtainStyledAttributes.getColorStateList(obtainStyledAttributes.getIndex(1));
        obtainStyledAttributes.recycle();
        Resources resources = context.getResources();
        this.mLineHalfHeight = ((float) resources.getDimensionPixelSize(C0049R.dimen.focus_line_height)) / 2.0f;
        this.mLineWidth = resources.getDimensionPixelSize(C0049R.dimen.focus_line_width);
        this.mLineLineGap = resources.getDimensionPixelSize(C0049R.dimen.focus_line_line_gap);
        this.mLineTextGap = resources.getDimensionPixelSize(C0049R.dimen.focus_line_text_gap);
        this.mLineColorDefault = resources.getColor(C0049R.color.manual_focus_line_color_default);
    }

    private String getDisplayedFocusValue(int i) {
        return i == 0 ? this.mContext.getString(C0049R.string.pref_camera_focusmode_entry_auto) : String.valueOf(i);
    }

    private int mapFocusToIndex(int i) {
        return 100 - (Util.clamp(i, 0, 1000) / 10);
    }

    private int mapIndexToFocus(int i) {
        return 1000 - ((i * 1000) / 100);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        CharSequence[] charSequenceArr = new CharSequence[11];
        for (int i = 0; i <= 10; i++) {
            charSequenceArr[i] = getDisplayedFocusValue(i * 10);
        }
        this.mHorizontalSlideView.setDrawAdapter(new HorizontalSlideViewAdapter(charSequenceArr));
        reloadPreference();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHorizontalSlideView = (HorizontalSlideView) findViewById(C0049R.id.horizon_slideview);
        this.mHorizontalSlideView.setOnItemSelectListener(this);
    }

    public void onItemSelect(HorizontalSlideView horizontalSlideView, int i) {
        CameraSettings.setFocusPosition(mapIndexToFocus(i));
        if (i != this.mCurrentIndex) {
            boolean z = this.mCurrentIndex == 0 || i == 0;
            this.mCurrentIndex = i;
            CameraSettings.setFocusModeSwitching(z);
            CameraSettings.setFocusMode(this.mCurrentIndex == 0 ? "continuous-picture" : "manual");
            if (isShown()) {
                ((ActivityBase) this.mContext).playCameraSound(6);
            }
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(7, 0, 0, z ? "pref_camera_focus_mode_key" : "pref_focus_position_key", null);
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
        }
    }

    public void reloadPreference() {
        this.mCurrentIndex = mapFocusToIndex(CameraSettings.getFocusPosition());
        this.mHorizontalSlideView.setSelection(this.mCurrentIndex);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mHorizontalSlideView.setEnabled(z);
    }

    public void setOrientation(int i, boolean z) {
    }
}

package com.android.camera.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.support.v7.recyclerview.C0049R;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Spline;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraManager;
import com.android.camera.CameraSettings;
import com.android.camera.Util;
import com.android.camera.module.BaseModule;
import com.android.camera.preferences.CameraSettingPreferences;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.camera.ui.HorizontalSlideView.HorizontalDrawAdapter;
import com.android.camera.ui.HorizontalSlideView.OnPositionSelectListener;

public class ZoomPopup extends V6AbstractSettingPopup implements OnPositionSelectListener {
    private static final String TAG = ZoomPopup.class.getSimpleName();
    private static final int[] sTextActivatedColorState = new int[]{16843518};
    private static final int[] sTextDefaultColorState = new int[]{0};
    private static float[] sX = new float[]{0.0f, 10.0f, 12.0f, 20.0f, 25.0f, 27.0f, 29.0f, 30.0f, 32.0f, 35.0f};
    private static float[] sY = new float[]{100.0f, 200.0f, 220.0f, 370.0f, 510.0f, 580.0f, 660.0f, 700.0f, 800.0f, 1000.0f};
    private float mCurrentPosition = -1.0f;
    private TextAppearanceSpan mDigitsTextStyle;
    private Spline mEntryToZoomRatioSpline;
    private HorizontalSlideView mHorizontalSlideView;
    private int mLineColorDefault;
    private float mLineHalfHeight;
    private int mLineLineGap;
    private int mLineTextGap;
    private int mLineWidth;
    private ColorStateList mTextColor;
    private int mTextSize;
    private TextAppearanceSpan mXTextStyle;
    private int mZoomMax;
    private int mZoomRatio;
    private int mZoomRatioMax;
    private int mZoomRatioMin = 100;
    private int mZoomRatioTele;
    private Spline mZoomRatioToEntrySpline;
    private int mZoomRatioWide;

    class HorizontalSlideViewAdapter extends HorizontalDrawAdapter {
        private CharSequence[] mEntries;
        private StaticLayout[] mEntryLayouts;
        Paint mPaint = new Paint();
        TextPaint mTextPaint;

        public HorizontalSlideViewAdapter(CharSequence[] charSequenceArr) {
            this.mEntries = charSequenceArr;
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStrokeWidth((float) ZoomPopup.this.mLineWidth);
            this.mPaint.setTextSize((float) ZoomPopup.this.mTextSize);
            this.mPaint.setTextAlign(Align.LEFT);
            this.mTextPaint = new TextPaint(this.mPaint);
            this.mEntryLayouts = new StaticLayout[this.mEntries.length];
            for (int i = 0; i < this.mEntries.length; i++) {
                this.mEntryLayouts[i] = new StaticLayout(this.mEntries[i], this.mTextPaint, Util.sWindowWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
        }

        private void drawText(int i, Canvas canvas) {
            float lineAscent = (float) (this.mEntryLayouts[i].getLineAscent(0) - this.mEntryLayouts[i].getLineDescent(0));
            canvas.save();
            canvas.translate(0.0f, lineAscent / 2.0f);
            this.mEntryLayouts[i].draw(canvas);
            canvas.restore();
        }

        private int indexToSection(int i) {
            return i == 0 ? 0 : i == 10 ? 1 : i == 47 ? 2 : -1;
        }

        public void draw(int i, Canvas canvas, boolean z) {
            if (i == 0 || i == 10 || i == 47) {
                this.mTextPaint.drawableState = z ? ZoomPopup.sTextActivatedColorState : ZoomPopup.sTextDefaultColorState;
                drawText(indexToSection(i), canvas);
                return;
            }
            this.mPaint.setColor(z ? ZoomPopup.this.mTextColor.getColorForState(ZoomPopup.sTextActivatedColorState, 0) : ZoomPopup.this.mLineColorDefault);
            canvas.drawLine(0.0f, -ZoomPopup.this.mLineHalfHeight, 0.0f, ZoomPopup.this.mLineHalfHeight, this.mPaint);
        }

        public Align getAlign(int i) {
            return Align.LEFT;
        }

        public int getCount() {
            return 48;
        }

        public float measureGap(int i) {
            return (i == 0 || i == 10 || i == 47) ? (float) ZoomPopup.this.mLineTextGap : (float) ZoomPopup.this.mLineLineGap;
        }

        public float measureWidth(int i) {
            return (i == 0 || i == 10 || i == 47) ? this.mEntryLayouts[indexToSection(i)].getLineWidth(0) : (float) ZoomPopup.this.mLineWidth;
        }
    }

    public ZoomPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(C0049R.style.SingeTextItemTextStyle, new int[]{16842901, 16842904});
        this.mTextSize = obtainStyledAttributes.getDimensionPixelSize(obtainStyledAttributes.getIndex(0), this.mTextSize);
        this.mTextColor = obtainStyledAttributes.getColorStateList(obtainStyledAttributes.getIndex(1));
        obtainStyledAttributes.recycle();
        Resources resources = context.getResources();
        this.mLineHalfHeight = ((float) resources.getDimensionPixelSize(C0049R.dimen.zoom_popup_line_height)) / 2.0f;
        this.mLineWidth = resources.getDimensionPixelSize(C0049R.dimen.zoom_popup_line_width);
        this.mLineLineGap = resources.getDimensionPixelSize(C0049R.dimen.zoom_popup_line_line_gap);
        this.mLineTextGap = resources.getDimensionPixelSize(C0049R.dimen.zoom_popup_line_text_gap);
        this.mLineColorDefault = resources.getColor(C0049R.color.zoom_popup_line_color_default);
        this.mDigitsTextStyle = new TextAppearanceSpan(context, C0049R.style.ZoomPopupDigitsTextStyle);
        this.mXTextStyle = new TextAppearanceSpan(context, C0049R.style.ZoomPopupXTextStyle);
        this.mZoomRatioWide = Integer.valueOf(context.getResources().getString(C0049R.string.pref_camera_zoom_ratio_wide)).intValue();
        this.mZoomRatioTele = Integer.valueOf(context.getResources().getString(C0049R.string.pref_camera_zoom_ratio_tele)).intValue();
    }

    private float[] convertSplineXToEntryX(float[] fArr) {
        int i = (int) ((fArr[fArr.length - 1] - 10.0f) + 1.0f);
        float[] fArr2 = new float[fArr.length];
        for (int i2 = 0; i2 < fArr.length; i2++) {
            if (fArr[i2] <= 10.0f) {
                fArr2[i2] = fArr[i2];
            } else {
                fArr2[i2] = (((fArr[i2] - 10.0f) / ((float) (i - 1))) * 37.0f) + 10.0f;
            }
        }
        return fArr2;
    }

    private float[] convertSplineYToZoomRatioY(float[] fArr) {
        int i = (int) fArr[fArr.length - 1];
        float[] fArr2 = new float[fArr.length];
        for (int i2 = 0; i2 < fArr.length; i2++) {
            if (fArr[i2] <= ((float) this.mZoomRatioTele)) {
                fArr2[i2] = fArr[i2];
            } else {
                fArr2[i2] = (((fArr[i2] - ((float) this.mZoomRatioTele)) / ((float) (i - this.mZoomRatioTele))) * ((float) (this.mZoomRatioMax - this.mZoomRatioTele))) + ((float) this.mZoomRatioTele);
            }
        }
        return fArr2;
    }

    private CharSequence getDisplayedZoomRatio(int i) {
        CharSequence spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(String.valueOf(i / 100), this.mDigitsTextStyle, 33);
        spannableStringBuilder.append("X", this.mXTextStyle, 33);
        return spannableStringBuilder;
    }

    private int mapPositionToZoomRatio(float f) {
        return Math.round(this.mEntryToZoomRatioSpline.interpolate(f));
    }

    private float mapZoomRatioToPosition(int i) {
        return this.mZoomRatioToEntrySpline.interpolate((float) i);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        BaseModule baseModule = (BaseModule) ((ActivityBase) this.mContext).getCurrentModule();
        this.mZoomMax = baseModule.getZoomMax();
        this.mZoomRatioMax = baseModule.getZoomMaxRatio();
        float[] convertSplineXToEntryX = convertSplineXToEntryX(sX);
        float[] convertSplineYToZoomRatioY = convertSplineYToZoomRatioY(sY);
        this.mEntryToZoomRatioSpline = Spline.createMonotoneCubicSpline(convertSplineXToEntryX, convertSplineYToZoomRatioY);
        this.mZoomRatioToEntrySpline = Spline.createMonotoneCubicSpline(convertSplineYToZoomRatioY, convertSplineXToEntryX);
        this.mHorizontalSlideView.setDrawAdapter(new HorizontalSlideViewAdapter(new CharSequence[]{getDisplayedZoomRatio(this.mZoomRatioWide), getDisplayedZoomRatio(this.mZoomRatioTele), getDisplayedZoomRatio(this.mZoomRatioMax)}));
        reloadPreference();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHorizontalSlideView = (HorizontalSlideView) findViewById(C0049R.id.horizon_slideview);
        this.mHorizontalSlideView.setOnPositionSelectListener(this);
        this.mHorizontalSlideView.setJustifyEnabled(false);
    }

    public void onPositionSelect(HorizontalSlideView horizontalSlideView, float f) {
        float f2 = f * 47.0f;
        if (f2 != this.mCurrentPosition) {
            this.mCurrentPosition = f2;
            if (isShown()) {
                ((ActivityBase) this.mContext).playCameraSound(6);
            }
            if (this.mMessageDispacher != null) {
                this.mMessageDispacher.dispacherMessage(7, 0, 0, getKey(), Integer.valueOf(mapPositionToZoomRatio(this.mCurrentPosition)));
            }
            AutoLockManager.getInstance(this.mContext).onUserInteraction();
        }
    }

    public void passTouchEvent(MotionEvent motionEvent) {
        this.mHorizontalSlideView.dispatchTouchEvent(motionEvent);
    }

    public void reloadPreference() {
        this.mZoomRatio = ((Integer) CameraManager.instance().getStashParameters().getZoomRatios().get(CameraSettings.readZoom(CameraSettingPreferences.instance()))).intValue();
        this.mCurrentPosition = mapZoomRatioToPosition(this.mZoomRatio);
        this.mHorizontalSlideView.setSelection(this.mCurrentPosition / 47.0f);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mHorizontalSlideView.setEnabled(z);
    }

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.mHorizontalSlideView.setOnTouchListener(onTouchListener);
    }

    public void setOrientation(int i, boolean z) {
    }

    public void updateBackground() {
        setBackgroundResource(C0049R.color.fullscreen_background);
    }
}

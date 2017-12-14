package com.android.camera.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.recyclerview.C0049R;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import com.android.camera.ActivityBase;
import com.android.camera.CameraAppImpl;
import com.android.camera.CameraScreenNail;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Util;
import com.android.camera.hardware.CameraHardwareProxy.CameraHardwareFace;

public class FaceView extends FrameView {
    private static Configuration configuration = CameraAppImpl.getAndroidContext().getResources().getConfiguration();
    private final boolean LOGV = true;
    private int mAgeFemaleHonPadding;
    private int mAgeMaleHonPadding;
    private String[] mAgeOnlyRangeAlias = CameraAppImpl.getAndroidContext().getResources().getStringArray(C0049R.array.pref_camera_show_age_reports);
    private String[] mAgeRangeAlias = CameraAppImpl.getAndroidContext().getResources().getStringArray(C0049R.array.pref_camera_show_gender_age_reports);
    private int mAgeVerPadding;
    private Drawable mBeautyScoreIc;
    private Drawable mBeautyScoreSurmounted;
    private Drawable mBeautyScoreWinner;
    private int mDisplayOrientation;
    private Drawable mFaceIndicator;
    private String mFaceInfoFormat = CameraAppImpl.getAndroidContext().getString(C0049R.string.face_analyze_info);
    private Drawable mFaceInfoPop;
    private int mFacePopupBottom;
    private CameraHardwareFace[] mFaces;
    private int mGap;
    private String mGenderFemale = CameraAppImpl.getAndroidContext().getString(C0049R.string.face_analyze_info_female);
    private String mGenderMale = CameraAppImpl.getAndroidContext().getString(C0049R.string.face_analyze_info_male);
    private int mLatestFaceIndex = -1;
    private CameraHardwareFace[] mLatestFaces = new CameraHardwareFace[6];
    private Paint mMagicPaint;
    private Matrix mMatrix = new Matrix();
    private boolean mMirror;
    private int mOrientation;
    private Paint mPaint = new Paint();
    private int mPopBottomMargin;
    private RectF mRect = new RectF();
    private Paint mRectPaint;
    private Drawable mSBeautyScoreSurmounted;
    private int mScoreHonPadding;
    private int mScoreVerPadding;
    private Drawable mSexFemailIc;
    private Drawable mSexMailIc;
    private String mShowAgeandAge;
    private int mSingleDrawableMargin;
    private boolean mSkipDraw;
    private Rect mTextBounds;
    private int mWinnerIndex = -1;

    public FaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint.setColor(-1);
        this.mPaint.setTextSize(context.getResources().getDimension(C0049R.dimen.face_info_textSize));
        this.mPaint.setAlpha(150);
        this.mFaceIndicator = getResources().getDrawable(C0049R.drawable.ic_face_detected);
        this.mTextBounds = new Rect();
        if (Device.isSupportedMagicMirror()) {
            this.mMagicPaint = new Paint();
            this.mMagicPaint.setColor(-1);
            this.mMagicPaint.setTextSize(context.getResources().getDimension(C0049R.dimen.face_info_magic_textSize));
            this.mMagicPaint.setTypeface(Util.getMiuiTypeface(CameraAppImpl.getAndroidContext()));
            this.mFaceInfoPop = getResources().getDrawable(C0049R.drawable.face_info_pop);
            this.mSexMailIc = getResources().getDrawable(C0049R.drawable.ic_sex_mail);
            this.mSexFemailIc = getResources().getDrawable(C0049R.drawable.ic_sex_femail);
            this.mBeautyScoreIc = getResources().getDrawable(C0049R.drawable.ic_beauty_score);
            this.mBeautyScoreWinner = getResources().getDrawable(C0049R.drawable.ic_beauty_score_winner);
            this.mBeautyScoreSurmounted = getResources().getDrawable(C0049R.drawable.ic_beauty_surmounted);
            this.mSBeautyScoreSurmounted = getResources().getDrawable(C0049R.drawable.ic_beauty_super_surmounted);
            this.mAgeVerPadding = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_ver_padding);
            this.mGap = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_text_left_dis);
            this.mPopBottomMargin = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_pop_bottom_margin);
            this.mScoreHonPadding = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_score_hon_padding);
            this.mScoreVerPadding = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_score_ver_padding);
            this.mAgeMaleHonPadding = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_male_hon_padding);
            this.mAgeFemaleHonPadding = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_female_hon_padding);
            this.mSingleDrawableMargin = this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_info_no_popup_bottom_margin);
            this.mFacePopupBottom = (int) (((double) this.mFaceInfoPop.getIntrinsicHeight()) * 0.3d);
            this.mRectPaint = new Paint();
            this.mRectPaint.setColor(-18377);
            this.mRectPaint.setStrokeWidth((float) this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.face_rect_width));
            this.mRectPaint.setStyle(Style.STROKE);
        }
    }

    private void drawFacePopInfo(Canvas canvas, Drawable drawable, Drawable drawable2, String str, int i, int i2, int i3, int i4) {
        if (TextUtils.isEmpty(str)) {
            this.mTextBounds.set(0, 0, 0, 0);
        } else {
            this.mMagicPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
        }
        int intrinsicWidth = ((this.mTextBounds.width() != 0 ? this.mGap : 0) + ((i * 2) + (drawable != null ? drawable.getIntrinsicWidth() : 0))) + this.mTextBounds.width();
        int intrinsicHeight = (i2 * 2) + (drawable != null ? drawable.getIntrinsicHeight() : 0);
        Rect rect = new Rect(((int) this.mRect.centerX()) - (intrinsicWidth / 2), ((((int) this.mRect.top) - intrinsicHeight) - i4) - i3, ((int) this.mRect.centerX()) + (intrinsicWidth / 2), ((int) this.mRect.top) - i4);
        if (drawable2 != null) {
            drawable2.setBounds(rect);
            drawable2.draw(canvas);
        }
        if (drawable != null) {
            drawable.setBounds(rect.left + i, rect.top + i2, (rect.left + i) + drawable.getIntrinsicWidth(), (rect.top + i2) + drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
        if (this.mTextBounds.width() != 0) {
            canvas.drawText(str, (float) ((drawable != null ? drawable.getIntrinsicWidth() + this.mGap : 0) + (rect.left + i)), (float) ((rect.top + (intrinsicHeight / 2)) + (this.mTextBounds.height() / 2)), this.mMagicPaint);
        }
    }

    private void drawFaceRect(Canvas canvas) {
        if (Device.isSupportedMagicMirror() && CameraSettings.isSwitchOn("pref_camera_magic_mirror_key")) {
            canvas.drawRect(this.mRect, this.mRectPaint);
            return;
        }
        this.mFaceIndicator.setBounds((int) this.mRect.left, (int) this.mRect.top, (int) this.mRect.right, (int) this.mRect.bottom);
        this.mFaceIndicator.draw(canvas);
    }

    private void drawGenderAge(Canvas canvas, CameraHardwareFace cameraHardwareFace) {
        if (!isValideAGInfo(cameraHardwareFace)) {
            return;
        }
        String str;
        if (Device.isSupportedMagicMirror() && CameraSettings.isSwitchOn("pref_camera_magic_mirror_key")) {
            Object obj = cameraHardwareFace.gender < 0.4f ? 1 : null;
            str = "";
            if ("on".equals(this.mShowAgeandAge)) {
                str = Integer.toString((int) (obj != null ? cameraHardwareFace.ageFemale : cameraHardwareFace.ageMale));
            } else {
                int ageIndex = getAgeIndex(cameraHardwareFace.ageMale);
                if (ageIndex < this.mAgeOnlyRangeAlias.length) {
                    str = this.mAgeOnlyRangeAlias[ageIndex];
                }
            }
            drawFacePopInfo(canvas, obj != null ? this.mSexFemailIc : this.mSexMailIc, this.mFaceInfoPop, str, obj != null ? this.mAgeFemaleHonPadding : this.mAgeMaleHonPadding, this.mAgeVerPadding, this.mFacePopupBottom, this.mPopBottomMargin);
            return;
        }
        str = getShowInfo(cameraHardwareFace);
        if (!TextUtils.isEmpty(str)) {
            this.mPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
            canvas.drawText(str, this.mRect.centerX() - ((float) this.mTextBounds.centerX()), this.mRect.top - ((float) (this.mTextBounds.bottom - this.mTextBounds.top)), this.mPaint);
        }
    }

    private int getAgeIndex(float f) {
        return f <= 7.0f ? 0 : f <= 17.0f ? 1 : f <= 30.0f ? 2 : f <= 44.0f ? 3 : f <= 60.0f ? 4 : 5;
    }

    private Drawable getScoreDrawable(int i) {
        return (i < 0 || this.mFaces == null || i > this.mFaces.length) ? null : this.mFaces[i].beautyscore > 98.0f ? this.mSBeautyScoreSurmounted : this.mFaces[i].beautyscore > 90.0f ? this.mBeautyScoreSurmounted : i == this.mWinnerIndex ? this.mBeautyScoreWinner : this.mBeautyScoreIc;
    }

    private String getShowInfo(CameraHardwareFace cameraHardwareFace) {
        if ("on".equals(this.mShowAgeandAge)) {
            String str = this.mGenderMale;
            String num = Integer.toString((int) cameraHardwareFace.ageMale);
            if (cameraHardwareFace.gender < 0.4f) {
                str = this.mGenderFemale;
                num = Integer.toString((int) cameraHardwareFace.ageFemale);
            }
            return String.format(configuration.locale, this.mFaceInfoFormat, new Object[]{str, num});
        }
        int ageIndex = getAgeIndex(cameraHardwareFace.ageMale);
        if (cameraHardwareFace.gender < 0.4f) {
            ageIndex = getAgeIndex(cameraHardwareFace.ageFemale) + 6;
        }
        return ageIndex < this.mAgeRangeAlias.length ? this.mAgeRangeAlias[ageIndex] : null;
    }

    private int getShowType(CameraHardwareFace[] cameraHardwareFaceArr) {
        if (cameraHardwareFaceArr == null || cameraHardwareFaceArr.length <= 0) {
            return 0;
        }
        if (!Device.isSupportedMagicMirror() || !CameraSettings.isSwitchOn("pref_camera_magic_mirror_key")) {
            return (showFaceInfo() && Device.isSupportedIntelligentBeautify()) ? 1 : 0;
        } else {
            int i = 0;
            this.mWinnerIndex = -1;
            for (int i2 = 0; i2 < this.mFaces.length; i2++) {
                CameraHardwareFace cameraHardwareFace = cameraHardwareFaceArr[i2];
                if (cameraHardwareFace.beautyscore > 0.0f) {
                    i++;
                    if (this.mWinnerIndex == -1 || cameraHardwareFace.beautyscore > cameraHardwareFaceArr[this.mWinnerIndex].beautyscore) {
                        this.mWinnerIndex = i2;
                    }
                }
            }
            return i > 1 ? 2 : i > 0 ? 4 : 0;
        }
    }

    private boolean isValideAGInfo(CameraHardwareFace cameraHardwareFace) {
        return 0.5f <= cameraHardwareFace.prob ? cameraHardwareFace.gender <= 0.4f || 0.6f <= cameraHardwareFace.gender : false;
    }

    private void setToVisible() {
        if (getVisibility() != 0) {
            setVisibility(0);
        }
    }

    private boolean showFaceInfo() {
        return !"off".equals(this.mShowAgeandAge);
    }

    private void updateLatestFaces() {
        if (this.mLatestFaceIndex >= 5) {
            this.mLatestFaceIndex = 0;
        } else {
            this.mLatestFaceIndex++;
        }
        if (faceExists()) {
            CameraHardwareFace cameraHardwareFace = this.mFaces[0];
            for (int i = 1; i < this.mFaces.length; i++) {
                if (this.mFaces[i].rect.right - this.mFaces[i].rect.left > cameraHardwareFace.rect.right - cameraHardwareFace.rect.left) {
                    cameraHardwareFace = this.mFaces[i];
                }
            }
            this.mLatestFaces[this.mLatestFaceIndex] = cameraHardwareFace;
            return;
        }
        this.mLatestFaces[this.mLatestFaceIndex] = null;
    }

    public void clear() {
        this.mFaces = null;
        clearPreviousFaces();
        invalidate();
    }

    public void clearPreviousFaces() {
        this.mLatestFaceIndex = -1;
        for (int i = 0; i < this.mLatestFaces.length; i++) {
            this.mLatestFaces[i] = null;
        }
    }

    public boolean faceExists() {
        return this.mFaces != null && this.mFaces.length > 0;
    }

    public CameraHardwareFace[] getFaces() {
        return this.mFaces;
    }

    public RectF getFocusRect() {
        RectF rectF = new RectF();
        CameraScreenNail cameraScreenNail = ((ActivityBase) getContext()).getCameraScreenNail();
        if (cameraScreenNail == null || this.mLatestFaceIndex < 0 || this.mLatestFaceIndex >= 6) {
            return null;
        }
        this.mMatrix.reset();
        Util.prepareMatrix(this.mMatrix, this.mMirror, this.mDisplayOrientation, cameraScreenNail.getRenderWidth(), cameraScreenNail.getRenderHeight(), getWidth() / 2, getHeight() / 2);
        rectF.set(this.mLatestFaces[this.mLatestFaceIndex].rect);
        this.mMatrix.postRotate((float) this.mOrientation);
        this.mMatrix.mapRect(rectF);
        return rectF;
    }

    public boolean isFaceStable() {
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        for (CameraHardwareFace cameraHardwareFace : this.mLatestFaces) {
            if (cameraHardwareFace == null) {
                i++;
                if (i >= 3) {
                    return false;
                }
            } else {
                i2 += cameraHardwareFace.rect.right - cameraHardwareFace.rect.left;
                i3 += cameraHardwareFace.rect.bottom - cameraHardwareFace.rect.top;
                i4 += cameraHardwareFace.rect.left;
                i5 += cameraHardwareFace.rect.top;
            }
        }
        int length = this.mLatestFaces.length - i;
        i2 /= length;
        i3 /= length;
        i4 /= length;
        i5 /= length;
        int i6 = i2 / 3 > 90 ? i2 / 3 : 90;
        for (CameraHardwareFace cameraHardwareFace2 : this.mLatestFaces) {
            if (cameraHardwareFace2 != null) {
                if (Math.abs((cameraHardwareFace2.rect.right - cameraHardwareFace2.rect.left) - i2) <= i6 && Math.abs(cameraHardwareFace2.rect.left - i4) <= 120) {
                    if (Math.abs(cameraHardwareFace2.rect.top - i5) > 120) {
                    }
                }
                return false;
            }
        }
        boolean z = i2 > 670 || i3 > 670;
        this.mIsBigEnoughRect = z;
        return true;
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mSkipDraw) {
            CameraScreenNail cameraScreenNail = ((ActivityBase) getContext()).getCameraScreenNail();
            if (!(this.mPause || this.mFaces == null || this.mFaces.length <= 0 || cameraScreenNail == null)) {
                this.mMatrix.reset();
                Util.prepareMatrix(this.mMatrix, this.mMirror, this.mDisplayOrientation, cameraScreenNail.getRenderWidth(), cameraScreenNail.getRenderHeight(), getWidth() / 2, getHeight() / 2);
                this.mMatrix.postRotate((float) this.mOrientation);
                canvas.save();
                canvas.rotate((float) (-this.mOrientation));
                int showType = getShowType(this.mFaces);
                boolean isSwitchOn = CameraSettings.isSwitchOn("pref_camera_square_mode_key");
                int i = 0;
                while (i < this.mFaces.length) {
                    this.mRect.set(this.mFaces[i].rect);
                    this.mMatrix.mapRect(this.mRect);
                    if (!isSwitchOn || Util.isContaints(cameraScreenNail.getRenderRect(), this.mRect)) {
                        drawFaceRect(canvas);
                        switch (showType) {
                            case 1:
                                drawGenderAge(canvas, this.mFaces[i]);
                                break;
                            case 2:
                                if (i != this.mWinnerIndex) {
                                    break;
                                }
                                drawFacePopInfo(canvas, getScoreDrawable(i), null, null, 0, 0, 0, this.mSingleDrawableMargin);
                                break;
                            case 4:
                                if (this.mFaces[i].beautyscore != 0.0f) {
                                    if (this.mFaces[i].beautyscore <= 90.0f) {
                                        Canvas canvas2 = canvas;
                                        drawFacePopInfo(canvas2, this.mBeautyScoreIc, this.mFaceInfoPop, String.format("%.1f", new Object[]{Float.valueOf(this.mFaces[i].beautyscore / 10.0f)}), this.mScoreHonPadding, this.mScoreVerPadding, this.mFacePopupBottom, this.mPopBottomMargin);
                                        break;
                                    }
                                    drawFacePopInfo(canvas, getScoreDrawable(i), null, null, 0, 0, 0, this.mSingleDrawableMargin);
                                    break;
                                }
                                break;
                            default:
                                break;
                        }
                        i++;
                    } else {
                        canvas.restore();
                    }
                }
                canvas.restore();
            }
        }
    }

    public void pause() {
        super.pause();
    }

    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        Log.v("FaceView", "mDisplayOrientation=" + i);
    }

    public boolean setFaces(CameraHardwareFace[] cameraHardwareFaceArr) {
        Log.v("FaceView", "Num of faces=" + cameraHardwareFaceArr.length);
        Object obj = (faceExists() || (cameraHardwareFaceArr != null && cameraHardwareFaceArr.length > 0)) ? 1 : null;
        this.mFaces = cameraHardwareFaceArr;
        updateLatestFaces();
        if (obj != null) {
            setToVisible();
            invalidate();
        }
        return true;
    }

    public void setMirror(boolean z) {
        this.mMirror = z;
        Log.v("FaceView", "mMirror=" + z);
    }

    public void setOrientation(int i, boolean z) {
        this.mOrientation = i;
        if (!this.mPause && faceExists() && !this.mSkipDraw) {
            invalidate();
        }
    }

    public void setShowGenderAndAge(String str) {
        this.mShowAgeandAge = str;
    }

    public void setSkipDraw(boolean z) {
        this.mSkipDraw = z;
    }

    public void showFail() {
        setToVisible();
        invalidate();
    }

    public void showStart() {
        setToVisible();
        invalidate();
    }

    public void showSuccess() {
        setToVisible();
        invalidate();
    }
}

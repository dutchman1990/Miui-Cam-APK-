package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.FloatMath;
import com.android.camera.CameraAppImpl;
import com.android.camera.Util;
import com.android.camera.aosp_porting.ReflectUtil;

public class StringTexture extends CanvasTexture {
    private final FontMetricsInt mMetrics;
    private final TextPaint mPaint;
    private final String mText;

    private StringTexture(String str, TextPaint textPaint, FontMetricsInt fontMetricsInt, int i, int i2) {
        super(i, i2);
        this.mText = str;
        this.mPaint = textPaint;
        this.mMetrics = fontMetricsInt;
    }

    public static TextPaint getDefaultPaint(float f, int i, int i2) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(f);
        textPaint.setAntiAlias(true);
        textPaint.setColor(i);
        if (i2 == 1) {
            textPaint.setTypeface(Util.getMiuiTypeface(CameraAppImpl.getAndroidContext()));
            textPaint.setShadowLayer(0.1f, 5.0f, 5.0f, -16777216);
            setLongshotMode(textPaint, 0.1f);
        } else if (i2 == 2) {
            textPaint.setTypeface(Util.getMiuiTimeTypeface(CameraAppImpl.getAndroidContext()));
            textPaint.setShadowLayer(0.1f, 0.0f, 3.0f, 771751936);
            setLongshotMode(textPaint, 0.1f);
        } else {
            textPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
        }
        return textPaint;
    }

    public static StringTexture newInstance(String str, float f, int i, float f2, boolean z, int i2) {
        TextPaint defaultPaint = getDefaultPaint(f, i, i2);
        if (z) {
            defaultPaint.setTypeface(Typeface.defaultFromStyle(1));
        }
        if (f2 > 0.0f) {
            str = TextUtils.ellipsize(str, defaultPaint, f2, TruncateAt.END).toString();
        }
        return newInstance(str, defaultPaint, i2);
    }

    public static StringTexture newInstance(String str, float f, int i, int i2) {
        return newInstance(str, getDefaultPaint(f, i, i2), i2);
    }

    private static StringTexture newInstance(String str, TextPaint textPaint, int i) {
        int i2 = 0;
        FontMetricsInt fontMetricsInt = textPaint.getFontMetricsInt();
        int ceil = (int) FloatMath.ceil(textPaint.measureText(str));
        if (i == 1) {
            i2 = 5;
        }
        int i3 = ceil + i2;
        int i4 = fontMetricsInt.descent - fontMetricsInt.ascent;
        if (i3 <= 0) {
            i3 = 1;
        }
        if (i4 <= 0) {
            i4 = 1;
        }
        return new StringTexture(str, textPaint, fontMetricsInt, i3, i4);
    }

    private static void setLongshotMode(TextPaint textPaint, float f) {
        if (VERSION.SDK_INT >= 21) {
            ReflectUtil.callMethod(TextPaint.class, textPaint, "setLetterSpacing", "(F)V", Float.valueOf(f));
        }
    }

    protected void onDraw(Canvas canvas, Bitmap bitmap) {
        canvas.translate(0.0f, (float) (-this.mMetrics.ascent));
        canvas.drawText(this.mText, 0.0f, 0.0f, this.mPaint);
    }
}

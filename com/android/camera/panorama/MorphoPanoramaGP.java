package com.android.camera.panorama;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import com.android.camera.Log;
import java.nio.ByteBuffer;

public class MorphoPanoramaGP {
    private static final String TAG = MorphoPanoramaGP.class.getSimpleName();
    private long mNative = 0;

    public static class InitParam {
        public double angle_of_view_degree;
        public int direction;
        public int draw_cur_image;
        public int dst_img_height;
        public int dst_img_width;
        public String format;
        public int output_rotation;
        public int preview_height;
        public int preview_img_height;
        public int preview_img_width;
        public int preview_shrink_ratio;
        public int preview_width;
        public int still_height;
        public int still_width;
        public int use_threshold;
    }

    static {
        try {
            System.loadLibrary("morpho_panorama");
            Log.m2e(TAG, "loadLibrary done");
        } catch (Throwable e) {
            Log.m2e(TAG, "can't loadLibrary " + e.getMessage());
        }
    }

    public MorphoPanoramaGP() {
        long createNativeObject = createNativeObject();
        if (createNativeObject != 0) {
            this.mNative = createNativeObject;
        } else {
            this.mNative = 0;
        }
    }

    public static int calcImageSize(InitParam initParam, double d) {
        return nativeCalcImageSize(initParam, d);
    }

    private final native long createNativeObject();

    private final native void deleteNativeObject(long j);

    private final native int nativeAttachPreview(long j, byte[] bArr, int i, int[] iArr, byte[] bArr2, int[] iArr2, Bitmap bitmap);

    private final native int nativeAttachStillImageExt(long j, ByteBuffer byteBuffer, int i, ByteBuffer byteBuffer2);

    private final native int nativeAttachStillImageRaw(long j, ByteBuffer byteBuffer, int i, ByteBuffer byteBuffer2);

    private static final native int nativeCalcImageSize(InitParam initParam, double d);

    private final native int nativeEnd(long j);

    private final native int nativeFinish(long j);

    private final native int nativeGetBoundingRect(long j, int[] iArr);

    private final native int nativeGetClippingRect(long j, int[] iArr);

    private final native int nativeGetCurrentDirection(long j, int[] iArr);

    private final native int nativeGetGuidancePos(long j, int[] iArr);

    private final native int nativeGetMoveSpeed(long j, int[] iArr);

    private final native int nativeInitialize(long j, InitParam initParam, int[] iArr);

    private final native int nativeSaveOutputJpeg(long j, String str, int i, int i2, int i3, int i4, int i5, int[] iArr);

    private final native int nativeSetJpegForCopyingExif(long j, ByteBuffer byteBuffer);

    private final native int nativeSetMotionlessThreshold(long j, int i);

    private final native int nativeSetUseSensorAssist(long j, int i, int i2);

    private final native int nativeSetUseSensorThreshold(long j, int i);

    private final native int nativeStart(long j);

    public int attachPreview(byte[] bArr, int i, int[] iArr, byte[] bArr2, int[] iArr2, Bitmap bitmap) {
        return this.mNative != 0 ? nativeAttachPreview(this.mNative, bArr, i, iArr, bArr2, iArr2, bitmap) : -2147483646;
    }

    public int attachSetJpegForCopyingExif(ByteBuffer byteBuffer) {
        return this.mNative != 0 ? nativeSetJpegForCopyingExif(this.mNative, byteBuffer) : -2147483646;
    }

    public int attachStillImageExt(ByteBuffer byteBuffer, int i, ByteBuffer byteBuffer2) {
        return this.mNative != 0 ? nativeAttachStillImageExt(this.mNative, byteBuffer, i, byteBuffer2) : -2147483646;
    }

    public int attachStillImageRaw(ByteBuffer byteBuffer, int i, ByteBuffer byteBuffer2) {
        return this.mNative != 0 ? nativeAttachStillImageRaw(this.mNative, byteBuffer, i, byteBuffer2) : -2147483646;
    }

    public int end() {
        return this.mNative != 0 ? nativeEnd(this.mNative) : -2147483646;
    }

    public int finish() {
        if (this.mNative == 0) {
            return -2147483646;
        }
        int nativeFinish = nativeFinish(this.mNative);
        deleteNativeObject(this.mNative);
        this.mNative = 0;
        return nativeFinish;
    }

    public int getBoundingRect(Rect rect) {
        int nativeGetBoundingRect;
        int[] iArr = new int[4];
        if (this.mNative != 0) {
            nativeGetBoundingRect = nativeGetBoundingRect(this.mNative, iArr);
            if (nativeGetBoundingRect == 0) {
                rect.set(iArr[0], iArr[1], iArr[2], iArr[3]);
            }
        } else {
            nativeGetBoundingRect = -2147483646;
        }
        if (nativeGetBoundingRect != 0) {
            rect.set(0, 0, 0, 0);
        }
        return nativeGetBoundingRect;
    }

    public int getClippingRect(Rect rect) {
        int nativeGetClippingRect;
        int[] iArr = new int[4];
        if (this.mNative != 0) {
            nativeGetClippingRect = nativeGetClippingRect(this.mNative, iArr);
            if (nativeGetClippingRect == 0) {
                rect.set(iArr[0], iArr[1], iArr[2], iArr[3]);
            }
        } else {
            nativeGetClippingRect = -2147483646;
        }
        if (nativeGetClippingRect != 0) {
            rect.set(0, 0, 0, 0);
        }
        return nativeGetClippingRect;
    }

    public int getCurrentDirection(int[] iArr) {
        return this.mNative != 0 ? nativeGetCurrentDirection(this.mNative, iArr) : -2147483646;
    }

    public int getGuidancePos(Point point, Point point2) {
        int[] iArr = new int[4];
        if (this.mNative == 0) {
            return -2147483646;
        }
        int nativeGetGuidancePos = nativeGetGuidancePos(this.mNative, iArr);
        point.set(iArr[0], iArr[1]);
        point2.set(iArr[2], iArr[3]);
        return nativeGetGuidancePos;
    }

    public int getMoveSpeed(int[] iArr) {
        return this.mNative != 0 ? nativeGetMoveSpeed(this.mNative, iArr) : -2147483646;
    }

    public int initialize(InitParam initParam, int[] iArr) {
        return this.mNative != 0 ? nativeInitialize(this.mNative, initParam, iArr) : -2147483646;
    }

    public int saveOutputJpeg(String str, Rect rect, int i, int[] iArr) {
        if (this.mNative == 0) {
            return -2147483646;
        }
        return nativeSaveOutputJpeg(this.mNative, str, rect.left, rect.top, rect.right, rect.bottom, i, iArr);
    }

    public int setMotionlessThreshold(int i) {
        return this.mNative != 0 ? nativeSetMotionlessThreshold(this.mNative, i) : -2147483646;
    }

    public int setUseSensorAssist(int i, int i2) {
        return this.mNative != 0 ? nativeSetUseSensorAssist(this.mNative, i, i2) : -2147483646;
    }

    public int setUseSensorThreshold(int i) {
        return this.mNative != 0 ? nativeSetUseSensorThreshold(this.mNative, i) : -2147483646;
    }

    public int start() {
        return this.mNative != 0 ? nativeStart(this.mNative) : -2147483646;
    }
}

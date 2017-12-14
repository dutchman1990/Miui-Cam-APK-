package com.android.camera.groupshot;

import com.android.camera.Log;

public class GroupShot {
    private static final String TAG = GroupShot.class.getSimpleName();
    private int mHeight;
    private int mMaxImageNum;
    private long mNative = 0;
    private boolean mStart;
    private int mWidth;

    static {
        try {
            System.loadLibrary("morpho_groupshot");
        } catch (Throwable e) {
            Log.m2e(TAG, "can't loadLibrary, " + e.getMessage());
        }
    }

    private final native int attach(long j, byte[] bArr);

    private final native int clearImages(long j);

    private final native long createNativeObject();

    private final native void deleteNativeObject(long j);

    private final native int end(long j);

    private final native int getImageAndSaveJpeg(long j, String str);

    private final native int initializeNativeObject(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7);

    private final native int saveInputImages(long j, String str);

    private final native int setBaseImage(long j, int i);

    private final native int setBestFace(long j);

    private final native int start(long j, int i);

    public int attach(byte[] bArr) {
        Log.m5v(TAG, String.format("GroupShot attach mNative=%x", new Object[]{Long.valueOf(this.mNative)}));
        return this.mNative == 0 ? -1 : attach(this.mNative, bArr);
    }

    public int attach_end() {
        Log.m5v(TAG, String.format("GroupShot attach end, mNative=%x", new Object[]{Long.valueOf(this.mNative)}));
        return this.mNative == 0 ? -1 : end(this.mNative);
    }

    public int attach_start(int i) {
        Log.m5v(TAG, String.format("GroupShot attach start mNative=%x", new Object[]{Long.valueOf(this.mNative)}));
        if (this.mNative == 0) {
            return -1;
        }
        this.mStart = true;
        return start(this.mNative, i);
    }

    public int clearImages() {
        Log.m5v(TAG, String.format("clearImages mNative=%x", new Object[]{Long.valueOf(this.mNative)}));
        return this.mNative == 0 ? -1 : clearImages(this.mNative);
    }

    public void finish() {
        if (this.mNative != 0) {
            Log.m5v(TAG, String.format("finish mNative=%x", new Object[]{Long.valueOf(this.mNative)}));
            deleteNativeObject(this.mNative);
            this.mWidth = 0;
            this.mHeight = 0;
            this.mMaxImageNum = 0;
            this.mStart = false;
            this.mNative = 0;
        }
    }

    public int getImageAndSaveJpeg(String str) {
        if (this.mNative == 0) {
            return -1;
        }
        Log.m5v(TAG, String.format("GroupShot getImageAndSaveJpeg, mNative=%x filename=%s", new Object[]{Long.valueOf(this.mNative), str}));
        return getImageAndSaveJpeg(this.mNative, str);
    }

    public int initialize(int i, int i2, int i3, int i4, int i5, int i6) {
        if (!this.mStart && this.mWidth == i3 && this.mHeight == i4 && this.mMaxImageNum == i) {
            return 0;
        }
        if (this.mStart) {
            if (this.mWidth == 0 && this.mHeight == 0) {
                if (this.mMaxImageNum != 0) {
                }
            }
            clearImages();
            finish();
        }
        if (this.mNative == 0) {
            this.mNative = createNativeObject();
            if (this.mNative == 0) {
                return -1;
            }
        }
        Log.m5v(TAG, String.format("initialize imagenum=%d, width=%d, height=%d, mStart=%b, mWidth=%d, mHeight=%d, mMaxImageNum=%d", new Object[]{Integer.valueOf(i), Integer.valueOf(i3), Integer.valueOf(i4), Boolean.valueOf(this.mStart), Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mMaxImageNum)}));
        initializeNativeObject(this.mNative, i, i2, i3, i4, i5, i6, 0);
        this.mMaxImageNum = i;
        this.mWidth = i3;
        this.mHeight = i4;
        this.mStart = false;
        return 0;
    }

    public boolean isUsed() {
        return this.mStart;
    }

    public int saveInputImages(String str) {
        return this.mNative == 0 ? -1 : saveInputImages(this.mNative, str);
    }

    public int setBaseImage(int i) {
        return this.mNative == 0 ? -1 : setBaseImage(this.mNative, i);
    }

    public int setBestFace() {
        return this.mNative == 0 ? -1 : setBestFace(this.mNative);
    }
}

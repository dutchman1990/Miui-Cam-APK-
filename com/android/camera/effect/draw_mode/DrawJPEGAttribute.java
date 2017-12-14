package com.android.camera.effect.draw_mode;

import android.location.Location;
import android.net.Uri;
import com.android.camera.effect.EffectController.EffectRectAttribute;
import com.android.gallery3d.exif.ExifInterface;

public class DrawJPEGAttribute extends DrawAttribute {
    public EffectRectAttribute mAttribute;
    public byte[] mData;
    public long mDate;
    public int mEffectIndex;
    public ExifInterface mExif;
    public boolean mFinalImage;
    public int mHeight;
    public int mJpegOrientation;
    public Location mLoc;
    public boolean mMirror;
    public int mOrientation;
    public boolean mPortrait;
    public int mPreviewHeight;
    public int mPreviewWidth;
    public float mShootRotation;
    public String mTitle;
    public Uri mUri;
    public int mWidth;

    public DrawJPEGAttribute(byte[] bArr, int i, int i2, int i3, int i4, int i5, EffectRectAttribute effectRectAttribute, Location location, String str, long j, int i6, int i7, float f, boolean z, boolean z2) {
        this.mPreviewWidth = i;
        this.mPreviewHeight = i2;
        this.mWidth = i3;
        this.mHeight = i4;
        this.mData = bArr;
        this.mDate = j;
        this.mEffectIndex = i5;
        this.mAttribute = effectRectAttribute;
        this.mLoc = location;
        this.mTitle = str;
        this.mOrientation = i6;
        this.mJpegOrientation = i7;
        this.mShootRotation = f;
        this.mMirror = z;
        this.mTarget = 9;
        this.mFinalImage = true;
        this.mPortrait = z2;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}

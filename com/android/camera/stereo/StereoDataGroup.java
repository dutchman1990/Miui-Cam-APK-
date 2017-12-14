package com.android.camera.stereo;

public class StereoDataGroup {
    private byte[] mClearImage;
    private byte[] mDepthMap;
    private byte[] mJpsData;
    private byte[] mLdcData;
    private byte[] mMaskAndConfigData;
    private byte[] mOriginalJpegData;
    private String mPictureName;

    public StereoDataGroup(String str, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
        this.mPictureName = str;
        this.mOriginalJpegData = bArr;
        this.mJpsData = bArr2;
        this.mMaskAndConfigData = bArr3;
        this.mDepthMap = bArr4;
        this.mClearImage = bArr5;
        this.mLdcData = bArr6;
    }

    public byte[] getClearImage() {
        return this.mClearImage;
    }

    public byte[] getDepthMap() {
        return this.mDepthMap;
    }

    public byte[] getJpsData() {
        return this.mJpsData;
    }

    public byte[] getLdcData() {
        return this.mLdcData;
    }

    public byte[] getMaskAndConfigData() {
        return this.mMaskAndConfigData;
    }

    public byte[] getOriginalJpegData() {
        return this.mOriginalJpegData;
    }

    public String getPictureName() {
        return this.mPictureName;
    }
}

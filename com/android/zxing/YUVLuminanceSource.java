package com.android.zxing;

import com.google.zxing.LuminanceSource;

public final class YUVLuminanceSource extends LuminanceSource {
    private final int mDataHeight;
    private final int mDataWidth;
    private final int mLeft;
    private final int mTop;
    private final byte[] mYUVData;

    public YUVLuminanceSource(byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6) {
        super(i5, i6);
        if (i3 + i5 > i || i4 + i6 > i2) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
        this.mYUVData = bArr;
        this.mDataWidth = i;
        this.mDataHeight = i2;
        this.mLeft = i3;
        this.mTop = i4;
    }

    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();
        if (width == this.mDataWidth && height == this.mDataHeight) {
            return this.mYUVData;
        }
        int i = width * height;
        byte[] bArr = new byte[i];
        int i2 = (this.mTop * this.mDataWidth) + this.mLeft;
        if (width == this.mDataWidth) {
            System.arraycopy(this.mYUVData, i2, bArr, 0, i);
            return bArr;
        }
        byte[] bArr2 = this.mYUVData;
        for (int i3 = 0; i3 < height; i3++) {
            System.arraycopy(bArr2, i2, bArr, i3 * width, width);
            i2 += this.mDataWidth;
        }
        return bArr;
    }

    public byte[] getRow(int i, byte[] bArr) {
        if (i < 0 || i >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + i);
        }
        int width = getWidth();
        if (bArr == null || bArr.length < width) {
            bArr = new byte[width];
        }
        System.arraycopy(this.mYUVData, ((this.mTop + i) * this.mDataWidth) + this.mLeft, bArr, 0, width);
        return bArr;
    }
}

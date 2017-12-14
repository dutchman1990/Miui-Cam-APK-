package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;

public class GlobalHistogramBinarizer extends Binarizer {
    private static final byte[] EMPTY = new byte[0];
    private final int[] buckets = new int[32];
    private byte[] luminances = EMPTY;

    public GlobalHistogramBinarizer(LuminanceSource luminanceSource) {
        super(luminanceSource);
    }

    private static int estimateBlackPoint(int[] iArr) throws NotFoundException {
        int i;
        int length = iArr.length;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (i = 0; i < length; i++) {
            if (iArr[i] > i4) {
                i3 = i;
                i4 = iArr[i];
            }
            if (iArr[i] > i2) {
                i2 = iArr[i];
            }
        }
        int i5 = 0;
        int i6 = 0;
        for (i = 0; i < length; i++) {
            int i7 = i - i3;
            int i8 = (iArr[i] * i7) * i7;
            if (i8 > i6) {
                i5 = i;
                i6 = i8;
            }
        }
        if (i3 > i5) {
            int i9 = i3;
            i3 = i5;
            i5 = i9;
        }
        if (i5 - i3 > length / 16) {
            int i10 = i5 - 1;
            int i11 = -1;
            for (i = i5 - 1; i > i3; i--) {
                int i12 = i - i3;
                i8 = ((i12 * i12) * (i5 - i)) * (i2 - iArr[i]);
                if (i8 > i11) {
                    i10 = i;
                    i11 = i8;
                }
            }
            return i10 << 3;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void initArrays(int i) {
        if (this.luminances.length < i) {
            this.luminances = new byte[i];
        }
        for (int i2 = 0; i2 < 32; i2++) {
            this.buckets[i2] = 0;
        }
    }

    public Binarizer createBinarizer(LuminanceSource luminanceSource) {
        return new GlobalHistogramBinarizer(luminanceSource);
    }

    public BitMatrix getBlackMatrix() throws NotFoundException {
        int i;
        byte[] row;
        LuminanceSource luminanceSource = getLuminanceSource();
        int width = luminanceSource.getWidth();
        int height = luminanceSource.getHeight();
        BitMatrix bitMatrix = new BitMatrix(width, height);
        initArrays(width);
        int[] iArr = this.buckets;
        for (i = 1; i < 5; i++) {
            int i2;
            row = luminanceSource.getRow((height * i) / 5, this.luminances);
            int i3 = (width * 4) / 5;
            for (i2 = width / 5; i2 < i3; i2++) {
                int i4 = (row[i2] & 255) >> 3;
                iArr[i4] = iArr[i4] + 1;
            }
        }
        int estimateBlackPoint = estimateBlackPoint(iArr);
        row = luminanceSource.getMatrix();
        for (i = 0; i < height; i++) {
            int i5 = i * width;
            for (i2 = 0; i2 < width; i2++) {
                if ((row[i5 + i2] & 255) < estimateBlackPoint) {
                    bitMatrix.set(i2, i);
                }
            }
        }
        return bitMatrix;
    }

    public BitArray getBlackRow(int i, BitArray bitArray) throws NotFoundException {
        int i2;
        LuminanceSource luminanceSource = getLuminanceSource();
        int width = luminanceSource.getWidth();
        if (bitArray != null && bitArray.getSize() >= width) {
            bitArray.clear();
        } else {
            bitArray = new BitArray(width);
        }
        initArrays(width);
        byte[] row = luminanceSource.getRow(i, this.luminances);
        int[] iArr = this.buckets;
        for (i2 = 0; i2 < width; i2++) {
            int i3 = (row[i2] & 255) >> 3;
            iArr[i3] = iArr[i3] + 1;
        }
        int estimateBlackPoint = estimateBlackPoint(iArr);
        int i4 = row[0] & 255;
        int i5 = row[1] & 255;
        for (i2 = 1; i2 < width - 1; i2++) {
            int i6 = row[i2 + 1] & 255;
            if ((((i5 * 4) - i4) - i6) / 2 < estimateBlackPoint) {
                bitArray.set(i2);
            }
            i4 = i5;
            i5 = i6;
        }
        return bitArray;
    }
}

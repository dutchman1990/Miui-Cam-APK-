package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import java.lang.reflect.Array;

public final class HybridBinarizer extends GlobalHistogramBinarizer {
    private BitMatrix matrix;

    public HybridBinarizer(LuminanceSource luminanceSource) {
        super(luminanceSource);
    }

    private static int[][] calculateBlackPoints(byte[] bArr, int i, int i2, int i3, int i4) {
        int[][] iArr = (int[][]) Array.newInstance(Integer.TYPE, new int[]{i2, i});
        for (int i5 = 0; i5 < i2; i5++) {
            int i6 = i5 << 3;
            int i7 = i4 - 8;
            if (i6 > i7) {
                i6 = i7;
            }
            int i8 = 0;
            while (i8 < i) {
                int i9 = i8 << 3;
                int i10 = i3 - 8;
                if (i9 > i10) {
                    i9 = i10;
                }
                int i11 = 0;
                int i12 = 255;
                int i13 = 0;
                int i14 = 0;
                int i15 = (i6 * i3) + i9;
                while (i14 < 8) {
                    int i16;
                    for (i16 = 0; i16 < 8; i16++) {
                        int i17 = bArr[i15 + i16] & 255;
                        i11 += i17;
                        if (i17 < i12) {
                            i12 = i17;
                        }
                        if (i17 > i13) {
                            i13 = i17;
                        }
                    }
                    if (i13 - i12 <= 24) {
                        i14++;
                        i15 += i3;
                    }
                    while (true) {
                        i14++;
                        i15 += i3;
                        if (i14 >= 8) {
                            break;
                        }
                        for (i16 = 0; i16 < 8; i16++) {
                            i11 += bArr[i15 + i16] & 255;
                        }
                    }
                    i14++;
                    i15 += i3;
                }
                int i18 = i11 >> 6;
                if (i13 - i12 <= 24) {
                    i18 = i12 / 2;
                    if (i5 > 0 && i8 > 0) {
                        int i19 = ((iArr[i5 - 1][i8] + (iArr[i5][i8 - 1] * 2)) + iArr[i5 - 1][i8 - 1]) / 4;
                        if (i12 < i19) {
                            i18 = i19;
                        }
                    }
                }
                iArr[i5][i8] = i18;
                i8++;
            }
        }
        return iArr;
    }

    private static void calculateThresholdForBlock(byte[] bArr, int i, int i2, int i3, int i4, int[][] iArr, BitMatrix bitMatrix) {
        for (int i5 = 0; i5 < i2; i5++) {
            int i6 = i5 << 3;
            int i7 = i4 - 8;
            if (i6 > i7) {
                i6 = i7;
            }
            for (int i8 = 0; i8 < i; i8++) {
                int i9 = i8 << 3;
                int i10 = i3 - 8;
                if (i9 > i10) {
                    i9 = i10;
                }
                int cap = cap(i8, 2, i - 3);
                int cap2 = cap(i5, 2, i2 - 3);
                int i11 = 0;
                for (int i12 = -2; i12 <= 2; i12++) {
                    int[] iArr2 = iArr[cap2 + i12];
                    i11 += (((iArr2[cap - 2] + iArr2[cap - 1]) + iArr2[cap]) + iArr2[cap + 1]) + iArr2[cap + 2];
                }
                thresholdBlock(bArr, i9, i6, i11 / 25, i3, bitMatrix);
            }
        }
    }

    private static int cap(int i, int i2, int i3) {
        return i >= i2 ? i <= i3 ? i : i3 : i2;
    }

    private static void thresholdBlock(byte[] bArr, int i, int i2, int i3, int i4, BitMatrix bitMatrix) {
        int i5 = 0;
        int i6 = (i2 * i4) + i;
        while (i5 < 8) {
            for (int i7 = 0; i7 < 8; i7++) {
                if ((bArr[i6 + i7] & 255) <= i3) {
                    bitMatrix.set(i + i7, i2 + i5);
                }
            }
            i5++;
            i6 += i4;
        }
    }

    public Binarizer createBinarizer(LuminanceSource luminanceSource) {
        return new HybridBinarizer(luminanceSource);
    }

    public BitMatrix getBlackMatrix() throws NotFoundException {
        if (this.matrix != null) {
            return this.matrix;
        }
        LuminanceSource luminanceSource = getLuminanceSource();
        int width = luminanceSource.getWidth();
        int height = luminanceSource.getHeight();
        if (width >= 40 && height >= 40) {
            byte[] matrix = luminanceSource.getMatrix();
            int i = width >> 3;
            if ((width & 7) != 0) {
                i++;
            }
            int i2 = height >> 3;
            if ((height & 7) != 0) {
                i2++;
            }
            int[][] calculateBlackPoints = calculateBlackPoints(matrix, i, i2, width, height);
            BitMatrix bitMatrix = new BitMatrix(width, height);
            calculateThresholdForBlock(matrix, i, i2, width, height, calculateBlackPoints, bitMatrix);
            this.matrix = bitMatrix;
        } else {
            this.matrix = super.getBlackMatrix();
        }
        return this.matrix;
    }
}

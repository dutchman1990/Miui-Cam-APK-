package com.google.zxing.pdf417.decoder;

import com.google.zxing.pdf417.PDF417Common;
import java.lang.reflect.Array;

final class PDF417CodewordDecoder {
    private static final float[][] RATIOS_TABLE;

    static {
        int length = PDF417Common.SYMBOL_TABLE.length;
        RATIOS_TABLE = (float[][]) Array.newInstance(Float.TYPE, new int[]{length, 8});
        for (int i = 0; i < PDF417Common.SYMBOL_TABLE.length; i++) {
            int i2 = PDF417Common.SYMBOL_TABLE[i];
            int i3 = i2 & 1;
            for (int i4 = 0; i4 < 8; i4++) {
                float f = 0.0f;
                while ((i2 & 1) == i3) {
                    f += 1.0f;
                    i2 >>= 1;
                }
                i3 = i2 & 1;
                RATIOS_TABLE[i][(8 - i4) - 1] = f / 17.0f;
            }
        }
    }

    private PDF417CodewordDecoder() {
    }

    private static int getBitValue(int[] iArr) {
        long j = 0;
        for (int i = 0; i < iArr.length; i++) {
            for (int i2 = 0; i2 < iArr[i]; i2++) {
                j = (j << 1) | ((long) (i % 2 != 0 ? 0 : 1));
            }
        }
        return (int) j;
    }

    private static int getClosestDecodedValue(int[] iArr) {
        int bitCountSum = PDF417Common.getBitCountSum(iArr);
        float[] fArr = new float[8];
        for (int i = 0; i < fArr.length; i++) {
            fArr[i] = ((float) iArr[i]) / ((float) bitCountSum);
        }
        float f = Float.MAX_VALUE;
        int i2 = -1;
        for (int i3 = 0; i3 < RATIOS_TABLE.length; i3++) {
            float f2 = 0.0f;
            float[] fArr2 = RATIOS_TABLE[i3];
            for (int i4 = 0; i4 < 8; i4++) {
                float f3 = fArr2[i4] - fArr[i4];
                f2 += f3 * f3;
                if (f2 >= f) {
                    break;
                }
            }
            if (f2 < f) {
                f = f2;
                i2 = PDF417Common.SYMBOL_TABLE[i3];
            }
        }
        return i2;
    }

    private static int getDecodedCodewordValue(int[] iArr) {
        int bitValue = getBitValue(iArr);
        return PDF417Common.getCodeword(bitValue) != -1 ? bitValue : -1;
    }

    static int getDecodedValue(int[] iArr) {
        int decodedCodewordValue = getDecodedCodewordValue(sampleBitCounts(iArr));
        return decodedCodewordValue == -1 ? getClosestDecodedValue(iArr) : decodedCodewordValue;
    }

    private static int[] sampleBitCounts(int[] iArr) {
        float bitCountSum = (float) PDF417Common.getBitCountSum(iArr);
        int[] iArr2 = new int[8];
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < 17; i3++) {
            if (((float) (iArr[i] + i2)) <= (bitCountSum / 34.0f) + ((((float) i3) * bitCountSum) / 17.0f)) {
                i2 += iArr[i];
                i++;
            }
            iArr2[i] = iArr2[i] + 1;
        }
        return iArr2;
    }
}

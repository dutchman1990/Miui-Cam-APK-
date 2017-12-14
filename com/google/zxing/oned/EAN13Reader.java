package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

public final class EAN13Reader extends UPCEANReader {
    static final int[] FIRST_DIGIT_ENCODINGS;
    private final int[] decodeMiddleCounters = new int[4];

    static {
        int[] iArr = new int[10];
        iArr[1] = 11;
        iArr[2] = 13;
        iArr[3] = 14;
        iArr[4] = 19;
        iArr[5] = 25;
        iArr[6] = 28;
        iArr[7] = 21;
        iArr[8] = 22;
        iArr[9] = 26;
        FIRST_DIGIT_ENCODINGS = iArr;
    }

    private static void determineFirstDigit(StringBuilder stringBuilder, int i) throws NotFoundException {
        int i2 = 0;
        while (i2 < 10) {
            if (i != FIRST_DIGIT_ENCODINGS[i2]) {
                i2++;
            } else {
                stringBuilder.insert(0, (char) (i2 + 48));
                return;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected int decodeMiddle(BitArray bitArray, int[] iArr, StringBuilder stringBuilder) throws NotFoundException {
        int i;
        int[] iArr2 = this.decodeMiddleCounters;
        iArr2[0] = 0;
        iArr2[1] = 0;
        iArr2[2] = 0;
        iArr2[3] = 0;
        int size = bitArray.getSize();
        int i2 = iArr[1];
        int i3 = 0;
        for (i = 0; i < 6 && i2 < size; i++) {
            int decodeDigit = UPCEANReader.decodeDigit(bitArray, iArr2, i2, L_AND_G_PATTERNS);
            stringBuilder.append((char) ((decodeDigit % 10) + 48));
            for (int i4 : iArr2) {
                i2 += i4;
            }
            if (decodeDigit >= 10) {
                i3 |= 1 << (5 - i);
            }
        }
        determineFirstDigit(stringBuilder, i3);
        i2 = UPCEANReader.findGuardPattern(bitArray, i2, true, MIDDLE_PATTERN)[1];
        for (i = 0; i < 6 && i2 < size; i++) {
            stringBuilder.append((char) (UPCEANReader.decodeDigit(bitArray, iArr2, i2, L_PATTERNS) + 48));
            for (int i42 : iArr2) {
                i2 += i42;
            }
        }
        return i2;
    }

    BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_13;
    }
}

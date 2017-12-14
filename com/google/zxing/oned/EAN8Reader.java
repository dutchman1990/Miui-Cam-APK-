package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

public final class EAN8Reader extends UPCEANReader {
    private final int[] decodeMiddleCounters = new int[4];

    protected int decodeMiddle(BitArray bitArray, int[] iArr, StringBuilder stringBuilder) throws NotFoundException {
        int i;
        int[] iArr2 = this.decodeMiddleCounters;
        iArr2[0] = 0;
        iArr2[1] = 0;
        iArr2[2] = 0;
        iArr2[3] = 0;
        int size = bitArray.getSize();
        int i2 = iArr[1];
        for (i = 0; i < 4 && i2 < size; i++) {
            stringBuilder.append((char) (UPCEANReader.decodeDigit(bitArray, iArr2, i2, L_PATTERNS) + 48));
            for (int i3 : iArr2) {
                i2 += i3;
            }
        }
        i2 = UPCEANReader.findGuardPattern(bitArray, i2, true, MIDDLE_PATTERN)[1];
        for (i = 0; i < 4 && i2 < size; i++) {
            stringBuilder.append((char) (UPCEANReader.decodeDigit(bitArray, iArr2, i2, L_PATTERNS) + 48));
            for (int i32 : iArr2) {
                i2 += i32;
            }
        }
        return i2;
    }

    BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_8;
    }
}

package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Map;

public final class ITFReader extends OneDReader {
    private static final int[] DEFAULT_ALLOWED_LENGTHS = new int[]{6, 8, 10, 12, 14};
    private static final int[] END_PATTERN_REVERSED = new int[]{1, 1, 3};
    static final int[][] PATTERNS;
    private static final int[] START_PATTERN = new int[]{1, 1, 1, 1};
    private int narrowLineWidth = -1;

    static {
        r0 = new int[10][];
        r0[0] = new int[]{1, 1, 3, 3, 1};
        r0[1] = new int[]{3, 1, 1, 1, 3};
        r0[2] = new int[]{1, 3, 1, 1, 3};
        r0[3] = new int[]{3, 3, 1, 1, 1};
        r0[4] = new int[]{1, 1, 3, 1, 3};
        r0[5] = new int[]{3, 1, 3, 1, 1};
        r0[6] = new int[]{1, 3, 3, 1, 1};
        r0[7] = new int[]{1, 1, 1, 3, 3};
        r0[8] = new int[]{3, 1, 1, 3, 1};
        r0[9] = new int[]{1, 3, 1, 3, 1};
        PATTERNS = r0;
    }

    private static int decodeDigit(int[] iArr) throws NotFoundException {
        float f = 0.38f;
        int i = -1;
        int length = PATTERNS.length;
        for (int i2 = 0; i2 < length; i2++) {
            float patternMatchVariance = OneDReader.patternMatchVariance(iArr, PATTERNS[i2], 0.78f);
            if (patternMatchVariance < f) {
                f = patternMatchVariance;
                i = i2;
            }
        }
        if (i >= 0) {
            return i;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void decodeMiddle(BitArray bitArray, int i, int i2, StringBuilder stringBuilder) throws NotFoundException {
        int[] iArr = new int[10];
        int[] iArr2 = new int[5];
        int[] iArr3 = new int[5];
        while (i < i2) {
            OneDReader.recordPattern(bitArray, i, iArr);
            for (int i3 = 0; i3 < 5; i3++) {
                int i4 = i3 * 2;
                iArr2[i3] = iArr[i4];
                iArr3[i3] = iArr[i4 + 1];
            }
            stringBuilder.append((char) (decodeDigit(iArr2) + 48));
            stringBuilder.append((char) (decodeDigit(iArr3) + 48));
            for (int i5 : iArr) {
                i += i5;
            }
        }
    }

    private static int[] findGuardPattern(BitArray bitArray, int i, int[] iArr) throws NotFoundException {
        int length = iArr.length;
        Object obj = new int[length];
        int size = bitArray.getSize();
        int i2 = 0;
        int i3 = 0;
        int i4 = i;
        for (int i5 = i; i5 < size; i5++) {
            if ((bitArray.get(i5) ^ i2) == 0) {
                if (i3 != length - 1) {
                    i3++;
                } else if (OneDReader.patternMatchVariance(obj, iArr, 0.78f) < 0.38f) {
                    return new int[]{i4, i5};
                } else {
                    i4 += obj[0] + obj[1];
                    System.arraycopy(obj, 2, obj, 0, length - 2);
                    obj[length - 2] = null;
                    obj[length - 1] = null;
                    i3--;
                }
                obj[i3] = 1;
                i2 = i2 == 0 ? 1 : 0;
            } else {
                obj[i3] = obj[i3] + 1;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int skipWhiteSpace(BitArray bitArray) throws NotFoundException {
        int size = bitArray.getSize();
        int nextSet = bitArray.getNextSet(0);
        if (nextSet != size) {
            return nextSet;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void validateQuietZone(BitArray bitArray, int i) throws NotFoundException {
        int i2 = this.narrowLineWidth * 10;
        if (i2 >= i) {
            i2 = i;
        }
        int i3 = i - 1;
        while (i2 > 0 && i3 >= 0 && !bitArray.get(i3)) {
            i2--;
            i3--;
        }
        if (i2 != 0) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    int[] decodeEnd(BitArray bitArray) throws NotFoundException {
        bitArray.reverse();
        try {
            int[] findGuardPattern = findGuardPattern(bitArray, skipWhiteSpace(bitArray), END_PATTERN_REVERSED);
            validateQuietZone(bitArray, findGuardPattern[0]);
            int i = findGuardPattern[0];
            findGuardPattern[0] = bitArray.getSize() - findGuardPattern[1];
            findGuardPattern[1] = bitArray.getSize() - i;
            return findGuardPattern;
        } finally {
            bitArray.reverse();
        }
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws FormatException, NotFoundException {
        int[] decodeStart = decodeStart(bitArray);
        int[] decodeEnd = decodeEnd(bitArray);
        StringBuilder stringBuilder = new StringBuilder(20);
        decodeMiddle(bitArray, decodeStart[1], decodeEnd[0], stringBuilder);
        String stringBuilder2 = stringBuilder.toString();
        int[] iArr = null;
        if (map != null) {
            iArr = (int[]) map.get(DecodeHintType.ALLOWED_LENGTHS);
        }
        if (iArr == null) {
            iArr = DEFAULT_ALLOWED_LENGTHS;
        }
        int length = stringBuilder2.length();
        Object obj = null;
        int i2 = 0;
        for (int i3 : r3) {
            if (length == i3) {
                obj = 1;
                break;
            }
            if (i3 > i2) {
                i2 = i3;
            }
        }
        if (obj == null && length > i2) {
            obj = 1;
        }
        if (obj != null) {
            return new Result(stringBuilder2, null, new ResultPoint[]{new ResultPoint((float) decodeStart[1], (float) i), new ResultPoint((float) decodeEnd[0], (float) i)}, BarcodeFormat.ITF);
        }
        throw FormatException.getFormatInstance();
    }

    int[] decodeStart(BitArray bitArray) throws NotFoundException {
        int[] findGuardPattern = findGuardPattern(bitArray, skipWhiteSpace(bitArray), START_PATTERN);
        this.narrowLineWidth = (findGuardPattern[1] - findGuardPattern[0]) / 4;
        validateQuietZone(bitArray, findGuardPattern[0]);
        return findGuardPattern;
    }
}

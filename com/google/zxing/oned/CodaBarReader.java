package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class CodaBarReader extends OneDReader {
    static final char[] ALPHABET = "0123456789-$:/.+ABCD".toCharArray();
    static final int[] CHARACTER_ENCODINGS = new int[]{3, 6, 9, 96, 18, 66, 33, 36, 48, 72, 12, 24, 69, 81, 84, 21, 26, 41, 11, 14};
    private static final char[] STARTEND_ENCODING = new char[]{'A', 'B', 'C', 'D'};
    private int counterLength = 0;
    private int[] counters = new int[80];
    private final StringBuilder decodeRowResult = new StringBuilder(20);

    static boolean arrayContains(char[] cArr, char c) {
        if (cArr != null) {
            for (char c2 : cArr) {
                if (c2 == c) {
                    return true;
                }
            }
        }
        return false;
    }

    private void counterAppend(int i) {
        this.counters[this.counterLength] = i;
        this.counterLength++;
        if (this.counterLength >= this.counters.length) {
            Object obj = new int[(this.counterLength * 2)];
            System.arraycopy(this.counters, 0, obj, 0, this.counterLength);
            this.counters = obj;
        }
    }

    private int findStartPattern() throws NotFoundException {
        int i = 1;
        while (i < this.counterLength) {
            int toNarrowWidePattern = toNarrowWidePattern(i);
            if (toNarrowWidePattern != -1 && arrayContains(STARTEND_ENCODING, ALPHABET[toNarrowWidePattern])) {
                int i2 = 0;
                for (int i3 = i; i3 < i + 7; i3++) {
                    i2 += this.counters[i3];
                }
                if (i == 1 || this.counters[i - 1] >= i2 / 2) {
                    return i;
                }
            }
            i += 2;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void setCounters(BitArray bitArray) throws NotFoundException {
        this.counterLength = 0;
        int nextUnset = bitArray.getNextUnset(0);
        int size = bitArray.getSize();
        if (nextUnset < size) {
            int i = 1;
            int i2 = 0;
            while (nextUnset < size) {
                if ((bitArray.get(nextUnset) ^ i) == 0) {
                    counterAppend(i2);
                    i2 = 1;
                    i = i == 0 ? 1 : 0;
                } else {
                    i2++;
                }
                nextUnset++;
            }
            counterAppend(i2);
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private int toNarrowWidePattern(int i) {
        int i2 = i + 7;
        if (i2 >= this.counterLength) {
            return -1;
        }
        int i3;
        int i4;
        int[] iArr = this.counters;
        int i5 = 0;
        int i6 = Integer.MAX_VALUE;
        for (i3 = i; i3 < i2; i3 += 2) {
            int i7 = iArr[i3];
            if (i7 < i6) {
                i6 = i7;
            }
            if (i7 > i5) {
                i5 = i7;
            }
        }
        int i8 = (i6 + i5) / 2;
        int i9 = 0;
        int i10 = Integer.MAX_VALUE;
        for (i3 = i + 1; i3 < i2; i3 += 2) {
            i7 = iArr[i3];
            if (i7 < i10) {
                i10 = i7;
            }
            if (i7 > i9) {
                i9 = i7;
            }
        }
        int i11 = (i10 + i9) / 2;
        int i12 = 128;
        int i13 = 0;
        for (i4 = 0; i4 < 7; i4++) {
            i12 >>= 1;
            if (iArr[i + i4] > ((i4 & 1) != 0 ? i11 : i8)) {
                i13 |= i12;
            }
        }
        for (i4 = 0; i4 < CHARACTER_ENCODINGS.length; i4++) {
            if (CHARACTER_ENCODINGS[i4] == i13) {
                return i4;
            }
        }
        return -1;
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException {
        int i2;
        Arrays.fill(this.counters, 0);
        setCounters(bitArray);
        int findStartPattern = findStartPattern();
        int i3 = findStartPattern;
        this.decodeRowResult.setLength(0);
        do {
            int toNarrowWidePattern = toNarrowWidePattern(i3);
            if (toNarrowWidePattern != -1) {
                this.decodeRowResult.append((char) toNarrowWidePattern);
                i3 += 8;
                if (this.decodeRowResult.length() > 1) {
                    if (arrayContains(STARTEND_ENCODING, ALPHABET[toNarrowWidePattern])) {
                        break;
                    }
                }
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } while (i3 < this.counterLength);
        int i4 = this.counters[i3 - 1];
        int i5 = 0;
        for (i2 = -8; i2 < -1; i2++) {
            i5 += this.counters[i3 + i2];
        }
        if (i3 < this.counterLength && i4 < i5 / 2) {
            throw NotFoundException.getNotFoundInstance();
        }
        validatePattern(findStartPattern);
        for (i2 = 0; i2 < this.decodeRowResult.length(); i2++) {
            this.decodeRowResult.setCharAt(i2, ALPHABET[this.decodeRowResult.charAt(i2)]);
        }
        if (arrayContains(STARTEND_ENCODING, this.decodeRowResult.charAt(0))) {
            if (!arrayContains(STARTEND_ENCODING, this.decodeRowResult.charAt(this.decodeRowResult.length() - 1))) {
                throw NotFoundException.getNotFoundInstance();
            } else if (this.decodeRowResult.length() > 3) {
                int i6;
                float f;
                float f2;
                if (map != null) {
                    if (map.containsKey(DecodeHintType.RETURN_CODABAR_START_END)) {
                        i6 = 0;
                        for (i2 = 0; i2 < findStartPattern; i2++) {
                            i6 += this.counters[i2];
                        }
                        f = (float) i6;
                        for (i2 = findStartPattern; i2 < i3 - 1; i2++) {
                            i6 += this.counters[i2];
                        }
                        f2 = (float) i6;
                        return new Result(this.decodeRowResult.toString(), null, new ResultPoint[]{new ResultPoint(f, (float) i), new ResultPoint(f2, (float) i)}, BarcodeFormat.CODABAR);
                    }
                }
                this.decodeRowResult.deleteCharAt(this.decodeRowResult.length() - 1);
                this.decodeRowResult.deleteCharAt(0);
                i6 = 0;
                for (i2 = 0; i2 < findStartPattern; i2++) {
                    i6 += this.counters[i2];
                }
                f = (float) i6;
                for (i2 = findStartPattern; i2 < i3 - 1; i2++) {
                    i6 += this.counters[i2];
                }
                f2 = (float) i6;
                return new Result(this.decodeRowResult.toString(), null, new ResultPoint[]{new ResultPoint(f, (float) i), new ResultPoint(f2, (float) i)}, BarcodeFormat.CODABAR);
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    void validatePattern(int i) throws NotFoundException {
        int i2;
        int[] iArr = new int[4];
        int[] iArr2 = new int[4];
        int length = this.decodeRowResult.length() - 1;
        int i3 = i;
        int i4 = 0;
        while (true) {
            int i5 = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i4)];
            for (i2 = 6; i2 >= 0; i2--) {
                int i6 = (i2 & 1) + ((i5 & 1) * 2);
                iArr[i6] = iArr[i6] + this.counters[i3 + i2];
                iArr2[i6] = iArr2[i6] + 1;
                i5 >>= 1;
            }
            if (i4 >= length) {
                break;
            }
            i3 += 8;
            i4++;
        }
        float[] fArr = new float[4];
        float[] fArr2 = new float[4];
        for (i4 = 0; i4 < 2; i4++) {
            fArr2[i4] = 0.0f;
            fArr2[i4 + 2] = ((((float) iArr[i4]) / ((float) iArr2[i4])) + (((float) iArr[i4 + 2]) / ((float) iArr2[i4 + 2]))) / 2.0f;
            fArr[i4] = fArr2[i4 + 2];
            fArr[i4 + 2] = ((((float) iArr[i4 + 2]) * 2.0f) + 1.5f) / ((float) iArr2[i4 + 2]);
        }
        i3 = i;
        i4 = 0;
        loop3:
        while (true) {
            i5 = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i4)];
            i2 = 6;
            while (i2 >= 0) {
                i6 = (i2 & 1) + ((i5 & 1) * 2);
                int i7 = this.counters[i3 + i2];
                if ((((float) i7) < fArr2[i6] ? 1 : null) == null && ((float) i7) <= fArr[i6]) {
                    i5 >>= 1;
                    i2--;
                }
            }
            if (i4 < length) {
                i3 += 8;
                i4++;
            } else {
                return;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}

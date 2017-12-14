package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class Code39Reader extends OneDReader {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".toCharArray();
    private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[39];
    static final int[] CHARACTER_ENCODINGS = new int[]{52, 289, 97, 352, 49, 304, 112, 37, 292, 100, 265, 73, 328, 25, 280, 88, 13, 268, 76, 28, 259, 67, 322, 19, 274, 82, 7, 262, 70, 22, 385, 193, 448, 145, 400, 208, 133, 388, 196, 148, 168, 162, 138, 42};
    private final int[] counters;
    private final StringBuilder decodeRowResult;
    private final boolean extendedMode;
    private final boolean usingCheckDigit;

    public Code39Reader() {
        this(false);
    }

    public Code39Reader(boolean z) {
        this(z, false);
    }

    public Code39Reader(boolean z, boolean z2) {
        this.usingCheckDigit = z;
        this.extendedMode = z2;
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[9];
    }

    private static String decodeExtended(CharSequence charSequence) throws FormatException {
        int length = charSequence.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt == '+' || charAt == '$' || charAt == '%' || charAt == '/') {
                char charAt2 = charSequence.charAt(i + 1);
                char c = '\u0000';
                switch (charAt) {
                    case '$':
                        if (charAt2 >= 'A' && charAt2 <= 'Z') {
                            c = (char) (charAt2 - 64);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    case '%':
                        if (charAt2 >= 'A' && charAt2 <= 'E') {
                            c = (char) (charAt2 - 38);
                            break;
                        } else if (charAt2 >= 'F' && charAt2 <= 'W') {
                            c = (char) (charAt2 - 11);
                            break;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        break;
                    case '+':
                        if (charAt2 >= 'A' && charAt2 <= 'Z') {
                            c = (char) (charAt2 + 32);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    case '/':
                        if (charAt2 < 'A' || charAt2 > 'O') {
                            if (charAt2 == 'Z') {
                                c = ':';
                                break;
                            }
                            throw FormatException.getFormatInstance();
                        }
                        c = (char) (charAt2 - 32);
                        break;
                }
                stringBuilder.append(c);
                i++;
            } else {
                stringBuilder.append(charAt);
            }
            i++;
        }
        return stringBuilder.toString();
    }

    private static int[] findAsteriskPattern(BitArray bitArray, int[] iArr) throws NotFoundException {
        int size = bitArray.getSize();
        int nextSet = bitArray.getNextSet(0);
        int i = 0;
        int i2 = nextSet;
        int i3 = 0;
        int length = iArr.length;
        int i4 = nextSet;
        while (i4 < size) {
            if ((bitArray.get(i4) ^ i3) == 0) {
                if (i != length - 1) {
                    i++;
                } else if (toNarrowWidePattern(iArr) == ASTERISK_ENCODING && bitArray.isRange(Math.max(0, i2 - ((i4 - i2) / 2)), i2, false)) {
                    return new int[]{i2, i4};
                } else {
                    i2 += iArr[0] + iArr[1];
                    System.arraycopy(iArr, 2, iArr, 0, length - 2);
                    iArr[length - 2] = 0;
                    iArr[length - 1] = 0;
                    i--;
                }
                iArr[i] = 1;
                i3 = i3 == 0 ? 1 : 0;
            } else {
                iArr[i] = iArr[i] + 1;
            }
            i4++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static char patternToChar(int i) throws NotFoundException {
        for (int i2 = 0; i2 < CHARACTER_ENCODINGS.length; i2++) {
            if (CHARACTER_ENCODINGS[i2] == i) {
                return ALPHABET[i2];
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toNarrowWidePattern(int[] iArr) {
        int length = iArr.length;
        int i = 0;
        int i2;
        do {
            int i3;
            int i4 = Integer.MAX_VALUE;
            for (int i5 : iArr) {
                int i52;
                if (i52 < i4 && i52 > r2) {
                    i4 = i52;
                }
            }
            i = i4;
            i2 = 0;
            int i6 = 0;
            int i7 = 0;
            for (i3 = 0; i3 < length; i3++) {
                i52 = iArr[i3];
                if (i52 > i) {
                    i7 |= 1 << ((length - 1) - i3);
                    i2++;
                    i6 += i52;
                }
            }
            if (i2 == 3) {
                for (i3 = 0; i3 < length && i2 > 0; i3++) {
                    i52 = iArr[i3];
                    if (i52 > i) {
                        i2--;
                        if (i52 * 2 >= i6) {
                            return -1;
                        }
                    }
                }
                return i7;
            }
        } while (i2 > 3);
        return -1;
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        int i2;
        int[] iArr = this.counters;
        Arrays.fill(iArr, 0);
        StringBuilder stringBuilder = this.decodeRowResult;
        stringBuilder.setLength(0);
        int[] findAsteriskPattern = findAsteriskPattern(bitArray, iArr);
        int nextSet = bitArray.getNextSet(findAsteriskPattern[1]);
        int size = bitArray.getSize();
        char patternToChar;
        do {
            OneDReader.recordPattern(bitArray, nextSet, iArr);
            int toNarrowWidePattern = toNarrowWidePattern(iArr);
            if (toNarrowWidePattern >= 0) {
                patternToChar = patternToChar(toNarrowWidePattern);
                stringBuilder.append(patternToChar);
                i2 = nextSet;
                for (int i3 : iArr) {
                    nextSet += i3;
                }
                nextSet = bitArray.getNextSet(nextSet);
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } while (patternToChar != '*');
        stringBuilder.setLength(stringBuilder.length() - 1);
        int i4 = 0;
        for (int i32 : iArr) {
            i4 += i32;
        }
        int i5 = (nextSet - i2) - i4;
        if (nextSet != size && i5 * 2 < i4) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (this.usingCheckDigit) {
            int length = stringBuilder.length() - 1;
            int i6 = 0;
            for (int i7 = 0; i7 < length; i7++) {
                i6 += "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(this.decodeRowResult.charAt(i7));
            }
            if (stringBuilder.charAt(length) == ALPHABET[i6 % 43]) {
                stringBuilder.setLength(length);
            } else {
                throw ChecksumException.getChecksumInstance();
            }
        }
        if (stringBuilder.length() != 0) {
            float f = ((float) (findAsteriskPattern[1] + findAsteriskPattern[0])) / 2.0f;
            float f2 = ((float) i2) + (((float) i4) / 2.0f);
            return new Result(!this.extendedMode ? stringBuilder.toString() : decodeExtended(stringBuilder), null, new ResultPoint[]{new ResultPoint(f, (float) i), new ResultPoint(f2, (float) i)}, BarcodeFormat.CODE_39);
        }
        throw NotFoundException.getNotFoundInstance();
    }
}

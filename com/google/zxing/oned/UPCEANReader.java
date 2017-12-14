package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public abstract class UPCEANReader extends OneDReader {
    static final int[][] L_AND_G_PATTERNS = new int[20][];
    static final int[][] L_PATTERNS;
    static final int[] MIDDLE_PATTERN = new int[]{1, 1, 1, 1, 1};
    static final int[] START_END_PATTERN = new int[]{1, 1, 1};
    private final StringBuilder decodeRowStringBuffer = new StringBuilder(20);
    private final EANManufacturerOrgSupport eanManSupport = new EANManufacturerOrgSupport();
    private final UPCEANExtensionSupport extensionReader = new UPCEANExtensionSupport();

    static {
        r4 = new int[10][];
        r4[0] = new int[]{3, 2, 1, 1};
        r4[1] = new int[]{2, 2, 2, 1};
        r4[2] = new int[]{2, 1, 2, 2};
        r4[3] = new int[]{1, 4, 1, 1};
        r4[4] = new int[]{1, 1, 3, 2};
        r4[5] = new int[]{1, 2, 3, 1};
        r4[6] = new int[]{1, 1, 1, 4};
        r4[7] = new int[]{1, 3, 1, 2};
        r4[8] = new int[]{1, 2, 1, 3};
        r4[9] = new int[]{3, 1, 1, 2};
        L_PATTERNS = r4;
        System.arraycopy(L_PATTERNS, 0, L_AND_G_PATTERNS, 0, 10);
        for (int i = 10; i < 20; i++) {
            int[] iArr = L_PATTERNS[i - 10];
            int[] iArr2 = new int[iArr.length];
            for (int i2 = 0; i2 < iArr.length; i2++) {
                iArr2[i2] = iArr[(iArr.length - i2) - 1];
            }
            L_AND_G_PATTERNS[i] = iArr2;
        }
    }

    protected UPCEANReader() {
    }

    static boolean checkStandardUPCEANChecksum(CharSequence charSequence) throws FormatException {
        int length = charSequence.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        int i2 = length - 2;
        while (i2 >= 0) {
            int charAt = charSequence.charAt(i2) - 48;
            if (charAt >= 0 && charAt <= 9) {
                i += charAt;
                i2 -= 2;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        i *= 3;
        i2 = length - 1;
        while (i2 >= 0) {
            charAt = charSequence.charAt(i2) - 48;
            if (charAt >= 0 && charAt <= 9) {
                i += charAt;
                i2 -= 2;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        return i % 10 == 0;
    }

    static int decodeDigit(BitArray bitArray, int[] iArr, int i, int[][] iArr2) throws NotFoundException {
        OneDReader.recordPattern(bitArray, i, iArr);
        float f = 0.48f;
        int i2 = -1;
        int length = iArr2.length;
        for (int i3 = 0; i3 < length; i3++) {
            float patternMatchVariance = OneDReader.patternMatchVariance(iArr, iArr2[i3], 0.7f);
            if (patternMatchVariance < f) {
                f = patternMatchVariance;
                i2 = i3;
            }
        }
        if (i2 >= 0) {
            return i2;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    static int[] findGuardPattern(BitArray bitArray, int i, boolean z, int[] iArr) throws NotFoundException {
        return findGuardPattern(bitArray, i, z, iArr, new int[iArr.length]);
    }

    private static int[] findGuardPattern(BitArray bitArray, int i, boolean z, int[] iArr, int[] iArr2) throws NotFoundException {
        int length = iArr.length;
        int size = bitArray.getSize();
        int i2 = z;
        i = !z ? bitArray.getNextSet(i) : bitArray.getNextUnset(i);
        int i3 = 0;
        int i4 = i;
        for (int i5 = i; i5 < size; i5++) {
            if ((bitArray.get(i5) ^ i2) == 0) {
                if (i3 != length - 1) {
                    i3++;
                } else if (OneDReader.patternMatchVariance(iArr2, iArr, 0.7f) < 0.48f) {
                    return new int[]{i4, i5};
                } else {
                    i4 += iArr2[0] + iArr2[1];
                    System.arraycopy(iArr2, 2, iArr2, 0, length - 2);
                    iArr2[length - 2] = 0;
                    iArr2[length - 1] = 0;
                    i3--;
                }
                iArr2[i3] = 1;
                i2 = i2 == 0 ? 1 : 0;
            } else {
                iArr2[i3] = iArr2[i3] + 1;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    static int[] findStartGuardPattern(BitArray bitArray) throws NotFoundException {
        boolean z = false;
        int[] iArr = null;
        int i = 0;
        int[] iArr2 = new int[START_END_PATTERN.length];
        while (!z) {
            Arrays.fill(iArr2, 0, START_END_PATTERN.length, 0);
            iArr = findGuardPattern(bitArray, i, false, START_END_PATTERN, iArr2);
            int i2 = iArr[0];
            i = iArr[1];
            int i3 = i2 - (i - i2);
            if (i3 >= 0) {
                z = bitArray.isRange(i3, i2, false);
            }
        }
        return iArr;
    }

    boolean checkChecksum(String str) throws FormatException {
        return checkStandardUPCEANChecksum(str);
    }

    int[] decodeEnd(BitArray bitArray, int i) throws NotFoundException {
        return findGuardPattern(bitArray, i, false, START_END_PATTERN);
    }

    protected abstract int decodeMiddle(BitArray bitArray, int[] iArr, StringBuilder stringBuilder) throws NotFoundException;

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        return decodeRow(i, bitArray, findStartGuardPattern(bitArray), map);
    }

    public Result decodeRow(int i, BitArray bitArray, int[] iArr, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        ResultPointCallback resultPointCallback = map != null ? (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK) : null;
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (iArr[0] + iArr[1])) / 2.0f, (float) i));
        }
        StringBuilder stringBuilder = this.decodeRowStringBuffer;
        stringBuilder.setLength(0);
        int decodeMiddle = decodeMiddle(bitArray, iArr, stringBuilder);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint((float) decodeMiddle, (float) i));
        }
        int[] decodeEnd = decodeEnd(bitArray, decodeMiddle);
        if (resultPointCallback != null) {
            resultPointCallback.foundPossibleResultPoint(new ResultPoint(((float) (decodeEnd[0] + decodeEnd[1])) / 2.0f, (float) i));
        }
        int i2 = decodeEnd[1];
        int i3 = i2 + (i2 - decodeEnd[0]);
        if (i3 < bitArray.getSize() && bitArray.isRange(i2, i3, false)) {
            String stringBuilder2 = stringBuilder.toString();
            if (stringBuilder2.length() < 8) {
                throw FormatException.getFormatInstance();
            } else if (checkChecksum(stringBuilder2)) {
                float f = ((float) (iArr[1] + iArr[0])) / 2.0f;
                float f2 = ((float) (decodeEnd[1] + decodeEnd[0])) / 2.0f;
                BarcodeFormat barcodeFormat = getBarcodeFormat();
                Result result = new Result(stringBuilder2, null, new ResultPoint[]{new ResultPoint(f, (float) i), new ResultPoint(f2, (float) i)}, barcodeFormat);
                int i4 = 0;
                try {
                    Result decodeRow = this.extensionReader.decodeRow(i, bitArray, decodeEnd[1]);
                    result.putMetadata(ResultMetadataType.UPC_EAN_EXTENSION, decodeRow.getText());
                    result.putAllMetadata(decodeRow.getResultMetadata());
                    result.addResultPoints(decodeRow.getResultPoints());
                    i4 = decodeRow.getText().length();
                } catch (ReaderException e) {
                }
                int[] iArr2 = map != null ? (int[]) map.get(DecodeHintType.ALLOWED_EAN_EXTENSIONS) : null;
                if (iArr2 != null) {
                    Object obj = null;
                    for (int i5 : iArr2) {
                        if (i4 == i5) {
                            obj = 1;
                            break;
                        }
                    }
                    if (obj == null) {
                        throw NotFoundException.getNotFoundInstance();
                    }
                }
                if (barcodeFormat == BarcodeFormat.EAN_13 || barcodeFormat == BarcodeFormat.UPC_A) {
                    String lookupCountryIdentifier = this.eanManSupport.lookupCountryIdentifier(stringBuilder2);
                    if (lookupCountryIdentifier != null) {
                        result.putMetadata(ResultMetadataType.POSSIBLE_COUNTRY, lookupCountryIdentifier);
                    }
                }
                return result;
            } else {
                throw ChecksumException.getChecksumInstance();
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    abstract BarcodeFormat getBarcodeFormat();
}

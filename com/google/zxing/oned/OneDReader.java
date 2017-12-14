package com.google.zxing.oned;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class OneDReader implements Reader {
    private Result doDecode(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException {
        int width = binaryBitmap.getWidth();
        int height = binaryBitmap.getHeight();
        BitArray bitArray = new BitArray(width);
        int i = height >> 1;
        Object obj = (map != null && map.containsKey(DecodeHintType.TRY_HARDER)) ? 1 : null;
        int max = Math.max(1, height >> (obj == null ? 5 : 8));
        int i2 = obj == null ? 15 : height;
        for (int i3 = 0; i3 < i2; i3++) {
            int i4 = (i3 + 1) / 2;
            if (((i3 & 1) != 0 ? null : 1) == null) {
                i4 = -i4;
            }
            int i5 = i + (max * i4);
            if (i5 < 0 || i5 >= height) {
                break;
            }
            try {
                bitArray = binaryBitmap.getBlackRow(i5, bitArray);
                int i6 = 0;
                while (i6 < 2) {
                    if (i6 == 1) {
                        bitArray.reverse();
                        if (map != null && map.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
                            Map<DecodeHintType, ?> enumMap = new EnumMap(DecodeHintType.class);
                            enumMap.putAll(map);
                            enumMap.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
                            map = enumMap;
                        }
                    }
                    try {
                        Result decodeRow = decodeRow(i5, bitArray, map);
                        if (i6 == 1) {
                            decodeRow.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(180));
                            ResultPoint[] resultPoints = decodeRow.getResultPoints();
                            if (resultPoints != null) {
                                resultPoints[0] = new ResultPoint((((float) width) - resultPoints[0].getX()) - 1.0f, resultPoints[0].getY());
                                resultPoints[1] = new ResultPoint((((float) width) - resultPoints[1].getX()) - 1.0f, resultPoints[1].getY());
                            }
                        }
                        return decodeRow;
                    } catch (ReaderException e) {
                        i6++;
                    }
                }
                continue;
            } catch (NotFoundException e2) {
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static float patternMatchVariance(int[] iArr, int[] iArr2, float f) {
        int length = iArr.length;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            i += iArr[i3];
            i2 += iArr2[i3];
        }
        if (i < i2) {
            return Float.POSITIVE_INFINITY;
        }
        float f2 = ((float) i) / ((float) i2);
        f *= f2;
        float f3 = 0.0f;
        for (int i4 = 0; i4 < length; i4++) {
            int i5 = iArr[i4];
            float f4 = ((float) iArr2[i4]) * f2;
            float f5 = ((float) i5) > f4 ? ((float) i5) - f4 : f4 - ((float) i5);
            if (f5 > f) {
                return Float.POSITIVE_INFINITY;
            }
            f3 += f5;
        }
        return f3 / ((float) i);
    }

    protected static void recordPattern(BitArray bitArray, int i, int[] iArr) throws NotFoundException {
        int length = iArr.length;
        Arrays.fill(iArr, 0, length, 0);
        int size = bitArray.getSize();
        if (i < size) {
            int i2 = !bitArray.get(i) ? 1 : 0;
            int i3 = 0;
            int i4 = i;
            while (i4 < size) {
                if ((bitArray.get(i4) ^ i2) == 0) {
                    i3++;
                    if (i3 == length) {
                        break;
                    }
                    iArr[i3] = 1;
                    i2 = i2 == 0 ? 1 : 0;
                } else {
                    iArr[i3] = iArr[i3] + 1;
                }
                i4++;
            }
            if (i3 != length) {
                if (i3 != length - 1 || i4 != size) {
                    throw NotFoundException.getNotFoundInstance();
                }
                return;
            }
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPatternInReverse(BitArray bitArray, int i, int[] iArr) throws NotFoundException {
        int length = iArr.length;
        boolean z = bitArray.get(i);
        while (i > 0 && length >= 0) {
            i--;
            if (bitArray.get(i) != z) {
                length--;
                z = !z;
            }
        }
        if (length < 0) {
            recordPattern(bitArray, i + 1, iArr);
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public Result decode(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        Object obj = null;
        try {
            return doDecode(binaryBitmap, map);
        } catch (NotFoundException e) {
            if (map != null && map.containsKey(DecodeHintType.TRY_HARDER)) {
                obj = 1;
            }
            if (obj != null && binaryBitmap.isRotateSupported()) {
                BinaryBitmap rotateCounterClockwise = binaryBitmap.rotateCounterClockwise();
                Result doDecode = doDecode(rotateCounterClockwise, map);
                Map resultMetadata = doDecode.getResultMetadata();
                int i = 270;
                if (resultMetadata != null && resultMetadata.containsKey(ResultMetadataType.ORIENTATION)) {
                    i = (((Integer) resultMetadata.get(ResultMetadataType.ORIENTATION)).intValue() + 270) % 360;
                }
                doDecode.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(i));
                ResultPoint[] resultPoints = doDecode.getResultPoints();
                if (resultPoints != null) {
                    int height = rotateCounterClockwise.getHeight();
                    for (int i2 = 0; i2 < resultPoints.length; i2++) {
                        resultPoints[i2] = new ResultPoint((((float) height) - resultPoints[i2].getY()) - 1.0f, resultPoints[i2].getX());
                    }
                }
                return doDecode;
            }
            throw e;
        }
    }

    public abstract Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException;

    public void reset() {
    }
}

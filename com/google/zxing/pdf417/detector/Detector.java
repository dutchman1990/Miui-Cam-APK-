package com.google.zxing.pdf417.detector;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Detector {
    private static final int[] INDEXES_START_PATTERN;
    private static final int[] INDEXES_STOP_PATTERN = new int[]{6, 2, 7, 3};
    private static final int[] START_PATTERN = new int[]{8, 1, 1, 1, 1, 1, 1, 3};
    private static final int[] STOP_PATTERN = new int[]{7, 1, 1, 3, 1, 1, 1, 2, 1};

    static {
        int[] iArr = new int[4];
        iArr[1] = 4;
        iArr[2] = 1;
        iArr[3] = 5;
        INDEXES_START_PATTERN = iArr;
    }

    private Detector() {
    }

    private static void copyToResult(ResultPoint[] resultPointArr, ResultPoint[] resultPointArr2, int[] iArr) {
        for (int i = 0; i < iArr.length; i++) {
            resultPointArr[iArr[i]] = resultPointArr2[i];
        }
    }

    public static PDF417DetectorResult detect(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map, boolean z) throws NotFoundException {
        BitMatrix blackMatrix = binaryBitmap.getBlackMatrix();
        List detect = detect(z, blackMatrix);
        if (detect.isEmpty()) {
            blackMatrix = blackMatrix.clone();
            blackMatrix.rotate180();
            detect = detect(z, blackMatrix);
        }
        return new PDF417DetectorResult(blackMatrix, detect);
    }

    private static List<ResultPoint[]> detect(boolean z, BitMatrix bitMatrix) {
        List<ResultPoint[]> arrayList = new ArrayList();
        int i = 0;
        int i2 = 0;
        Object obj = null;
        while (i < bitMatrix.getHeight()) {
            Object findVertices = findVertices(bitMatrix, i, i2);
            if (findVertices[0] == null && findVertices[3] == null) {
                if (obj == null) {
                    break;
                }
                obj = null;
                i2 = 0;
                for (ResultPoint[] resultPointArr : arrayList) {
                    if (resultPointArr[1] != null) {
                        i = (int) Math.max((float) i, resultPointArr[1].getY());
                    }
                    if (resultPointArr[3] != null) {
                        i = Math.max(i, (int) resultPointArr[3].getY());
                    }
                }
                i += 5;
            } else {
                obj = 1;
                arrayList.add(findVertices);
                if (!z) {
                    break;
                } else if (findVertices[2] == null) {
                    i2 = (int) findVertices[4].getX();
                    i = (int) findVertices[4].getY();
                } else {
                    i2 = (int) findVertices[2].getX();
                    i = (int) findVertices[2].getY();
                }
            }
        }
        return arrayList;
    }

    private static int[] findGuardPattern(BitMatrix bitMatrix, int i, int i2, int i3, boolean z, int[] iArr, int[] iArr2) {
        Arrays.fill(iArr2, 0, iArr2.length, 0);
        int length = iArr.length;
        int i4 = z;
        int i5 = i;
        int i6 = 0;
        while (bitMatrix.get(i5, i2)) {
            if (i5 > 0) {
                int i7 = i6 + 1;
                if (i6 >= 3) {
                    i6 = i7;
                    break;
                }
                i5--;
                i6 = i7;
            } else {
                break;
            }
        }
        int i8 = 0;
        for (int i9 = i5; i9 < i3; i9++) {
            if ((bitMatrix.get(i9, i2) ^ i4) == 0) {
                if (i8 != length - 1) {
                    i8++;
                } else if (patternMatchVariance(iArr2, iArr, 0.8f) < 0.42f) {
                    return new int[]{i5, i9};
                } else {
                    i5 += iArr2[0] + iArr2[1];
                    System.arraycopy(iArr2, 2, iArr2, 0, length - 2);
                    iArr2[length - 2] = 0;
                    iArr2[length - 1] = 0;
                    i8--;
                }
                iArr2[i8] = 1;
                i4 = i4 == 0 ? 1 : 0;
            } else {
                iArr2[i8] = iArr2[i8] + 1;
            }
        }
        if (i8 != length - 1 || patternMatchVariance(iArr2, iArr, 0.8f) >= 0.42f) {
            return null;
        }
        return new int[]{i5, i9 - 1};
    }

    private static ResultPoint[] findRowsWithPattern(BitMatrix bitMatrix, int i, int i2, int i3, int i4, int[] iArr) {
        int i5;
        ResultPoint[] resultPointArr = new ResultPoint[4];
        Object obj = null;
        int[] iArr2 = new int[iArr.length];
        while (i3 < i) {
            int[] findGuardPattern;
            int i6;
            int i7;
            int[] findGuardPattern2 = findGuardPattern(bitMatrix, i4, i3, i2, false, iArr, iArr2);
            if (findGuardPattern2 == null) {
                i3 += 5;
            } else {
                while (i3 > 0) {
                    i3--;
                    findGuardPattern = findGuardPattern(bitMatrix, i4, i3, i2, false, iArr, iArr2);
                    if (findGuardPattern == null) {
                        i3++;
                        break;
                    }
                    findGuardPattern2 = findGuardPattern;
                }
                resultPointArr[0] = new ResultPoint((float) findGuardPattern2[0], (float) i3);
                resultPointArr[1] = new ResultPoint((float) findGuardPattern2[1], (float) i3);
                obj = 1;
                i5 = i3 + 1;
                if (obj != null) {
                    i6 = 0;
                    findGuardPattern = new int[]{(int) resultPointArr[0].getX(), (int) resultPointArr[1].getX()};
                    while (i5 < i) {
                        findGuardPattern2 = findGuardPattern(bitMatrix, findGuardPattern[0], i5, i2, false, iArr, iArr2);
                        if (findGuardPattern2 == null || Math.abs(findGuardPattern[0] - findGuardPattern2[0]) >= 5 || Math.abs(findGuardPattern[1] - findGuardPattern2[1]) >= 5) {
                            if (i6 <= 25) {
                                break;
                            }
                            i6++;
                        } else {
                            findGuardPattern = findGuardPattern2;
                            i6 = 0;
                        }
                        i5++;
                    }
                    i5 -= i6 + 1;
                    resultPointArr[2] = new ResultPoint((float) findGuardPattern[0], (float) i5);
                    resultPointArr[3] = new ResultPoint((float) findGuardPattern[1], (float) i5);
                }
                if (i5 - i3 < 10) {
                    for (i7 = 0; i7 < resultPointArr.length; i7++) {
                        resultPointArr[i7] = null;
                    }
                }
                return resultPointArr;
            }
        }
        i5 = i3 + 1;
        if (obj != null) {
            i6 = 0;
            findGuardPattern = new int[]{(int) resultPointArr[0].getX(), (int) resultPointArr[1].getX()};
            while (i5 < i) {
                findGuardPattern2 = findGuardPattern(bitMatrix, findGuardPattern[0], i5, i2, false, iArr, iArr2);
                if (findGuardPattern2 == null) {
                    findGuardPattern = findGuardPattern2;
                    i6 = 0;
                    i5++;
                }
                if (i6 <= 25) {
                    break;
                }
                i6++;
                i5++;
            }
            i5 -= i6 + 1;
            resultPointArr[2] = new ResultPoint((float) findGuardPattern[0], (float) i5);
            resultPointArr[3] = new ResultPoint((float) findGuardPattern[1], (float) i5);
        }
        if (i5 - i3 < 10) {
            for (i7 = 0; i7 < resultPointArr.length; i7++) {
                resultPointArr[i7] = null;
            }
        }
        return resultPointArr;
    }

    private static ResultPoint[] findVertices(BitMatrix bitMatrix, int i, int i2) {
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        ResultPoint[] resultPointArr = new ResultPoint[8];
        copyToResult(resultPointArr, findRowsWithPattern(bitMatrix, height, width, i, i2, START_PATTERN), INDEXES_START_PATTERN);
        if (resultPointArr[4] != null) {
            i2 = (int) resultPointArr[4].getX();
            i = (int) resultPointArr[4].getY();
        }
        copyToResult(resultPointArr, findRowsWithPattern(bitMatrix, height, width, i, i2, STOP_PATTERN), INDEXES_STOP_PATTERN);
        return resultPointArr;
    }

    private static float patternMatchVariance(int[] iArr, int[] iArr2, float f) {
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
}

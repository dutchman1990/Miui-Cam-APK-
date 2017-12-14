package com.google.zxing.pdf417.decoder;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.pdf417.PDF417Common;
import com.google.zxing.pdf417.decoder.ec.ErrorCorrection;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PDF417ScanningDecoder {
    private static final ErrorCorrection errorCorrection = new ErrorCorrection();

    private PDF417ScanningDecoder() {
    }

    private static BoundingBox adjustBoundingBox(DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn) throws NotFoundException, FormatException {
        if (detectionResultRowIndicatorColumn == null) {
            return null;
        }
        int[] rowHeights = detectionResultRowIndicatorColumn.getRowHeights();
        if (rowHeights == null) {
            return null;
        }
        int max = getMax(rowHeights);
        int i = 0;
        for (int i2 : rowHeights) {
            i += max - i2;
            if (i2 > 0) {
                break;
            }
        }
        Codeword[] codewords = detectionResultRowIndicatorColumn.getCodewords();
        int i3 = 0;
        while (i > 0 && codewords[i3] == null) {
            i--;
            i3++;
        }
        int i4 = 0;
        for (i3 = rowHeights.length - 1; i3 >= 0; i3--) {
            i4 += max - rowHeights[i3];
            if (rowHeights[i3] > 0) {
                break;
            }
        }
        i3 = codewords.length - 1;
        while (i4 > 0 && codewords[i3] == null) {
            i4--;
            i3--;
        }
        return detectionResultRowIndicatorColumn.getBoundingBox().addMissingRows(i, i4, detectionResultRowIndicatorColumn.isLeft());
    }

    private static void adjustCodewordCount(DetectionResult detectionResult, BarcodeValue[][] barcodeValueArr) throws NotFoundException {
        int[] value = barcodeValueArr[0][1].getValue();
        int barcodeColumnCount = (detectionResult.getBarcodeColumnCount() * detectionResult.getBarcodeRowCount()) - getNumberOfECCodeWords(detectionResult.getBarcodeECLevel());
        if (value.length != 0) {
            if (value[0] != barcodeColumnCount) {
                barcodeValueArr[0][1].setValue(barcodeColumnCount);
            }
        } else if (barcodeColumnCount >= 1 && barcodeColumnCount <= 928) {
            barcodeValueArr[0][1].setValue(barcodeColumnCount);
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private static int adjustCodewordStartColumn(BitMatrix bitMatrix, int i, int i2, boolean z, int i3, int i4) {
        int i5 = i3;
        int i6 = !z ? 1 : -1;
        for (int i7 = 0; i7 < 2; i7++) {
            while (true) {
                if (!z || i5 < i) {
                    if (!z) {
                        if (i5 >= i2) {
                            break;
                        }
                    }
                    break;
                }
                if (z != bitMatrix.get(i5, i4)) {
                    break;
                } else if (Math.abs(i3 - i5) > 2) {
                    return i3;
                } else {
                    i5 += i6;
                }
            }
            i6 = -i6;
            z = !z;
        }
        return i5;
    }

    private static boolean checkCodewordSkew(int i, int i2, int i3) {
        return i2 + -2 <= i && i <= i3 + 2;
    }

    private static int correctErrors(int[] iArr, int[] iArr2, int i) throws ChecksumException {
        if (iArr2 == null || iArr2.length <= (i / 2) + 3) {
            if (i >= 0 && i <= 512) {
                return errorCorrection.decode(iArr, i, iArr2);
            }
        }
        throw ChecksumException.getChecksumInstance();
    }

    private static BarcodeValue[][] createBarcodeMatrix(DetectionResult detectionResult) throws FormatException {
        int i;
        int barcodeRowCount = detectionResult.getBarcodeRowCount();
        int barcodeColumnCount = detectionResult.getBarcodeColumnCount() + 2;
        BarcodeValue[][] barcodeValueArr = (BarcodeValue[][]) Array.newInstance(BarcodeValue.class, new int[]{barcodeRowCount, barcodeColumnCount});
        for (int i2 = 0; i2 < barcodeValueArr.length; i2++) {
            for (i = 0; i < barcodeValueArr[i2].length; i++) {
                barcodeValueArr[i2][i] = new BarcodeValue();
            }
        }
        i = 0;
        for (DetectionResultColumn detectionResultColumn : detectionResult.getDetectionResultColumns()) {
            if (detectionResultColumn != null) {
                for (Codeword codeword : detectionResultColumn.getCodewords()) {
                    if (codeword != null) {
                        int rowNumber = codeword.getRowNumber();
                        if (rowNumber < 0) {
                            continue;
                        } else if (rowNumber < barcodeValueArr.length) {
                            barcodeValueArr[rowNumber][i].setValue(codeword.getValue());
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                    }
                }
                continue;
            }
            i++;
        }
        return barcodeValueArr;
    }

    private static DecoderResult createDecoderResult(DetectionResult detectionResult) throws FormatException, ChecksumException, NotFoundException {
        BarcodeValue[][] createBarcodeMatrix = createBarcodeMatrix(detectionResult);
        adjustCodewordCount(detectionResult, createBarcodeMatrix);
        Collection arrayList = new ArrayList();
        int[] iArr = new int[(detectionResult.getBarcodeRowCount() * detectionResult.getBarcodeColumnCount())];
        List arrayList2 = new ArrayList();
        Collection arrayList3 = new ArrayList();
        for (int i = 0; i < detectionResult.getBarcodeRowCount(); i++) {
            for (int i2 = 0; i2 < detectionResult.getBarcodeColumnCount(); i2++) {
                Object value = createBarcodeMatrix[i][i2 + 1].getValue();
                int barcodeColumnCount = (detectionResult.getBarcodeColumnCount() * i) + i2;
                if (value.length == 0) {
                    arrayList.add(Integer.valueOf(barcodeColumnCount));
                } else if (value.length != 1) {
                    arrayList3.add(Integer.valueOf(barcodeColumnCount));
                    arrayList2.add(value);
                } else {
                    iArr[barcodeColumnCount] = value[0];
                }
            }
        }
        int[][] iArr2 = new int[arrayList2.size()][];
        for (int i3 = 0; i3 < iArr2.length; i3++) {
            iArr2[i3] = (int[]) arrayList2.get(i3);
        }
        return createDecoderResultFromAmbiguousValues(detectionResult.getBarcodeECLevel(), iArr, PDF417Common.toIntArray(arrayList), PDF417Common.toIntArray(arrayList3), iArr2);
    }

    private static DecoderResult createDecoderResultFromAmbiguousValues(int i, int[] iArr, int[] iArr2, int[] iArr3, int[][] iArr4) throws FormatException, ChecksumException {
        int i2;
        int[] iArr5 = new int[iArr3.length];
        int i3 = 100;
        while (true) {
            int i4 = i3 - 1;
            if (i3 > 0) {
                for (i2 = 0; i2 < iArr5.length; i2++) {
                    iArr[iArr3[i2]] = iArr4[i2][iArr5[i2]];
                }
                try {
                    break;
                } catch (ChecksumException e) {
                    if (iArr5.length != 0) {
                        i2 = 0;
                        while (i2 < iArr5.length) {
                            if (iArr5[i2] < iArr4[i2].length - 1) {
                                iArr5[i2] = iArr5[i2] + 1;
                                i3 = i4;
                                break;
                            }
                            iArr5[i2] = 0;
                            if (i2 != iArr5.length - 1) {
                                i2++;
                            } else {
                                throw ChecksumException.getChecksumInstance();
                            }
                        }
                        i3 = i4;
                    } else {
                        throw ChecksumException.getChecksumInstance();
                    }
                }
            }
            throw ChecksumException.getChecksumInstance();
        }
        return decodeCodewords(iArr, i, iArr2);
    }

    public static DecoderResult decode(BitMatrix bitMatrix, ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4, int i, int i2) throws NotFoundException, FormatException, ChecksumException {
        BoundingBox boundingBox = new BoundingBox(bitMatrix, resultPoint, resultPoint2, resultPoint3, resultPoint4);
        DetectionResultColumn detectionResultColumn = null;
        DetectionResultColumn detectionResultColumn2 = null;
        DetectionResult detectionResult = null;
        int i3 = 0;
        while (i3 < 2) {
            if (resultPoint != null) {
                detectionResultColumn = getRowIndicatorColumn(bitMatrix, boundingBox, resultPoint, true, i, i2);
            }
            if (resultPoint3 != null) {
                detectionResultColumn2 = getRowIndicatorColumn(bitMatrix, boundingBox, resultPoint3, false, i, i2);
            }
            detectionResult = merge(detectionResultColumn, detectionResultColumn2);
            if (detectionResult != null) {
                if (i3 == 0 && detectionResult.getBoundingBox() != null) {
                    if (detectionResult.getBoundingBox().getMinY() < boundingBox.getMinY() || detectionResult.getBoundingBox().getMaxY() > boundingBox.getMaxY()) {
                        boundingBox = detectionResult.getBoundingBox();
                        i3++;
                    }
                }
                detectionResult.setBoundingBox(boundingBox);
                break;
            }
            throw NotFoundException.getNotFoundInstance();
        }
        int barcodeColumnCount = detectionResult.getBarcodeColumnCount() + 1;
        detectionResult.setDetectionResultColumn(0, detectionResultColumn);
        detectionResult.setDetectionResultColumn(barcodeColumnCount, detectionResultColumn2);
        boolean z = detectionResultColumn != null;
        int i4 = 1;
        while (i4 <= barcodeColumnCount) {
            int i5 = !z ? barcodeColumnCount - i4 : i4;
            if (detectionResult.getDetectionResultColumn(i5) == null) {
                DetectionResultColumn detectionResultRowIndicatorColumn;
                if (i5 == 0 || i5 == barcodeColumnCount) {
                    detectionResultRowIndicatorColumn = new DetectionResultRowIndicatorColumn(boundingBox, i5 == 0);
                } else {
                    detectionResultRowIndicatorColumn = new DetectionResultColumn(boundingBox);
                }
                detectionResult.setDetectionResultColumn(i5, detectionResultRowIndicatorColumn);
                int i6 = -1;
                for (int minY = boundingBox.getMinY(); minY <= boundingBox.getMaxY(); minY++) {
                    int startColumn = getStartColumn(detectionResult, i5, minY, z);
                    if (startColumn < 0 || startColumn > boundingBox.getMaxX()) {
                        if (i6 != -1) {
                            startColumn = i6;
                        } else {
                        }
                    }
                    Codeword detectCodeword = detectCodeword(bitMatrix, boundingBox.getMinX(), boundingBox.getMaxX(), z, startColumn, minY, i, i2);
                    if (detectCodeword != null) {
                        detectionResultRowIndicatorColumn.setCodeword(minY, detectCodeword);
                        i6 = startColumn;
                        i = Math.min(i, detectCodeword.getWidth());
                        i2 = Math.max(i2, detectCodeword.getWidth());
                    }
                }
            }
            i4++;
        }
        return createDecoderResult(detectionResult);
    }

    private static DecoderResult decodeCodewords(int[] iArr, int i, int[] iArr2) throws FormatException, ChecksumException {
        if (iArr.length != 0) {
            int i2 = 1 << (i + 1);
            int correctErrors = correctErrors(iArr, iArr2, i2);
            verifyCodewordCount(iArr, i2);
            DecoderResult decode = DecodedBitStreamParser.decode(iArr, String.valueOf(i));
            decode.setErrorsCorrected(Integer.valueOf(correctErrors));
            decode.setErasures(Integer.valueOf(iArr2.length));
            return decode;
        }
        throw FormatException.getFormatInstance();
    }

    private static Codeword detectCodeword(BitMatrix bitMatrix, int i, int i2, boolean z, int i3, int i4, int i5, int i6) {
        i3 = adjustCodewordStartColumn(bitMatrix, i, i2, z, i3, i4);
        int[] moduleBitCount = getModuleBitCount(bitMatrix, i, i2, z, i3, i4);
        if (moduleBitCount == null) {
            return null;
        }
        int i7;
        int bitCountSum = PDF417Common.getBitCountSum(moduleBitCount);
        if (z) {
            i7 = i3 + bitCountSum;
        } else {
            for (int i8 = 0; i8 < moduleBitCount.length / 2; i8++) {
                int i9 = moduleBitCount[i8];
                moduleBitCount[i8] = moduleBitCount[(moduleBitCount.length - 1) - i8];
                moduleBitCount[(moduleBitCount.length - 1) - i8] = i9;
            }
            i7 = i3;
            i3 -= bitCountSum;
        }
        if (!checkCodewordSkew(bitCountSum, i5, i6)) {
            return null;
        }
        int decodedValue = PDF417CodewordDecoder.getDecodedValue(moduleBitCount);
        int codeword = PDF417Common.getCodeword(decodedValue);
        return codeword != -1 ? new Codeword(i3, i7, getCodewordBucketNumber(decodedValue), codeword) : null;
    }

    private static BarcodeMetadata getBarcodeMetadata(DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn, DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn2) {
        BarcodeMetadata barcodeMetadata = null;
        if (detectionResultRowIndicatorColumn != null) {
            BarcodeMetadata barcodeMetadata2 = detectionResultRowIndicatorColumn.getBarcodeMetadata();
            if (barcodeMetadata2 != null) {
                if (detectionResultRowIndicatorColumn2 != null) {
                    BarcodeMetadata barcodeMetadata3 = detectionResultRowIndicatorColumn2.getBarcodeMetadata();
                    return (barcodeMetadata3 == null || barcodeMetadata2.getColumnCount() == barcodeMetadata3.getColumnCount() || barcodeMetadata2.getErrorCorrectionLevel() == barcodeMetadata3.getErrorCorrectionLevel() || barcodeMetadata2.getRowCount() == barcodeMetadata3.getRowCount()) ? barcodeMetadata2 : null;
                }
                return barcodeMetadata2;
            }
        }
        if (detectionResultRowIndicatorColumn2 != null) {
            barcodeMetadata = detectionResultRowIndicatorColumn2.getBarcodeMetadata();
        }
        return barcodeMetadata;
    }

    private static int[] getBitCountForCodeword(int i) {
        int[] iArr = new int[8];
        int i2 = 0;
        int length = iArr.length - 1;
        while (true) {
            if ((i & 1) != i2) {
                i2 = i & 1;
                length--;
                if (length < 0) {
                    return iArr;
                }
            }
            iArr[length] = iArr[length] + 1;
            i >>= 1;
        }
    }

    private static int getCodewordBucketNumber(int i) {
        return getCodewordBucketNumber(getBitCountForCodeword(i));
    }

    private static int getCodewordBucketNumber(int[] iArr) {
        return ((((iArr[0] - iArr[2]) + iArr[4]) - iArr[6]) + 9) % 9;
    }

    private static int getMax(int[] iArr) {
        int i = -1;
        for (int max : iArr) {
            i = Math.max(i, max);
        }
        return i;
    }

    private static int[] getModuleBitCount(BitMatrix bitMatrix, int i, int i2, boolean z, int i3, int i4) {
        int i5 = i3;
        int[] iArr = new int[8];
        int i6 = 0;
        int i7 = !z ? -1 : 1;
        boolean z2 = z;
        while (true) {
            if (!z || i5 >= i2) {
                if (!z) {
                    if (i5 < i) {
                        break;
                    }
                }
                break;
            }
            if (i6 >= iArr.length) {
                break;
            } else if (bitMatrix.get(i5, i4) != z2) {
                i6++;
                z2 = !z2;
            } else {
                iArr[i6] = iArr[i6] + 1;
                i5 += i7;
            }
        }
        if (i6 != iArr.length) {
            if (!(z && i5 == i2)) {
                if (!z) {
                    if (i5 != i) {
                    }
                }
                return null;
            }
            if (i6 != iArr.length - 1) {
                return null;
            }
        }
        return iArr;
    }

    private static int getNumberOfECCodeWords(int i) {
        return 2 << i;
    }

    private static DetectionResultRowIndicatorColumn getRowIndicatorColumn(BitMatrix bitMatrix, BoundingBox boundingBox, ResultPoint resultPoint, boolean z, int i, int i2) {
        DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn = new DetectionResultRowIndicatorColumn(boundingBox, z);
        int i3 = 0;
        while (i3 < 2) {
            int i4 = i3 != 0 ? -1 : 1;
            int x = (int) resultPoint.getX();
            int y = (int) resultPoint.getY();
            while (y <= boundingBox.getMaxY() && y >= boundingBox.getMinY()) {
                Codeword detectCodeword = detectCodeword(bitMatrix, 0, bitMatrix.getWidth(), z, x, y, i, i2);
                if (detectCodeword != null) {
                    detectionResultRowIndicatorColumn.setCodeword(y, detectCodeword);
                    x = !z ? detectCodeword.getEndX() : detectCodeword.getStartX();
                }
                y += i4;
            }
            i3++;
        }
        return detectionResultRowIndicatorColumn;
    }

    private static int getStartColumn(DetectionResult detectionResult, int i, int i2, boolean z) {
        int i3 = !z ? -1 : 1;
        Codeword codeword = null;
        if (isValidBarcodeColumn(detectionResult, i - i3)) {
            codeword = detectionResult.getDetectionResultColumn(i - i3).getCodeword(i2);
        }
        if (codeword == null) {
            codeword = detectionResult.getDetectionResultColumn(i).getCodewordNearby(i2);
            if (codeword == null) {
                if (isValidBarcodeColumn(detectionResult, i - i3)) {
                    codeword = detectionResult.getDetectionResultColumn(i - i3).getCodewordNearby(i2);
                }
                if (codeword == null) {
                    int i4 = 0;
                    while (isValidBarcodeColumn(detectionResult, i - i3)) {
                        i -= i3;
                        Codeword[] codewords = detectionResult.getDetectionResultColumn(i).getCodewords();
                        int length = codewords.length;
                        int i5 = 0;
                        while (i5 < length) {
                            Codeword codeword2 = codewords[i5];
                            if (codeword2 == null) {
                                i5++;
                            } else {
                                return (!z ? codeword2.getStartX() : codeword2.getEndX()) + ((i3 * i4) * (codeword2.getEndX() - codeword2.getStartX()));
                            }
                        }
                        i4++;
                    }
                    return !z ? detectionResult.getBoundingBox().getMaxX() : detectionResult.getBoundingBox().getMinX();
                }
                return !z ? codeword.getStartX() : codeword.getEndX();
            }
            return !z ? codeword.getEndX() : codeword.getStartX();
        }
        return !z ? codeword.getStartX() : codeword.getEndX();
    }

    private static boolean isValidBarcodeColumn(DetectionResult detectionResult, int i) {
        return i >= 0 && i <= detectionResult.getBarcodeColumnCount() + 1;
    }

    private static DetectionResult merge(DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn, DetectionResultRowIndicatorColumn detectionResultRowIndicatorColumn2) throws NotFoundException, FormatException {
        if (detectionResultRowIndicatorColumn == null && detectionResultRowIndicatorColumn2 == null) {
            return null;
        }
        BarcodeMetadata barcodeMetadata = getBarcodeMetadata(detectionResultRowIndicatorColumn, detectionResultRowIndicatorColumn2);
        return barcodeMetadata != null ? new DetectionResult(barcodeMetadata, BoundingBox.merge(adjustBoundingBox(detectionResultRowIndicatorColumn), adjustBoundingBox(detectionResultRowIndicatorColumn2))) : null;
    }

    private static void verifyCodewordCount(int[] iArr, int i) throws FormatException {
        if (iArr.length >= 4) {
            int i2 = iArr[0];
            if (i2 > iArr.length) {
                throw FormatException.getFormatInstance();
            } else if (i2 == 0) {
                if (i >= iArr.length) {
                    throw FormatException.getFormatInstance();
                }
                iArr[0] = iArr.length - i;
                return;
            } else {
                return;
            }
        }
        throw FormatException.getFormatInstance();
    }
}

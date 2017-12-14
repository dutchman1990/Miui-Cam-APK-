package com.google.zxing.aztec.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.aztec.AztecDetectorResult;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;

public final class Detector {
    private static final int[] EXPECTED_CORNER_BITS = new int[]{3808, 476, 2107, 1799};
    private boolean compact;
    private final BitMatrix image;
    private int nbCenterLayers;
    private int nbDataBlocks;
    private int nbLayers;
    private int shift;

    static final class Point {
        private final int f3x;
        private final int f4y;

        Point(int i, int i2) {
            this.f3x = i;
            this.f4y = i2;
        }

        int getX() {
            return this.f3x;
        }

        int getY() {
            return this.f4y;
        }

        ResultPoint toResultPoint() {
            return new ResultPoint((float) getX(), (float) getY());
        }

        public String toString() {
            return "<" + this.f3x + ' ' + this.f4y + '>';
        }
    }

    public Detector(BitMatrix bitMatrix) {
        this.image = bitMatrix;
    }

    private static float distance(ResultPoint resultPoint, ResultPoint resultPoint2) {
        return MathUtils.distance(resultPoint.getX(), resultPoint.getY(), resultPoint2.getX(), resultPoint2.getY());
    }

    private static float distance(Point point, Point point2) {
        return MathUtils.distance(point.getX(), point.getY(), point2.getX(), point2.getY());
    }

    private static ResultPoint[] expandSquare(ResultPoint[] resultPointArr, float f, float f2) {
        float f3 = f2 / (2.0f * f);
        float x = resultPointArr[0].getX() - resultPointArr[2].getX();
        float y = resultPointArr[0].getY() - resultPointArr[2].getY();
        float x2 = (resultPointArr[0].getX() + resultPointArr[2].getX()) / 2.0f;
        float y2 = (resultPointArr[0].getY() + resultPointArr[2].getY()) / 2.0f;
        ResultPoint resultPoint = new ResultPoint((f3 * x) + x2, (f3 * y) + y2);
        ResultPoint resultPoint2 = new ResultPoint(x2 - (f3 * x), y2 - (f3 * y));
        x = resultPointArr[1].getX() - resultPointArr[3].getX();
        y = resultPointArr[1].getY() - resultPointArr[3].getY();
        x2 = (resultPointArr[1].getX() + resultPointArr[3].getX()) / 2.0f;
        y2 = (resultPointArr[1].getY() + resultPointArr[3].getY()) / 2.0f;
        ResultPoint resultPoint3 = new ResultPoint((f3 * x) + x2, (f3 * y) + y2);
        ResultPoint resultPoint4 = new ResultPoint(x2 - (f3 * x), y2 - (f3 * y));
        return new ResultPoint[]{resultPoint, resultPoint3, resultPoint2, resultPoint4};
    }

    private void extractParameters(ResultPoint[] resultPointArr) throws NotFoundException {
        if (isValid(resultPointArr[0]) && isValid(resultPointArr[1]) && isValid(resultPointArr[2]) && isValid(resultPointArr[3])) {
            int[] iArr = new int[]{sampleLine(resultPointArr[0], resultPointArr[1], this.nbCenterLayers * 2), sampleLine(resultPointArr[1], resultPointArr[2], this.nbCenterLayers * 2), sampleLine(resultPointArr[2], resultPointArr[3], this.nbCenterLayers * 2), sampleLine(resultPointArr[3], resultPointArr[0], this.nbCenterLayers * 2)};
            this.shift = getRotation(iArr, this.nbCenterLayers * 2);
            long j = 0;
            for (int i = 0; i < 4; i++) {
                int i2 = iArr[(this.shift + i) % 4];
                j = !this.compact ? (j << 10) + ((long) (((i2 >> 2) & 992) + ((i2 >> 1) & 31))) : (j << 7) + ((long) ((i2 >> 1) & 127));
            }
            int correctedParameterData = getCorrectedParameterData(j, this.compact);
            if (this.compact) {
                this.nbLayers = (correctedParameterData >> 6) + 1;
                this.nbDataBlocks = (correctedParameterData & 63) + 1;
                return;
            }
            this.nbLayers = (correctedParameterData >> 11) + 1;
            this.nbDataBlocks = (correctedParameterData & 2047) + 1;
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private ResultPoint[] getBullsEyeCorners(Point point) throws NotFoundException {
        Point point2 = point;
        Point point3 = point;
        Point point4 = point;
        Point point5 = point;
        boolean z = true;
        this.nbCenterLayers = 1;
        while (this.nbCenterLayers < 9) {
            Point firstDifferent = getFirstDifferent(point2, z, 1, -1);
            Point firstDifferent2 = getFirstDifferent(point3, z, 1, 1);
            Point firstDifferent3 = getFirstDifferent(point4, z, -1, 1);
            Point firstDifferent4 = getFirstDifferent(point5, z, -1, -1);
            if (this.nbCenterLayers > 2) {
                float distance = (distance(firstDifferent4, firstDifferent) * ((float) this.nbCenterLayers)) / (distance(point5, point2) * ((float) (this.nbCenterLayers + 2)));
                if ((((double) distance) < 0.75d ? 1 : null) == null) {
                    if ((((double) distance) > 1.25d ? 1 : null) == null) {
                        if (!isWhiteOrBlackRectangle(firstDifferent, firstDifferent2, firstDifferent3, firstDifferent4)) {
                            break;
                        }
                    }
                    break;
                }
                break;
            }
            point2 = firstDifferent;
            point3 = firstDifferent2;
            point4 = firstDifferent3;
            point5 = firstDifferent4;
            z = !z;
            this.nbCenterLayers++;
        }
        if (this.nbCenterLayers == 5 || this.nbCenterLayers == 7) {
            this.compact = this.nbCenterLayers == 5;
            ResultPoint resultPoint = new ResultPoint(((float) point2.getX()) + 0.5f, ((float) point2.getY()) - 0.5f);
            ResultPoint resultPoint2 = new ResultPoint(((float) point3.getX()) + 0.5f, ((float) point3.getY()) + 0.5f);
            ResultPoint resultPoint3 = new ResultPoint(((float) point4.getX()) - 0.5f, ((float) point4.getY()) + 0.5f);
            ResultPoint resultPoint4 = new ResultPoint(((float) point5.getX()) - 0.5f, ((float) point5.getY()) - 0.5f);
            return expandSquare(new ResultPoint[]{resultPoint, resultPoint2, resultPoint3, resultPoint4}, (float) ((this.nbCenterLayers * 2) - 3), (float) (this.nbCenterLayers * 2));
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private int getColor(Point point, Point point2) {
        float distance = distance(point, point2);
        float x = ((float) (point2.getX() - point.getX())) / distance;
        float y = ((float) (point2.getY() - point.getY())) / distance;
        int i = 0;
        float x2 = (float) point.getX();
        float y2 = (float) point.getY();
        boolean z = this.image.get(point.getX(), point.getY());
        int i2 = 0;
        while (true) {
            if ((((float) i2) < distance ? 1 : null) == null) {
                break;
            }
            x2 += x;
            y2 += y;
            if (this.image.get(MathUtils.round(x2), MathUtils.round(y2)) != z) {
                i++;
            }
            i2++;
        }
        float f = ((float) i) / distance;
        if (f > 0.1f && f < 0.9f) {
            return 0;
        }
        return ((f > 0.1f ? 1 : (f == 0.1f ? 0 : -1)) <= 0) != z ? -1 : 1;
    }

    private static int getCorrectedParameterData(long j, boolean z) throws NotFoundException {
        int i;
        int i2;
        int i3;
        if (z) {
            i = 7;
            i2 = 2;
        } else {
            i = 10;
            i2 = 4;
        }
        int i4 = i - i2;
        int[] iArr = new int[i];
        for (i3 = i - 1; i3 >= 0; i3--) {
            iArr[i3] = ((int) j) & 15;
            j >>= 4;
        }
        try {
            new ReedSolomonDecoder(GenericGF.AZTEC_PARAM).decode(iArr, i4);
            int i5 = 0;
            for (i3 = 0; i3 < i2; i3++) {
                i5 = (i5 << 4) + iArr[i3];
            }
            return i5;
        } catch (ReedSolomonException e) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private int getDimension() {
        return !this.compact ? this.nbLayers > 4 ? ((this.nbLayers * 4) + ((((this.nbLayers - 4) / 8) + 1) * 2)) + 15 : (this.nbLayers * 4) + 15 : (this.nbLayers * 4) + 11;
    }

    private Point getFirstDifferent(Point point, boolean z, int i, int i2) {
        int x = point.getX() + i;
        int y = point.getY() + i2;
        while (isValid(x, y) && this.image.get(x, y) == z) {
            x += i;
            y += i2;
        }
        x -= i;
        y -= i2;
        while (isValid(x, y) && this.image.get(x, y) == z) {
            x += i;
        }
        x -= i;
        while (isValid(x, y) && this.image.get(x, y) == z) {
            y += i2;
        }
        return new Point(x, y - i2);
    }

    private Point getMatrixCenter() {
        ResultPoint resultPoint;
        ResultPoint resultPoint2;
        ResultPoint resultPoint3;
        ResultPoint resultPoint4;
        int width;
        int height;
        try {
            ResultPoint[] detect = new WhiteRectangleDetector(this.image).detect();
            resultPoint = detect[0];
            resultPoint2 = detect[1];
            resultPoint3 = detect[2];
            resultPoint4 = detect[3];
        } catch (NotFoundException e) {
            width = this.image.getWidth() / 2;
            height = this.image.getHeight() / 2;
            resultPoint = getFirstDifferent(new Point(width + 7, height - 7), false, 1, -1).toResultPoint();
            resultPoint2 = getFirstDifferent(new Point(width + 7, height + 7), false, 1, 1).toResultPoint();
            resultPoint3 = getFirstDifferent(new Point(width - 7, height + 7), false, -1, 1).toResultPoint();
            resultPoint4 = getFirstDifferent(new Point(width - 7, height - 7), false, -1, -1).toResultPoint();
        }
        width = MathUtils.round((((resultPoint.getX() + resultPoint4.getX()) + resultPoint2.getX()) + resultPoint3.getX()) / 4.0f);
        height = MathUtils.round((((resultPoint.getY() + resultPoint4.getY()) + resultPoint2.getY()) + resultPoint3.getY()) / 4.0f);
        try {
            detect = new WhiteRectangleDetector(this.image, 15, width, height).detect();
            resultPoint = detect[0];
            resultPoint2 = detect[1];
            resultPoint3 = detect[2];
            resultPoint4 = detect[3];
        } catch (NotFoundException e2) {
            resultPoint = getFirstDifferent(new Point(width + 7, height - 7), false, 1, -1).toResultPoint();
            resultPoint2 = getFirstDifferent(new Point(width + 7, height + 7), false, 1, 1).toResultPoint();
            resultPoint3 = getFirstDifferent(new Point(width - 7, height + 7), false, -1, 1).toResultPoint();
            resultPoint4 = getFirstDifferent(new Point(width - 7, height - 7), false, -1, -1).toResultPoint();
        }
        return new Point(MathUtils.round((((resultPoint.getX() + resultPoint4.getX()) + resultPoint2.getX()) + resultPoint3.getX()) / 4.0f), MathUtils.round((((resultPoint.getY() + resultPoint4.getY()) + resultPoint2.getY()) + resultPoint3.getY()) / 4.0f));
    }

    private ResultPoint[] getMatrixCornerPoints(ResultPoint[] resultPointArr) {
        return expandSquare(resultPointArr, (float) (this.nbCenterLayers * 2), (float) getDimension());
    }

    private static int getRotation(int[] iArr, int i) throws NotFoundException {
        int i2 = 0;
        for (int i3 : iArr) {
            i2 = (i2 << 3) + (((i3 >> (i - 2)) << 1) + (i3 & 1));
        }
        i2 = ((i2 & 1) << 11) + (i2 >> 1);
        for (int i4 = 0; i4 < 4; i4++) {
            if (Integer.bitCount(EXPECTED_CORNER_BITS[i4] ^ i2) <= 2) {
                return i4;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private boolean isValid(int i, int i2) {
        return i >= 0 && i < this.image.getWidth() && i2 > 0 && i2 < this.image.getHeight();
    }

    private boolean isValid(ResultPoint resultPoint) {
        return isValid(MathUtils.round(resultPoint.getX()), MathUtils.round(resultPoint.getY()));
    }

    private boolean isWhiteOrBlackRectangle(Point point, Point point2, Point point3, Point point4) {
        Point point5 = new Point(point.getX() - 3, point.getY() + 3);
        Point point6 = new Point(point2.getX() - 3, point2.getY() - 3);
        Point point7 = new Point(point3.getX() + 3, point3.getY() - 3);
        Point point8 = new Point(point4.getX() + 3, point4.getY() + 3);
        int color = getColor(point8, point5);
        return color != 0 && getColor(point5, point6) == color && getColor(point6, point7) == color && getColor(point7, point8) == color;
    }

    private BitMatrix sampleGrid(BitMatrix bitMatrix, ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4) throws NotFoundException {
        GridSampler instance = GridSampler.getInstance();
        int dimension = getDimension();
        float f = (((float) dimension) / 2.0f) - ((float) this.nbCenterLayers);
        float f2 = (((float) dimension) / 2.0f) + ((float) this.nbCenterLayers);
        return instance.sampleGrid(bitMatrix, dimension, dimension, f, f, f2, f, f2, f2, f, f2, resultPoint.getX(), resultPoint.getY(), resultPoint2.getX(), resultPoint2.getY(), resultPoint3.getX(), resultPoint3.getY(), resultPoint4.getX(), resultPoint4.getY());
    }

    private int sampleLine(ResultPoint resultPoint, ResultPoint resultPoint2, int i) {
        int i2 = 0;
        float distance = distance(resultPoint, resultPoint2);
        float f = distance / ((float) i);
        float x = resultPoint.getX();
        float y = resultPoint.getY();
        float x2 = ((resultPoint2.getX() - resultPoint.getX()) * f) / distance;
        float y2 = ((resultPoint2.getY() - resultPoint.getY()) * f) / distance;
        for (int i3 = 0; i3 < i; i3++) {
            if (this.image.get(MathUtils.round((((float) i3) * x2) + x), MathUtils.round((((float) i3) * y2) + y))) {
                i2 |= 1 << ((i - i3) - 1);
            }
        }
        return i2;
    }

    public AztecDetectorResult detect(boolean z) throws NotFoundException {
        ResultPoint[] bullsEyeCorners = getBullsEyeCorners(getMatrixCenter());
        if (z) {
            ResultPoint resultPoint = bullsEyeCorners[0];
            bullsEyeCorners[0] = bullsEyeCorners[2];
            bullsEyeCorners[2] = resultPoint;
        }
        extractParameters(bullsEyeCorners);
        return new AztecDetectorResult(sampleGrid(this.image, bullsEyeCorners[this.shift % 4], bullsEyeCorners[(this.shift + 1) % 4], bullsEyeCorners[(this.shift + 2) % 4], bullsEyeCorners[(this.shift + 3) % 4]), getMatrixCornerPoints(bullsEyeCorners), this.compact, this.nbDataBlocks, this.nbLayers);
    }
}

package com.google.zxing.datamatrix.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class Detector {
    private final BitMatrix image;
    private final WhiteRectangleDetector rectangleDetector;

    private static final class ResultPointsAndTransitions {
        private final ResultPoint from;
        private final ResultPoint to;
        private final int transitions;

        private ResultPointsAndTransitions(ResultPoint resultPoint, ResultPoint resultPoint2, int i) {
            this.from = resultPoint;
            this.to = resultPoint2;
            this.transitions = i;
        }

        ResultPoint getFrom() {
            return this.from;
        }

        ResultPoint getTo() {
            return this.to;
        }

        public int getTransitions() {
            return this.transitions;
        }

        public String toString() {
            return this.from + "/" + this.to + '/' + this.transitions;
        }
    }

    private static final class ResultPointsAndTransitionsComparator implements Comparator<ResultPointsAndTransitions>, Serializable {
        private ResultPointsAndTransitionsComparator() {
        }

        public int compare(ResultPointsAndTransitions resultPointsAndTransitions, ResultPointsAndTransitions resultPointsAndTransitions2) {
            return resultPointsAndTransitions.getTransitions() - resultPointsAndTransitions2.getTransitions();
        }
    }

    public Detector(BitMatrix bitMatrix) throws NotFoundException {
        this.image = bitMatrix;
        this.rectangleDetector = new WhiteRectangleDetector(bitMatrix);
    }

    private ResultPoint correctTopRight(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4, int i) {
        float distance = ((float) distance(resultPoint, resultPoint2)) / ((float) i);
        int distance2 = distance(resultPoint3, resultPoint4);
        ResultPoint resultPoint5 = new ResultPoint(resultPoint4.getX() + (distance * ((resultPoint4.getX() - resultPoint3.getX()) / ((float) distance2))), resultPoint4.getY() + (distance * ((resultPoint4.getY() - resultPoint3.getY()) / ((float) distance2))));
        distance = ((float) distance(resultPoint, resultPoint3)) / ((float) i);
        distance2 = distance(resultPoint2, resultPoint4);
        ResultPoint resultPoint6 = new ResultPoint(resultPoint4.getX() + (distance * ((resultPoint4.getX() - resultPoint2.getX()) / ((float) distance2))), resultPoint4.getY() + (distance * ((resultPoint4.getY() - resultPoint2.getY()) / ((float) distance2))));
        if (!isValid(resultPoint5)) {
            return !isValid(resultPoint6) ? null : resultPoint6;
        } else {
            if (!isValid(resultPoint6)) {
                return resultPoint5;
            }
            if (Math.abs(transitionsBetween(resultPoint3, resultPoint5).getTransitions() - transitionsBetween(resultPoint2, resultPoint5).getTransitions()) <= Math.abs(transitionsBetween(resultPoint3, resultPoint6).getTransitions() - transitionsBetween(resultPoint2, resultPoint6).getTransitions())) {
                resultPoint6 = resultPoint5;
            }
            return resultPoint6;
        }
    }

    private ResultPoint correctTopRightRectangular(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4, int i, int i2) {
        float distance = ((float) distance(resultPoint, resultPoint2)) / ((float) i);
        int distance2 = distance(resultPoint3, resultPoint4);
        ResultPoint resultPoint5 = new ResultPoint(resultPoint4.getX() + (distance * ((resultPoint4.getX() - resultPoint3.getX()) / ((float) distance2))), resultPoint4.getY() + (distance * ((resultPoint4.getY() - resultPoint3.getY()) / ((float) distance2))));
        distance = ((float) distance(resultPoint, resultPoint3)) / ((float) i2);
        distance2 = distance(resultPoint2, resultPoint4);
        ResultPoint resultPoint6 = new ResultPoint(resultPoint4.getX() + (distance * ((resultPoint4.getX() - resultPoint2.getX()) / ((float) distance2))), resultPoint4.getY() + (distance * ((resultPoint4.getY() - resultPoint2.getY()) / ((float) distance2))));
        return isValid(resultPoint5) ? (!isValid(resultPoint6) || Math.abs(i - transitionsBetween(resultPoint3, resultPoint5).getTransitions()) + Math.abs(i2 - transitionsBetween(resultPoint2, resultPoint5).getTransitions()) <= Math.abs(i - transitionsBetween(resultPoint3, resultPoint6).getTransitions()) + Math.abs(i2 - transitionsBetween(resultPoint2, resultPoint6).getTransitions())) ? resultPoint5 : resultPoint6 : !isValid(resultPoint6) ? null : resultPoint6;
    }

    private static int distance(ResultPoint resultPoint, ResultPoint resultPoint2) {
        return MathUtils.round(ResultPoint.distance(resultPoint, resultPoint2));
    }

    private static void increment(Map<ResultPoint, Integer> map, ResultPoint resultPoint) {
        Integer num = (Integer) map.get(resultPoint);
        map.put(resultPoint, Integer.valueOf(num != null ? num.intValue() + 1 : 1));
    }

    private boolean isValid(ResultPoint resultPoint) {
        return resultPoint.getX() >= 0.0f && resultPoint.getX() < ((float) this.image.getWidth()) && resultPoint.getY() > 0.0f && resultPoint.getY() < ((float) this.image.getHeight());
    }

    private static BitMatrix sampleGrid(BitMatrix bitMatrix, ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4, int i, int i2) throws NotFoundException {
        return GridSampler.getInstance().sampleGrid(bitMatrix, i, i2, 0.5f, 0.5f, ((float) i) - 0.5f, 0.5f, ((float) i) - 0.5f, ((float) i2) - 0.5f, 0.5f, ((float) i2) - 0.5f, resultPoint.getX(), resultPoint.getY(), resultPoint4.getX(), resultPoint4.getY(), resultPoint3.getX(), resultPoint3.getY(), resultPoint2.getX(), resultPoint2.getY());
    }

    private ResultPointsAndTransitions transitionsBetween(ResultPoint resultPoint, ResultPoint resultPoint2) {
        int x = (int) resultPoint.getX();
        int y = (int) resultPoint.getY();
        int x2 = (int) resultPoint2.getX();
        int y2 = (int) resultPoint2.getY();
        Object obj = Math.abs(y2 - y) <= Math.abs(x2 - x) ? null : 1;
        if (obj != null) {
            int i = x;
            x = y;
            y = i;
            i = x2;
            x2 = y2;
            y2 = i;
        }
        int abs = Math.abs(x2 - x);
        int abs2 = Math.abs(y2 - y);
        int i2 = (-abs) / 2;
        int i3 = y >= y2 ? -1 : 1;
        int i4 = x >= x2 ? -1 : 1;
        int i5 = 0;
        boolean z = this.image.get(obj == null ? x : y, obj == null ? y : x);
        int i6 = y;
        for (int i7 = x; i7 != x2; i7 += i4) {
            boolean z2 = this.image.get(obj == null ? i7 : i6, obj == null ? i6 : i7);
            if (z2 != z) {
                i5++;
                z = z2;
            }
            i2 += abs2;
            if (i2 > 0) {
                if (i6 == y2) {
                    break;
                }
                i6 += i3;
                i2 -= abs;
            }
        }
        return new ResultPointsAndTransitions(resultPoint, resultPoint2, i5);
    }

    public DetectorResult detect() throws NotFoundException {
        ResultPoint[] detect = this.rectangleDetector.detect();
        ResultPoint resultPoint = detect[0];
        ResultPoint resultPoint2 = detect[1];
        ResultPoint resultPoint3 = detect[2];
        ResultPoint resultPoint4 = detect[3];
        ArrayList arrayList = new ArrayList(4);
        arrayList.add(transitionsBetween(resultPoint, resultPoint2));
        arrayList.add(transitionsBetween(resultPoint, resultPoint3));
        arrayList.add(transitionsBetween(resultPoint2, resultPoint4));
        arrayList.add(transitionsBetween(resultPoint3, resultPoint4));
        Collections.sort(arrayList, new ResultPointsAndTransitionsComparator());
        ResultPointsAndTransitions resultPointsAndTransitions = (ResultPointsAndTransitions) arrayList.get(0);
        ResultPointsAndTransitions resultPointsAndTransitions2 = (ResultPointsAndTransitions) arrayList.get(1);
        Map hashMap = new HashMap();
        increment(hashMap, resultPointsAndTransitions.getFrom());
        increment(hashMap, resultPointsAndTransitions.getTo());
        increment(hashMap, resultPointsAndTransitions2.getFrom());
        increment(hashMap, resultPointsAndTransitions2.getTo());
        ResultPoint resultPoint5 = null;
        ResultPoint resultPoint6 = null;
        ResultPoint resultPoint7 = null;
        for (Entry entry : hashMap.entrySet()) {
            ResultPoint resultPoint8 = (ResultPoint) entry.getKey();
            if (((Integer) entry.getValue()).intValue() == 2) {
                resultPoint6 = resultPoint8;
            } else if (resultPoint5 != null) {
                resultPoint7 = resultPoint8;
            } else {
                resultPoint5 = resultPoint8;
            }
        }
        if (resultPoint5 == null || resultPoint6 == null || resultPoint7 == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint correctTopRight;
        BitMatrix sampleGrid;
        ResultPoint[] resultPointArr = new ResultPoint[]{resultPoint5, resultPoint6, resultPoint7};
        ResultPoint.orderBestPatterns(resultPointArr);
        ResultPoint resultPoint9 = resultPointArr[0];
        resultPoint6 = resultPointArr[1];
        ResultPoint resultPoint10 = resultPointArr[2];
        ResultPoint resultPoint11 = hashMap.containsKey(resultPoint) ? hashMap.containsKey(resultPoint2) ? hashMap.containsKey(resultPoint3) ? resultPoint4 : resultPoint3 : resultPoint2 : resultPoint;
        int transitions = transitionsBetween(resultPoint10, resultPoint11).getTransitions();
        int transitions2 = transitionsBetween(resultPoint9, resultPoint11).getTransitions();
        if ((transitions & 1) == 1) {
            transitions++;
        }
        transitions += 2;
        if ((transitions2 & 1) == 1) {
            transitions2++;
        }
        transitions2 += 2;
        if (transitions * 4 < transitions2 * 7 && transitions2 * 4 < transitions * 7) {
            correctTopRight = correctTopRight(resultPoint6, resultPoint9, resultPoint10, resultPoint11, Math.min(transitions2, transitions));
            if (correctTopRight == null) {
                correctTopRight = resultPoint11;
            }
            int max = Math.max(transitionsBetween(resultPoint10, correctTopRight).getTransitions(), transitionsBetween(resultPoint9, correctTopRight).getTransitions()) + 1;
            if ((max & 1) == 1) {
                max++;
            }
            sampleGrid = sampleGrid(this.image, resultPoint10, resultPoint6, resultPoint9, correctTopRight, max, max);
        } else {
            correctTopRight = correctTopRightRectangular(resultPoint6, resultPoint9, resultPoint10, resultPoint11, transitions, transitions2);
            if (correctTopRight == null) {
                correctTopRight = resultPoint11;
            }
            transitions = transitionsBetween(resultPoint10, correctTopRight).getTransitions();
            transitions2 = transitionsBetween(resultPoint9, correctTopRight).getTransitions();
            if ((transitions & 1) == 1) {
                transitions++;
            }
            if ((transitions2 & 1) == 1) {
                transitions2++;
            }
            sampleGrid = sampleGrid(this.image, resultPoint10, resultPoint6, resultPoint9, correctTopRight, transitions, transitions2);
        }
        return new DetectorResult(sampleGrid, new ResultPoint[]{resultPoint10, resultPoint6, resultPoint9, correctTopRight});
    }
}

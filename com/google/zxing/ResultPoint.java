package com.google.zxing;

import com.google.zxing.common.detector.MathUtils;

public class ResultPoint {
    private final float f1x;
    private final float f2y;

    public ResultPoint(float f, float f2) {
        this.f1x = f;
        this.f2y = f2;
    }

    private static float crossProductZ(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3) {
        float f = resultPoint2.f1x;
        float f2 = resultPoint2.f2y;
        return ((resultPoint3.f1x - f) * (resultPoint.f2y - f2)) - ((resultPoint3.f2y - f2) * (resultPoint.f1x - f));
    }

    public static float distance(ResultPoint resultPoint, ResultPoint resultPoint2) {
        return MathUtils.distance(resultPoint.f1x, resultPoint.f2y, resultPoint2.f1x, resultPoint2.f2y);
    }

    public static void orderBestPatterns(ResultPoint[] resultPointArr) {
        ResultPoint resultPoint;
        ResultPoint resultPoint2;
        ResultPoint resultPoint3;
        float distance = distance(resultPointArr[0], resultPointArr[1]);
        float distance2 = distance(resultPointArr[1], resultPointArr[2]);
        float distance3 = distance(resultPointArr[0], resultPointArr[2]);
        if (distance2 >= distance && distance2 >= distance3) {
            resultPoint = resultPointArr[0];
            resultPoint2 = resultPointArr[1];
            resultPoint3 = resultPointArr[2];
        } else if (distance3 < distance2 || distance3 < distance) {
            resultPoint = resultPointArr[2];
            resultPoint2 = resultPointArr[0];
            resultPoint3 = resultPointArr[1];
        } else {
            resultPoint = resultPointArr[1];
            resultPoint2 = resultPointArr[0];
            resultPoint3 = resultPointArr[2];
        }
        if (crossProductZ(resultPoint2, resultPoint, resultPoint3) < 0.0f) {
            ResultPoint resultPoint4 = resultPoint2;
            resultPoint2 = resultPoint3;
            resultPoint3 = resultPoint4;
        }
        resultPointArr[0] = resultPoint2;
        resultPointArr[1] = resultPoint;
        resultPointArr[2] = resultPoint3;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof ResultPoint)) {
            return false;
        }
        ResultPoint resultPoint = (ResultPoint) obj;
        return this.f1x == resultPoint.f1x && this.f2y == resultPoint.f2y;
    }

    public final float getX() {
        return this.f1x;
    }

    public final float getY() {
        return this.f2y;
    }

    public final int hashCode() {
        return (Float.floatToIntBits(this.f1x) * 31) + Float.floatToIntBits(this.f2y);
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder(25);
        stringBuilder.append('(');
        stringBuilder.append(this.f1x);
        stringBuilder.append(',');
        stringBuilder.append(this.f2y);
        stringBuilder.append(')');
        return stringBuilder.toString();
    }
}

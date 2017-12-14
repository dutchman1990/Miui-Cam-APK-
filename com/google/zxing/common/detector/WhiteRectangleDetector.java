package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;

public final class WhiteRectangleDetector {
    private final int downInit;
    private final int height;
    private final BitMatrix image;
    private final int leftInit;
    private final int rightInit;
    private final int upInit;
    private final int width;

    public WhiteRectangleDetector(BitMatrix bitMatrix) throws NotFoundException {
        this(bitMatrix, 10, bitMatrix.getWidth() / 2, bitMatrix.getHeight() / 2);
    }

    public WhiteRectangleDetector(BitMatrix bitMatrix, int i, int i2, int i3) throws NotFoundException {
        this.image = bitMatrix;
        this.height = bitMatrix.getHeight();
        this.width = bitMatrix.getWidth();
        int i4 = i / 2;
        this.leftInit = i2 - i4;
        this.rightInit = i2 + i4;
        this.upInit = i3 - i4;
        this.downInit = i3 + i4;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private ResultPoint[] centerEdges(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4) {
        float x = resultPoint.getX();
        float y = resultPoint.getY();
        float x2 = resultPoint2.getX();
        float y2 = resultPoint2.getY();
        float x3 = resultPoint3.getX();
        float y3 = resultPoint3.getY();
        float x4 = resultPoint4.getX();
        float y4 = resultPoint4.getY();
        if (x < ((float) this.width) / 2.0f) {
            return new ResultPoint[]{new ResultPoint(x4 - 1.0f, 1.0f + y4), new ResultPoint(1.0f + x2, 1.0f + y2), new ResultPoint(x3 - 1.0f, y3 - 1.0f), new ResultPoint(1.0f + x, y - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(1.0f + x4, 1.0f + y4), new ResultPoint(1.0f + x2, y2 - 1.0f), new ResultPoint(x3 - 1.0f, 1.0f + y3), new ResultPoint(x - 1.0f, y - 1.0f)};
    }

    private boolean containsBlackPoint(int i, int i2, int i3, boolean z) {
        if (z) {
            for (int i4 = i; i4 <= i2; i4++) {
                if (this.image.get(i4, i3)) {
                    return true;
                }
            }
        } else {
            for (int i5 = i; i5 <= i2; i5++) {
                if (this.image.get(i3, i5)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ResultPoint getBlackPointOnSegment(float f, float f2, float f3, float f4) {
        int round = MathUtils.round(MathUtils.distance(f, f2, f3, f4));
        float f5 = (f3 - f) / ((float) round);
        float f6 = (f4 - f2) / ((float) round);
        for (int i = 0; i < round; i++) {
            int round2 = MathUtils.round((((float) i) * f5) + f);
            int round3 = MathUtils.round((((float) i) * f6) + f2);
            if (this.image.get(round2, round3)) {
                return new ResultPoint((float) round2, (float) round3);
            }
        }
        return null;
    }

    public ResultPoint[] detect() throws NotFoundException {
        int i = this.leftInit;
        int i2 = this.rightInit;
        int i3 = this.upInit;
        int i4 = this.downInit;
        Object obj = null;
        Object obj2 = 1;
        Object obj3 = null;
        Object obj4 = null;
        Object obj5 = null;
        Object obj6 = null;
        Object obj7 = null;
        while (obj2 != null) {
            obj2 = null;
            boolean z = true;
            while (true) {
                if (!(z || obj4 == null) || i2 >= this.width) {
                } else {
                    z = containsBlackPoint(i3, i4, i2, false);
                    if (z) {
                        i2++;
                        obj2 = 1;
                        obj4 = 1;
                    } else if (obj4 == null) {
                        i2++;
                    }
                }
            }
            if (i2 >= this.width) {
                obj = 1;
                break;
            }
            boolean z2 = true;
            while (true) {
                if ((z2 || obj5 == null) && i4 < this.height) {
                    z2 = containsBlackPoint(i, i2, i4, true);
                    if (z2) {
                        i4++;
                        obj2 = 1;
                        obj5 = 1;
                    } else if (obj5 == null) {
                        i4++;
                    }
                }
            }
            if (i4 >= this.height) {
                obj = 1;
                break;
            }
            boolean z3 = true;
            while (true) {
                boolean z4;
                if ((!z3 && obj6 != null) || i < 0) {
                    if (i < 0) {
                        obj = 1;
                        break;
                    }
                    z4 = true;
                    while (true) {
                        if ((z4 && obj7 != null) || i3 < 0) {
                            if (i3 >= 0) {
                                obj = 1;
                                break;
                            } else if (obj2 == null) {
                                obj3 = 1;
                            }
                        } else {
                            z4 = containsBlackPoint(i, i2, i3, true);
                            if (z4) {
                                i3--;
                                obj2 = 1;
                                obj7 = 1;
                            } else if (obj7 == null) {
                                i3--;
                            }
                        }
                    }
                    if (i3 >= 0) {
                        obj = 1;
                        break;
                    } else if (obj2 == null) {
                        obj3 = 1;
                    }
                } else {
                    z3 = containsBlackPoint(i3, i4, i, false);
                    if (z3) {
                        i--;
                        obj2 = 1;
                        obj6 = 1;
                    } else if (obj6 == null) {
                        i--;
                    }
                }
            }
            if (i < 0) {
                z4 = true;
                while (true) {
                    if (!z4) {
                        break;
                    }
                }
                if (i3 >= 0) {
                    obj = 1;
                    break;
                } else if (obj2 == null) {
                    obj3 = 1;
                }
            } else {
                obj = 1;
                break;
            }
        }
        if (obj == null && r6 != null) {
            int i5;
            int i6 = i2 - i;
            ResultPoint resultPoint = null;
            for (i5 = 1; i5 < i6; i5++) {
                resultPoint = getBlackPointOnSegment((float) i, (float) (i4 - i5), (float) (i + i5), (float) i4);
                if (resultPoint != null) {
                    break;
                }
            }
            if (resultPoint != null) {
                ResultPoint resultPoint2 = null;
                for (i5 = 1; i5 < i6; i5++) {
                    resultPoint2 = getBlackPointOnSegment((float) i, (float) (i3 + i5), (float) (i + i5), (float) i3);
                    if (resultPoint2 != null) {
                        break;
                    }
                }
                if (resultPoint2 != null) {
                    ResultPoint resultPoint3 = null;
                    for (i5 = 1; i5 < i6; i5++) {
                        resultPoint3 = getBlackPointOnSegment((float) i2, (float) (i3 + i5), (float) (i2 - i5), (float) i3);
                        if (resultPoint3 != null) {
                            break;
                        }
                    }
                    if (resultPoint3 != null) {
                        ResultPoint resultPoint4 = null;
                        for (i5 = 1; i5 < i6; i5++) {
                            resultPoint4 = getBlackPointOnSegment((float) i2, (float) (i4 - i5), (float) (i2 - i5), (float) i4);
                            if (resultPoint4 != null) {
                                break;
                            }
                        }
                        if (resultPoint4 != null) {
                            return centerEdges(resultPoint4, resultPoint, resultPoint3, resultPoint2);
                        }
                        throw NotFoundException.getNotFoundInstance();
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                throw NotFoundException.getNotFoundInstance();
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }
}

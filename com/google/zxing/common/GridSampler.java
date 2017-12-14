package com.google.zxing.common;

import com.google.zxing.NotFoundException;

public abstract class GridSampler {
    private static GridSampler gridSampler = new DefaultGridSampler();

    protected static void checkAndNudgePoints(BitMatrix bitMatrix, float[] fArr) throws NotFoundException {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Object obj = 1;
        int i = 0;
        while (i < fArr.length && r1 != null) {
            int i2 = (int) fArr[i];
            int i3 = (int) fArr[i + 1];
            if (i2 >= -1 && i2 <= width && i3 >= -1 && i3 <= height) {
                obj = null;
                if (i2 == -1) {
                    fArr[i] = 0.0f;
                    obj = 1;
                } else if (i2 == width) {
                    fArr[i] = (float) (width - 1);
                    obj = 1;
                }
                if (i3 == -1) {
                    fArr[i + 1] = 0.0f;
                    obj = 1;
                } else if (i3 == height) {
                    fArr[i + 1] = (float) (height - 1);
                    obj = 1;
                }
                i += 2;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        obj = 1;
        i = fArr.length - 2;
        while (i >= 0 && r1 != null) {
            i2 = (int) fArr[i];
            i3 = (int) fArr[i + 1];
            if (i2 >= -1 && i2 <= width && i3 >= -1 && i3 <= height) {
                obj = null;
                if (i2 == -1) {
                    fArr[i] = 0.0f;
                    obj = 1;
                } else if (i2 == width) {
                    fArr[i] = (float) (width - 1);
                    obj = 1;
                }
                if (i3 == -1) {
                    fArr[i + 1] = 0.0f;
                    obj = 1;
                } else if (i3 == height) {
                    fArr[i + 1] = (float) (height - 1);
                    obj = 1;
                }
                i -= 2;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
    }

    public static GridSampler getInstance() {
        return gridSampler;
    }

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) throws NotFoundException;

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, PerspectiveTransform perspectiveTransform) throws NotFoundException;
}

package com.google.zxing.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FinderPatternFinder {
    private final int[] crossCheckStateCount = new int[5];
    private boolean hasSkipped;
    private final BitMatrix image;
    private final List<FinderPattern> possibleCenters = new ArrayList();
    private final ResultPointCallback resultPointCallback;

    private static final class CenterComparator implements Comparator<FinderPattern>, Serializable {
        private final float average;

        private CenterComparator(float f) {
            this.average = f;
        }

        public int compare(FinderPattern finderPattern, FinderPattern finderPattern2) {
            if (finderPattern2.getCount() != finderPattern.getCount()) {
                return finderPattern2.getCount() - finderPattern.getCount();
            }
            float abs = Math.abs(finderPattern2.getEstimatedModuleSize() - this.average);
            float abs2 = Math.abs(finderPattern.getEstimatedModuleSize() - this.average);
            int i = abs < abs2 ? 1 : abs == abs2 ? 0 : -1;
            return i;
        }
    }

    private static final class FurthestFromAverageComparator implements Comparator<FinderPattern>, Serializable {
        private final float average;

        private FurthestFromAverageComparator(float f) {
            this.average = f;
        }

        public int compare(FinderPattern finderPattern, FinderPattern finderPattern2) {
            float abs = Math.abs(finderPattern2.getEstimatedModuleSize() - this.average);
            float abs2 = Math.abs(finderPattern.getEstimatedModuleSize() - this.average);
            return abs < abs2 ? -1 : abs == abs2 ? 0 : 1;
        }
    }

    public FinderPatternFinder(BitMatrix bitMatrix, ResultPointCallback resultPointCallback) {
        this.image = bitMatrix;
        this.resultPointCallback = resultPointCallback;
    }

    private static float centerFromEnd(int[] iArr, int i) {
        return ((float) ((i - iArr[4]) - iArr[3])) - (((float) iArr[2]) / 2.0f);
    }

    private boolean crossCheckDiagonal(int i, int i2, int i3, int i4) {
        int[] crossCheckStateCount = getCrossCheckStateCount();
        int i5 = 0;
        while (i >= i5) {
            if (i2 >= i5) {
                if (!this.image.get(i2 - i5, i - i5)) {
                    break;
                }
                crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i < i5 || i2 < i5) {
            return false;
        }
        while (i >= i5 && i2 >= i5) {
            if (!this.image.get(i2 - i5, i - i5)) {
                if (crossCheckStateCount[1] > i3) {
                    break;
                }
                crossCheckStateCount[1] = crossCheckStateCount[1] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i < i5 || i2 < i5 || crossCheckStateCount[1] > i3) {
            return false;
        }
        while (i >= i5 && i2 >= i5) {
            if (this.image.get(i2 - i5, i - i5)) {
                if (crossCheckStateCount[0] > i3) {
                    break;
                }
                crossCheckStateCount[0] = crossCheckStateCount[0] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (crossCheckStateCount[0] > i3) {
            return false;
        }
        int height = this.image.getHeight();
        int width = this.image.getWidth();
        i5 = 1;
        while (i + i5 < height) {
            if (i2 + i5 < width) {
                if (!this.image.get(i2 + i5, i + i5)) {
                    break;
                }
                crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i + i5 >= height || i2 + i5 >= width) {
            return false;
        }
        while (i + i5 < height && i2 + i5 < width) {
            if (!this.image.get(i2 + i5, i + i5)) {
                if (crossCheckStateCount[3] >= i3) {
                    break;
                }
                crossCheckStateCount[3] = crossCheckStateCount[3] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i + i5 >= height || i2 + i5 >= width || crossCheckStateCount[3] >= i3) {
            return false;
        }
        while (i + i5 < height && i2 + i5 < width) {
            if (this.image.get(i2 + i5, i + i5)) {
                if (crossCheckStateCount[4] >= i3) {
                    break;
                }
                crossCheckStateCount[4] = crossCheckStateCount[4] + 1;
                i5++;
            } else {
                break;
            }
        }
        return crossCheckStateCount[4] < i3 ? Math.abs(((((crossCheckStateCount[0] + crossCheckStateCount[1]) + crossCheckStateCount[2]) + crossCheckStateCount[3]) + crossCheckStateCount[4]) - i4) < i4 * 2 && foundPatternCross(crossCheckStateCount) : false;
    }

    private float crossCheckHorizontal(int i, int i2, int i3, int i4) {
        BitMatrix bitMatrix = this.image;
        int width = bitMatrix.getWidth();
        int[] crossCheckStateCount = getCrossCheckStateCount();
        int i5 = i;
        while (i5 >= 0 && bitMatrix.get(i5, i2)) {
            crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
            i5--;
        }
        if (i5 < 0) {
            return Float.NaN;
        }
        while (i5 >= 0) {
            if (!bitMatrix.get(i5, i2)) {
                if (crossCheckStateCount[1] > i3) {
                    break;
                }
                crossCheckStateCount[1] = crossCheckStateCount[1] + 1;
                i5--;
            } else {
                break;
            }
        }
        if (i5 < 0 || crossCheckStateCount[1] > i3) {
            return Float.NaN;
        }
        while (i5 >= 0) {
            if (bitMatrix.get(i5, i2)) {
                if (crossCheckStateCount[0] > i3) {
                    break;
                }
                crossCheckStateCount[0] = crossCheckStateCount[0] + 1;
                i5--;
            } else {
                break;
            }
        }
        if (crossCheckStateCount[0] > i3) {
            return Float.NaN;
        }
        i5 = i + 1;
        while (i5 < width && bitMatrix.get(i5, i2)) {
            crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
            i5++;
        }
        if (i5 == width) {
            return Float.NaN;
        }
        while (i5 < width) {
            if (!bitMatrix.get(i5, i2)) {
                if (crossCheckStateCount[3] >= i3) {
                    break;
                }
                crossCheckStateCount[3] = crossCheckStateCount[3] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i5 == width || crossCheckStateCount[3] >= i3) {
            return Float.NaN;
        }
        while (i5 < width) {
            if (bitMatrix.get(i5, i2)) {
                if (crossCheckStateCount[4] >= i3) {
                    break;
                }
                crossCheckStateCount[4] = crossCheckStateCount[4] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (crossCheckStateCount[4] >= i3) {
            return Float.NaN;
        }
        if (Math.abs(((((crossCheckStateCount[0] + crossCheckStateCount[1]) + crossCheckStateCount[2]) + crossCheckStateCount[3]) + crossCheckStateCount[4]) - i4) * 5 >= i4) {
            return Float.NaN;
        }
        return !foundPatternCross(crossCheckStateCount) ? Float.NaN : centerFromEnd(crossCheckStateCount, i5);
    }

    private float crossCheckVertical(int i, int i2, int i3, int i4) {
        BitMatrix bitMatrix = this.image;
        int height = bitMatrix.getHeight();
        int[] crossCheckStateCount = getCrossCheckStateCount();
        int i5 = i;
        while (i5 >= 0 && bitMatrix.get(i2, i5)) {
            crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
            i5--;
        }
        if (i5 < 0) {
            return Float.NaN;
        }
        while (i5 >= 0) {
            if (!bitMatrix.get(i2, i5)) {
                if (crossCheckStateCount[1] > i3) {
                    break;
                }
                crossCheckStateCount[1] = crossCheckStateCount[1] + 1;
                i5--;
            } else {
                break;
            }
        }
        if (i5 < 0 || crossCheckStateCount[1] > i3) {
            return Float.NaN;
        }
        while (i5 >= 0) {
            if (bitMatrix.get(i2, i5)) {
                if (crossCheckStateCount[0] > i3) {
                    break;
                }
                crossCheckStateCount[0] = crossCheckStateCount[0] + 1;
                i5--;
            } else {
                break;
            }
        }
        if (crossCheckStateCount[0] > i3) {
            return Float.NaN;
        }
        i5 = i + 1;
        while (i5 < height && bitMatrix.get(i2, i5)) {
            crossCheckStateCount[2] = crossCheckStateCount[2] + 1;
            i5++;
        }
        if (i5 == height) {
            return Float.NaN;
        }
        while (i5 < height) {
            if (!bitMatrix.get(i2, i5)) {
                if (crossCheckStateCount[3] >= i3) {
                    break;
                }
                crossCheckStateCount[3] = crossCheckStateCount[3] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (i5 == height || crossCheckStateCount[3] >= i3) {
            return Float.NaN;
        }
        while (i5 < height) {
            if (bitMatrix.get(i2, i5)) {
                if (crossCheckStateCount[4] >= i3) {
                    break;
                }
                crossCheckStateCount[4] = crossCheckStateCount[4] + 1;
                i5++;
            } else {
                break;
            }
        }
        if (crossCheckStateCount[4] >= i3) {
            return Float.NaN;
        }
        if (Math.abs(((((crossCheckStateCount[0] + crossCheckStateCount[1]) + crossCheckStateCount[2]) + crossCheckStateCount[3]) + crossCheckStateCount[4]) - i4) * 5 >= i4 * 2) {
            return Float.NaN;
        }
        return !foundPatternCross(crossCheckStateCount) ? Float.NaN : centerFromEnd(crossCheckStateCount, i5);
    }

    private int findRowSkip() {
        if (this.possibleCenters.size() <= 1) {
            return 0;
        }
        ResultPoint resultPoint = null;
        for (ResultPoint resultPoint2 : this.possibleCenters) {
            if (resultPoint2.getCount() >= 2) {
                if (resultPoint != null) {
                    this.hasSkipped = true;
                    return ((int) (Math.abs(resultPoint.getX() - resultPoint2.getX()) - Math.abs(resultPoint.getY() - resultPoint2.getY()))) / 2;
                }
                resultPoint = resultPoint2;
            }
        }
        return 0;
    }

    protected static boolean foundPatternCross(int[] iArr) {
        int i = 0;
        for (int i2 = 0; i2 < 5; i2++) {
            int i3 = iArr[i2];
            if (i3 == 0) {
                return false;
            }
            i += i3;
        }
        if (i < 7) {
            return false;
        }
        float f = ((float) i) / 7.0f;
        float f2 = f / 2.0f;
        return Math.abs(f - ((float) iArr[0])) < f2 && Math.abs(f - ((float) iArr[1])) < f2 && Math.abs((3.0f * f) - ((float) iArr[2])) < 3.0f * f2 && Math.abs(f - ((float) iArr[3])) < f2 && Math.abs(f - ((float) iArr[4])) < f2;
    }

    private int[] getCrossCheckStateCount() {
        this.crossCheckStateCount[0] = 0;
        this.crossCheckStateCount[1] = 0;
        this.crossCheckStateCount[2] = 0;
        this.crossCheckStateCount[3] = 0;
        this.crossCheckStateCount[4] = 0;
        return this.crossCheckStateCount;
    }

    private boolean haveMultiplyConfirmedCenters() {
        int i = 0;
        float f = 0.0f;
        int size = this.possibleCenters.size();
        for (FinderPattern finderPattern : this.possibleCenters) {
            if (finderPattern.getCount() >= 2) {
                i++;
                f += finderPattern.getEstimatedModuleSize();
            }
        }
        if (i < 3) {
            return false;
        }
        float f2 = f / ((float) size);
        float f3 = 0.0f;
        for (FinderPattern finderPattern2 : this.possibleCenters) {
            f3 += Math.abs(finderPattern2.getEstimatedModuleSize() - f2);
        }
        return f3 <= 0.05f * f;
    }

    private FinderPattern[] selectBestPatterns() throws NotFoundException {
        int size = this.possibleCenters.size();
        if (size >= 3) {
            float f;
            if (size > 3) {
                f = 0.0f;
                float f2 = 0.0f;
                for (FinderPattern estimatedModuleSize : this.possibleCenters) {
                    float estimatedModuleSize2 = estimatedModuleSize.getEstimatedModuleSize();
                    f += estimatedModuleSize2;
                    f2 += estimatedModuleSize2 * estimatedModuleSize2;
                }
                float f3 = f / ((float) size);
                float sqrt = (float) Math.sqrt((double) ((f2 / ((float) size)) - (f3 * f3)));
                Collections.sort(this.possibleCenters, new FurthestFromAverageComparator(f3));
                float max = Math.max(0.2f * f3, sqrt);
                int i = 0;
                while (i < this.possibleCenters.size() && this.possibleCenters.size() > 3) {
                    if (Math.abs(((FinderPattern) this.possibleCenters.get(i)).getEstimatedModuleSize() - f3) > max) {
                        this.possibleCenters.remove(i);
                        i--;
                    }
                    i++;
                }
            }
            if (this.possibleCenters.size() > 3) {
                f = 0.0f;
                for (FinderPattern estimatedModuleSize3 : this.possibleCenters) {
                    f += estimatedModuleSize3.getEstimatedModuleSize();
                }
                Collections.sort(this.possibleCenters, new CenterComparator(f / ((float) this.possibleCenters.size())));
                this.possibleCenters.subList(3, this.possibleCenters.size()).clear();
            }
            return new FinderPattern[]{(FinderPattern) this.possibleCenters.get(0), (FinderPattern) this.possibleCenters.get(1), (FinderPattern) this.possibleCenters.get(2)};
        }
        throw NotFoundException.getNotFoundInstance();
    }

    final FinderPatternInfo find(Map<DecodeHintType, ?> map) throws NotFoundException {
        Object obj;
        boolean z;
        int height;
        int width;
        int i;
        boolean z2;
        int[] iArr;
        int i2;
        int i3;
        int i4;
        int findRowSkip;
        ResultPoint[] selectBestPatterns;
        if (map != null) {
            if (map.containsKey(DecodeHintType.TRY_HARDER)) {
                obj = 1;
                if (map != null) {
                    if (map.containsKey(DecodeHintType.PURE_BARCODE)) {
                        z = true;
                        height = this.image.getHeight();
                        width = this.image.getWidth();
                        i = (height * 3) / 228;
                        if (i < 3 || r13 != null) {
                            i = 3;
                        }
                        z2 = false;
                        iArr = new int[5];
                        i2 = i - 1;
                        while (i2 < height && !r3) {
                            iArr[0] = 0;
                            iArr[1] = 0;
                            iArr[2] = 0;
                            iArr[3] = 0;
                            iArr[4] = 0;
                            i3 = 0;
                            i4 = 0;
                            while (i4 < width) {
                                if (!this.image.get(i4, i2)) {
                                    if ((i3 & 1) == 1) {
                                        i3++;
                                    }
                                    iArr[i3] = iArr[i3] + 1;
                                } else if ((i3 & 1) == 0) {
                                    iArr[i3] = iArr[i3] + 1;
                                } else if (i3 == 4) {
                                    i3++;
                                    iArr[i3] = iArr[i3] + 1;
                                } else if (foundPatternCross(iArr)) {
                                    iArr[0] = iArr[2];
                                    iArr[1] = iArr[3];
                                    iArr[2] = iArr[4];
                                    iArr[3] = 1;
                                    iArr[4] = 0;
                                    i3 = 3;
                                } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                                    i = 2;
                                    if (this.hasSkipped) {
                                        z2 = haveMultiplyConfirmedCenters();
                                    } else {
                                        findRowSkip = findRowSkip();
                                        if (findRowSkip > iArr[2]) {
                                            i2 += (findRowSkip - iArr[2]) - 2;
                                            i4 = width - 1;
                                        }
                                    }
                                    i3 = 0;
                                    iArr[0] = 0;
                                    iArr[1] = 0;
                                    iArr[2] = 0;
                                    iArr[3] = 0;
                                    iArr[4] = 0;
                                } else {
                                    iArr[0] = iArr[2];
                                    iArr[1] = iArr[3];
                                    iArr[2] = iArr[4];
                                    iArr[3] = 1;
                                    iArr[4] = 0;
                                    i3 = 3;
                                }
                                i4++;
                            }
                            if (foundPatternCross(iArr) && handlePossibleCenter(iArr, i2, width, z)) {
                                i = iArr[0];
                                if (!this.hasSkipped) {
                                    z2 = haveMultiplyConfirmedCenters();
                                }
                            }
                            i2 += i;
                        }
                        selectBestPatterns = selectBestPatterns();
                        ResultPoint.orderBestPatterns(selectBestPatterns);
                        return new FinderPatternInfo(selectBestPatterns);
                    }
                }
                z = false;
                height = this.image.getHeight();
                width = this.image.getWidth();
                i = (height * 3) / 228;
                if (i < 3) {
                    z2 = false;
                    iArr = new int[5];
                    i2 = i - 1;
                    while (i2 < height) {
                        iArr[0] = 0;
                        iArr[1] = 0;
                        iArr[2] = 0;
                        iArr[3] = 0;
                        iArr[4] = 0;
                        i3 = 0;
                        i4 = 0;
                        while (i4 < width) {
                            if (!this.image.get(i4, i2)) {
                                if ((i3 & 1) == 1) {
                                    i3++;
                                }
                                iArr[i3] = iArr[i3] + 1;
                            } else if ((i3 & 1) == 0) {
                                iArr[i3] = iArr[i3] + 1;
                            } else if (i3 == 4) {
                                i3++;
                                iArr[i3] = iArr[i3] + 1;
                            } else if (foundPatternCross(iArr)) {
                                iArr[0] = iArr[2];
                                iArr[1] = iArr[3];
                                iArr[2] = iArr[4];
                                iArr[3] = 1;
                                iArr[4] = 0;
                                i3 = 3;
                            } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                                i = 2;
                                if (this.hasSkipped) {
                                    z2 = haveMultiplyConfirmedCenters();
                                } else {
                                    findRowSkip = findRowSkip();
                                    if (findRowSkip > iArr[2]) {
                                        i2 += (findRowSkip - iArr[2]) - 2;
                                        i4 = width - 1;
                                    }
                                }
                                i3 = 0;
                                iArr[0] = 0;
                                iArr[1] = 0;
                                iArr[2] = 0;
                                iArr[3] = 0;
                                iArr[4] = 0;
                            } else {
                                iArr[0] = iArr[2];
                                iArr[1] = iArr[3];
                                iArr[2] = iArr[4];
                                iArr[3] = 1;
                                iArr[4] = 0;
                                i3 = 3;
                            }
                            i4++;
                        }
                        i = iArr[0];
                        if (!this.hasSkipped) {
                            z2 = haveMultiplyConfirmedCenters();
                        }
                        i2 += i;
                    }
                    selectBestPatterns = selectBestPatterns();
                    ResultPoint.orderBestPatterns(selectBestPatterns);
                    return new FinderPatternInfo(selectBestPatterns);
                }
                i = 3;
                z2 = false;
                iArr = new int[5];
                i2 = i - 1;
                while (i2 < height) {
                    iArr[0] = 0;
                    iArr[1] = 0;
                    iArr[2] = 0;
                    iArr[3] = 0;
                    iArr[4] = 0;
                    i3 = 0;
                    i4 = 0;
                    while (i4 < width) {
                        if (!this.image.get(i4, i2)) {
                            if ((i3 & 1) == 1) {
                                i3++;
                            }
                            iArr[i3] = iArr[i3] + 1;
                        } else if ((i3 & 1) == 0) {
                            iArr[i3] = iArr[i3] + 1;
                        } else if (i3 == 4) {
                            i3++;
                            iArr[i3] = iArr[i3] + 1;
                        } else if (foundPatternCross(iArr)) {
                            iArr[0] = iArr[2];
                            iArr[1] = iArr[3];
                            iArr[2] = iArr[4];
                            iArr[3] = 1;
                            iArr[4] = 0;
                            i3 = 3;
                        } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                            iArr[0] = iArr[2];
                            iArr[1] = iArr[3];
                            iArr[2] = iArr[4];
                            iArr[3] = 1;
                            iArr[4] = 0;
                            i3 = 3;
                        } else {
                            i = 2;
                            if (this.hasSkipped) {
                                findRowSkip = findRowSkip();
                                if (findRowSkip > iArr[2]) {
                                    i2 += (findRowSkip - iArr[2]) - 2;
                                    i4 = width - 1;
                                }
                            } else {
                                z2 = haveMultiplyConfirmedCenters();
                            }
                            i3 = 0;
                            iArr[0] = 0;
                            iArr[1] = 0;
                            iArr[2] = 0;
                            iArr[3] = 0;
                            iArr[4] = 0;
                        }
                        i4++;
                    }
                    i = iArr[0];
                    if (!this.hasSkipped) {
                        z2 = haveMultiplyConfirmedCenters();
                    }
                    i2 += i;
                }
                selectBestPatterns = selectBestPatterns();
                ResultPoint.orderBestPatterns(selectBestPatterns);
                return new FinderPatternInfo(selectBestPatterns);
            }
        }
        obj = null;
        if (map != null) {
            if (map.containsKey(DecodeHintType.PURE_BARCODE)) {
                z = true;
                height = this.image.getHeight();
                width = this.image.getWidth();
                i = (height * 3) / 228;
                if (i < 3) {
                    z2 = false;
                    iArr = new int[5];
                    i2 = i - 1;
                    while (i2 < height) {
                        iArr[0] = 0;
                        iArr[1] = 0;
                        iArr[2] = 0;
                        iArr[3] = 0;
                        iArr[4] = 0;
                        i3 = 0;
                        i4 = 0;
                        while (i4 < width) {
                            if (!this.image.get(i4, i2)) {
                                if ((i3 & 1) == 1) {
                                    i3++;
                                }
                                iArr[i3] = iArr[i3] + 1;
                            } else if ((i3 & 1) == 0) {
                                iArr[i3] = iArr[i3] + 1;
                            } else if (i3 == 4) {
                                i3++;
                                iArr[i3] = iArr[i3] + 1;
                            } else if (foundPatternCross(iArr)) {
                                iArr[0] = iArr[2];
                                iArr[1] = iArr[3];
                                iArr[2] = iArr[4];
                                iArr[3] = 1;
                                iArr[4] = 0;
                                i3 = 3;
                            } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                                i = 2;
                                if (this.hasSkipped) {
                                    z2 = haveMultiplyConfirmedCenters();
                                } else {
                                    findRowSkip = findRowSkip();
                                    if (findRowSkip > iArr[2]) {
                                        i2 += (findRowSkip - iArr[2]) - 2;
                                        i4 = width - 1;
                                    }
                                }
                                i3 = 0;
                                iArr[0] = 0;
                                iArr[1] = 0;
                                iArr[2] = 0;
                                iArr[3] = 0;
                                iArr[4] = 0;
                            } else {
                                iArr[0] = iArr[2];
                                iArr[1] = iArr[3];
                                iArr[2] = iArr[4];
                                iArr[3] = 1;
                                iArr[4] = 0;
                                i3 = 3;
                            }
                            i4++;
                        }
                        i = iArr[0];
                        if (!this.hasSkipped) {
                            z2 = haveMultiplyConfirmedCenters();
                        }
                        i2 += i;
                    }
                    selectBestPatterns = selectBestPatterns();
                    ResultPoint.orderBestPatterns(selectBestPatterns);
                    return new FinderPatternInfo(selectBestPatterns);
                }
                i = 3;
                z2 = false;
                iArr = new int[5];
                i2 = i - 1;
                while (i2 < height) {
                    iArr[0] = 0;
                    iArr[1] = 0;
                    iArr[2] = 0;
                    iArr[3] = 0;
                    iArr[4] = 0;
                    i3 = 0;
                    i4 = 0;
                    while (i4 < width) {
                        if (!this.image.get(i4, i2)) {
                            if ((i3 & 1) == 1) {
                                i3++;
                            }
                            iArr[i3] = iArr[i3] + 1;
                        } else if ((i3 & 1) == 0) {
                            iArr[i3] = iArr[i3] + 1;
                        } else if (i3 == 4) {
                            i3++;
                            iArr[i3] = iArr[i3] + 1;
                        } else if (foundPatternCross(iArr)) {
                            iArr[0] = iArr[2];
                            iArr[1] = iArr[3];
                            iArr[2] = iArr[4];
                            iArr[3] = 1;
                            iArr[4] = 0;
                            i3 = 3;
                        } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                            iArr[0] = iArr[2];
                            iArr[1] = iArr[3];
                            iArr[2] = iArr[4];
                            iArr[3] = 1;
                            iArr[4] = 0;
                            i3 = 3;
                        } else {
                            i = 2;
                            if (this.hasSkipped) {
                                findRowSkip = findRowSkip();
                                if (findRowSkip > iArr[2]) {
                                    i2 += (findRowSkip - iArr[2]) - 2;
                                    i4 = width - 1;
                                }
                            } else {
                                z2 = haveMultiplyConfirmedCenters();
                            }
                            i3 = 0;
                            iArr[0] = 0;
                            iArr[1] = 0;
                            iArr[2] = 0;
                            iArr[3] = 0;
                            iArr[4] = 0;
                        }
                        i4++;
                    }
                    i = iArr[0];
                    if (!this.hasSkipped) {
                        z2 = haveMultiplyConfirmedCenters();
                    }
                    i2 += i;
                }
                selectBestPatterns = selectBestPatterns();
                ResultPoint.orderBestPatterns(selectBestPatterns);
                return new FinderPatternInfo(selectBestPatterns);
            }
        }
        z = false;
        height = this.image.getHeight();
        width = this.image.getWidth();
        i = (height * 3) / 228;
        if (i < 3) {
            z2 = false;
            iArr = new int[5];
            i2 = i - 1;
            while (i2 < height) {
                iArr[0] = 0;
                iArr[1] = 0;
                iArr[2] = 0;
                iArr[3] = 0;
                iArr[4] = 0;
                i3 = 0;
                i4 = 0;
                while (i4 < width) {
                    if (!this.image.get(i4, i2)) {
                        if ((i3 & 1) == 1) {
                            i3++;
                        }
                        iArr[i3] = iArr[i3] + 1;
                    } else if ((i3 & 1) == 0) {
                        iArr[i3] = iArr[i3] + 1;
                    } else if (i3 == 4) {
                        i3++;
                        iArr[i3] = iArr[i3] + 1;
                    } else if (foundPatternCross(iArr)) {
                        iArr[0] = iArr[2];
                        iArr[1] = iArr[3];
                        iArr[2] = iArr[4];
                        iArr[3] = 1;
                        iArr[4] = 0;
                        i3 = 3;
                    } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                        i = 2;
                        if (this.hasSkipped) {
                            z2 = haveMultiplyConfirmedCenters();
                        } else {
                            findRowSkip = findRowSkip();
                            if (findRowSkip > iArr[2]) {
                                i2 += (findRowSkip - iArr[2]) - 2;
                                i4 = width - 1;
                            }
                        }
                        i3 = 0;
                        iArr[0] = 0;
                        iArr[1] = 0;
                        iArr[2] = 0;
                        iArr[3] = 0;
                        iArr[4] = 0;
                    } else {
                        iArr[0] = iArr[2];
                        iArr[1] = iArr[3];
                        iArr[2] = iArr[4];
                        iArr[3] = 1;
                        iArr[4] = 0;
                        i3 = 3;
                    }
                    i4++;
                }
                i = iArr[0];
                if (!this.hasSkipped) {
                    z2 = haveMultiplyConfirmedCenters();
                }
                i2 += i;
            }
            selectBestPatterns = selectBestPatterns();
            ResultPoint.orderBestPatterns(selectBestPatterns);
            return new FinderPatternInfo(selectBestPatterns);
        }
        i = 3;
        z2 = false;
        iArr = new int[5];
        i2 = i - 1;
        while (i2 < height) {
            iArr[0] = 0;
            iArr[1] = 0;
            iArr[2] = 0;
            iArr[3] = 0;
            iArr[4] = 0;
            i3 = 0;
            i4 = 0;
            while (i4 < width) {
                if (!this.image.get(i4, i2)) {
                    if ((i3 & 1) == 1) {
                        i3++;
                    }
                    iArr[i3] = iArr[i3] + 1;
                } else if ((i3 & 1) == 0) {
                    iArr[i3] = iArr[i3] + 1;
                } else if (i3 == 4) {
                    i3++;
                    iArr[i3] = iArr[i3] + 1;
                } else if (foundPatternCross(iArr)) {
                    iArr[0] = iArr[2];
                    iArr[1] = iArr[3];
                    iArr[2] = iArr[4];
                    iArr[3] = 1;
                    iArr[4] = 0;
                    i3 = 3;
                } else if (handlePossibleCenter(iArr, i2, i4, z)) {
                    iArr[0] = iArr[2];
                    iArr[1] = iArr[3];
                    iArr[2] = iArr[4];
                    iArr[3] = 1;
                    iArr[4] = 0;
                    i3 = 3;
                } else {
                    i = 2;
                    if (this.hasSkipped) {
                        findRowSkip = findRowSkip();
                        if (findRowSkip > iArr[2]) {
                            i2 += (findRowSkip - iArr[2]) - 2;
                            i4 = width - 1;
                        }
                    } else {
                        z2 = haveMultiplyConfirmedCenters();
                    }
                    i3 = 0;
                    iArr[0] = 0;
                    iArr[1] = 0;
                    iArr[2] = 0;
                    iArr[3] = 0;
                    iArr[4] = 0;
                }
                i4++;
            }
            i = iArr[0];
            if (!this.hasSkipped) {
                z2 = haveMultiplyConfirmedCenters();
            }
            i2 += i;
        }
        selectBestPatterns = selectBestPatterns();
        ResultPoint.orderBestPatterns(selectBestPatterns);
        return new FinderPatternInfo(selectBestPatterns);
    }

    protected final boolean handlePossibleCenter(int[] iArr, int i, int i2, boolean z) {
        int i3 = (((iArr[0] + iArr[1]) + iArr[2]) + iArr[3]) + iArr[4];
        float centerFromEnd = centerFromEnd(iArr, i2);
        float crossCheckVertical = crossCheckVertical(i, (int) centerFromEnd, iArr[2], i3);
        if (!Float.isNaN(crossCheckVertical)) {
            centerFromEnd = crossCheckHorizontal((int) centerFromEnd, (int) crossCheckVertical, iArr[2], i3);
            if (!Float.isNaN(centerFromEnd)) {
                if (!z || crossCheckDiagonal((int) crossCheckVertical, (int) centerFromEnd, iArr[2], i3)) {
                    float f = ((float) i3) / 7.0f;
                    Object obj = null;
                    for (int i4 = 0; i4 < this.possibleCenters.size(); i4++) {
                        FinderPattern finderPattern = (FinderPattern) this.possibleCenters.get(i4);
                        if (finderPattern.aboutEquals(f, crossCheckVertical, centerFromEnd)) {
                            this.possibleCenters.set(i4, finderPattern.combineEstimate(crossCheckVertical, centerFromEnd, f));
                            obj = 1;
                            break;
                        }
                    }
                    if (obj == null) {
                        ResultPoint finderPattern2 = new FinderPattern(centerFromEnd, crossCheckVertical, f);
                        this.possibleCenters.add(finderPattern2);
                        if (this.resultPointCallback != null) {
                            this.resultPointCallback.foundPossibleResultPoint(finderPattern2);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}

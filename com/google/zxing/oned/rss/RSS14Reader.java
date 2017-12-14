package com.google.zxing.oned.rss;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.OneDReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RSS14Reader extends AbstractRSSReader {
    private static final int[][] FINDER_PATTERNS;
    private static final int[] INSIDE_GSUM;
    private static final int[] INSIDE_ODD_TOTAL_SUBSET = new int[]{4, 20, 48, 81};
    private static final int[] INSIDE_ODD_WIDEST = new int[]{2, 4, 6, 8};
    private static final int[] OUTSIDE_EVEN_TOTAL_SUBSET = new int[]{1, 10, 34, 70, 126};
    private static final int[] OUTSIDE_GSUM;
    private static final int[] OUTSIDE_ODD_WIDEST = new int[]{8, 6, 4, 3, 1};
    private final List<Pair> possibleLeftPairs = new ArrayList();
    private final List<Pair> possibleRightPairs = new ArrayList();

    static {
        int[] iArr = new int[5];
        iArr[1] = 161;
        iArr[2] = 961;
        iArr[3] = 2015;
        iArr[4] = 2715;
        OUTSIDE_GSUM = iArr;
        iArr = new int[4];
        iArr[1] = 336;
        iArr[2] = 1036;
        iArr[3] = 1516;
        INSIDE_GSUM = iArr;
        r0 = new int[9][];
        r0[0] = new int[]{3, 8, 2, 1};
        r0[1] = new int[]{3, 5, 5, 1};
        r0[2] = new int[]{3, 3, 7, 1};
        r0[3] = new int[]{3, 1, 9, 1};
        r0[4] = new int[]{2, 7, 4, 1};
        r0[5] = new int[]{2, 5, 6, 1};
        r0[6] = new int[]{2, 3, 8, 1};
        r0[7] = new int[]{1, 5, 7, 1};
        r0[8] = new int[]{1, 3, 9, 1};
        FINDER_PATTERNS = r0;
    }

    private static void addOrTally(Collection<Pair> collection, Pair pair) {
        if (pair != null) {
            Object obj = null;
            for (Pair pair2 : collection) {
                if (pair2.getValue() == pair.getValue()) {
                    pair2.incrementCount();
                    obj = 1;
                    break;
                }
            }
            if (obj == null) {
                collection.add(pair);
            }
        }
    }

    private void adjustOddEvenCounts(boolean z, int i) throws NotFoundException {
        int count = AbstractRSSReader.count(getOddCounts());
        int count2 = AbstractRSSReader.count(getEvenCounts());
        int i2 = (count + count2) - i;
        Object obj = (count & 1) != (!z ? 0 : 1) ? null : 1;
        Object obj2 = (count2 & 1) != 1 ? null : 1;
        Object obj3 = null;
        Object obj4 = null;
        Object obj5 = null;
        Object obj6 = null;
        if (z) {
            if (count > 12) {
                obj4 = 1;
            } else if (count < 4) {
                obj3 = 1;
            }
            if (count2 > 12) {
                obj6 = 1;
            } else if (count2 < 4) {
                obj5 = 1;
            }
        } else {
            if (count > 11) {
                obj4 = 1;
            } else if (count < 5) {
                obj3 = 1;
            }
            if (count2 > 10) {
                obj6 = 1;
            } else if (count2 < 4) {
                obj5 = 1;
            }
        }
        if (i2 != 1) {
            if (i2 != -1) {
                if (i2 != 0) {
                    throw NotFoundException.getNotFoundInstance();
                } else if (obj == null) {
                    if (obj2 != null) {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else if (obj2 == null) {
                    throw NotFoundException.getNotFoundInstance();
                } else if (count >= count2) {
                    obj4 = 1;
                    obj5 = 1;
                } else {
                    obj3 = 1;
                    obj6 = 1;
                }
            } else if (obj == null) {
                if (obj2 != null) {
                    obj5 = 1;
                } else {
                    throw NotFoundException.getNotFoundInstance();
                }
            } else if (obj2 == null) {
                obj3 = 1;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (obj == null) {
            if (obj2 != null) {
                obj6 = 1;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        } else if (obj2 == null) {
            obj4 = 1;
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
        if (obj3 != null) {
            if (obj4 == null) {
                AbstractRSSReader.increment(getOddCounts(), getOddRoundingErrors());
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        if (obj4 != null) {
            AbstractRSSReader.decrement(getOddCounts(), getOddRoundingErrors());
        }
        if (obj5 != null) {
            if (obj6 == null) {
                AbstractRSSReader.increment(getEvenCounts(), getOddRoundingErrors());
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        if (obj6 != null) {
            AbstractRSSReader.decrement(getEvenCounts(), getEvenRoundingErrors());
        }
    }

    private static boolean checkChecksum(Pair pair, Pair pair2) {
        int checksumPortion = (pair.getChecksumPortion() + (pair2.getChecksumPortion() * 16)) % 79;
        int value = (pair.getFinderPattern().getValue() * 9) + pair2.getFinderPattern().getValue();
        if (value > 72) {
            value--;
        }
        if (value > 8) {
            value--;
        }
        return checksumPortion == value;
    }

    private static Result constructResult(Pair pair, Pair pair2) {
        int length;
        String valueOf = String.valueOf((((long) pair.getValue()) * 4537077) + ((long) pair2.getValue()));
        StringBuilder stringBuilder = new StringBuilder(14);
        for (length = 13 - valueOf.length(); length > 0; length--) {
            stringBuilder.append('0');
        }
        stringBuilder.append(valueOf);
        int i = 0;
        for (length = 0; length < 13; length++) {
            int charAt = stringBuilder.charAt(length) - 48;
            if ((length & 1) == 0) {
                charAt *= 3;
            }
            i += charAt;
        }
        i = 10 - (i % 10);
        if (i == 10) {
            i = 0;
        }
        stringBuilder.append(i);
        ResultPoint[] resultPoints = pair.getFinderPattern().getResultPoints();
        ResultPoint[] resultPoints2 = pair2.getFinderPattern().getResultPoints();
        return new Result(String.valueOf(stringBuilder.toString()), null, new ResultPoint[]{resultPoints[0], resultPoints[1], resultPoints2[0], resultPoints2[1]}, BarcodeFormat.RSS_14);
    }

    private DataCharacter decodeDataCharacter(BitArray bitArray, FinderPattern finderPattern, boolean z) throws NotFoundException {
        int i;
        int[] dataCharacterCounters = getDataCharacterCounters();
        dataCharacterCounters[0] = 0;
        dataCharacterCounters[1] = 0;
        dataCharacterCounters[2] = 0;
        dataCharacterCounters[3] = 0;
        dataCharacterCounters[4] = 0;
        dataCharacterCounters[5] = 0;
        dataCharacterCounters[6] = 0;
        dataCharacterCounters[7] = 0;
        if (z) {
            OneDReader.recordPatternInReverse(bitArray, finderPattern.getStartEnd()[0], dataCharacterCounters);
        } else {
            OneDReader.recordPattern(bitArray, finderPattern.getStartEnd()[1] + 1, dataCharacterCounters);
            i = 0;
            for (int length = dataCharacterCounters.length - 1; i < length; length--) {
                int i2 = dataCharacterCounters[i];
                dataCharacterCounters[i] = dataCharacterCounters[length];
                dataCharacterCounters[length] = i2;
                i++;
            }
        }
        int i3 = !z ? 15 : 16;
        float count = ((float) AbstractRSSReader.count(dataCharacterCounters)) / ((float) i3);
        int[] oddCounts = getOddCounts();
        int[] evenCounts = getEvenCounts();
        float[] oddRoundingErrors = getOddRoundingErrors();
        float[] evenRoundingErrors = getEvenRoundingErrors();
        for (i = 0; i < dataCharacterCounters.length; i++) {
            float f = ((float) dataCharacterCounters[i]) / count;
            int i4 = (int) (0.5f + f);
            if (i4 < 1) {
                i4 = 1;
            } else if (i4 > 8) {
                i4 = 8;
            }
            int i5 = i / 2;
            if ((i & 1) != 0) {
                evenCounts[i5] = i4;
                evenRoundingErrors[i5] = f - ((float) i4);
            } else {
                oddCounts[i5] = i4;
                oddRoundingErrors[i5] = f - ((float) i4);
            }
        }
        adjustOddEvenCounts(z, i3);
        int i6 = 0;
        int i7 = 0;
        for (i = oddCounts.length - 1; i >= 0; i--) {
            i7 = (i7 * 9) + oddCounts[i];
            i6 += oddCounts[i];
        }
        int i8 = 0;
        int i9 = 0;
        for (i = evenCounts.length - 1; i >= 0; i--) {
            i8 = (i8 * 9) + evenCounts[i];
            i9 += evenCounts[i];
        }
        int i10 = i7 + (i8 * 3);
        int i11;
        int i12;
        if (z) {
            if ((i6 & 1) == 0 && i6 <= 12 && i6 >= 4) {
                i11 = (12 - i6) / 2;
                i12 = OUTSIDE_ODD_WIDEST[i11];
                int i13 = 9 - i12;
                return new DataCharacter(((RSSUtils.getRSSvalue(oddCounts, i12, false) * OUTSIDE_EVEN_TOTAL_SUBSET[i11]) + RSSUtils.getRSSvalue(evenCounts, i13, true)) + OUTSIDE_GSUM[i11], i10);
            }
            throw NotFoundException.getNotFoundInstance();
        } else if ((i9 & 1) == 0 && i9 <= 10 && i9 >= 4) {
            i11 = (10 - i9) / 2;
            i12 = INSIDE_ODD_WIDEST[i11];
            return new DataCharacter(((RSSUtils.getRSSvalue(evenCounts, 9 - i12, false) * INSIDE_ODD_TOTAL_SUBSET[i11]) + RSSUtils.getRSSvalue(oddCounts, i12, true)) + INSIDE_GSUM[i11], i10);
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private Pair decodePair(BitArray bitArray, boolean z, int i, Map<DecodeHintType, ?> map) {
        try {
            int[] findFinderPattern = findFinderPattern(bitArray, 0, z);
            FinderPattern parseFoundFinderPattern = parseFoundFinderPattern(bitArray, i, z, findFinderPattern);
            ResultPointCallback resultPointCallback = map != null ? (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK) : null;
            if (resultPointCallback != null) {
                float f = ((float) (findFinderPattern[0] + findFinderPattern[1])) / 2.0f;
                if (z) {
                    f = ((float) (bitArray.getSize() - 1)) - f;
                }
                resultPointCallback.foundPossibleResultPoint(new ResultPoint(f, (float) i));
            }
            DataCharacter decodeDataCharacter = decodeDataCharacter(bitArray, parseFoundFinderPattern, true);
            DataCharacter decodeDataCharacter2 = decodeDataCharacter(bitArray, parseFoundFinderPattern, false);
            return new Pair((decodeDataCharacter.getValue() * 1597) + decodeDataCharacter2.getValue(), decodeDataCharacter.getChecksumPortion() + (decodeDataCharacter2.getChecksumPortion() * 4), parseFoundFinderPattern);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private int[] findFinderPattern(BitArray bitArray, int i, boolean z) throws NotFoundException {
        int[] decodeFinderCounters = getDecodeFinderCounters();
        decodeFinderCounters[0] = 0;
        decodeFinderCounters[1] = 0;
        decodeFinderCounters[2] = 0;
        decodeFinderCounters[3] = 0;
        int size = bitArray.getSize();
        int i2 = 0;
        while (i < size) {
            i2 = !bitArray.get(i) ? 1 : 0;
            if (z == i2) {
                break;
            }
            i++;
        }
        int i3 = 0;
        int i4 = i;
        for (int i5 = i; i5 < size; i5++) {
            if ((bitArray.get(i5) ^ i2) == 0) {
                if (i3 != 3) {
                    i3++;
                } else if (AbstractRSSReader.isFinderPattern(decodeFinderCounters)) {
                    return new int[]{i4, i5};
                } else {
                    i4 += decodeFinderCounters[0] + decodeFinderCounters[1];
                    decodeFinderCounters[0] = decodeFinderCounters[2];
                    decodeFinderCounters[1] = decodeFinderCounters[3];
                    decodeFinderCounters[2] = 0;
                    decodeFinderCounters[3] = 0;
                    i3--;
                }
                decodeFinderCounters[i3] = 1;
                i2 = i2 == 0 ? 1 : 0;
            } else {
                decodeFinderCounters[i3] = decodeFinderCounters[i3] + 1;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private FinderPattern parseFoundFinderPattern(BitArray bitArray, int i, boolean z, int[] iArr) throws NotFoundException {
        boolean z2 = bitArray.get(iArr[0]);
        int i2 = iArr[0] - 1;
        while (i2 >= 0 && (bitArray.get(i2) ^ z2) != 0) {
            i2--;
        }
        i2++;
        int i3 = iArr[0] - i2;
        Object decodeFinderCounters = getDecodeFinderCounters();
        System.arraycopy(decodeFinderCounters, 0, decodeFinderCounters, 1, decodeFinderCounters.length - 1);
        decodeFinderCounters[0] = i3;
        int parseFinderValue = AbstractRSSReader.parseFinderValue(decodeFinderCounters, FINDER_PATTERNS);
        int i4 = i2;
        int i5 = iArr[1];
        if (z) {
            i4 = (bitArray.getSize() - 1) - i2;
            i5 = (bitArray.getSize() - 1) - i5;
        }
        return new FinderPattern(parseFinderValue, new int[]{i2, iArr[1]}, i4, i5, i);
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException {
        addOrTally(this.possibleLeftPairs, decodePair(bitArray, false, i, map));
        bitArray.reverse();
        addOrTally(this.possibleRightPairs, decodePair(bitArray, true, i, map));
        bitArray.reverse();
        int size = this.possibleLeftPairs.size();
        for (int i2 = 0; i2 < size; i2++) {
            Pair pair = (Pair) this.possibleLeftPairs.get(i2);
            if (pair.getCount() > 1) {
                int size2 = this.possibleRightPairs.size();
                for (int i3 = 0; i3 < size2; i3++) {
                    Pair pair2 = (Pair) this.possibleRightPairs.get(i3);
                    if (pair2.getCount() > 1 && checkChecksum(pair, pair2)) {
                        return constructResult(pair, pair2);
                    }
                }
                continue;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public void reset() {
        this.possibleLeftPairs.clear();
        this.possibleRightPairs.clear();
    }
}

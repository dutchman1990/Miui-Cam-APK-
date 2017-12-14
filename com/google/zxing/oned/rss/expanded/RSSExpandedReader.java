package com.google.zxing.oned.rss.expanded;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.OneDReader;
import com.google.zxing.oned.rss.AbstractRSSReader;
import com.google.zxing.oned.rss.DataCharacter;
import com.google.zxing.oned.rss.FinderPattern;
import com.google.zxing.oned.rss.RSSUtils;
import com.google.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RSSExpandedReader extends AbstractRSSReader {
    private static final int[] EVEN_TOTAL_SUBSET = new int[]{4, 20, 52, 104, 204};
    private static final int[][] FINDER_PATTERNS;
    private static final int[][] FINDER_PATTERN_SEQUENCES;
    private static final int[] GSUM;
    private static final int[] SYMBOL_WIDEST = new int[]{7, 5, 4, 3, 1};
    private static final int[][] WEIGHTS;
    private final List<ExpandedPair> pairs = new ArrayList(11);
    private final List<ExpandedRow> rows = new ArrayList();
    private final int[] startEnd = new int[2];
    private boolean startFromEven = false;

    static {
        int[] iArr = new int[5];
        iArr[1] = 348;
        iArr[2] = 1388;
        iArr[3] = 2948;
        iArr[4] = 3988;
        GSUM = iArr;
        r0 = new int[6][];
        r0[0] = new int[]{1, 8, 4, 1};
        r0[1] = new int[]{3, 6, 4, 1};
        r0[2] = new int[]{3, 4, 6, 1};
        r0[3] = new int[]{3, 2, 8, 1};
        r0[4] = new int[]{2, 6, 5, 1};
        r0[5] = new int[]{2, 2, 9, 1};
        FINDER_PATTERNS = r0;
        r0 = new int[23][];
        r0[0] = new int[]{1, 3, 9, 27, 81, 32, 96, 77};
        r0[1] = new int[]{20, 60, 180, 118, 143, 7, 21, 63};
        r0[2] = new int[]{189, 145, 13, 39, 117, 140, 209, 205};
        r0[3] = new int[]{193, 157, 49, 147, 19, 57, 171, 91};
        r0[4] = new int[]{62, 186, 136, 197, 169, 85, 44, 132};
        r0[5] = new int[]{185, 133, 188, 142, 4, 12, 36, 108};
        r0[6] = new int[]{113, 128, 173, 97, 80, 29, 87, 50};
        r0[7] = new int[]{150, 28, 84, 41, 123, 158, 52, 156};
        r0[8] = new int[]{46, 138, 203, 187, 139, 206, 196, 166};
        r0[9] = new int[]{76, 17, 51, 153, 37, 111, 122, 155};
        r0[10] = new int[]{43, 129, 176, 106, 107, 110, 119, 146};
        r0[11] = new int[]{16, 48, 144, 10, 30, 90, 59, 177};
        r0[12] = new int[]{109, 116, 137, 200, 178, 112, 125, 164};
        r0[13] = new int[]{70, 210, 208, 202, 184, 130, 179, 115};
        r0[14] = new int[]{134, 191, 151, 31, 93, 68, 204, 190};
        r0[15] = new int[]{148, 22, 66, 198, 172, 94, 71, 2};
        r0[16] = new int[]{6, 18, 54, 162, 64, 192, 154, 40};
        r0[17] = new int[]{120, 149, 25, 75, 14, 42, 126, 167};
        r0[18] = new int[]{79, 26, 78, 23, 69, 207, 199, 175};
        r0[19] = new int[]{103, 98, 83, 38, 114, 131, 182, 124};
        r0[20] = new int[]{161, 61, 183, 127, 170, 88, 53, 159};
        r0[21] = new int[]{55, 165, 73, 8, 24, 72, 5, 15};
        r0[22] = new int[]{45, 135, 194, 160, 58, 174, 100, 89};
        WEIGHTS = r0;
        r0 = new int[10][];
        int[] iArr2 = new int[]{1, 1, iArr2};
        iArr2 = new int[]{2, 1, 3, iArr2};
        iArr2 = new int[]{4, 1, 3, 2, iArr2};
        iArr2 = new int[]{4, 1, 3, 3, 5, iArr2};
        iArr2 = new int[]{4, 1, 3, 4, 5, 5, iArr2};
        int[] iArr3 = new int[8];
        iArr3[2] = 1;
        iArr3[3] = 1;
        iArr3[4] = 2;
        iArr3[5] = 2;
        iArr3[6] = 3;
        iArr3[7] = 3;
        r0[6] = iArr3;
        iArr3 = new int[9];
        iArr3[2] = 1;
        iArr3[3] = 1;
        iArr3[4] = 2;
        iArr3[5] = 2;
        iArr3[6] = 3;
        iArr3[7] = 4;
        iArr3[8] = 4;
        r0[7] = iArr3;
        iArr3 = new int[10];
        iArr3[2] = 1;
        iArr3[3] = 1;
        iArr3[4] = 2;
        iArr3[5] = 2;
        iArr3[6] = 3;
        iArr3[7] = 4;
        iArr3[8] = 5;
        iArr3[9] = 5;
        r0[8] = iArr3;
        iArr3 = new int[11];
        iArr3[2] = 1;
        iArr3[3] = 1;
        iArr3[4] = 2;
        iArr3[5] = 3;
        iArr3[6] = 3;
        iArr3[7] = 4;
        iArr3[8] = 4;
        iArr3[9] = 5;
        iArr3[10] = 5;
        r0[9] = iArr3;
        FINDER_PATTERN_SEQUENCES = r0;
    }

    private void adjustOddEvenCounts(int i) throws NotFoundException {
        int count = AbstractRSSReader.count(getOddCounts());
        int count2 = AbstractRSSReader.count(getEvenCounts());
        int i2 = (count + count2) - i;
        if ((count & 1) != 1) {
            Object obj = null;
        } else {
            int i3 = 1;
        }
        if ((count2 & 1) != 0) {
            Object obj2 = null;
        } else {
            int i4 = 1;
        }
        Object obj3 = null;
        Object obj4 = null;
        if (count > 13) {
            obj4 = 1;
        } else if (count < 4) {
            obj3 = 1;
        }
        Object obj5 = null;
        Object obj6 = null;
        if (count2 > 13) {
            obj6 = 1;
        } else if (count2 < 4) {
            obj5 = 1;
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

    private boolean checkChecksum() {
        ExpandedPair expandedPair = (ExpandedPair) this.pairs.get(0);
        DataCharacter leftChar = expandedPair.getLeftChar();
        DataCharacter rightChar = expandedPair.getRightChar();
        if (rightChar == null) {
            return false;
        }
        int checksumPortion = rightChar.getChecksumPortion();
        int i = 2;
        for (int i2 = 1; i2 < this.pairs.size(); i2++) {
            ExpandedPair expandedPair2 = (ExpandedPair) this.pairs.get(i2);
            checksumPortion += expandedPair2.getLeftChar().getChecksumPortion();
            i++;
            DataCharacter rightChar2 = expandedPair2.getRightChar();
            if (rightChar2 != null) {
                checksumPortion += rightChar2.getChecksumPortion();
                i++;
            }
        }
        return ((i + -4) * 211) + (checksumPortion % 211) == leftChar.getValue();
    }

    private List<ExpandedPair> checkRows(List<ExpandedRow> list, int i) throws NotFoundException {
        int i2 = i;
        while (i2 < this.rows.size()) {
            ExpandedRow expandedRow = (ExpandedRow) this.rows.get(i2);
            this.pairs.clear();
            int size = list.size();
            for (int i3 = 0; i3 < size; i3++) {
                this.pairs.addAll(((ExpandedRow) list.get(i3)).getPairs());
            }
            this.pairs.addAll(expandedRow.getPairs());
            if (!isValidSequence(this.pairs)) {
                i2++;
            } else if (checkChecksum()) {
                return this.pairs;
            } else {
                List arrayList = new ArrayList();
                arrayList.addAll(list);
                arrayList.add(expandedRow);
                try {
                    return checkRows(arrayList, i2 + 1);
                } catch (NotFoundException e) {
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private List<ExpandedPair> checkRows(boolean z) {
        if (this.rows.size() <= 25) {
            this.pairs.clear();
            if (z) {
                Collections.reverse(this.rows);
            }
            List<ExpandedPair> list = null;
            try {
                list = checkRows(new ArrayList(), 0);
            } catch (NotFoundException e) {
            }
            if (z) {
                Collections.reverse(this.rows);
            }
            return list;
        }
        this.rows.clear();
        return null;
    }

    static Result constructResult(List<ExpandedPair> list) throws NotFoundException, FormatException {
        String parseInformation = AbstractExpandedDecoder.createDecoder(BitArrayBuilder.buildBitArray(list)).parseInformation();
        ResultPoint[] resultPoints = ((ExpandedPair) list.get(0)).getFinderPattern().getResultPoints();
        ResultPoint[] resultPoints2 = ((ExpandedPair) list.get(list.size() - 1)).getFinderPattern().getResultPoints();
        return new Result(parseInformation, null, new ResultPoint[]{resultPoints[0], resultPoints[1], resultPoints2[0], resultPoints2[1]}, BarcodeFormat.RSS_EXPANDED);
    }

    private void findNextPair(BitArray bitArray, List<ExpandedPair> list, int i) throws NotFoundException {
        int[] decodeFinderCounters = getDecodeFinderCounters();
        decodeFinderCounters[0] = 0;
        decodeFinderCounters[1] = 0;
        decodeFinderCounters[2] = 0;
        decodeFinderCounters[3] = 0;
        int size = bitArray.getSize();
        int i2 = i < 0 ? !list.isEmpty() ? ((ExpandedPair) list.get(list.size() - 1)).getFinderPattern().getStartEnd()[1] : 0 : i;
        Object obj = list.size() % 2 == 0 ? null : 1;
        if (this.startFromEven) {
            obj = obj == null ? 1 : null;
        }
        int i3 = 0;
        while (i2 < size) {
            i3 = !bitArray.get(i2) ? 1 : 0;
            if (i3 == 0) {
                break;
            }
            i2++;
        }
        int i4 = 0;
        int i5 = i2;
        for (int i6 = i2; i6 < size; i6++) {
            if ((bitArray.get(i6) ^ i3) == 0) {
                if (i4 != 3) {
                    i4++;
                } else {
                    if (obj != null) {
                        reverseCounters(decodeFinderCounters);
                    }
                    if (AbstractRSSReader.isFinderPattern(decodeFinderCounters)) {
                        this.startEnd[0] = i5;
                        this.startEnd[1] = i6;
                        return;
                    }
                    if (obj != null) {
                        reverseCounters(decodeFinderCounters);
                    }
                    i5 += decodeFinderCounters[0] + decodeFinderCounters[1];
                    decodeFinderCounters[0] = decodeFinderCounters[2];
                    decodeFinderCounters[1] = decodeFinderCounters[3];
                    decodeFinderCounters[2] = 0;
                    decodeFinderCounters[3] = 0;
                    i4--;
                }
                decodeFinderCounters[i4] = 1;
                i3 = i3 == 0 ? 1 : 0;
            } else {
                decodeFinderCounters[i4] = decodeFinderCounters[i4] + 1;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int getNextSecondBar(BitArray bitArray, int i) {
        return !bitArray.get(i) ? bitArray.getNextUnset(bitArray.getNextSet(i)) : bitArray.getNextSet(bitArray.getNextUnset(i));
    }

    private static boolean isNotA1left(FinderPattern finderPattern, boolean z, boolean z2) {
        if (finderPattern.getValue() == 0 && z) {
            if (z2) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPartialRow(Iterable<ExpandedPair> iterable, Iterable<ExpandedRow> iterable2) {
        for (ExpandedRow expandedRow : iterable2) {
            Object obj = 1;
            for (ExpandedPair expandedPair : iterable) {
                Object obj2 = null;
                for (ExpandedPair equals : expandedRow.getPairs()) {
                    if (expandedPair.equals(equals)) {
                        obj2 = 1;
                        continue;
                        break;
                    }
                }
                if (obj2 == null) {
                    obj = null;
                    continue;
                    break;
                }
            }
            if (obj != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidSequence(List<ExpandedPair> list) {
        for (int[] iArr : FINDER_PATTERN_SEQUENCES) {
            if (list.size() <= iArr.length) {
                Object obj = 1;
                for (int i = 0; i < list.size(); i++) {
                    if (((ExpandedPair) list.get(i)).getFinderPattern().getValue() != iArr[i]) {
                        obj = null;
                        break;
                    }
                }
                if (obj != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private FinderPattern parseFoundFinderPattern(BitArray bitArray, int i, boolean z) {
        int i2;
        int i3;
        int i4;
        if (z) {
            int i5 = this.startEnd[0] - 1;
            while (i5 >= 0 && !bitArray.get(i5)) {
                i5--;
            }
            i5++;
            i2 = this.startEnd[0] - i5;
            i3 = i5;
            i4 = this.startEnd[1];
        } else {
            i3 = this.startEnd[0];
            i4 = bitArray.getNextUnset(this.startEnd[1] + 1);
            i2 = i4 - this.startEnd[1];
        }
        Object decodeFinderCounters = getDecodeFinderCounters();
        System.arraycopy(decodeFinderCounters, 0, decodeFinderCounters, 1, decodeFinderCounters.length - 1);
        decodeFinderCounters[0] = i2;
        try {
            return new FinderPattern(AbstractRSSReader.parseFinderValue(decodeFinderCounters, FINDER_PATTERNS), new int[]{i3, i4}, i3, i4, i);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private static void removePartialRows(List<ExpandedPair> list, List<ExpandedRow> list2) {
        Iterator it = list2.iterator();
        while (it.hasNext()) {
            ExpandedRow expandedRow = (ExpandedRow) it.next();
            if (expandedRow.getPairs().size() != list.size()) {
                Object obj = 1;
                for (ExpandedPair expandedPair : expandedRow.getPairs()) {
                    Object obj2 = null;
                    for (ExpandedPair equals : list) {
                        if (expandedPair.equals(equals)) {
                            obj2 = 1;
                            continue;
                            break;
                        }
                    }
                    if (obj2 == null) {
                        obj = null;
                        break;
                    }
                }
                if (obj != null) {
                    it.remove();
                }
            }
        }
    }

    private static void reverseCounters(int[] iArr) {
        int length = iArr.length;
        for (int i = 0; i < length / 2; i++) {
            int i2 = iArr[i];
            iArr[i] = iArr[(length - i) - 1];
            iArr[(length - i) - 1] = i2;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void storeRow(int r8, boolean r9) {
        /*
        r7 = this;
        r1 = 0;
        r3 = 0;
        r2 = 0;
    L_0x0003:
        r4 = r7.rows;
        r4 = r4.size();
        if (r1 >= r4) goto L_0x0028;
    L_0x000b:
        r4 = r7.rows;
        r0 = r4.get(r1);
        r0 = (com.google.zxing.oned.rss.expanded.ExpandedRow) r0;
        r4 = r0.getRowNumber();
        if (r4 > r8) goto L_0x0022;
    L_0x0019:
        r4 = r7.pairs;
        r3 = r0.isEquivalent(r4);
        r1 = r1 + 1;
        goto L_0x0003;
    L_0x0022:
        r4 = r7.pairs;
        r2 = r0.isEquivalent(r4);
    L_0x0028:
        if (r2 == 0) goto L_0x002b;
    L_0x002a:
        return;
    L_0x002b:
        if (r3 != 0) goto L_0x002a;
    L_0x002d:
        r4 = r7.pairs;
        r5 = r7.rows;
        r4 = isPartialRow(r4, r5);
        if (r4 != 0) goto L_0x004b;
    L_0x0037:
        r4 = r7.rows;
        r5 = new com.google.zxing.oned.rss.expanded.ExpandedRow;
        r6 = r7.pairs;
        r5.<init>(r6, r8, r9);
        r4.add(r1, r5);
        r4 = r7.pairs;
        r5 = r7.rows;
        removePartialRows(r4, r5);
        return;
    L_0x004b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.oned.rss.expanded.RSSExpandedReader.storeRow(int, boolean):void");
    }

    DataCharacter decodeDataCharacter(BitArray bitArray, FinderPattern finderPattern, boolean z, boolean z2) throws NotFoundException {
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
        if (z2) {
            OneDReader.recordPatternInReverse(bitArray, finderPattern.getStartEnd()[0], dataCharacterCounters);
        } else {
            OneDReader.recordPattern(bitArray, finderPattern.getStartEnd()[1], dataCharacterCounters);
            i = 0;
            for (int length = dataCharacterCounters.length - 1; i < length; length--) {
                int i2 = dataCharacterCounters[i];
                dataCharacterCounters[i] = dataCharacterCounters[length];
                dataCharacterCounters[length] = i2;
                i++;
            }
        }
        float count = ((float) AbstractRSSReader.count(dataCharacterCounters)) / 17.0f;
        float f = ((float) (finderPattern.getStartEnd()[1] - finderPattern.getStartEnd()[0])) / 15.0f;
        if (Math.abs(count - f) / f > 0.3f) {
            throw NotFoundException.getNotFoundInstance();
        }
        int[] oddCounts = getOddCounts();
        int[] evenCounts = getEvenCounts();
        float[] oddRoundingErrors = getOddRoundingErrors();
        float[] evenRoundingErrors = getEvenRoundingErrors();
        for (i = 0; i < dataCharacterCounters.length; i++) {
            float f2 = (((float) dataCharacterCounters[i]) * 1.0f) / count;
            int i3 = (int) (0.5f + f2);
            if (i3 >= 1) {
                if (i3 > 8) {
                    if (f2 > 8.7f) {
                        throw NotFoundException.getNotFoundInstance();
                    }
                    i3 = 8;
                }
            } else if (f2 < 0.3f) {
                throw NotFoundException.getNotFoundInstance();
            } else {
                i3 = 1;
            }
            int i4 = i / 2;
            if ((i & 1) != 0) {
                evenCounts[i4] = i3;
                evenRoundingErrors[i4] = f2 - ((float) i3);
            } else {
                oddCounts[i4] = i3;
                oddRoundingErrors[i4] = f2 - ((float) i3);
            }
        }
        adjustOddEvenCounts(17);
        int value = ((!z2 ? 1 : 0) + ((finderPattern.getValue() * 4) + (!z ? 2 : 0))) - 1;
        int i5 = 0;
        int i6 = 0;
        for (i = oddCounts.length - 1; i >= 0; i--) {
            if (isNotA1left(finderPattern, z, z2)) {
                i6 += oddCounts[i] * WEIGHTS[value][i * 2];
            }
            i5 += oddCounts[i];
        }
        int i7 = 0;
        for (i = evenCounts.length - 1; i >= 0; i--) {
            if (isNotA1left(finderPattern, z, z2)) {
                i7 += evenCounts[i] * WEIGHTS[value][(i * 2) + 1];
            }
        }
        int i8 = i6 + i7;
        if ((i5 & 1) == 0 && i5 <= 13 && i5 >= 4) {
            int i9 = (13 - i5) / 2;
            int i10 = SYMBOL_WIDEST[i9];
            int i11 = 9 - i10;
            return new DataCharacter(((RSSUtils.getRSSvalue(oddCounts, i10, true) * EVEN_TOTAL_SUBSET[i9]) + RSSUtils.getRSSvalue(evenCounts, i11, false)) + GSUM[i9], i8);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        this.pairs.clear();
        this.startFromEven = false;
        try {
            return constructResult(decodeRow2pairs(i, bitArray));
        } catch (NotFoundException e) {
            this.pairs.clear();
            this.startFromEven = true;
            return constructResult(decodeRow2pairs(i, bitArray));
        }
    }

    List<ExpandedPair> decodeRow2pairs(int i, BitArray bitArray) throws NotFoundException {
        while (true) {
            try {
                this.pairs.add(retrieveNextPair(bitArray, this.pairs, i));
            } catch (NotFoundException e) {
                if (this.pairs.isEmpty()) {
                    throw e;
                } else if (checkChecksum()) {
                    return this.pairs;
                } else {
                    boolean z = !this.rows.isEmpty();
                    storeRow(i, false);
                    if (z) {
                        List<ExpandedPair> checkRows = checkRows(false);
                        if (checkRows != null) {
                            return checkRows;
                        }
                        checkRows = checkRows(true);
                        if (checkRows != null) {
                            return checkRows;
                        }
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
            }
        }
    }

    public void reset() {
        this.pairs.clear();
        this.rows.clear();
    }

    ExpandedPair retrieveNextPair(BitArray bitArray, List<ExpandedPair> list, int i) throws NotFoundException {
        FinderPattern parseFoundFinderPattern;
        boolean z = list.size() % 2 == 0;
        if (this.startFromEven) {
            z = !z;
        }
        Object obj = 1;
        int i2 = -1;
        do {
            findNextPair(bitArray, list, i2);
            parseFoundFinderPattern = parseFoundFinderPattern(bitArray, i, z);
            if (parseFoundFinderPattern != null) {
                obj = null;
                continue;
            } else {
                i2 = getNextSecondBar(bitArray, this.startEnd[0]);
                continue;
            }
        } while (obj != null);
        DataCharacter decodeDataCharacter = decodeDataCharacter(bitArray, parseFoundFinderPattern, z, true);
        if (!list.isEmpty() && ((ExpandedPair) list.get(list.size() - 1)).mustBeLast()) {
            throw NotFoundException.getNotFoundInstance();
        }
        DataCharacter decodeDataCharacter2;
        try {
            decodeDataCharacter2 = decodeDataCharacter(bitArray, parseFoundFinderPattern, z, false);
        } catch (NotFoundException e) {
            decodeDataCharacter2 = null;
        }
        return new ExpandedPair(decodeDataCharacter, decodeDataCharacter2, parseFoundFinderPattern, true);
    }
}

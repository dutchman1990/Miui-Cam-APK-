package com.google.zxing.oned.rss;

public final class RSSUtils {
    private RSSUtils() {
    }

    private static int combins(int i, int i2) {
        int i3;
        int i4;
        if (i - i2 <= i2) {
            i3 = i - i2;
            i4 = i2;
        } else {
            i3 = i2;
            i4 = i - i2;
        }
        int i5 = 1;
        int i6 = 1;
        for (int i7 = i; i7 > i4; i7--) {
            i5 *= i7;
            if (i6 <= i3) {
                i5 /= i6;
                i6++;
            }
        }
        while (i6 <= i3) {
            i5 /= i6;
            i6++;
        }
        return i5;
    }

    public static int getRSSvalue(int[] iArr, int i, boolean z) {
        int length = iArr.length;
        int i2 = 0;
        for (int i3 : iArr) {
            i2 += i3;
        }
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        while (i6 < length - 1) {
            int i7 = 1;
            i5 |= 1 << i6;
            while (i7 < iArr[i6]) {
                int combins = combins((i2 - i7) - 1, (length - i6) - 2);
                if (z && i5 == 0 && (i2 - i7) - ((length - i6) - 1) >= (length - i6) - 1) {
                    combins -= combins((i2 - i7) - (length - i6), (length - i6) - 2);
                }
                if ((length - i6) - 1 > 1) {
                    int i8 = 0;
                    for (int i9 = (i2 - i7) - ((length - i6) - 2); i9 > i; i9--) {
                        i8 += combins(((i2 - i7) - i9) - 1, (length - i6) - 3);
                    }
                    combins -= ((length - 1) - i6) * i8;
                } else if (i2 - i7 > i) {
                    combins--;
                }
                i4 += combins;
                i7++;
                i5 &= (1 << i6) ^ -1;
            }
            i2 -= i7;
            i6++;
        }
        return i4;
    }
}

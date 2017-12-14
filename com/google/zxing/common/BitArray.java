package com.google.zxing.common;

import java.util.Arrays;

public final class BitArray implements Cloneable {
    private int[] bits;
    private int size;

    public BitArray() {
        this.size = 0;
        this.bits = new int[1];
    }

    public BitArray(int i) {
        this.size = i;
        this.bits = makeArray(i);
    }

    BitArray(int[] iArr, int i) {
        this.bits = iArr;
        this.size = i;
    }

    private static int[] makeArray(int i) {
        return new int[((i + 31) / 32)];
    }

    public void clear() {
        int length = this.bits.length;
        for (int i = 0; i < length; i++) {
            this.bits[i] = 0;
        }
    }

    public BitArray clone() {
        return new BitArray((int[]) this.bits.clone(), this.size);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BitArray)) {
            return false;
        }
        BitArray bitArray = (BitArray) obj;
        return this.size == bitArray.size && Arrays.equals(this.bits, bitArray.bits);
    }

    public boolean get(int i) {
        return (this.bits[i / 32] & (1 << (i & 31))) != 0;
    }

    public int[] getBitArray() {
        return this.bits;
    }

    public int getNextSet(int i) {
        if (i >= this.size) {
            return this.size;
        }
        int i2 = i / 32;
        int i3 = this.bits[i2] & (((1 << (i & 31)) - 1) ^ -1);
        while (i3 == 0) {
            i2++;
            if (i2 == this.bits.length) {
                return this.size;
            }
            i3 = this.bits[i2];
        }
        int numberOfTrailingZeros = (i2 * 32) + Integer.numberOfTrailingZeros(i3);
        if (numberOfTrailingZeros > this.size) {
            numberOfTrailingZeros = this.size;
        }
        return numberOfTrailingZeros;
    }

    public int getNextUnset(int i) {
        if (i >= this.size) {
            return this.size;
        }
        int i2 = i / 32;
        int i3 = (this.bits[i2] ^ -1) & (((1 << (i & 31)) - 1) ^ -1);
        while (i3 == 0) {
            i2++;
            if (i2 == this.bits.length) {
                return this.size;
            }
            i3 = this.bits[i2] ^ -1;
        }
        int numberOfTrailingZeros = (i2 * 32) + Integer.numberOfTrailingZeros(i3);
        if (numberOfTrailingZeros > this.size) {
            numberOfTrailingZeros = this.size;
        }
        return numberOfTrailingZeros;
    }

    public int getSize() {
        return this.size;
    }

    public int hashCode() {
        return (this.size * 31) + Arrays.hashCode(this.bits);
    }

    public boolean isRange(int i, int i2, boolean z) {
        if (i2 < i) {
            throw new IllegalArgumentException();
        } else if (i2 == i) {
            return true;
        } else {
            i2--;
            int i3 = i / 32;
            int i4 = i2 / 32;
            int i5 = i3;
            while (i5 <= i4) {
                int i6;
                int i7 = i5 <= i3 ? i & 31 : 0;
                int i8 = i5 >= i4 ? i2 & 31 : 31;
                if (i7 == 0 && i8 == 31) {
                    i6 = -1;
                } else {
                    i6 = 0;
                    for (int i9 = i7; i9 <= i8; i9++) {
                        i6 |= 1 << i9;
                    }
                }
                int i10 = this.bits[i5] & i6;
                if (!z) {
                    i6 = 0;
                }
                if (i10 != i6) {
                    return false;
                }
                i5++;
            }
            return true;
        }
    }

    public void reverse() {
        int i;
        int[] iArr = new int[this.bits.length];
        int i2 = (this.size - 1) / 32;
        int i3 = i2 + 1;
        for (i = 0; i < i3; i++) {
            long j = (long) this.bits[i];
            j = ((j >> 1) & 1431655765) | ((1431655765 & j) << 1);
            j = ((j >> 2) & 858993459) | ((858993459 & j) << 2);
            j = ((j >> 4) & 252645135) | ((252645135 & j) << 4);
            j = ((j >> 8) & 16711935) | ((16711935 & j) << 8);
            iArr[i2 - i] = (int) (((j >> 16) & 65535) | ((65535 & j) << 16));
        }
        if (this.size != i3 * 32) {
            int i4 = (i3 * 32) - this.size;
            int i5 = 1;
            for (i = 0; i < 31 - i4; i++) {
                i5 = (i5 << 1) | 1;
            }
            int i6 = (iArr[0] >> i4) & i5;
            for (i = 1; i < i3; i++) {
                int i7 = iArr[i];
                iArr[i - 1] = i6 | (i7 << (32 - i4));
                i6 = (i7 >> i4) & i5;
            }
            iArr[i3 - 1] = i6;
        }
        this.bits = iArr;
    }

    public void set(int i) {
        int[] iArr = this.bits;
        int i2 = i / 32;
        iArr[i2] = iArr[i2] | (1 << (i & 31));
    }

    public void setBulk(int i, int i2) {
        this.bits[i / 32] = i2;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.size);
        for (int i = 0; i < this.size; i++) {
            if ((i & 7) == 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(!get(i) ? '.' : 'X');
        }
        return stringBuilder.toString();
    }
}

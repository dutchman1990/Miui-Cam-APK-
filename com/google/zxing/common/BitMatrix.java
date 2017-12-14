package com.google.zxing.common;

import java.util.Arrays;

public final class BitMatrix implements Cloneable {
    private final int[] bits;
    private final int height;
    private final int rowSize;
    private final int width;

    public BitMatrix(int i) {
        this(i, i);
    }

    public BitMatrix(int i, int i2) {
        if (i >= 1 && i2 >= 1) {
            this.width = i;
            this.height = i2;
            this.rowSize = (i + 31) / 32;
            this.bits = new int[(this.rowSize * i2)];
            return;
        }
        throw new IllegalArgumentException("Both dimensions must be greater than 0");
    }

    private BitMatrix(int i, int i2, int i3, int[] iArr) {
        this.width = i;
        this.height = i2;
        this.rowSize = i3;
        this.bits = iArr;
    }

    public BitMatrix clone() {
        return new BitMatrix(this.width, this.height, this.rowSize, (int[]) this.bits.clone());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BitMatrix)) {
            return false;
        }
        BitMatrix bitMatrix = (BitMatrix) obj;
        return this.width == bitMatrix.width && this.height == bitMatrix.height && this.rowSize == bitMatrix.rowSize && Arrays.equals(this.bits, bitMatrix.bits);
    }

    public void flip(int i, int i2) {
        int i3 = (this.rowSize * i2) + (i / 32);
        int[] iArr = this.bits;
        iArr[i3] = iArr[i3] ^ (1 << (i & 31));
    }

    public boolean get(int i, int i2) {
        return ((this.bits[(this.rowSize * i2) + (i / 32)] >>> (i & 31)) & 1) != 0;
    }

    public int[] getBottomRightOnBit() {
        int length = this.bits.length - 1;
        while (length >= 0 && this.bits[length] == 0) {
            length--;
        }
        if (length < 0) {
            return null;
        }
        int i;
        int i2 = length / this.rowSize;
        int i3 = (length % this.rowSize) * 32;
        for (i = 31; (this.bits[length] >>> i) == 0; i--) {
        }
        return new int[]{i3 + i, i2};
    }

    public int[] getEnclosingRectangle() {
        int i = this.width;
        int i2 = this.height;
        int i3 = -1;
        int i4 = -1;
        for (int i5 = 0; i5 < this.height; i5++) {
            for (int i6 = 0; i6 < this.rowSize; i6++) {
                int i7 = this.bits[(this.rowSize * i5) + i6];
                if (i7 != 0) {
                    int i8;
                    if (i5 < i2) {
                        i2 = i5;
                    }
                    if (i5 > i4) {
                        i4 = i5;
                    }
                    if (i6 * 32 < i) {
                        i8 = 0;
                        while ((i7 << (31 - i8)) == 0) {
                            i8++;
                        }
                        if ((i6 * 32) + i8 < i) {
                            i = (i6 * 32) + i8;
                        }
                    }
                    if ((i6 * 32) + 31 > i3) {
                        i8 = 31;
                        while ((i7 >>> i8) == 0) {
                            i8--;
                        }
                        if ((i6 * 32) + i8 > i3) {
                            i3 = (i6 * 32) + i8;
                        }
                    }
                }
            }
        }
        int i9 = i4 - i2;
        if (i3 - i < 0 || i9 < 0) {
            return null;
        }
        return new int[]{i, i2, r7, i9};
    }

    public int getHeight() {
        return this.height;
    }

    public BitArray getRow(int i, BitArray bitArray) {
        if (bitArray != null && bitArray.getSize() >= this.width) {
            bitArray.clear();
        } else {
            bitArray = new BitArray(this.width);
        }
        int i2 = i * this.rowSize;
        for (int i3 = 0; i3 < this.rowSize; i3++) {
            bitArray.setBulk(i3 * 32, this.bits[i2 + i3]);
        }
        return bitArray;
    }

    public int[] getTopLeftOnBit() {
        int i = 0;
        while (i < this.bits.length && this.bits[i] == 0) {
            i++;
        }
        if (i == this.bits.length) {
            return null;
        }
        int i2;
        int i3 = i / this.rowSize;
        int i4 = (i % this.rowSize) * 32;
        for (i2 = 0; (this.bits[i] << (31 - i2)) == 0; i2++) {
        }
        return new int[]{i4 + i2, i3};
    }

    public int getWidth() {
        return this.width;
    }

    public int hashCode() {
        return (((((((this.width * 31) + this.width) * 31) + this.height) * 31) + this.rowSize) * 31) + Arrays.hashCode(this.bits);
    }

    public void rotate180() {
        int width = getWidth();
        int height = getHeight();
        BitArray bitArray = new BitArray(width);
        BitArray bitArray2 = new BitArray(width);
        for (int i = 0; i < (height + 1) / 2; i++) {
            bitArray = getRow(i, bitArray);
            bitArray2 = getRow((height - 1) - i, bitArray2);
            bitArray.reverse();
            bitArray2.reverse();
            setRow(i, bitArray2);
            setRow((height - 1) - i, bitArray);
        }
    }

    public void set(int i, int i2) {
        int i3 = (this.rowSize * i2) + (i / 32);
        int[] iArr = this.bits;
        iArr[i3] = iArr[i3] | (1 << (i & 31));
    }

    public void setRegion(int i, int i2, int i3, int i4) {
        if (i2 < 0 || i < 0) {
            throw new IllegalArgumentException("Left and top must be nonnegative");
        } else if (i4 >= 1 && i3 >= 1) {
            int i5 = i + i3;
            int i6 = i2 + i4;
            if (i6 <= this.height && i5 <= this.width) {
                for (int i7 = i2; i7 < i6; i7++) {
                    int i8 = i7 * this.rowSize;
                    for (int i9 = i; i9 < i5; i9++) {
                        int[] iArr = this.bits;
                        int i10 = (i9 / 32) + i8;
                        iArr[i10] = iArr[i10] | (1 << (i9 & 31));
                    }
                }
                return;
            }
            throw new IllegalArgumentException("The region must fit inside the matrix");
        } else {
            throw new IllegalArgumentException("Height and width must be at least 1");
        }
    }

    public void setRow(int i, BitArray bitArray) {
        System.arraycopy(bitArray.getBitArray(), 0, this.bits, this.rowSize * i, this.rowSize);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.height * (this.width + 1));
        for (int i = 0; i < this.height; i++) {
            for (int i2 = 0; i2 < this.width; i2++) {
                stringBuilder.append(!get(i2, i) ? "  " : "X ");
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}

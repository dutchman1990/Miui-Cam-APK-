package com.android.gallery3d.exif;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class ExifTag {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    private static final int[] TYPE_TO_SIZE_MAP = new int[11];
    private static Charset US_ASCII = Charset.forName("US-ASCII");
    private int mComponentCountActual;
    private final short mDataType;
    private boolean mHasDefinedDefaultComponentCount;
    private int mIfd;
    private int mOffset;
    private final short mTagId;
    private Object mValue = null;

    static {
        TYPE_TO_SIZE_MAP[1] = 1;
        TYPE_TO_SIZE_MAP[2] = 1;
        TYPE_TO_SIZE_MAP[3] = 2;
        TYPE_TO_SIZE_MAP[4] = 4;
        TYPE_TO_SIZE_MAP[5] = 8;
        TYPE_TO_SIZE_MAP[7] = 1;
        TYPE_TO_SIZE_MAP[9] = 4;
        TYPE_TO_SIZE_MAP[10] = 8;
    }

    ExifTag(short s, short s2, int i, int i2, boolean z) {
        this.mTagId = s;
        this.mDataType = s2;
        this.mComponentCountActual = i;
        this.mHasDefinedDefaultComponentCount = z;
        this.mIfd = i2;
    }

    private boolean checkBadComponentCount(int i) {
        return this.mHasDefinedDefaultComponentCount && this.mComponentCountActual != i;
    }

    private boolean checkOverflowForRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < -2147483648L || rational.getDenominator() < -2147483648L || rational.getNumerator() > 2147483647L || rational.getDenominator() > 2147483647L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(int[] iArr) {
        for (int i : iArr) {
            if (i < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(long[] jArr) {
        for (long j : jArr) {
            if (j < 0 || j > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedRational(Rational[] rationalArr) {
        for (Rational rational : rationalArr) {
            if (rational.getNumerator() < 0 || rational.getDenominator() < 0 || rational.getNumerator() > 4294967295L || rational.getDenominator() > 4294967295L) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedShort(int[] iArr) {
        for (int i : iArr) {
            if (i > 65535 || i < 0) {
                return true;
            }
        }
        return false;
    }

    private static String convertTypeToString(short s) {
        switch (s) {
            case (short) 1:
                return "UNSIGNED_BYTE";
            case (short) 2:
                return "ASCII";
            case (short) 3:
                return "UNSIGNED_SHORT";
            case (short) 4:
                return "UNSIGNED_LONG";
            case (short) 5:
                return "UNSIGNED_RATIONAL";
            case (short) 7:
                return "UNDEFINED";
            case (short) 9:
                return "LONG";
            case (short) 10:
                return "RATIONAL";
            default:
                return "";
        }
    }

    public static int getElementSize(short s) {
        return TYPE_TO_SIZE_MAP[s];
    }

    public static boolean isValidIfd(int i) {
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 4;
    }

    public static boolean isValidType(short s) {
        return s == (short) 1 || s == (short) 2 || s == (short) 3 || s == (short) 4 || s == (short) 5 || s == (short) 7 || s == (short) 9 || s == (short) 10;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof ExifTag)) {
            return false;
        }
        ExifTag exifTag = (ExifTag) obj;
        if (exifTag.mTagId != this.mTagId || exifTag.mComponentCountActual != this.mComponentCountActual || exifTag.mDataType != this.mDataType) {
            return false;
        }
        if (this.mValue != null) {
            return exifTag.mValue == null ? false : this.mValue instanceof long[] ? !(exifTag.mValue instanceof long[]) ? false : Arrays.equals((long[]) this.mValue, (long[]) exifTag.mValue) : this.mValue instanceof Rational[] ? !(exifTag.mValue instanceof Rational[]) ? false : Arrays.equals((Rational[]) this.mValue, (Rational[]) exifTag.mValue) : this.mValue instanceof byte[] ? !(exifTag.mValue instanceof byte[]) ? false : Arrays.equals((byte[]) this.mValue, (byte[]) exifTag.mValue) : this.mValue.equals(exifTag.mValue);
        } else {
            if (exifTag.mValue == null) {
                z = true;
            }
            return z;
        }
    }

    public String forceGetValueAsString() {
        if (this.mValue == null) {
            return "";
        }
        if (this.mValue instanceof byte[]) {
            return this.mDataType == (short) 2 ? new String((byte[]) this.mValue, US_ASCII) : Arrays.toString((byte[]) this.mValue);
        } else {
            if (this.mValue instanceof long[]) {
                return ((long[]) this.mValue).length == 1 ? String.valueOf(((long[]) this.mValue)[0]) : Arrays.toString((long[]) this.mValue);
            } else {
                if (!(this.mValue instanceof Object[])) {
                    return this.mValue.toString();
                }
                if (((Object[]) this.mValue).length != 1) {
                    return Arrays.toString((Object[]) this.mValue);
                }
                Object obj = ((Object[]) this.mValue)[0];
                return obj == null ? "" : obj.toString();
            }
        }
    }

    protected void forceSetComponentCount(int i) {
        this.mComponentCountActual = i;
    }

    protected void getBytes(byte[] bArr) {
        getBytes(bArr, 0, bArr.length);
    }

    protected void getBytes(byte[] bArr, int i, int i2) {
        if (this.mDataType == (short) 7 || this.mDataType == (short) 1) {
            Object obj = this.mValue;
            if (i2 > this.mComponentCountActual) {
                i2 = this.mComponentCountActual;
            }
            System.arraycopy(obj, 0, bArr, i, i2);
            return;
        }
        throw new IllegalArgumentException("Cannot get BYTE value from " + convertTypeToString(this.mDataType));
    }

    public int getComponentCount() {
        return this.mComponentCountActual;
    }

    public int getDataSize() {
        return getComponentCount() * getElementSize(getDataType());
    }

    public short getDataType() {
        return this.mDataType;
    }

    public int getIfd() {
        return this.mIfd;
    }

    protected int getOffset() {
        return this.mOffset;
    }

    protected Rational getRational(int i) {
        if (this.mDataType == (short) 10 || this.mDataType == (short) 5) {
            return ((Rational[]) this.mValue)[i];
        }
        throw new IllegalArgumentException("Cannot get RATIONAL value from " + convertTypeToString(this.mDataType));
    }

    protected byte[] getStringByte() {
        return (byte[]) this.mValue;
    }

    public short getTagId() {
        return this.mTagId;
    }

    public Object getValue() {
        return this.mValue;
    }

    protected long getValueAt(int i) {
        if (this.mValue instanceof long[]) {
            return ((long[]) this.mValue)[i];
        }
        if (this.mValue instanceof byte[]) {
            return (long) ((byte[]) this.mValue)[i];
        }
        throw new IllegalArgumentException("Cannot get integer value from " + convertTypeToString(this.mDataType));
    }

    protected boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    public boolean hasValue() {
        return this.mValue != null;
    }

    protected void setHasDefinedCount(boolean z) {
        this.mHasDefinedDefaultComponentCount = z;
    }

    protected void setIfd(int i) {
        this.mIfd = i;
    }

    protected void setOffset(int i) {
        this.mOffset = i;
    }

    public boolean setValue(int i) {
        return setValue(new int[]{i});
    }

    public boolean setValue(String str) {
        if (this.mDataType != (short) 2 && this.mDataType != (short) 7) {
            return false;
        }
        Object bytes = str.getBytes(US_ASCII);
        Object obj = bytes;
        if (bytes.length > 0) {
            obj = (bytes[bytes.length + -1] == (byte) 0 || this.mDataType == (short) 7) ? bytes : Arrays.copyOf(bytes, bytes.length + 1);
            if (!(bytes[bytes.length - 1] == (byte) 0 || this.mDataType == (short) 7)) {
                this.mComponentCountActual++;
            }
        } else if (this.mDataType == (short) 2 && this.mComponentCountActual == 1) {
            obj = new byte[]{null};
        }
        int length = obj.length;
        if (checkBadComponentCount(length)) {
            return false;
        }
        this.mComponentCountActual = length;
        this.mValue = obj;
        return true;
    }

    public boolean setValue(byte[] bArr) {
        return setValue(bArr, 0, bArr.length);
    }

    public boolean setValue(byte[] bArr, int i, int i2) {
        if (checkBadComponentCount(i2)) {
            return false;
        }
        if (this.mDataType != (short) 1 && this.mDataType != (short) 7) {
            return false;
        }
        this.mValue = new byte[i2];
        System.arraycopy(bArr, i, this.mValue, 0, i2);
        this.mComponentCountActual = i2;
        return true;
    }

    public boolean setValue(int[] iArr) {
        if (checkBadComponentCount(iArr.length)) {
            return false;
        }
        if (this.mDataType != (short) 3 && this.mDataType != (short) 9 && this.mDataType != (short) 4) {
            return false;
        }
        if (this.mDataType == (short) 3 && checkOverflowForUnsignedShort(iArr)) {
            return false;
        }
        if (this.mDataType == (short) 4 && checkOverflowForUnsignedLong(iArr)) {
            return false;
        }
        Object obj = new long[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            obj[i] = (long) iArr[i];
        }
        this.mValue = obj;
        this.mComponentCountActual = iArr.length;
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setValue(long[] r4) {
        /*
        r3 = this;
        r2 = 0;
        r0 = r4.length;
        r0 = r3.checkBadComponentCount(r0);
        if (r0 != 0) goto L_0x000d;
    L_0x0008:
        r0 = r3.mDataType;
        r1 = 4;
        if (r0 == r1) goto L_0x000e;
    L_0x000d:
        return r2;
    L_0x000e:
        r0 = r3.checkOverflowForUnsignedLong(r4);
        if (r0 == 0) goto L_0x0015;
    L_0x0014:
        return r2;
    L_0x0015:
        r3.mValue = r4;
        r0 = r4.length;
        r3.mComponentCountActual = r0;
        r0 = 1;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.exif.ExifTag.setValue(long[]):boolean");
    }

    public boolean setValue(Rational[] rationalArr) {
        if (checkBadComponentCount(rationalArr.length)) {
            return false;
        }
        if (this.mDataType != (short) 5 && this.mDataType != (short) 10) {
            return false;
        }
        if (this.mDataType == (short) 5 && checkOverflowForUnsignedRational(rationalArr)) {
            return false;
        }
        if (this.mDataType == (short) 10 && checkOverflowForRational(rationalArr)) {
            return false;
        }
        this.mValue = rationalArr;
        this.mComponentCountActual = rationalArr.length;
        return true;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "tag id: %04X\n", new Object[]{Short.valueOf(this.mTagId)}) + "ifd id: " + this.mIfd + "\ntype: " + convertTypeToString(this.mDataType) + "\ncount: " + this.mComponentCountActual + "\noffset: " + this.mOffset + "\nvalue: " + forceGetValueAsString() + "\n";
    }
}

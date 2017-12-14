package com.android.gallery3d.exif;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

class CountedDataInputStream extends FilterInputStream {
    static final /* synthetic */ boolean -assertionsDisabled = (!CountedDataInputStream.class.desiredAssertionStatus());
    private final byte[] mByteArray = new byte[8];
    private final ByteBuffer mByteBuffer = ByteBuffer.wrap(this.mByteArray);
    private int mCount = 0;

    protected CountedDataInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public ByteOrder getByteOrder() {
        return this.mByteBuffer.order();
    }

    public int getReadByteCount() {
        return this.mCount;
    }

    public int read() throws IOException {
        int i = 0;
        int read = this.in.read();
        int i2 = this.mCount;
        if (read >= 0) {
            i = 1;
        }
        this.mCount = i + i2;
        return read;
    }

    public int read(byte[] bArr) throws IOException {
        int i = 0;
        int read = this.in.read(bArr);
        int i2 = this.mCount;
        if (read >= 0) {
            i = read;
        }
        this.mCount = i + i2;
        return read;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        int read = this.in.read(bArr, i, i2);
        int i4 = this.mCount;
        if (read >= 0) {
            i3 = read;
        }
        this.mCount = i3 + i4;
        return read;
    }

    public int readInt() throws IOException {
        readOrThrow(this.mByteArray, 0, 4);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getInt();
    }

    public void readOrThrow(byte[] bArr) throws IOException {
        readOrThrow(bArr, 0, bArr.length);
    }

    public void readOrThrow(byte[] bArr, int i, int i2) throws IOException {
        if (read(bArr, i, i2) != i2) {
            throw new EOFException();
        }
    }

    public short readShort() throws IOException {
        readOrThrow(this.mByteArray, 0, 2);
        this.mByteBuffer.rewind();
        return this.mByteBuffer.getShort();
    }

    public String readString(int i, Charset charset) throws IOException {
        byte[] bArr = new byte[i];
        readOrThrow(bArr);
        return new String(bArr, charset);
    }

    public long readUnsignedInt() throws IOException {
        return ((long) readInt()) & 4294967295L;
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 65535;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.mByteBuffer.order(byteOrder);
    }

    public long skip(long j) throws IOException {
        long skip = this.in.skip(j);
        this.mCount = (int) (((long) this.mCount) + skip);
        return skip;
    }

    public void skipOrThrow(long j) throws IOException {
        if (skip(j) != j) {
            throw new EOFException();
        }
    }

    public void skipTo(long j) throws IOException {
        long j2 = j - ((long) this.mCount);
        if (!-assertionsDisabled) {
            if ((j2 >= 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        skipOrThrow(j2);
    }
}

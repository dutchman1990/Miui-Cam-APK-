package com.android.gallery3d.exif;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class ExifOutputStream extends FilterOutputStream {
    private ByteBuffer mBuffer = ByteBuffer.allocate(4);
    private int mByteToCopy;
    private int mByteToSkip;
    private ExifData mExifData;
    private final ExifInterface mInterface;
    private byte[] mSingleByteArray = new byte[1];
    private int mState = 0;

    protected ExifOutputStream(OutputStream outputStream, ExifInterface exifInterface) {
        super(new BufferedOutputStream(outputStream, 65536));
        this.mInterface = exifInterface;
    }

    private int calculateAllOffset() {
        IfdData ifdData = this.mExifData.getIfdData(0);
        int calculateOffsetOfIfd = calculateOffsetOfIfd(ifdData, 8);
        ifdData.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD)).setValue(calculateOffsetOfIfd);
        IfdData ifdData2 = this.mExifData.getIfdData(2);
        calculateOffsetOfIfd = calculateOffsetOfIfd(ifdData2, calculateOffsetOfIfd);
        IfdData ifdData3 = this.mExifData.getIfdData(3);
        if (ifdData3 != null) {
            ifdData2.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD)).setValue(calculateOffsetOfIfd);
            calculateOffsetOfIfd = calculateOffsetOfIfd(ifdData3, calculateOffsetOfIfd);
        }
        IfdData ifdData4 = this.mExifData.getIfdData(4);
        if (ifdData4 != null) {
            ifdData.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD)).setValue(calculateOffsetOfIfd);
            calculateOffsetOfIfd = calculateOffsetOfIfd(ifdData4, calculateOffsetOfIfd);
        }
        IfdData ifdData5 = this.mExifData.getIfdData(1);
        if (ifdData5 != null) {
            ifdData.setOffsetToNextIfd(calculateOffsetOfIfd);
            calculateOffsetOfIfd = calculateOffsetOfIfd(ifdData5, calculateOffsetOfIfd);
        }
        if (this.mExifData.hasCompressedThumbnail()) {
            ifdData5.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)).setValue(calculateOffsetOfIfd);
            return calculateOffsetOfIfd + this.mExifData.getCompressedThumbnail().length;
        } else if (!this.mExifData.hasUncompressedStrip()) {
            return calculateOffsetOfIfd;
        } else {
            long[] jArr = new long[this.mExifData.getStripCount()];
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                jArr[i] = (long) calculateOffsetOfIfd;
                calculateOffsetOfIfd += this.mExifData.getStrip(i).length;
            }
            ifdData5.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS)).setValue(jArr);
            return calculateOffsetOfIfd;
        }
    }

    private int calculateOffsetOfIfd(IfdData ifdData, int i) {
        i += ((ifdData.getTagCount() * 12) + 2) + 4;
        for (ExifTag exifTag : ifdData.getAllTags()) {
            if (exifTag.getDataSize() > 4) {
                exifTag.setOffset(i);
                i += exifTag.getDataSize();
            }
        }
        return i;
    }

    private void createRequiredIfdAndTag() throws IOException {
        IfdData ifdData = this.mExifData.getIfdData(0);
        if (ifdData == null) {
            ifdData = new IfdData(0);
            this.mExifData.addIfdData(ifdData);
        }
        ExifTag buildUninitializedTag = this.mInterface.buildUninitializedTag(ExifInterface.TAG_EXIF_IFD);
        if (buildUninitializedTag == null) {
            throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_EXIF_IFD);
        }
        ifdData.setTag(buildUninitializedTag);
        IfdData ifdData2 = this.mExifData.getIfdData(2);
        if (ifdData2 == null) {
            ifdData2 = new IfdData(2);
            this.mExifData.addIfdData(ifdData2);
        }
        if (this.mExifData.getIfdData(4) != null) {
            ExifTag buildUninitializedTag2 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_GPS_IFD);
            if (buildUninitializedTag2 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_GPS_IFD);
            }
            ifdData.setTag(buildUninitializedTag2);
        }
        if (this.mExifData.getIfdData(3) != null) {
            ExifTag buildUninitializedTag3 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_INTEROPERABILITY_IFD);
            if (buildUninitializedTag3 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_INTEROPERABILITY_IFD);
            }
            ifdData2.setTag(buildUninitializedTag3);
        }
        IfdData ifdData3 = this.mExifData.getIfdData(1);
        ExifTag buildUninitializedTag4;
        ExifTag buildUninitializedTag5;
        if (this.mExifData.hasCompressedThumbnail()) {
            if (ifdData3 == null) {
                ifdData3 = new IfdData(1);
                this.mExifData.addIfdData(ifdData3);
            }
            buildUninitializedTag4 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
            if (buildUninitializedTag4 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
            }
            ifdData3.setTag(buildUninitializedTag4);
            buildUninitializedTag5 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            if (buildUninitializedTag5 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
            }
            buildUninitializedTag5.setValue(this.mExifData.getCompressedThumbnail().length);
            ifdData3.setTag(buildUninitializedTag5);
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
        } else if (this.mExifData.hasUncompressedStrip()) {
            if (ifdData3 == null) {
                ifdData3 = new IfdData(1);
                this.mExifData.addIfdData(ifdData3);
            }
            int stripCount = this.mExifData.getStripCount();
            buildUninitializedTag4 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_STRIP_OFFSETS);
            if (buildUninitializedTag4 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_OFFSETS);
            }
            buildUninitializedTag5 = this.mInterface.buildUninitializedTag(ExifInterface.TAG_STRIP_BYTE_COUNTS);
            if (buildUninitializedTag5 == null) {
                throw new IOException("No definition for crucial exif tag: " + ExifInterface.TAG_STRIP_BYTE_COUNTS);
            }
            long[] jArr = new long[stripCount];
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                jArr[i] = (long) this.mExifData.getStrip(i).length;
            }
            buildUninitializedTag5.setValue(jArr);
            ifdData3.setTag(buildUninitializedTag4);
            ifdData3.setTag(buildUninitializedTag5);
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        } else if (ifdData3 != null) {
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS));
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS));
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT));
            ifdData3.removeTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH));
        }
    }

    private int requestByteToBuffer(int i, byte[] bArr, int i2, int i3) {
        int position = i - this.mBuffer.position();
        int i4 = i3 > position ? position : i3;
        this.mBuffer.put(bArr, i2, i4);
        return i4;
    }

    private ArrayList<ExifTag> stripNullValueTags(ExifData exifData) {
        ArrayList<ExifTag> arrayList = new ArrayList();
        for (ExifTag exifTag : exifData.getAllTags()) {
            if (exifTag.getValue() == null && !ExifInterface.isOffsetTag(exifTag.getTagId())) {
                exifData.removeTag(exifTag.getTagId(), exifTag.getIfd());
                arrayList.add(exifTag);
            }
        }
        return arrayList;
    }

    private void writeAllTags(OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        writeIfd(this.mExifData.getIfdData(0), orderedDataOutputStream);
        writeIfd(this.mExifData.getIfdData(2), orderedDataOutputStream);
        IfdData ifdData = this.mExifData.getIfdData(3);
        if (ifdData != null) {
            writeIfd(ifdData, orderedDataOutputStream);
        }
        IfdData ifdData2 = this.mExifData.getIfdData(4);
        if (ifdData2 != null) {
            writeIfd(ifdData2, orderedDataOutputStream);
        }
        if (this.mExifData.getIfdData(1) != null) {
            writeIfd(this.mExifData.getIfdData(1), orderedDataOutputStream);
        }
    }

    private void writeExifData() throws IOException {
        if (this.mExifData != null) {
            Iterable<ExifTag> stripNullValueTags = stripNullValueTags(this.mExifData);
            createRequiredIfdAndTag();
            int calculateAllOffset = calculateAllOffset();
            if (calculateAllOffset + 8 > 65535) {
                throw new IOException("Exif header is too large (>64Kb)");
            }
            OrderedDataOutputStream orderedDataOutputStream = new OrderedDataOutputStream(this.out);
            orderedDataOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN);
            orderedDataOutputStream.writeShort((short) -31);
            orderedDataOutputStream.writeShort((short) (calculateAllOffset + 8));
            orderedDataOutputStream.writeInt(1165519206);
            orderedDataOutputStream.writeShort((short) 0);
            if (this.mExifData.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                orderedDataOutputStream.writeShort((short) 19789);
            } else {
                orderedDataOutputStream.writeShort((short) 18761);
            }
            orderedDataOutputStream.setByteOrder(this.mExifData.getByteOrder());
            orderedDataOutputStream.writeShort((short) 42);
            orderedDataOutputStream.writeInt(8);
            writeAllTags(orderedDataOutputStream);
            writeThumbnail(orderedDataOutputStream);
            for (ExifTag addTag : stripNullValueTags) {
                this.mExifData.addTag(addTag);
            }
        }
    }

    private void writeIfd(IfdData ifdData, OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        int i = 0;
        ExifTag[] allTags = ifdData.getAllTags();
        orderedDataOutputStream.writeShort((short) allTags.length);
        for (ExifTag exifTag : allTags) {
            ExifTag exifTag2;
            orderedDataOutputStream.writeShort(exifTag2.getTagId());
            orderedDataOutputStream.writeShort(exifTag2.getDataType());
            orderedDataOutputStream.writeInt(exifTag2.getComponentCount());
            if (exifTag2.getDataSize() > 4) {
                orderedDataOutputStream.writeInt(exifTag2.getOffset());
            } else {
                writeTagValue(exifTag2, orderedDataOutputStream);
                int dataSize = 4 - exifTag2.getDataSize();
                for (int i2 = 0; i2 < dataSize; i2++) {
                    orderedDataOutputStream.write(0);
                }
            }
        }
        orderedDataOutputStream.writeInt(ifdData.getOffsetToNextIfd());
        int length = allTags.length;
        while (i < length) {
            exifTag2 = allTags[i];
            if (exifTag2.getDataSize() > 4) {
                writeTagValue(exifTag2, orderedDataOutputStream);
            }
            i++;
        }
    }

    static void writeTagValue(ExifTag exifTag, OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        byte[] bArr;
        int componentCount;
        int i;
        switch (exifTag.getDataType()) {
            case (short) 1:
            case (short) 7:
                bArr = new byte[exifTag.getComponentCount()];
                exifTag.getBytes(bArr);
                orderedDataOutputStream.write(bArr);
                return;
            case (short) 2:
                bArr = exifTag.getStringByte();
                if (bArr.length == exifTag.getComponentCount()) {
                    bArr[bArr.length - 1] = (byte) 0;
                    orderedDataOutputStream.write(bArr);
                    return;
                }
                orderedDataOutputStream.write(bArr);
                orderedDataOutputStream.write(0);
                return;
            case (short) 3:
                componentCount = exifTag.getComponentCount();
                for (i = 0; i < componentCount; i++) {
                    orderedDataOutputStream.writeShort((short) ((int) exifTag.getValueAt(i)));
                }
                return;
            case (short) 4:
            case (short) 9:
                componentCount = exifTag.getComponentCount();
                for (i = 0; i < componentCount; i++) {
                    orderedDataOutputStream.writeInt((int) exifTag.getValueAt(i));
                }
                return;
            case (short) 5:
            case (short) 10:
                componentCount = exifTag.getComponentCount();
                for (i = 0; i < componentCount; i++) {
                    orderedDataOutputStream.writeRational(exifTag.getRational(i));
                }
                return;
            default:
                return;
        }
    }

    private void writeThumbnail(OrderedDataOutputStream orderedDataOutputStream) throws IOException {
        if (this.mExifData.hasCompressedThumbnail()) {
            orderedDataOutputStream.write(this.mExifData.getCompressedThumbnail());
        } else if (this.mExifData.hasUncompressedStrip()) {
            for (int i = 0; i < this.mExifData.getStripCount(); i++) {
                orderedDataOutputStream.write(this.mExifData.getStrip(i));
            }
        }
    }

    protected void setExifData(ExifData exifData) {
        this.mExifData = exifData;
    }

    public void write(int i) throws IOException {
        this.mSingleByteArray[0] = (byte) (i & 255);
        write(this.mSingleByteArray);
    }

    public void write(byte[] bArr) throws IOException {
        write(bArr, 0, bArr.length);
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        while (true) {
            if ((this.mByteToSkip > 0 || this.mByteToCopy > 0 || this.mState != 2) && i2 > 0) {
                int i3;
                if (this.mByteToSkip > 0) {
                    i3 = i2 > this.mByteToSkip ? this.mByteToSkip : i2;
                    i2 -= i3;
                    this.mByteToSkip -= i3;
                    i += i3;
                }
                if (this.mByteToCopy > 0) {
                    i3 = i2 > this.mByteToCopy ? this.mByteToCopy : i2;
                    this.out.write(bArr, i, i3);
                    i2 -= i3;
                    this.mByteToCopy -= i3;
                    i += i3;
                }
                if (i2 != 0) {
                    int requestByteToBuffer;
                    switch (this.mState) {
                        case 0:
                            requestByteToBuffer = requestByteToBuffer(2, bArr, i, i2);
                            i += requestByteToBuffer;
                            i2 -= requestByteToBuffer;
                            if (this.mBuffer.position() >= 2) {
                                this.mBuffer.rewind();
                                if (this.mBuffer.getShort() == (short) -40) {
                                    this.out.write(this.mBuffer.array(), 0, 2);
                                    this.mState = 1;
                                    this.mBuffer.rewind();
                                    writeExifData();
                                    break;
                                }
                                throw new IOException("Not a valid jpeg image, cannot write exif");
                            }
                            return;
                        case 1:
                            requestByteToBuffer = requestByteToBuffer(4, bArr, i, i2);
                            i += requestByteToBuffer;
                            i2 -= requestByteToBuffer;
                            if (this.mBuffer.position() == 2 && this.mBuffer.getShort() == (short) -39) {
                                this.out.write(this.mBuffer.array(), 0, 2);
                                this.mBuffer.rewind();
                            }
                            if (this.mBuffer.position() >= 4) {
                                this.mBuffer.rewind();
                                short s = this.mBuffer.getShort();
                                if (s == (short) -31) {
                                    this.mByteToSkip = (this.mBuffer.getShort() & 65535) - 2;
                                    this.mState = 2;
                                } else if (JpegHeader.isSofMarker(s)) {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mState = 2;
                                } else {
                                    this.out.write(this.mBuffer.array(), 0, 4);
                                    this.mByteToCopy = (this.mBuffer.getShort() & 65535) - 2;
                                }
                                this.mBuffer.rewind();
                                break;
                            }
                            return;
                        default:
                            break;
                    }
                }
                return;
            }
            if (i2 > 0) {
                this.out.write(bArr, i, i2);
            }
            return;
        }
    }
}

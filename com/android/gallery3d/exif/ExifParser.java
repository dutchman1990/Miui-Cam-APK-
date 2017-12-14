package com.android.gallery3d.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

class ExifParser {
    private static final short TAG_EXIF_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_EXIF_IFD);
    private static final short TAG_GPS_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_GPS_IFD);
    private static final short TAG_INTEROPERABILITY_IFD = ExifInterface.getTrueTagKey(ExifInterface.TAG_INTEROPERABILITY_IFD);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT);
    private static final short TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = ExifInterface.getTrueTagKey(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
    private static final short TAG_STRIP_BYTE_COUNTS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_BYTE_COUNTS);
    private static final short TAG_STRIP_OFFSETS = ExifInterface.getTrueTagKey(ExifInterface.TAG_STRIP_OFFSETS);
    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private int mApp1End;
    private boolean mContainExifData = false;
    private final TreeMap<Integer, Object> mCorrespondingEvent = new TreeMap();
    private byte[] mDataAboveIfd0;
    private int mIfd0Position;
    private int mIfdStartOffset = 0;
    private int mIfdType;
    private ImageEvent mImageEvent;
    private final ExifInterface mInterface;
    private ExifTag mJpegSizeTag;
    private boolean mNeedToParseOffsetsInCurrentIfd;
    private int mNumOfTagInIfd = 0;
    private int mOffsetToApp1EndFromSOF = 0;
    private final int mOptions;
    private ExifTag mStripSizeTag;
    private ExifTag mTag;
    private int mTiffStartPosition;
    private final CountedDataInputStream mTiffStream;

    private static class ExifTagEvent {
        boolean isRequested;
        ExifTag tag;

        ExifTagEvent(ExifTag exifTag, boolean z) {
            this.tag = exifTag;
            this.isRequested = z;
        }
    }

    private static class IfdEvent {
        int ifd;
        boolean isRequested;

        IfdEvent(int i, boolean z) {
            this.ifd = i;
            this.isRequested = z;
        }
    }

    private static class ImageEvent {
        int stripIndex;
        int type;

        ImageEvent(int i) {
            this.stripIndex = 0;
            this.type = i;
        }

        ImageEvent(int i, int i2) {
            this.type = i;
            this.stripIndex = i2;
        }
    }

    private ExifParser(InputStream inputStream, int i, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        if (inputStream == null) {
            throw new IOException("Null argument inputStream to ExifParser");
        }
        this.mInterface = exifInterface;
        this.mContainExifData = seekTiffData(inputStream);
        this.mTiffStream = new CountedDataInputStream(inputStream);
        this.mOptions = i;
        if (this.mContainExifData) {
            parseTiffHeader();
            long readUnsignedInt = this.mTiffStream.readUnsignedInt();
            if (readUnsignedInt > 2147483647L) {
                throw new ExifInvalidFormatException("Invalid offset " + readUnsignedInt);
            }
            this.mIfd0Position = (int) readUnsignedInt;
            this.mIfdType = 0;
            if (isIfdRequested(0) || needToParseOffsetsInCurrentIfd()) {
                registerIfd(0, readUnsignedInt);
                if (readUnsignedInt != 8) {
                    this.mDataAboveIfd0 = new byte[(((int) readUnsignedInt) - 8)];
                    read(this.mDataAboveIfd0);
                }
            }
        }
    }

    private boolean checkAllowed(int i, int i2) {
        int i3 = this.mInterface.getTagInfo().get(i2);
        return i3 == 0 ? false : ExifInterface.isIfdAllowed(i3, i);
    }

    private void checkOffsetOrImageTag(ExifTag exifTag) {
        if (exifTag.getComponentCount() != 0) {
            short tagId = exifTag.getTagId();
            int ifd = exifTag.getIfd();
            if (tagId == TAG_EXIF_IFD && checkAllowed(ifd, ExifInterface.TAG_EXIF_IFD)) {
                if (isIfdRequested(2) || isIfdRequested(3)) {
                    registerIfd(2, exifTag.getValueAt(0));
                }
            } else if (tagId == TAG_GPS_IFD && checkAllowed(ifd, ExifInterface.TAG_GPS_IFD)) {
                if (isIfdRequested(4)) {
                    registerIfd(4, exifTag.getValueAt(0));
                }
            } else if (tagId == TAG_INTEROPERABILITY_IFD && checkAllowed(ifd, ExifInterface.TAG_INTEROPERABILITY_IFD)) {
                if (isIfdRequested(3)) {
                    registerIfd(3, exifTag.getValueAt(0));
                }
            } else if (tagId == TAG_JPEG_INTERCHANGE_FORMAT && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT)) {
                if (isThumbnailRequested()) {
                    registerCompressedImage(exifTag.getValueAt(0));
                }
            } else if (tagId == TAG_JPEG_INTERCHANGE_FORMAT_LENGTH && checkAllowed(ifd, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)) {
                if (isThumbnailRequested()) {
                    this.mJpegSizeTag = exifTag;
                }
            } else if (tagId == TAG_STRIP_OFFSETS && checkAllowed(ifd, ExifInterface.TAG_STRIP_OFFSETS)) {
                if (isThumbnailRequested()) {
                    if (exifTag.hasValue()) {
                        for (int i = 0; i < exifTag.getComponentCount(); i++) {
                            if (exifTag.getDataType() == (short) 3) {
                                registerUncompressedStrip(i, exifTag.getValueAt(i));
                            } else {
                                registerUncompressedStrip(i, exifTag.getValueAt(i));
                            }
                        }
                    } else {
                        this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, false));
                    }
                }
            } else if (tagId == TAG_STRIP_BYTE_COUNTS && checkAllowed(ifd, ExifInterface.TAG_STRIP_BYTE_COUNTS) && isThumbnailRequested() && exifTag.hasValue()) {
                this.mStripSizeTag = exifTag;
            }
        }
    }

    private boolean isIfdRequested(int i) {
        boolean z = true;
        switch (i) {
            case 0:
                if ((this.mOptions & 1) == 0) {
                    z = false;
                }
                return z;
            case 1:
                if ((this.mOptions & 2) == 0) {
                    z = false;
                }
                return z;
            case 2:
                if ((this.mOptions & 4) == 0) {
                    z = false;
                }
                return z;
            case 3:
                if ((this.mOptions & 16) == 0) {
                    z = false;
                }
                return z;
            case 4:
                if ((this.mOptions & 8) == 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    private boolean isThumbnailRequested() {
        return (this.mOptions & 32) != 0;
    }

    private boolean needToParseOffsetsInCurrentIfd() {
        boolean z = true;
        switch (this.mIfdType) {
            case 0:
                if (!(isIfdRequested(2) || isIfdRequested(4) || isIfdRequested(3))) {
                    z = isIfdRequested(1);
                }
                return z;
            case 1:
                return isThumbnailRequested();
            case 2:
                return isIfdRequested(3);
            default:
                return false;
        }
    }

    protected static ExifParser parse(InputStream inputStream, ExifInterface exifInterface) throws IOException, ExifInvalidFormatException {
        return new ExifParser(inputStream, 63, exifInterface);
    }

    private void parseTiffHeader() throws IOException, ExifInvalidFormatException {
        short readShort = this.mTiffStream.readShort();
        if ((short) 18761 == readShort) {
            this.mTiffStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else if ((short) 19789 == readShort) {
            this.mTiffStream.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
        if (this.mTiffStream.readShort() != (short) 42) {
            throw new ExifInvalidFormatException("Invalid TIFF header");
        }
    }

    private ExifTag readTag() throws IOException, ExifInvalidFormatException {
        short readShort = this.mTiffStream.readShort();
        short readShort2 = this.mTiffStream.readShort();
        long readUnsignedInt = this.mTiffStream.readUnsignedInt();
        if (readUnsignedInt > 2147483647L) {
            throw new ExifInvalidFormatException("Number of component is larger then Integer.MAX_VALUE");
        } else if (ExifTag.isValidType(readShort2)) {
            ExifTag exifTag = new ExifTag(readShort, readShort2, (int) readUnsignedInt, this.mIfdType, ((int) readUnsignedInt) != 0);
            int dataSize = exifTag.getDataSize();
            if (dataSize > 4) {
                long readUnsignedInt2 = this.mTiffStream.readUnsignedInt();
                if (readUnsignedInt2 > 2147483647L) {
                    throw new ExifInvalidFormatException("offset is larger then Integer.MAX_VALUE");
                } else if (readUnsignedInt2 >= ((long) this.mIfd0Position) || readShort2 != (short) 7) {
                    exifTag.setOffset((int) readUnsignedInt2);
                } else {
                    byte[] bArr = new byte[((int) readUnsignedInt)];
                    System.arraycopy(this.mDataAboveIfd0, ((int) readUnsignedInt2) - 8, bArr, 0, (int) readUnsignedInt);
                    exifTag.setValue(bArr);
                }
            } else {
                boolean hasDefinedCount = exifTag.hasDefinedCount();
                exifTag.setHasDefinedCount(false);
                readFullTagValue(exifTag);
                exifTag.setHasDefinedCount(hasDefinedCount);
                this.mTiffStream.skip((long) (4 - dataSize));
                exifTag.setOffset(this.mTiffStream.getReadByteCount() - 4);
            }
            return exifTag;
        } else {
            Log.w("ExifParser", String.format(Locale.ENGLISH, "Tag %04x: Invalid data type %d", new Object[]{Short.valueOf(readShort), Short.valueOf(readShort2)}));
            this.mTiffStream.skip(4);
            return null;
        }
    }

    private void registerCompressedImage(long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(3));
    }

    private void registerIfd(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new IfdEvent(i, isIfdRequested(i)));
    }

    private void registerUncompressedStrip(int i, long j) {
        this.mCorrespondingEvent.put(Integer.valueOf((int) j), new ImageEvent(4, i));
    }

    private boolean seekTiffData(InputStream inputStream) throws IOException, ExifInvalidFormatException {
        CountedDataInputStream countedDataInputStream = new CountedDataInputStream(inputStream);
        if (countedDataInputStream.readShort() != (short) -40) {
            throw new ExifInvalidFormatException("Invalid JPEG format");
        }
        short readShort = countedDataInputStream.readShort();
        while (readShort != (short) -39 && !JpegHeader.isSofMarker(readShort)) {
            int readUnsignedShort = countedDataInputStream.readUnsignedShort();
            if (readShort == (short) -31 && readUnsignedShort >= 8) {
                int readInt = countedDataInputStream.readInt();
                short readShort2 = countedDataInputStream.readShort();
                readUnsignedShort -= 6;
                if (readInt == 1165519206 && readShort2 == (short) 0) {
                    this.mTiffStartPosition = countedDataInputStream.getReadByteCount();
                    this.mApp1End = readUnsignedShort;
                    this.mOffsetToApp1EndFromSOF = this.mTiffStartPosition + this.mApp1End;
                    return true;
                }
            }
            if (readUnsignedShort < 2 || ((long) (readUnsignedShort - 2)) != countedDataInputStream.skip((long) (readUnsignedShort - 2))) {
                Log.w("ExifParser", "Invalid JPEG format.");
                return false;
            }
            readShort = countedDataInputStream.readShort();
        }
        return false;
    }

    private void skipTo(int i) throws IOException {
        this.mTiffStream.skipTo((long) i);
        while (!this.mCorrespondingEvent.isEmpty() && ((Integer) this.mCorrespondingEvent.firstKey()).intValue() < i) {
            this.mCorrespondingEvent.pollFirstEntry();
        }
    }

    protected ByteOrder getByteOrder() {
        return this.mTiffStream.getByteOrder();
    }

    protected int getCompressedImageSize() {
        return this.mJpegSizeTag == null ? 0 : (int) this.mJpegSizeTag.getValueAt(0);
    }

    protected int getCurrentIfd() {
        return this.mIfdType;
    }

    protected int getStripIndex() {
        return this.mImageEvent.stripIndex;
    }

    protected int getStripSize() {
        return this.mStripSizeTag == null ? 0 : (int) this.mStripSizeTag.getValueAt(0);
    }

    protected ExifTag getTag() {
        return this.mTag;
    }

    protected int next() throws IOException, ExifInvalidFormatException {
        if (!this.mContainExifData) {
            return 5;
        }
        int readByteCount = this.mTiffStream.getReadByteCount();
        int i = (this.mIfdStartOffset + 2) + (this.mNumOfTagInIfd * 12);
        if (readByteCount < i) {
            this.mTag = readTag();
            if (this.mTag == null) {
                return next();
            }
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                checkOffsetOrImageTag(this.mTag);
            }
            return 1;
        }
        if (readByteCount == i) {
            long readUnsignedLong;
            if (this.mIfdType == 0) {
                readUnsignedLong = readUnsignedLong();
                if ((isIfdRequested(1) || isThumbnailRequested()) && readUnsignedLong != 0) {
                    registerIfd(1, readUnsignedLong);
                }
            } else {
                int i2 = 4;
                if (this.mCorrespondingEvent.size() > 0) {
                    i2 = ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() - this.mTiffStream.getReadByteCount();
                }
                if (i2 < 4) {
                    Log.w("ExifParser", "Invalid size of link to next IFD: " + i2);
                } else {
                    readUnsignedLong = readUnsignedLong();
                    if (readUnsignedLong != 0) {
                        Log.w("ExifParser", "Invalid link to next IFD: " + readUnsignedLong);
                    }
                }
            }
        }
        while (this.mCorrespondingEvent.size() != 0) {
            Entry pollFirstEntry = this.mCorrespondingEvent.pollFirstEntry();
            Object value = pollFirstEntry.getValue();
            try {
                skipTo(((Integer) pollFirstEntry.getKey()).intValue());
                if (value instanceof IfdEvent) {
                    this.mIfdType = ((IfdEvent) value).ifd;
                    this.mNumOfTagInIfd = this.mTiffStream.readUnsignedShort();
                    this.mIfdStartOffset = ((Integer) pollFirstEntry.getKey()).intValue();
                    if (((this.mNumOfTagInIfd * 12) + this.mIfdStartOffset) + 2 > this.mApp1End) {
                        Log.w("ExifParser", "Invalid size of IFD " + this.mIfdType);
                        return 5;
                    }
                    this.mNeedToParseOffsetsInCurrentIfd = needToParseOffsetsInCurrentIfd();
                    if (((IfdEvent) value).isRequested) {
                        return 0;
                    }
                    skipRemainingTagsInCurrentIfd();
                } else if (value instanceof ImageEvent) {
                    this.mImageEvent = (ImageEvent) value;
                    return this.mImageEvent.type;
                } else {
                    ExifTagEvent exifTagEvent = (ExifTagEvent) value;
                    this.mTag = exifTagEvent.tag;
                    if (this.mTag.getDataType() != (short) 7) {
                        readFullTagValue(this.mTag);
                        checkOffsetOrImageTag(this.mTag);
                    }
                    if (exifTagEvent.isRequested) {
                        return 2;
                    }
                }
            } catch (IOException e) {
                Log.w("ExifParser", "Failed to skip to data at: " + pollFirstEntry.getKey() + " for " + value.getClass().getName() + ", the file may be broken.");
            }
        }
        return 5;
    }

    protected int read(byte[] bArr) throws IOException {
        return this.mTiffStream.read(bArr);
    }

    protected void readFullTagValue(ExifTag exifTag) throws IOException {
        int[] iArr;
        int length;
        int i;
        long[] jArr;
        Rational[] rationalArr;
        short dataType = exifTag.getDataType();
        if (!(dataType == (short) 2 || dataType == (short) 7)) {
            if (dataType == (short) 1) {
            }
            switch (exifTag.getDataType()) {
                case (short) 1:
                case (short) 7:
                    byte[] bArr = new byte[exifTag.getComponentCount()];
                    read(bArr);
                    exifTag.setValue(bArr);
                    return;
                case (short) 2:
                    exifTag.setValue(readString(exifTag.getComponentCount()));
                    return;
                case (short) 3:
                    iArr = new int[exifTag.getComponentCount()];
                    length = iArr.length;
                    for (i = 0; i < length; i++) {
                        iArr[i] = readUnsignedShort();
                    }
                    exifTag.setValue(iArr);
                    return;
                case (short) 4:
                    jArr = new long[exifTag.getComponentCount()];
                    length = jArr.length;
                    for (i = 0; i < length; i++) {
                        jArr[i] = readUnsignedLong();
                    }
                    exifTag.setValue(jArr);
                    return;
                case (short) 5:
                    rationalArr = new Rational[exifTag.getComponentCount()];
                    length = rationalArr.length;
                    for (i = 0; i < length; i++) {
                        rationalArr[i] = readUnsignedRational();
                    }
                    exifTag.setValue(rationalArr);
                    return;
                case (short) 9:
                    iArr = new int[exifTag.getComponentCount()];
                    length = iArr.length;
                    for (i = 0; i < length; i++) {
                        iArr[i] = readLong();
                    }
                    exifTag.setValue(iArr);
                    return;
                case (short) 10:
                    rationalArr = new Rational[exifTag.getComponentCount()];
                    length = rationalArr.length;
                    for (i = 0; i < length; i++) {
                        rationalArr[i] = readRational();
                    }
                    exifTag.setValue(rationalArr);
                    return;
                default:
                    return;
            }
        }
        int componentCount = exifTag.getComponentCount();
        if (this.mCorrespondingEvent.size() > 0 && ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() < this.mTiffStream.getReadByteCount() + componentCount) {
            Object value = this.mCorrespondingEvent.firstEntry().getValue();
            if (value instanceof ImageEvent) {
                Log.w("ExifParser", "Thumbnail overlaps value for tag: \n" + exifTag.toString());
                Log.w("ExifParser", "Invalid thumbnail offset: " + this.mCorrespondingEvent.pollFirstEntry().getKey());
            } else {
                if (value instanceof IfdEvent) {
                    Log.w("ExifParser", "Ifd " + ((IfdEvent) value).ifd + " overlaps value for tag: \n" + exifTag.toString());
                } else if (value instanceof ExifTagEvent) {
                    Log.w("ExifParser", "Tag value for tag: \n" + ((ExifTagEvent) value).tag.toString() + " overlaps value for tag: \n" + exifTag.toString());
                }
                componentCount = ((Integer) this.mCorrespondingEvent.firstEntry().getKey()).intValue() - this.mTiffStream.getReadByteCount();
                Log.w("ExifParser", "Invalid size of tag: \n" + exifTag.toString() + " setting count to: " + componentCount);
                exifTag.forceSetComponentCount(componentCount);
            }
        }
        switch (exifTag.getDataType()) {
            case (short) 1:
            case (short) 7:
                byte[] bArr2 = new byte[exifTag.getComponentCount()];
                read(bArr2);
                exifTag.setValue(bArr2);
                return;
            case (short) 2:
                exifTag.setValue(readString(exifTag.getComponentCount()));
                return;
            case (short) 3:
                iArr = new int[exifTag.getComponentCount()];
                length = iArr.length;
                for (i = 0; i < length; i++) {
                    iArr[i] = readUnsignedShort();
                }
                exifTag.setValue(iArr);
                return;
            case (short) 4:
                jArr = new long[exifTag.getComponentCount()];
                length = jArr.length;
                for (i = 0; i < length; i++) {
                    jArr[i] = readUnsignedLong();
                }
                exifTag.setValue(jArr);
                return;
            case (short) 5:
                rationalArr = new Rational[exifTag.getComponentCount()];
                length = rationalArr.length;
                for (i = 0; i < length; i++) {
                    rationalArr[i] = readUnsignedRational();
                }
                exifTag.setValue(rationalArr);
                return;
            case (short) 9:
                iArr = new int[exifTag.getComponentCount()];
                length = iArr.length;
                for (i = 0; i < length; i++) {
                    iArr[i] = readLong();
                }
                exifTag.setValue(iArr);
                return;
            case (short) 10:
                rationalArr = new Rational[exifTag.getComponentCount()];
                length = rationalArr.length;
                for (i = 0; i < length; i++) {
                    rationalArr[i] = readRational();
                }
                exifTag.setValue(rationalArr);
                return;
            default:
                return;
        }
    }

    protected int readLong() throws IOException {
        return this.mTiffStream.readInt();
    }

    protected Rational readRational() throws IOException {
        return new Rational((long) readLong(), (long) readLong());
    }

    protected String readString(int i) throws IOException {
        return readString(i, US_ASCII);
    }

    protected String readString(int i, Charset charset) throws IOException {
        return i > 0 ? this.mTiffStream.readString(i, charset) : "";
    }

    protected long readUnsignedLong() throws IOException {
        return ((long) readLong()) & 4294967295L;
    }

    protected Rational readUnsignedRational() throws IOException {
        return new Rational(readUnsignedLong(), readUnsignedLong());
    }

    protected int readUnsignedShort() throws IOException {
        return this.mTiffStream.readShort() & 65535;
    }

    protected void registerForTagValue(ExifTag exifTag) {
        if (exifTag.getOffset() >= this.mTiffStream.getReadByteCount()) {
            this.mCorrespondingEvent.put(Integer.valueOf(exifTag.getOffset()), new ExifTagEvent(exifTag, true));
        }
    }

    protected void skipRemainingTagsInCurrentIfd() throws IOException, ExifInvalidFormatException {
        int i = (this.mIfdStartOffset + 2) + (this.mNumOfTagInIfd * 12);
        int readByteCount = this.mTiffStream.getReadByteCount();
        if (readByteCount <= i) {
            if (this.mNeedToParseOffsetsInCurrentIfd) {
                while (readByteCount < i) {
                    this.mTag = readTag();
                    readByteCount += 12;
                    if (this.mTag != null) {
                        checkOffsetOrImageTag(this.mTag);
                    }
                }
            } else {
                skipTo(i);
            }
            long readUnsignedLong = readUnsignedLong();
            if (this.mIfdType == 0 && ((isIfdRequested(1) || isThumbnailRequested()) && readUnsignedLong > 0)) {
                registerIfd(1, readUnsignedLong);
            }
        }
    }
}

package com.android.gallery3d.exif;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExifData {
    private static final byte[] USER_COMMENT_ASCII = new byte[]{(byte) 65, (byte) 83, (byte) 67, (byte) 73, (byte) 73, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_JIS = new byte[]{(byte) 74, (byte) 73, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_UNICODE = new byte[]{(byte) 85, (byte) 78, (byte) 73, (byte) 67, (byte) 79, (byte) 68, (byte) 69, (byte) 0};
    private final ByteOrder mByteOrder;
    private final IfdData[] mIfdDatas = new IfdData[5];
    private ArrayList<byte[]> mStripBytes = new ArrayList();
    private byte[] mThumbnail;

    ExifData(ByteOrder byteOrder) {
        this.mByteOrder = byteOrder;
    }

    protected void addIfdData(IfdData ifdData) {
        this.mIfdDatas[ifdData.getId()] = ifdData;
    }

    protected ExifTag addTag(ExifTag exifTag) {
        return exifTag != null ? addTag(exifTag, exifTag.getIfd()) : null;
    }

    protected ExifTag addTag(ExifTag exifTag, int i) {
        return (exifTag == null || !ExifTag.isValidIfd(i)) ? null : getOrCreateIfdData(i).setTag(exifTag);
    }

    protected void clearThumbnailAndStrips() {
        this.mThumbnail = null;
        this.mStripBytes.clear();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ExifData)) {
            return false;
        }
        ExifData exifData = (ExifData) obj;
        if (exifData.mByteOrder != this.mByteOrder || exifData.mStripBytes.size() != this.mStripBytes.size() || !Arrays.equals(exifData.mThumbnail, this.mThumbnail)) {
            return false;
        }
        int i;
        for (i = 0; i < this.mStripBytes.size(); i++) {
            if (!Arrays.equals((byte[]) exifData.mStripBytes.get(i), (byte[]) this.mStripBytes.get(i))) {
                return false;
            }
        }
        for (i = 0; i < 5; i++) {
            IfdData ifdData = exifData.getIfdData(i);
            IfdData ifdData2 = getIfdData(i);
            if (ifdData != ifdData2 && ifdData != null && !ifdData.equals(ifdData2)) {
                return false;
            }
        }
        return true;
    }

    protected List<ExifTag> getAllTags() {
        List arrayList = new ArrayList();
        for (IfdData ifdData : this.mIfdDatas) {
            if (ifdData != null) {
                ExifTag[] allTags = ifdData.getAllTags();
                if (allTags != null) {
                    for (Object add : allTags) {
                        arrayList.add(add);
                    }
                }
            }
        }
        return arrayList.size() == 0 ? null : arrayList;
    }

    protected ByteOrder getByteOrder() {
        return this.mByteOrder;
    }

    protected byte[] getCompressedThumbnail() {
        return this.mThumbnail;
    }

    protected IfdData getIfdData(int i) {
        return ExifTag.isValidIfd(i) ? this.mIfdDatas[i] : null;
    }

    protected IfdData getOrCreateIfdData(int i) {
        IfdData ifdData = this.mIfdDatas[i];
        if (ifdData != null) {
            return ifdData;
        }
        ifdData = new IfdData(i);
        this.mIfdDatas[i] = ifdData;
        return ifdData;
    }

    protected byte[] getStrip(int i) {
        return (byte[]) this.mStripBytes.get(i);
    }

    protected int getStripCount() {
        return this.mStripBytes.size();
    }

    protected boolean hasCompressedThumbnail() {
        return this.mThumbnail != null;
    }

    protected boolean hasUncompressedStrip() {
        return this.mStripBytes.size() != 0;
    }

    protected void removeTag(short s, int i) {
        IfdData ifdData = this.mIfdDatas[i];
        if (ifdData != null) {
            ifdData.removeTag(s);
        }
    }

    protected void setCompressedThumbnail(byte[] bArr) {
        this.mThumbnail = bArr;
    }

    protected void setStripBytes(int i, byte[] bArr) {
        if (i < this.mStripBytes.size()) {
            this.mStripBytes.set(i, bArr);
            return;
        }
        for (int size = this.mStripBytes.size(); size < i; size++) {
            this.mStripBytes.add(null);
        }
        this.mStripBytes.add(bArr);
    }
}

package com.android.gallery3d.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

class ExifReader {
    private final ExifInterface mInterface;

    ExifReader(ExifInterface exifInterface) {
        this.mInterface = exifInterface;
    }

    protected ExifData read(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        ExifParser parse = ExifParser.parse(inputStream, this.mInterface);
        ExifData exifData = new ExifData(parse.getByteOrder());
        for (int next = parse.next(); next != 5; next = parse.next()) {
            ExifTag tag;
            byte[] bArr;
            switch (next) {
                case 0:
                    exifData.addIfdData(new IfdData(parse.getCurrentIfd()));
                    break;
                case 1:
                    tag = parse.getTag();
                    if (!tag.hasValue()) {
                        parse.registerForTagValue(tag);
                        break;
                    }
                    exifData.getIfdData(tag.getIfd()).setTag(tag);
                    break;
                case 2:
                    tag = parse.getTag();
                    if (tag.getDataType() == (short) 7) {
                        parse.readFullTagValue(tag);
                    }
                    exifData.getIfdData(tag.getIfd()).setTag(tag);
                    break;
                case 3:
                    int compressedImageSize = parse.getCompressedImageSize();
                    if (compressedImageSize <= 0) {
                        Log.d("ExifReader", "compressedImageSize=" + compressedImageSize);
                        break;
                    }
                    bArr = new byte[compressedImageSize];
                    if (bArr.length != parse.read(bArr)) {
                        Log.w("ExifReader", "Failed to read the compressed thumbnail");
                        break;
                    }
                    exifData.setCompressedThumbnail(bArr);
                    break;
                case 4:
                    bArr = new byte[parse.getStripSize()];
                    if (bArr.length != parse.read(bArr)) {
                        Log.w("ExifReader", "Failed to read the strip bytes");
                        break;
                    }
                    exifData.setStripBytes(parse.getStripIndex(), bArr);
                    break;
                default:
                    break;
            }
        }
        return exifData;
    }
}

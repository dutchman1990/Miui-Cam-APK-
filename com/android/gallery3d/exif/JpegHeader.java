package com.android.gallery3d.exif;

class JpegHeader {
    JpegHeader() {
    }

    public static final boolean isSofMarker(short s) {
        return (s < (short) -64 || s > (short) -49 || s == (short) -60 || s == (short) -56 || s == (short) -52) ? false : true;
    }
}

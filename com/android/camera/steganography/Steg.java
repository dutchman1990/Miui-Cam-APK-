package com.android.camera.steganography;

import android.graphics.Bitmap;

public class Steg {
    private final int PASS_NONE = 0;
    private final int PASS_SIMPLE_XOR = 1;
    private Bitmap inBitmap = null;
    private String key = null;
    private int passmode = 0;

    private int bytesAvaliableInBitmap() {
        return this.inBitmap == null ? 0 : (((this.inBitmap.getWidth() * this.inBitmap.getHeight()) * 3) / 8) - 12;
    }

    private void setInputBitmap(Bitmap bitmap) {
        this.inBitmap = bitmap;
    }

    public static Steg withInput(Bitmap bitmap) {
        Steg steg = new Steg();
        steg.setInputBitmap(bitmap);
        return steg;
    }

    public EncodedObject encode(String str) throws Exception {
        return encode(str.getBytes());
    }

    public EncodedObject encode(byte[] bArr) throws Exception {
        if (bArr.length <= bytesAvaliableInBitmap()) {
            return new EncodedObject(BitmapEncoder.encode(this.inBitmap, bArr));
        }
        throw new IllegalArgumentException("Not enough space in bitmap to hold data (max:" + bytesAvaliableInBitmap() + ")");
    }
}

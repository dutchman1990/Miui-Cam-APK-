package com.android.gallery3d.ui;

import android.graphics.Bitmap;

public class BitmapTexture extends UploadedTexture {
    protected Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    private BitmapTexture(Bitmap bitmap, boolean z) {
        boolean z2 = false;
        super(z);
        if (!(bitmap == null || bitmap.isRecycled())) {
            z2 = true;
        }
        Utils.assertTrue(z2);
        this.mContentBitmap = bitmap;
    }

    public void draw(GLCanvas gLCanvas, int i, int i2, int i3, int i4) {
        int width = this.mContentBitmap.getWidth();
        int height = this.mContentBitmap.getHeight();
        if (width * i4 != i3 * height) {
            int i5 = i;
            int i6 = i2;
            int i7 = i3;
            int i8 = i4;
            if (width * i4 > i3 * height) {
                i7 = (i4 * width) / height;
                i8 = i4;
                i5 = i + ((i3 - i7) / 2);
                i6 = i2;
            } else {
                i7 = i3;
                i8 = (i3 * height) / width;
                i5 = i;
                i6 = i2 + ((i4 - i8) / 2);
            }
            super.draw(gLCanvas, i5, i6, i7, i8);
            return;
        }
        super.draw(gLCanvas, i, i2, i3, i4);
    }

    protected void onFreeBitmap(Bitmap bitmap) {
    }

    protected Bitmap onGetBitmap() {
        return this.mContentBitmap;
    }
}

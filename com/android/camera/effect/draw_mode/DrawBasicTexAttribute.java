package com.android.camera.effect.draw_mode;

import com.android.gallery3d.ui.BasicTexture;

public class DrawBasicTexAttribute extends DrawAttribute {
    public BasicTexture mBasicTexture;
    public int mHeight;
    public boolean mIsSnapshot;
    public int mWidth;
    public int mX;
    public int mY;

    public DrawBasicTexAttribute(BasicTexture basicTexture, int i, int i2, int i3, int i4) {
        this.mX = i;
        this.mY = i2;
        this.mWidth = i3;
        this.mHeight = i4;
        this.mBasicTexture = basicTexture;
        this.mTarget = 5;
        this.mIsSnapshot = false;
    }

    public DrawBasicTexAttribute init(BasicTexture basicTexture, int i, int i2, int i3, int i4) {
        this.mX = i;
        this.mY = i2;
        this.mWidth = i3;
        this.mHeight = i4;
        this.mBasicTexture = basicTexture;
        this.mTarget = 5;
        this.mIsSnapshot = false;
        return this;
    }

    public DrawBasicTexAttribute init(BasicTexture basicTexture, int i, int i2, int i3, int i4, boolean z) {
        this.mX = i;
        this.mY = i2;
        this.mWidth = i3;
        this.mHeight = i4;
        this.mBasicTexture = basicTexture;
        this.mTarget = 5;
        this.mIsSnapshot = z;
        return this;
    }
}

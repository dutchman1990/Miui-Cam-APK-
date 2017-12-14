package com.android.camera;

import android.hardware.Camera.Size;

public class PictureSize implements Comparable<PictureSize> {
    public int height;
    public int width;

    public PictureSize() {
        setPictureSize(0, 0);
    }

    public PictureSize(int i, int i2) {
        setPictureSize(i, i2);
    }

    public PictureSize(String str) {
        setPictureSize(str);
    }

    public int area() {
        return isEmpty() ? 0 : this.width * this.height;
    }

    public int compareTo(PictureSize pictureSize) {
        return (pictureSize.width == this.width && pictureSize.height == this.height) ? 0 : -1;
    }

    public boolean isAspectRatio16_9() {
        return isEmpty() ? false : CameraSettings.isAspectRatio16_9(this.width, this.height);
    }

    public boolean isAspectRatio1_1() {
        return isEmpty() ? false : CameraSettings.isAspectRatio1_1(this.width, this.height);
    }

    public boolean isAspectRatio4_3() {
        return isEmpty() ? false : CameraSettings.isAspectRatio4_3(this.width, this.height);
    }

    public boolean isEmpty() {
        return this.width * this.height <= 0;
    }

    public PictureSize setPictureSize(int i, int i2) {
        this.width = i;
        this.height = i2;
        return this;
    }

    public PictureSize setPictureSize(Size size) {
        if (size != null) {
            this.width = size.width;
            this.height = size.height;
        } else {
            this.width = 0;
            this.height = 0;
        }
        return this;
    }

    public PictureSize setPictureSize(String str) {
        int indexOf = str == null ? -1 : str.indexOf(120);
        if (indexOf == -1) {
            this.width = 0;
            this.height = 0;
        } else {
            this.width = Integer.parseInt(str.substring(0, indexOf));
            this.height = Integer.parseInt(str.substring(indexOf + 1));
        }
        return this;
    }

    public String toString() {
        return this.width + "x" + this.height;
    }
}

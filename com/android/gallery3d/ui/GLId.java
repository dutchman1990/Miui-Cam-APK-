package com.android.gallery3d.ui;

import android.opengl.GLES20;

public class GLId {
    private static int sNextId = 1;

    public static void glDeleteBuffers(int i, int[] iArr, int i2) {
        GLES20.glDeleteBuffers(i, iArr, i2);
    }

    public static void glDeleteFrameBuffers(int i, int[] iArr, int i2) {
        GLES20.glDeleteFramebuffers(i, iArr, i2);
    }

    public static void glDeleteTextures(int i, int[] iArr, int i2) {
        GLES20.glDeleteTextures(i, iArr, i2);
    }

    public static synchronized void glGenFrameBuffers(int i, int[] iArr, int i2) {
        synchronized (GLId.class) {
            while (true) {
                int i3 = i - 1;
                if (i > 0) {
                    int i4 = i2 + i3;
                    int i5 = sNextId;
                    sNextId = i5 + 1;
                    iArr[i4] = i5;
                    i = i3;
                }
            }
        }
    }

    public static synchronized void glGenTextures(int i, int[] iArr, int i2) {
        synchronized (GLId.class) {
            while (true) {
                int i3 = i - 1;
                if (i > 0) {
                    int i4 = i2 + i3;
                    int i5 = sNextId;
                    sNextId = i5 + 1;
                    iArr[i4] = i5;
                    i = i3;
                }
            }
        }
    }
}

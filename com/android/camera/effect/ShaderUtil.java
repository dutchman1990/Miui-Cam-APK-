package com.android.camera.effect;

import android.opengl.GLES20;
import android.util.Log;
import com.android.camera.CameraAppImpl;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ShaderUtil {
    public static void checkGlError(String str) {
        int glGetError = GLES20.glGetError();
        if (glGetError != 0) {
            Log.e("Camera_ShaderUtil", "ES20_ERROR: op " + str + ": glError " + glGetError);
            throw new RuntimeException(str + ": glError " + glGetError);
        }
    }

    public static int createProgram(String str, String str2) {
        int loadShader = loadShader(35633, str);
        if (loadShader == 0) {
            Log.e("Camera_ShaderUtil", "Fail to init vertex shader " + str);
            return 0;
        }
        int loadShader2 = loadShader(35632, str2);
        if (loadShader2 == 0) {
            Log.e("Camera_ShaderUtil", "Fail to init fragment shader " + str2);
            return 0;
        }
        int glCreateProgram = GLES20.glCreateProgram();
        if (glCreateProgram != 0) {
            GLES20.glGetError();
            GLES20.glAttachShader(glCreateProgram, loadShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(glCreateProgram, loadShader2);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(glCreateProgram);
            int[] iArr = new int[1];
            GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
            if (iArr[0] != 1) {
                Log.e("Camera_ShaderUtil", "Could not link program: ");
                Log.e("Camera_ShaderUtil", GLES20.glGetProgramInfoLog(glCreateProgram));
                GLES20.glDeleteProgram(glCreateProgram);
                glCreateProgram = 0;
            }
        }
        return glCreateProgram;
    }

    public static String loadFromAssetsFile(String str) {
        Exception e;
        String str2 = null;
        try {
            InputStream open = CameraAppImpl.getAndroidContext().getAssets().open("shading_script/" + str);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                int read = open.read();
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(read);
            }
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            open.close();
            String str3 = new String(toByteArray, "UTF-8");
            try {
                str2 = str3.replaceAll("\\r\\n", "\n");
            } catch (Exception e2) {
                e = e2;
                str2 = str3;
                e.printStackTrace();
                return str2;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return str2;
        }
        return str2;
    }

    public static int loadShader(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        if (glCreateShader == 0) {
            return glCreateShader;
        }
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glCompileShader(glCreateShader);
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return glCreateShader;
        }
        Log.e("Camera_ShaderUtil", "Could not compile shader " + i + ":" + str);
        Log.e("Camera_ShaderUtil", "Info: " + GLES20.glGetShaderInfoLog(glCreateShader));
        GLES20.glDeleteShader(glCreateShader);
        return 0;
    }
}

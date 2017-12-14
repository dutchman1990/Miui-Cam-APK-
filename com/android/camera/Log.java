package com.android.camera;

public class Log {
    public static int m0d(String str, String str2) {
        return android.util.Log.d(str, str2);
    }

    public static int m1d(String str, String str2, Throwable th) {
        return android.util.Log.d(str, str2, th);
    }

    public static int m2e(String str, String str2) {
        return android.util.Log.e(str, str2);
    }

    public static int m3e(String str, String str2, Throwable th) {
        return android.util.Log.e(str, str2, th);
    }

    public static int m4i(String str, String str2) {
        return android.util.Log.i(str, str2);
    }

    public static int m5v(String str, String str2) {
        return Util.sIsDumpLog ? android.util.Log.v(str, str2) : -1;
    }
}

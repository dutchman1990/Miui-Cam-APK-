package android.support.v4.view;

import android.view.WindowInsets;

class WindowInsetsCompatApi20 {
    WindowInsetsCompatApi20() {
    }

    public static int getSystemWindowInsetBottom(Object obj) {
        return ((WindowInsets) obj).getSystemWindowInsetBottom();
    }
}

package android.support.v4.view;

import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;

class ViewCompatLollipop {

    public interface OnApplyWindowInsetsListenerBridge {
        Object onApplyWindowInsets(View view, Object obj);
    }

    ViewCompatLollipop() {
    }

    public static Object onApplyWindowInsets(View view, Object obj) {
        WindowInsets windowInsets = (WindowInsets) obj;
        WindowInsets onApplyWindowInsets = view.onApplyWindowInsets(windowInsets);
        return onApplyWindowInsets != windowInsets ? new WindowInsets(onApplyWindowInsets) : obj;
    }

    public static void setOnApplyWindowInsetsListener(View view, final OnApplyWindowInsetsListenerBridge onApplyWindowInsetsListenerBridge) {
        if (onApplyWindowInsetsListenerBridge == null) {
            view.setOnApplyWindowInsetsListener(null);
        } else {
            view.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    return (WindowInsets) onApplyWindowInsetsListenerBridge.onApplyWindowInsets(view, windowInsets);
                }
            });
        }
    }

    public static void stopNestedScroll(View view) {
        view.stopNestedScroll();
    }
}

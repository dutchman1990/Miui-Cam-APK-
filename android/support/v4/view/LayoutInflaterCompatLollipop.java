package android.support.v4.view;

import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;

class LayoutInflaterCompatLollipop {
    LayoutInflaterCompatLollipop() {
    }

    static void setFactory(LayoutInflater layoutInflater, LayoutInflaterFactory layoutInflaterFactory) {
        Factory2 factory2 = null;
        if (layoutInflaterFactory != null) {
            factory2 = new FactoryWrapperHC(layoutInflaterFactory);
        }
        layoutInflater.setFactory2(factory2);
    }
}

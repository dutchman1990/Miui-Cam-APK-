package com.android.camera.ui;

import android.content.Context;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;

public class PopupManager {
    private static HashMap<Context, PopupManager> sMap = new HashMap();
    private OnOtherPopupShowedListener mLastListener;
    private ArrayList<OnOtherPopupShowedListener> mListeners = new ArrayList();

    public interface OnOtherPopupShowedListener {
        boolean onOtherPopupShowed(int i);

        void recoverIfNeeded();
    }

    private PopupManager() {
    }

    public static PopupManager getInstance(Context context) {
        PopupManager popupManager = (PopupManager) sMap.get(context);
        if (popupManager != null) {
            return popupManager;
        }
        popupManager = new PopupManager();
        sMap.put(context, popupManager);
        return popupManager;
    }

    private void onDestroy() {
        if (this.mListeners != null) {
            this.mListeners.clear();
        }
        this.mListeners = null;
        this.mLastListener = null;
    }

    public static void removeInstance(Context context) {
        PopupManager popupManager = (PopupManager) sMap.get(context);
        if (popupManager != null) {
            popupManager.onDestroy();
            sMap.remove(context);
        }
    }

    public void clearRecoveredPopupListenerIfNeeded(OnOtherPopupShowedListener onOtherPopupShowedListener) {
        if (this.mLastListener == onOtherPopupShowedListener) {
            this.mLastListener = null;
        }
    }

    public OnOtherPopupShowedListener getLastOnOtherPopupShowedListener() {
        return this.mLastListener;
    }

    public void notifyDismissPopup() {
        if (this.mLastListener != null) {
            this.mLastListener.recoverIfNeeded();
            this.mLastListener = null;
        }
    }

    public void notifyShowPopup(View view, int i) {
        for (int i2 = 0; i2 < this.mListeners.size(); i2++) {
            OnOtherPopupShowedListener onOtherPopupShowedListener = (OnOtherPopupShowedListener) this.mListeners.get(i2);
            if (((View) onOtherPopupShowedListener) != view && onOtherPopupShowedListener.onOtherPopupShowed(i)) {
                this.mLastListener = onOtherPopupShowedListener;
            }
        }
    }

    public void removeOnOtherPopupShowedListener(OnOtherPopupShowedListener onOtherPopupShowedListener) {
        this.mListeners.remove(onOtherPopupShowedListener);
    }

    public void setOnOtherPopupShowedListener(OnOtherPopupShowedListener onOtherPopupShowedListener) {
        if (!this.mListeners.contains(onOtherPopupShowedListener)) {
            this.mListeners.add(onOtherPopupShowedListener);
        }
    }
}

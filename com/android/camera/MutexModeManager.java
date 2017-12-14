package com.android.camera;

import java.util.HashMap;

public class MutexModeManager {
    private int mCurrentMutexMode = 0;
    private int mLastMutexMode = 0;
    private HashMap<String, HashMap<String, Runnable>> mRunnableMap;

    public MutexModeManager(HashMap<String, HashMap<String, Runnable>> hashMap) {
        this.mRunnableMap = hashMap;
    }

    private void enter(int i) {
        this.mCurrentMutexMode = i;
        if (i != 0 && this.mRunnableMap != null) {
            HashMap hashMap = (HashMap) this.mRunnableMap.get(getMutexModeName(i));
            if (hashMap != null) {
                Runnable runnable = (Runnable) hashMap.get("enter");
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    private void exit(int i) {
        this.mLastMutexMode = this.mCurrentMutexMode;
        this.mCurrentMutexMode = 0;
        if (i != 0 && this.mRunnableMap != null) {
            HashMap hashMap = (HashMap) this.mRunnableMap.get(getMutexModeName(i));
            if (hashMap != null) {
                Runnable runnable = (Runnable) hashMap.get("exit");
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    public static String getMutexModeName(int i) {
        switch (i) {
            case 1:
                return "hdr";
            case 2:
                return "aohdr";
            case 3:
                return "hand-night";
            case 4:
                return "raw";
            case 7:
                return "burst-shoot";
            default:
                return "none";
        }
    }

    private void switchMutexMode(int i, int i2) {
        if (i != i2) {
            exit(i);
            enter(i2);
        }
    }

    public int getLastMutexMode() {
        return this.mLastMutexMode;
    }

    public int getMutexMode() {
        return this.mCurrentMutexMode;
    }

    public String getSuffix() {
        switch (this.mCurrentMutexMode) {
            case 1:
            case 5:
                return "_HDR";
            case 2:
                return "_AO_HDR";
            case 3:
                return "_HHT";
            case 4:
                return "_RAW";
            default:
                return "";
        }
    }

    public boolean isAoHdr() {
        return this.mCurrentMutexMode == 2;
    }

    public boolean isBurstShoot() {
        return this.mCurrentMutexMode == 7;
    }

    public boolean isHandNight() {
        return this.mCurrentMutexMode == 3;
    }

    public boolean isHdr() {
        return this.mCurrentMutexMode == 2 || this.mCurrentMutexMode == 1 || this.mCurrentMutexMode == 5;
    }

    public boolean isMorphoHdr() {
        return this.mCurrentMutexMode == 1;
    }

    public boolean isNeedComposed() {
        return (this.mCurrentMutexMode == 0 || this.mCurrentMutexMode == 2 || this.mCurrentMutexMode == 7) ? false : true;
    }

    public boolean isNormal() {
        return this.mCurrentMutexMode == 0;
    }

    public boolean isRAW() {
        return this.mCurrentMutexMode == 4;
    }

    public boolean isSceneHdr() {
        return this.mCurrentMutexMode == 5;
    }

    public boolean isSupportedFlashOn() {
        return this.mCurrentMutexMode == 0 || this.mCurrentMutexMode == 4;
    }

    public boolean isSupportedTorch() {
        return Device.isSupportedTorchCapture() ? this.mCurrentMutexMode == 0 || this.mCurrentMutexMode == 7 || this.mCurrentMutexMode == 2 : false;
    }

    public boolean isUbiFocus() {
        return this.mCurrentMutexMode == 6;
    }

    public void resetMutexMode() {
        switchMutexMode(this.mCurrentMutexMode, 0);
    }

    public void resetMutexModeDummy() {
        this.mLastMutexMode = this.mCurrentMutexMode;
        this.mCurrentMutexMode = 0;
    }

    public void setMutexMode(int i) {
        switchMutexMode(this.mCurrentMutexMode, i);
    }
}

package com.android.camera.ui;

import android.content.Context;
import android.support.v7.recyclerview.C0049R;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.Log;
import com.android.camera.Util;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import java.util.ArrayList;
import java.util.List;

public class SubScreenIndicatorButton extends V6AbstractIndicator implements MessageDispacher {
    private String mOverrideValue;
    private SubScreenPopup mParentPopup;

    public SubScreenIndicatorButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        PopupManager.getInstance(context).setOnOtherPopupShowedListener(this);
        setClickable(true);
    }

    private void filterPreference() {
        if (this.mPreference != null && this.mPreference.getEntryValues() != null && this.mPreference.getEntryValues().length != 0) {
            Object obj = null;
            if ("pref_qc_camera_exposuretime_key".equals(this.mPreference.getKey())) {
                CharSequence[] entryValues = this.mPreference.getEntryValues();
                try {
                    int intValue = Integer.valueOf(this.mPreference.getValue()).intValue();
                    int length = entryValues.length - 1;
                    while (length >= 0) {
                        int intValue2 = Integer.valueOf(entryValues[length].toString()).intValue();
                        if (intValue2 == intValue) {
                            break;
                        } else if (intValue > intValue2) {
                            this.mPreference.setValueIndex(length);
                            obj = 1;
                            break;
                        } else {
                            length--;
                        }
                    }
                } catch (NumberFormatException e) {
                    this.mPreference.print();
                }
            } else if (this.mPreference.findIndexOfValue(this.mPreference.getValue()) < 0) {
                this.mPreference.setValueIndex(0);
                obj = 1;
            }
            if (obj != null) {
                reloadPreference();
            }
        }
    }

    private void filterPreference(List<String> list, List<String> list2) {
        String[] stringArray = this.mContext.getResources().getStringArray(C0049R.array.pref_camera_exposuretime_entries);
        String[] stringArray2 = this.mContext.getResources().getStringArray(C0049R.array.pref_camera_exposuretime_entryvalues);
        int maxExposureTimes = Device.IS_MI4 ? 2000000 : CameraSettings.getMaxExposureTimes(this.mContext);
        int minExposureTimes = CameraSettings.getMinExposureTimes(this.mContext);
        for (int i = 0; i < stringArray2.length; i++) {
            int parseInt = Integer.parseInt(stringArray2[i]);
            if (minExposureTimes > parseInt || parseInt > maxExposureTimes) {
                if (i == 0 && this.mContext.getResources().getString(C0049R.string.pref_camera_exposuretime_default).equals(stringArray2[0])) {
                }
            }
            list.add(stringArray[i]);
            list2.add(stringArray2[i]);
        }
    }

    private int getPreferenceSize() {
        CharSequence[] entryValues = this.mPreference.getEntryValues();
        return entryValues != null ? entryValues.length : 0;
    }

    private void initializePopup() {
        if (this.mPreference == null || !this.mPreference.hasPopup()) {
            Log.m4i("SubScreenIndicatorButton", "no need to initialize popup, key=" + this.mPreference.getKey() + " mPreference=" + this.mPreference + " mPopup=" + this.mPopup);
        } else if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        } else {
            this.mPopup = SettingPopupFactory.createSettingPopup(getKey(), this.mPopupRoot, getContext());
            this.mPopup.initialize(this.mPreferenceGroup, this.mPreference, this);
            this.mPopupRoot.addView(this.mPopup);
        }
    }

    private void notifyMessageToDispatcher(int i) {
        Log.m5v("Camera5", "mMessageDispatcher=" + this.mMessageDispacher);
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(i, 0, 3, getKey(), this);
        }
    }

    private void rebuildPreference() {
        if ("pref_qc_camera_exposuretime_key".equals(this.mPreference.getKey()) && Device.IS_XIAOMI && Device.isQcomPlatform()) {
            List arrayList = new ArrayList();
            List arrayList2 = new ArrayList();
            filterPreference(arrayList, arrayList2);
            this.mPreference.setEntries((CharSequence[]) arrayList.toArray(new String[arrayList.size()]));
            this.mPreference.setEntryValues((CharSequence[]) arrayList2.toArray(new String[arrayList2.size()]));
        }
    }

    private void toggle() {
        if (this.mPreference != null) {
            int findIndexOfValue = this.mPreference.findIndexOfValue(this.mPreference.getValue()) + 1;
            if (findIndexOfValue >= getPreferenceSize()) {
                findIndexOfValue = 0;
            }
            this.mPreference.setValueIndex(findIndexOfValue);
            reloadPreference();
        }
        notifyMessageToDispatcher(6);
    }

    public boolean dismissPopup() {
        setPressed(false);
        if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
            return false;
        }
        this.mParentPopup.dismissChildPopup(this.mPopup);
        return true;
    }

    public boolean dispacherMessage(int i, int i2, int i3, Object obj, Object obj2) {
        if (this.mMessageDispacher == null) {
            return false;
        }
        if (!((obj2 instanceof Boolean) && ((Boolean) obj2).booleanValue())) {
            this.mMessageDispacher.dispacherMessage(i, i2, i3, obj, this);
        }
        reloadPreference();
        return true;
    }

    public String getKey() {
        return this.mPreference.getKey();
    }

    protected int getShowedColor() {
        return (this.mPreference == null || !this.mPreference.hasPopup()) ? TEXT_COLOR_DEFAULT : super.getShowedColor();
    }

    public void initialize(IconListPreference iconListPreference, MessageDispacher messageDispacher, ViewGroup viewGroup, int i, int i2, PreferenceGroup preferenceGroup, V6AbstractSettingPopup v6AbstractSettingPopup) {
        super.initialize(iconListPreference, messageDispacher, viewGroup, i, i2, preferenceGroup);
        this.mParentPopup = (SubScreenPopup) v6AbstractSettingPopup;
        rebuildPreference();
        filterPreference();
    }

    public boolean isOverridden() {
        return this.mOverrideValue != null;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMessageDispacher = null;
    }

    public void onDismiss() {
        super.onDismiss();
        PopupManager.getInstance(getContext()).removeOnOtherPopupShowedListener(this);
    }

    public boolean onOtherPopupShowed(int i) {
        if (i == 2) {
            dismissPopup();
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0 && !isOverridden()) {
            setPressed(true);
            return true;
        } else if (action == 3) {
            dismissPopup();
            return true;
        } else if (!isPressed() || action != 1) {
            return true;
        } else {
            if (Util.pointInView(motionEvent.getRawX(), motionEvent.getRawY(), this)) {
                if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
                    CameraDataAnalytics.instance().trackEvent(getKey());
                }
                if (!isOverridden() && this.mPreference.hasPopup()) {
                    if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
                        showPopup();
                        PopupManager.getInstance(getContext()).notifyShowPopup(this, 2);
                    } else {
                        dismissPopup();
                    }
                }
            }
            if (this.mPopup == null || this.mPopup.getVisibility() != 0) {
                setPressed(false);
                if (!(this.mPreference == null || this.mPreference.hasPopup())) {
                    toggle();
                }
            }
            notifyMessageToDispatcher(10);
            return true;
        }
    }

    public void reloadPreference() {
        updateContent();
        if (this.mPopup != null) {
            this.mPopup.reloadPreference();
        }
        PopupManager.getInstance(getContext()).setOnOtherPopupShowedListener(this);
    }

    public void setOrientation(int i, boolean z) {
        if (this.mPopup != null) {
            this.mPopup.setOrientation(i, z);
        }
    }

    public void showPopup() {
        initializePopup();
        if (this.mPopup != null) {
            this.mPopup.setOrientation(this.mOrientation, false);
            this.mParentPopup.showChildPopup(this.mPopup);
        }
    }

    protected void updateContent() {
        if (this.mContent != null) {
            if ("pref_skin_beautify_skin_color_key".equals(this.mPreference.getKey()) || "pref_skin_beautify_slim_face_key".equals(this.mPreference.getKey()) || "pref_skin_beautify_skin_smooth_key".equals(this.mPreference.getKey()) || "pref_skin_beautify_enlarge_eye_key".equals(this.mPreference.getKey())) {
                this.mContent.setText(CameraSettings.getSkinBeautifyHumanReadableValue(this.mContext, this.mPreference));
            } else if (this.mPreference.getEntries() != null && this.mPreference.getEntries().length != 0) {
                Util.setNumberText(this.mContent, this.mPreference.getEntry());
            } else if ("pref_focus_position_key".equals(this.mPreference.getKey())) {
                this.mContent.setText(CameraSettings.getManualFocusName(this.mContext, CameraSettings.getFocusPosition()));
            } else {
                this.mContent.setText(this.mPreference.getValue());
            }
        }
    }
}

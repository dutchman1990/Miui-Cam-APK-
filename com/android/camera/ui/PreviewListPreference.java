package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import java.util.ArrayList;
import java.util.List;

public class PreviewListPreference extends ListPreference {
    private CharSequence[] mDefaultValues;

    public PreviewListPreference(Context context) {
        this(context, null);
    }

    public PreviewListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (this.mDefaultValues != null) {
            setDefaultValue(findSupportedDefaultValue(this.mDefaultValues));
        }
    }

    private CharSequence findSupportedDefaultValue(CharSequence[] charSequenceArr) {
        CharSequence[] entryValues = getEntryValues();
        if (entryValues == null) {
            return null;
        }
        for (Object obj : entryValues) {
            for (CharSequence charSequence : charSequenceArr) {
                if (obj != null && obj.equals(charSequence)) {
                    return charSequence;
                }
            }
        }
        return null;
    }

    public void filterUnsupported(List<String> list) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int length = entries.length;
        for (int i = 0; i < length; i++) {
            if (list.indexOf(entryValues[i].toString()) >= 0) {
                arrayList.add(entries[i]);
                arrayList2.add(entryValues[i]);
            }
        }
        int size = arrayList.size();
        setEntries((CharSequence[]) arrayList.toArray(new CharSequence[size]));
        setEntryValues((CharSequence[]) arrayList2.toArray(new CharSequence[size]));
    }

    protected Object onGetDefaultValue(TypedArray typedArray, int i) {
        TypedValue peekValue = typedArray.peekValue(i);
        if (peekValue != null && peekValue.type == 1) {
            this.mDefaultValues = typedArray.getTextArray(i);
        }
        return this.mDefaultValues != null ? this.mDefaultValues[0] : typedArray.getString(i);
    }

    public void setEntryValues(CharSequence[] charSequenceArr) {
        super.setEntryValues(charSequenceArr);
        if (this.mDefaultValues != null) {
            setDefaultValue(findSupportedDefaultValue(this.mDefaultValues));
        }
    }

    public void setValue(String str) {
        super.setValue(str);
        setSummary(getEntry());
    }
}

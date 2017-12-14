package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;

class SlidingPaneLayout$SavedState extends AbsSavedState {
    public static final Creator<SlidingPaneLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new C00481());
    boolean isOpen;

    static class C00481 implements ParcelableCompatCreatorCallbacks<SlidingPaneLayout$SavedState> {
        C00481() {
        }

        public SlidingPaneLayout$SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new SlidingPaneLayout$SavedState(parcel, classLoader);
        }

        public SlidingPaneLayout$SavedState[] newArray(int i) {
            return new SlidingPaneLayout$SavedState[i];
        }
    }

    SlidingPaneLayout$SavedState(Parcel parcel, ClassLoader classLoader) {
        boolean z = false;
        super(parcel, classLoader);
        if (parcel.readInt() != 0) {
            z = true;
        }
        this.isOpen = z;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.isOpen ? 1 : 0);
    }
}

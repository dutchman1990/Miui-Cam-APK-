package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

class StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem implements Parcelable {
    public static final Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> CREATOR = new C00691();
    int mGapDir;
    int[] mGapPerSpan;
    boolean mHasUnwantedGapAfter;
    int mPosition;

    static class C00691 implements Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> {
        C00691() {
        }

        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem createFromParcel(Parcel parcel) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(parcel);
        }

        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[] newArray(int i) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[i];
        }
    }

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(Parcel parcel) {
        boolean z = true;
        this.mPosition = parcel.readInt();
        this.mGapDir = parcel.readInt();
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.mHasUnwantedGapAfter = z;
        int readInt = parcel.readInt();
        if (readInt > 0) {
            this.mGapPerSpan = new int[readInt];
            parcel.readIntArray(this.mGapPerSpan);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mPosition);
        parcel.writeInt(this.mGapDir);
        parcel.writeInt(this.mHasUnwantedGapAfter ? 1 : 0);
        if (this.mGapPerSpan == null || this.mGapPerSpan.length <= 0) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(this.mGapPerSpan.length);
        parcel.writeIntArray(this.mGapPerSpan);
    }
}

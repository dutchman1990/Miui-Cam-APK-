package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.ResultReceiver;

final class MediaSessionCompat$ResultReceiverWrapper implements Parcelable {
    public static final Creator<MediaSessionCompat$ResultReceiverWrapper> CREATOR = new C00241();
    private ResultReceiver mResultReceiver;

    static class C00241 implements Creator<MediaSessionCompat$ResultReceiverWrapper> {
        C00241() {
        }

        public MediaSessionCompat$ResultReceiverWrapper createFromParcel(Parcel parcel) {
            return new MediaSessionCompat$ResultReceiverWrapper(parcel);
        }

        public MediaSessionCompat$ResultReceiverWrapper[] newArray(int i) {
            return new MediaSessionCompat$ResultReceiverWrapper[i];
        }
    }

    MediaSessionCompat$ResultReceiverWrapper(Parcel parcel) {
        this.mResultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        this.mResultReceiver.writeToParcel(parcel, i);
    }
}

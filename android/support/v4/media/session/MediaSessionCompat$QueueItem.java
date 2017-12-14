package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.media.MediaDescriptionCompat;

public final class MediaSessionCompat$QueueItem implements Parcelable {
    public static final Creator<MediaSessionCompat$QueueItem> CREATOR = new C00231();
    private final MediaDescriptionCompat mDescription;
    private final long mId;

    static class C00231 implements Creator<MediaSessionCompat$QueueItem> {
        C00231() {
        }

        public MediaSessionCompat$QueueItem createFromParcel(Parcel parcel) {
            return new MediaSessionCompat$QueueItem(parcel);
        }

        public MediaSessionCompat$QueueItem[] newArray(int i) {
            return new MediaSessionCompat$QueueItem[i];
        }
    }

    MediaSessionCompat$QueueItem(Parcel parcel) {
        this.mDescription = (MediaDescriptionCompat) MediaDescriptionCompat.CREATOR.createFromParcel(parcel);
        this.mId = parcel.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
    }

    public void writeToParcel(Parcel parcel, int i) {
        this.mDescription.writeToParcel(parcel, i);
        parcel.writeLong(this.mId);
    }
}

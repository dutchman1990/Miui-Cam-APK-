package com.android.camera;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import com.android.camera.permission.PermissionManager;

public class LocationManager {
    private static LocationManager sLocationManager;
    private Listener mListener;
    LocationListener[] mLocationListeners = new LocationListener[]{new LocationListener("gps"), new LocationListener("network")};
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;

    public interface Listener {
        void hideGpsOnScreenIndicator();

        void showGpsOnScreenIndicator(boolean z);
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        String mProvider;
        boolean mValid = false;

        public LocationListener(String str) {
            this.mProvider = str;
            this.mLastLocation = new Location(this.mProvider);
        }

        public Location current() {
            return this.mValid ? this.mLastLocation : null;
        }

        public void onLocationChanged(Location location) {
            if (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d) {
                if (LocationManager.this.mListener != null && LocationManager.this.mRecordLocation && "gps".equals(this.mProvider)) {
                    LocationManager.this.mListener.showGpsOnScreenIndicator(true);
                }
                if (this.mValid) {
                    Log.v("LocationManager", "update location, it is from " + this.mProvider);
                } else {
                    Log.d("LocationManager", "Got first location, it is from " + this.mProvider);
                }
                this.mLastLocation.set(location);
                this.mValid = true;
            }
        }

        public void onProviderDisabled(String str) {
            this.mValid = false;
        }

        public void onProviderEnabled(String str) {
        }

        public void onStatusChanged(String str, int i, Bundle bundle) {
            switch (i) {
                case 0:
                case 1:
                    this.mValid = false;
                    if (LocationManager.this.mListener != null && LocationManager.this.mRecordLocation && "gps".equals(str)) {
                        LocationManager.this.mListener.showGpsOnScreenIndicator(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private LocationManager() {
    }

    public static LocationManager instance() {
        if (sLocationManager == null) {
            sLocationManager = new LocationManager();
        }
        return sLocationManager;
    }

    private void startReceivingLocationUpdates() {
        if (this.mLocationManager == null) {
            this.mLocationManager = (android.location.LocationManager) CameraAppImpl.getAndroidContext().getSystemService("location");
        }
        if (this.mLocationManager != null) {
            try {
                this.mLocationManager.requestLocationUpdates("network", 1000, 0.0f, this.mLocationListeners[1]);
            } catch (Throwable e) {
                Log.i("LocationManager", "fail to request location update, ignore", e);
            } catch (IllegalArgumentException e2) {
                Log.d("LocationManager", "provider does not exist " + e2.getMessage());
            }
            try {
                this.mLocationManager.requestLocationUpdates("gps", 1000, 0.0f, this.mLocationListeners[0]);
                if (this.mListener != null) {
                    this.mListener.showGpsOnScreenIndicator(false);
                }
            } catch (Throwable e3) {
                Log.i("LocationManager", "fail to request location update, ignore", e3);
            } catch (IllegalArgumentException e22) {
                Log.d("LocationManager", "provider does not exist " + e22.getMessage());
            }
            Log.d("LocationManager", "startReceivingLocationUpdates");
        }
    }

    private void stopReceivingLocationUpdates() {
        if (this.mLocationManager != null) {
            for (int i = 0; i < this.mLocationListeners.length; i++) {
                try {
                    this.mLocationManager.removeUpdates(this.mLocationListeners[i]);
                } catch (Throwable e) {
                    Log.i("LocationManager", "fail to remove location listners, ignore", e);
                }
                this.mLocationListeners[i].mValid = false;
            }
            Log.d("LocationManager", "stopReceivingLocationUpdates");
        }
        if (this.mListener != null) {
            this.mListener.hideGpsOnScreenIndicator();
        }
    }

    public Location getCurrentLocation() {
        if (!this.mRecordLocation) {
            return null;
        }
        for (int i = 0; i < this.mLocationListeners.length; i++) {
            Location current = this.mLocationListeners[i].current();
            if (current != null) {
                Log.v("LocationManager", "get current location, it is from " + this.mLocationListeners[i].mProvider);
                return current;
            }
        }
        Log.d("LocationManager", "No location received yet.");
        return null;
    }

    public void recordLocation(boolean z) {
        if (this.mRecordLocation != z) {
            this.mRecordLocation = z;
            if (z && PermissionManager.checkCameraLocationPermissions()) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }
}

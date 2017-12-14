package com.android.camera;

import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class ExifHelper {
    private static DateFormat mDateTimeStampFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private static DateFormat mGPSDateStampFormat = new SimpleDateFormat("yyyy:MM:dd");
    private static DateFormat mGPSTimeStampFormat = new SimpleDateFormat("HH:mm:ss");

    static {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        mGPSDateStampFormat.setTimeZone(timeZone);
        mGPSTimeStampFormat.setTimeZone(timeZone);
    }

    public static String convertDoubleToLaLon(double d) {
        int floor = (int) Math.floor(Math.abs(d));
        double floor2 = Math.floor((Math.abs(d) - ((double) floor)) * 60.0d);
        double floor3 = Math.floor(((Math.abs(d) - ((double) floor)) - (floor2 / 60.0d)) * 3600000.0d);
        return d < 0.0d ? "-" + floor + "/1," + ((int) floor2) + "/1," + ((int) floor3) + "/1000" : floor + "/1," + ((int) floor2) + "/1," + ((int) floor3) + "/1000";
    }

    private static String getExifOrientation(int i) {
        switch (i) {
            case 0:
                return String.valueOf(1);
            case 90:
                return String.valueOf(6);
            case 180:
                return String.valueOf(3);
            case 270:
                return String.valueOf(8);
            default:
                throw new AssertionError("invalid: " + i);
        }
    }

    public static void writeExif(String str, int i, Location location, long j) {
        try {
            ExifInterface exifInterface = new ExifInterface(str);
            exifInterface.setAttribute("GPSDateStamp", mGPSDateStampFormat.format(Long.valueOf(j)));
            exifInterface.setAttribute("GPSTimeStamp", mGPSTimeStampFormat.format(Long.valueOf(j)));
            exifInterface.setAttribute("DateTime", mDateTimeStampFormat.format(Long.valueOf(j)));
            exifInterface.setAttribute("Orientation", getExifOrientation(i));
            exifInterface.setAttribute("Make", Build.MANUFACTURER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                exifInterface.setAttribute("GPSLatitude", convertDoubleToLaLon(latitude));
                exifInterface.setAttribute("GPSLongitude", convertDoubleToLaLon(longitude));
                if (latitude > 0.0d) {
                    exifInterface.setAttribute("GPSLatitudeRef", "N");
                } else {
                    exifInterface.setAttribute("GPSLatitudeRef", "S");
                }
                if (longitude > 0.0d) {
                    exifInterface.setAttribute("GPSLongitudeRef", "E");
                } else {
                    exifInterface.setAttribute("GPSLongitudeRef", "W");
                }
            }
            if (Device.IS_MI2 || Device.IS_MI2A) {
                exifInterface.setAttribute("Model", "MiTwo");
                exifInterface.setAttribute("FocalLength", String.valueOf("354/100"));
            } else {
                exifInterface.setAttribute("Model", Device.MODULE);
            }
            exifInterface.saveAttributes();
        } catch (IOException e) {
        }
    }
}

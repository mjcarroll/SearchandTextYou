package com.nuspatial.geoqar;

import android.util.Log;

import java.lang.Math;

/**
 * Created by michael on 2/25/17.
 */

public class GeoPoint {
    public double mLat;
    public double mLon;
    public double mAlt;

    public GeoPoint(double lat, double lon, double alt) {
        mLat = lat;
        mLon = lon;
        mAlt = alt;
    }

    public GeoPoint(double[] pos) {
        mLat = pos[0];
        mLon = pos[1];
        mAlt = pos[2];
    }

    double[] toECEF() {
        double[] lla = new double[] {mLat, mLon, mAlt};
        return GeoUtils.lla2ecef(lla);
    }

    // generally from navpy: https://github.com/NavPy/NavPy/blob/master/navpy/core/navpy.py#L958
    double[] toNED(GeoPoint origin) {

        double[] ecef_o = origin.toECEF();
        double[] ecef_t = this.toECEF();

        double dx = ecef_t[0] - ecef_o[0];
        double dy = ecef_t[1] - ecef_o[1];
        double dz = ecef_t[2] - ecef_o[2];

        // Log.i("GeoUtils", Double.toString(dx) + " " + Double.toString(dy) + " " + Double.toString(dz));

        return GeoUtils.ecef2ned(new double[] {dx, dy, dz},
                origin.mLat, origin.mLon, origin.mAlt);
    }

    public String toString() {
        String str = "";
        str += Double.toString(mLat * 180.0 / Math.PI) + " ";
        str += Double.toString(mLon * 180.0 / Math.PI) + " ";
        str += mAlt;
        return str;
    }

}

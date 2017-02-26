package com.nuspatial.geoqar;

/**
 * Created by michael on 2/25/17.
 */

public class PointOfInterest {
    public GeoPoint mPoint;
    public String mStatus;

    public PointOfInterest(GeoPoint point, String status) {
        mPoint = point;
        mStatus = status;
    }
}
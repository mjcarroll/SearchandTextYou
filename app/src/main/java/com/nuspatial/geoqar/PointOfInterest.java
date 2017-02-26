package com.nuspatial.geoqar;

/**
 * Created by michael on 2/25/17.
 */

public class PointOfInterest {
    public GeoPoint mPoint;
    public String mStatus;
    public int mId;

    public PointOfInterest(int id, GeoPoint point, String status) {
        mId = id;
        mPoint = point;
        mStatus = status;
    }
}
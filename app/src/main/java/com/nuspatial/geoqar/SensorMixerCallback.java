package com.nuspatial.geoqar;

/**
 * Created by michael on 2/25/17.
 */

public interface SensorMixerCallback {
    void onLocation(double[] location);
    void onMatrix(float[] matrix);
}

package com.nuspatial.geoqar;

import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
 * Created by michael on 2/25/17.
 */

public class GeoUtils {

    /* From: https://gist.github.com/klucar/1536194 */

    /*
*
*  ECEF - Earth Centered Earth Fixed
*
*  LLA - Lat Lon Alt
*
*  ported from matlab code at
*  https://gist.github.com/1536054
*     and
*  https://gist.github.com/1536056
*/

    // WGS84 ellipsoid constants
    private final static double a = 6378137; // radius
    private final static double e = 8.1819190842622e-2;  // eccentricity

    private final static double asq = Math.pow(a,2);
    private final static double esq = Math.pow(e,2);

    public static double[] ecef2lla(double[] ecef){
        double x = ecef[0];
        double y = ecef[1];
        double z = ecef[2];

        double b = Math.sqrt( asq * (1-esq) );
        double bsq = Math.pow(b,2);
        double ep = Math.sqrt( (asq - bsq)/bsq);
        double p = Math.sqrt( Math.pow(x,2) + Math.pow(y,2) );
        double th = Math.atan2(a*z, b*p);

        double lon = Math.atan2(y,x);
        double lat = Math.atan2( (z + Math.pow(ep,2)*b*Math.pow(sin(th),3) ), (p - esq*a*Math.pow(Math.cos(th),3)) );
        double N = a/( Math.sqrt(1-esq*Math.pow(sin(lat),2)) );
        double alt = p / Math.cos(lat) - N;

        // mod lat to 0-2pi
        lon = lon % (2*Math.PI);

        // correction for altitude near poles left out.

        double[] ret = {lat, lon, alt};

        return ret;
    }


    public static double[] lla2ecef(double[] lla){
        double lat = lla[0];
        double lon = lla[1];
        double alt = lla[2];

        double N = a / Math.sqrt(1 - esq * Math.pow(sin(lat),2) );

        double x = (N+alt) * Math.cos(lat) * Math.cos(lon);
        double y = (N+alt) * Math.cos(lat) * sin(lon);
        double z = ((1-esq) * N + alt) * sin(lat);

        double[] ret = {x, y, z};
        return ret;
    }

    /*
    timbo [8:02 PM]
Here's the code that converts two ECEF points, one being the reference, and generates ENU or NE-U:

[8:02]
//ENU
       double X = -(eX-eXR)*sin(lonRef)
                  +(eY-eYR)*cos(lonRef);

       double Y = -(eX-eXR)*sin(latRef)*cos(lonRef)
                  -(eY-eYR)*sin(latRef)*sin(lonRef)
                  +(eZ-eZR)*cos(latRef);
       double Z =  (eX-eXR)*cos(latRef)*cos(lonRef)
                  +(eY-eYR)*cos(latRef)*sin(lonRef)
                  +(eZ-eZR)*sin(latRef);

[8:04]
eX, eY, eZ = point of interest, possibly converted from LLA;   eXR, eYR, eZR = reference point in ECEF, possibl converted from LLA

[8:04]
(X, Y, Z)  == (E, N, U)
     */

    public static double[] ecef2ned(double[] ecef, double lat, double lon, double alt) {

        double slat = sin(lat);
        double clat = cos(lat);
        double slon = sin(lon);
        double clon = cos(lon);

        double r00 = -slat * clon;
        double r01 = -slat * slon;
        double r02 = clat;

        double r10 = -slon;
        double r11 = clon;
        double r12 = 0;

        double r20 = -clat * clon;
        double r21 = -clat * slon;
        double r22 = -slat;

        double n = r00 * ecef[0] + r01 * ecef[1] + r02 * ecef[2];
        double e = r10 * ecef[0] + r11 * ecef[1] + r12 * ecef[2];
        double d = r20 * ecef[0] + r21 * ecef[1] + r22 * ecef[2];

        return new double[] {n, e, d};
    }



}

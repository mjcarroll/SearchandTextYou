package com.nuspatial.geoqar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by michael on 2/26/17.
 */

public class DetailFragment extends Fragment {

    private String mStr;
    public DetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.detail_view, container, false);
        TextView text = (TextView) v.findViewById(R.id.textView);
        if(mStr != null)
            text.setText(mStr);
        return v;
    }

    public void setPOI(PointOfInterest point) {
        mStr = new String();
        mStr += "Trigage Found: " + point.mId + "\n";
        mStr += "  Status: " + point.mStatus + "\n";
        mStr += "  Point: " + point.mPoint.toString() + "\n";
    }
}

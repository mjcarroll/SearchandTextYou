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

    public DetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.detail_view, container, false);
        return v;
    }

    public void setPOI(PointOfInterest point) {
        String str = new String();
        str += "Trigage Found: " + point.mId + "\n";
        str += "  Status: " + point.mStatus + "\n";
        str += "  Point: " + point.mPoint.toString() + "\n";
        TextView mText = (TextView) getView().findViewById(R.id.textView);
        mText.setText(str);
    }
}

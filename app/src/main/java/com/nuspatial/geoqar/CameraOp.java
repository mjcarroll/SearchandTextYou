package com.nuspatial.geoqar;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by michael on 2/25/17.
 */

// Horrifically deprecated, but hey, hackathon: http://stackoverflow.com/a/7607759

public class CameraOp extends SurfaceView  implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private Camera camera;

    public CameraOp(Context context, SurfaceView view) {
        super(context);

        mSurfaceView = view;
        mSurfaceHolder = this.mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

        Camera.Parameters parameters = camera.getParameters();
        camera.setParameters(parameters);
        camera.startPreview();
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }
}

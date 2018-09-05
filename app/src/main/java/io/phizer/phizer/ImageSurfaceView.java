package io.phizer.phizer;

        import android.hardware.Camera;
        import android.content.Context;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;

        import java.io.IOException;

/**
 * Created by Ben on 5/19/17.
 */

public class ImageSurfaceView extends SurfaceView  implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    // Called when surface is rotated
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
       /* this.camera.stopPreview();
        Camera.Parameters parameters = this.camera.getParameters();
        Display getOrient = getWindowManager().getDefaultDisplay().getRotation();
        int orientation=this.getResources().getConfiguration().orientation;

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(height, width);
            this.camera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(height, width);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            this.camera.setDisplayOrientation(180);
        }

        this.camera.setParameters(parameters);
        try {
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();
        } catch(Exception e) {

        }
*/

    // }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        // start preview with new settings
        try {
            this.camera.setPreviewDisplay(surfaceHolder);
            this.camera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.camera.stopPreview();
        this.camera.release();
    }
}

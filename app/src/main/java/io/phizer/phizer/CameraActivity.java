package io.phizer.phizer;

        import android.Manifest;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.ActivityInfo;
        import android.content.pm.PackageManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.hardware.Camera;
        import android.hardware.Camera.PictureCallback;
        import android.hardware.SensorManager;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.constraint.ConstraintLayout;
        import android.support.constraint.ConstraintSet;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.Display;
        import android.view.OrientationEventListener;
        import android.view.View;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.widget.TextView;

        import java.io.FileOutputStream;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.List;


public class CameraActivity extends AppCompatActivity {

    private ImageSurfaceView previewView;
    private Camera camera;
    private ConstraintLayout parentLayout;
    private FrameLayout cameraLayout;
    private TextView photoCountLabel;
    private int photoCount;
    private OrientationEventListener mOrientationListener;
    private int orientationn;
    private Button cameraButton;
    private String rootDirectory;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoCount = 0;

        // Detect Orientation Changes
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                orientationn = orientation;
            }
        };
        mOrientationListener.enable();

        // Get the root directory from the intent
        Intent intent = getIntent();
        rootDirectory = intent.getStringExtra("path");

        // Sets the view to be fullscreen, hiding navigation and status bars
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              //  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*ActionBar actionBar = getSupportActionBar(); //or getSupportActionBar();
        actionBar.hide();*/

        // Set the content view
        setContentView(R.layout.activity_camera);


        // set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // attach fields to activity XML
        cameraLayout = (FrameLayout)findViewById(R.id.previewLayout);
        parentLayout = (ConstraintLayout)findViewById(R.id.parentLayout);
        photoCountLabel = (TextView)findViewById(R.id.photoCounter);
        cameraButton = (Button)findViewById(R.id.button);
        backButton = (Button)findViewById(R.id.doneButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CameraActivity.this,MainActivity.class);
                i.putExtra("path",rootDirectory);
                startActivity(i);
            }
        });
        camera = checkDeviceCamera();
        if (camera != null) {
            setupCamera();
        }
    }

    public void goBack() {

    }
    // Assumes this.camera != null
    public void setupCamera() {

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        previewView = new ImageSurfaceView(CameraActivity.this, camera);

        //set camera to continually auto-focus
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        //add in the camera layout
        cameraLayout.addView(previewView);
        List<Camera.Size> picSizes = params.getSupportedPictureSizes();
        List<Camera.Size> previews = params.getSupportedPreviewSizes();

        // check the sizes
        Camera.Size bestRes = picSizes.get(0);
        long bestTotalRes = picSizes.get(0).width * picSizes.get(0).height;

        // Find the camera size that captures the most "stuff" aka most pixels. If two are the same
        // pick the one with more width.
        for (int i = 1; i < picSizes.size(); i++) {
            long res = picSizes.get(i).width * picSizes.get(i).height;
            if (bestTotalRes < res || (bestTotalRes == res && picSizes.get(i).width > bestRes.width)) {
                bestRes = picSizes.get(i);
                bestTotalRes = res;
            }
        }
        camera.setDisplayOrientation(90);
        params.setPictureSize(bestRes.width, bestRes.height);

        // Now get the corresponding preview size that matches the picture size
        double ratio = (double) bestRes.width / bestRes.height;
        bestRes = null;
        bestTotalRes = 0;
        for (int i = 0; i < previews.size(); i++) {
            double ratio2 = (double) previews.get(i).width / previews.get(i).height;
            if (ratio == ratio2 && previews.get(i).width * previews.get(i).height > bestTotalRes) {
                bestRes = previews.get(i);
                bestTotalRes = previews.get(i).width * previews.get(i).height;
            }
        }
        if (bestRes != null) {
            params.setPreviewSize(bestRes.width, bestRes.height);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            double newHeight = width * ratio;

            // setup the constraints of the preview image

            ConstraintSet set = new ConstraintSet();

            // Set Height
            set.constrainHeight(R.id.previewLayout, (int) newHeight);

            // Center Vertically
            set.centerVertically(R.id.previewLayout, R.id.parentLayout);

            // pin to the left and right sides
            set.connect(R.id.previewLayout, ConstraintSet.LEFT, R.id.parentLayout, ConstraintSet.LEFT, 0);
            set.connect(R.id.previewLayout, ConstraintSet.RIGHT, R.id.parentLayout, ConstraintSet.RIGHT, 0);

            set.applyTo(parentLayout);
        }


        camera.setParameters(params);
    }

    // Takes a picture, disables button
    public void captureImage(View v) {
        v.setEnabled(false);
        camera.takePicture(null, null, pictureCallback);
    }

    // Requests camera permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            new AlertDialog.Builder(CameraActivity.this)
                    .setTitle("Error")
                    .setMessage("Please allow camera permissions")
                    .setCancelable(false)
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // ask for permission again
                            checkDeviceCamera();

                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // oh well

                        }
                    })
                    .show();
        } else {
            this.camera = checkDeviceCamera();
            setupCamera();
        }
    }
    private Camera checkDeviceCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
            return null;
        } else {
            Camera cam = null;

            try {
                cam = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return cam;
        }


    }

    // Called after image is taken
    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // this is a JPEG callback
            try {
                // Write to file
                final byte[] newData = data;

                // Handle image rotation and writing in a background thread
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap bitmapOrg = BitmapFactory.decodeByteArray(newData,0,newData.length);

                            // rotate the image to proper orientation, but only if needed
                            if (getProperRotation() != 0) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(getProperRotation());
                                Bitmap rotated = Bitmap.createBitmap(bitmapOrg, 0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true);
                                bitmapOrg = rotated;
                            }

                            // get path then write to file
                            String filePath = createImageFilePath();
                            FileOutputStream outStream = new FileOutputStream(filePath);
                            bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                            outStream.close();
                        } catch (Exception e) {

                        }

                    }
                });

                photoCount++;
                photoCountLabel.setText("Photos Taken: " + photoCount);

                // Adjust orientation

                Log.d("test", "" + orientationn);
                camera.startPreview();
                cameraButton.setEnabled(true);
            } catch (Exception e) {

            }



        }
    };

  /*  private void pushToScreen(String path) {
        Intent intent = new Intent(this, ZoomingImageView.class);

        intent.putExtra("com.nbm.phizer.imagepath", path);
        mOrientationListener.disable();
        startActivity(intent);
    }*/

    // Returns how the image should be rotated
    private int getProperRotation() {
        if (orientationn > 315 || (orientationn >= 0 && orientationn < 45)) {
            return 90;
        } else if (orientationn >= 45 && orientationn < 135) {
            return 180;
        } else if (orientationn >= 135 && orientationn < 225) {
            return 270;
        } else {
            return 0;
        }

    }


    // Returns a file path for the image
    private String createImageFilePath() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "/IMG_" + timeStamp + ".jpg";

        // If root directory not specified, save in root files directory, otherwise, return proper
        // place
        if (rootDirectory == null) {
            return String.format(getFilesDir().getAbsolutePath() + imageFileName);
        } else {
            return rootDirectory + imageFileName;
        }
    }
}

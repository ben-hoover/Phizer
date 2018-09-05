package io.phizer.phizer;

import android.content.Context;
import android.content.IntentSender;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Stack;

public class CreateGDriveDirectories extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    // GoogleApiClient mGoogleApiClient;
    final String TAG = "FUCKTHIS";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final int REQUEST_CODE_OPENER = 2;
    private GoogleApiClient mGoogleApiClient;
    private boolean fileOperation = false;
    private DriveId mFileId;
    private DriveId mParentId;
    private String mPath;
    public DriveFile file;
    private Stack<String> imgPaths;
    private Stack<String> foldPaths;
    private Context mContext;


    public CreateGDriveDirectories(Context mContext, String mPath, DriveId mParentId) {
        this.imgPaths=imgPaths;
        this.mContext = mContext;
        this.mParentId=mParentId;
        this.mPath=mPath;
        getFileList();
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mGoogleApiClient.connect();

    }

    final private ResultCallback<DriveFolder.DriveFolderResult> folderCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    mFileId = result.getDriveFolder().getDriveId();
                    if (foldPaths!=null) {
                        for (String fold : foldPaths) {
                            new CreateGDriveDirectories(mContext, fold, mFileId);
                        }
                    }
                    if (imgPaths!=null) {
                        for (String ipath : imgPaths) {
                            new PushToGDrive(ipath, mContext, mFileId).init();
                        }
                    }
                }
            };

    public DriveId getId() {
        return mFileId;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Connected");
        MetadataChangeSet changeSet;
        DriveFolder df;
        if (mParentId!=null){
            df=Drive.DriveApi.getFolder(mGoogleApiClient,mParentId);
            changeSet = new MetadataChangeSet.Builder()
                    .setTitle(mPath.substring(mPath.lastIndexOf("/")+1))
                    .build();
        }else {
            df =Drive.DriveApi.getRootFolder(mGoogleApiClient);
            if (!mPath.equals(mContext.getFilesDir().getPath())){
                changeSet = new MetadataChangeSet.Builder()
                        .setTitle(mPath.substring(mPath.lastIndexOf("/")+1))
                        .build();
            } else {
                changeSet = new MetadataChangeSet.Builder()
                        .setTitle("Phizer")
                        .build();
            }
        }
        df.createFolder(mGoogleApiClient, changeSet).setResultCallback(folderCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection suspended");
    }

    private void getFileList(){
        List<String> allFiles = new ContentScraper(mPath).getFiles();
        imgPaths = new Stack<String>();
        foldPaths = new Stack<String>();
        if (allFiles!=null) {
            for (String path : allFiles) {
                if (path.length()>4&&path.substring(path.length() - 4).equals(".jpg")) {
                    imgPaths.push(mPath+"/"+path);
                } else {
                    foldPaths.push(mPath+"/"+path);
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public void onClickCreateFile() {

        fileOperation = true;
        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);

    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if (result.getStatus().isSuccess()) {
                        if (fileOperation == true) {

                            CreateFileOnGoogleDrive(result);

                        }
                    }
                }
            };
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {

                        Log.d(TAG, "file created");

                    }

                    return;

                }
            };

    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result) {

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write("TEST");
                    writer.close();

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("MyFile")
                        .setMimeType("text/plain")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
    }
}


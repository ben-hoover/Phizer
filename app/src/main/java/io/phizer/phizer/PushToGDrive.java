package io.phizer.phizer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;



public class PushToGDrive implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    // GoogleApiClient mGoogleApiClient;
    final String TAG = "FUCKTHIS";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final int REQUEST_CODE_OPENER = 2;
    private GoogleApiClient mGoogleApiClient;
    private boolean fileOperation = false;
    private DriveId mFileId;
    public DriveFile file;
    private List<String> imageData;
    private String imagePath;
    private String mPath;
    public Context mContext;
    public DriveFolder phizerFolder;
    public DriveApi.DriveContentsResult drivecontentsResult;
    private DriveId rID;

    public PushToGDrive(String imagePath, Context mContext,DriveId rID) {
        this.mContext = mContext;
        this.imagePath = imagePath;
        this.rID=rID;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d(TAG, "constructor run");
        mGoogleApiClient.connect();
    }

    public void init() {
        Log.d(TAG, "init run");
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsCallback);


    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            drivecontentsResult=driveContentsResult;
            //Drive.DriveApi.fetchDriveId(mGoogleApiClient,rID).setResultCallback(phizerCallback);
            onresult();
        }

};
        //@Override
        public void onresult() {
            Log.d(TAG,"result run");
            if (!drivecontentsResult.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create new file contents");
                return;
            }

            final DriveContents driveContents = drivecontentsResult.getDriveContents();
            try {
                final OutputStream outputStream = driveContents.getOutputStream();


                try {
                    InputStream inputStream = new FileInputStream(new File(imagePath).toString());

                    if (inputStream != null) {
                        byte[] data = new byte[1024];
                        while (inputStream.read(data) != -1) {
                            outputStream.write(data);
                        }
                        inputStream.close();
                    }


                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(imagePath.substring(imagePath.lastIndexOf("/") + 1))
                        .setMimeType("image/jpg")
                        .setStarred(true).build();

                Drive.DriveApi.getFolder(mGoogleApiClient,rID).createFile(mGoogleApiClient, changeSet, driveContents).setResultCallback(fileCallback);


                outputStream.close();
            }catch(IOException e){
                Log.e(TAG,e.getMessage());
            }
        }


    final private ResultCallback<DriveApi.DriveIdResult> phizerCallback = new ResultCallback<DriveApi.DriveIdResult>(){

        @Override
        public void onResult(DriveApi.DriveIdResult driveIdResult) {
            phizerFolder=Drive.DriveApi.getFolder(mGoogleApiClient,(DriveId)driveIdResult);
            onresult();
        }

    };
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {

                        Log.d(TAG,"file created");

                    }
                    mGoogleApiClient.connect();
                    return;

                }
            };

}

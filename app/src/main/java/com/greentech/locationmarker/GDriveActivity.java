/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greentech.locationmarker;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

import org.json.JSONArray;
import org.json.JSONException;

/**
    Customized by Cyril Mathew
    For Use with Green Tech App
*/


/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
public class GDriveActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private static GoogleApiClient mGoogleApiClient;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private StringBuffer buffer;
    private JSONArray gArray;
    private Bitmap mBitmapToSave;
    private static boolean savedYet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        savedYet = false;
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final File file = new File(getFilesDir(), "geoJSON.json");
        savedYet = true;

        try
        {
            fis = openFileInput(file.getName());
            bis = new BufferedInputStream(fis);
            buffer = new StringBuffer();

            while (bis.available() != 0) {
                char c = (char) bis.read();
                buffer.append(c);
            }

            bis.close();
            fis.close();

            gArray = new JSONArray(buffer.toString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

        //final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(DriveContentsResult result)
                    {
                        // If the operation was not successful, we cannot do anything
                        // and must fail.
                        if (!result.getStatus().isSuccess())
                        {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }

                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");

                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();

                        // Write the bitmap data from it.
                       // ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        //image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);

                        try
                        {
                            //outputStream.write(bitmapStream.toByteArray());
                            outputStream.write(gArray.toString().getBytes());
                        }
                        catch (IOException e1){Log.i(TAG, "Unable to write file contents.");}

                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("application/json").setTitle("geoJSON.json").build();

                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);

                        try {
                            startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        if(savedYet) finish();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "File successfully saved.");
                    //mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                           // REQUEST_CODE_CAPTURE_IMAGE);

                    finish();

                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if(!savedYet)
        {
            saveFileToDrive();
        }
        else if(savedYet) {
            savedYet = false;
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
}
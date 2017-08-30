package com.parse;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;


public class BuddyLocationRequestPermissionActivity extends FragmentActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity start");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {

            // the app has requested this permission previously and the user denied the request.
            PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity shouldShowRequestPermissionRationale true");

        } else {

            // the app has yet to prompt
            PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity shouldShowRequestPermissionRationale false");

            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startService() {
        BuddyAltDataTracker.getInstance().setupServices();

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity onRequestPermissionsResult");
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity onRequestPermissionsResult PERMISSION_GRANTED");
            } else {
                PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity onRequestPermissionsResult NOT PERMISSION_GRANTED");
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                        PERMISSION_REQUEST_CODE);
            }
        }

        startService();
    }
}
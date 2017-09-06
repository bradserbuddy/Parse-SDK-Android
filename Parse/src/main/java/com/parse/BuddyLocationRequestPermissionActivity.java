package com.parse;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


public class BuddyLocationRequestPermissionActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final static int PERMISSION_REQUEST_CODE = 0;

    public static void invoke(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent permissionIntent = new Intent(context, BuddyLocationRequestPermissionActivity.class);
            permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(permissionIntent);
        } else {
            BuddyAltDataTracker.getInstance().setup(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity onCreate");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            BuddyAltDataTracker.getInstance().setup(false);

            this.finish();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                this.showRationaleDialog();
            } else {
                this.requestPermissions();
            }
        }
    }

    private void showRationaleDialog() {
        (new AlertDialog.Builder(this))
                .setMessage(R.string.request_location_permission_rationale)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions();
                    }
                })
                .create()
                .show();
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(
            this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PLog.i(BuddyAltDataTracker.TAG, "BuddyLocationRequestPermissionActivity onRequestPermissionsResult");
        if (requestCode == PERMISSION_REQUEST_CODE) {
            BuddyAltDataTracker.getInstance().setup(true);
        }

        this.finish();
    }
}
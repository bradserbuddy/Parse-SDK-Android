package com.parse;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


class BuddyUploadCriteria {
    private BuddyPowerConnectionStatus powerStatus = BuddyPowerConnectionStatus.Unknown;
    private BuddyConnectivityStatus connectivityStatus = BuddyConnectivityStatus.Unknown;
    private boolean hasEnoughBattery = false;
    private static boolean isUploading = false;
    private static int uploadJobsCount = 0;
    private final int milliSecondsPerDay = 24 * 60 * 60 * 1000;
    private BuddyPreferences preferences = new BuddyPreferences();
    private BuddyConfiguration configuration;
    public static final String TAG = "com.parse.BuddyUploadCriteria";

    BuddyUploadCriteria() {
        configuration = preferences.getConfig();
        long lastUploaded = configuration.getLastUploadedEpoch();

        if (lastUploaded == 0) {
            // first time, so save the current time.
            configuration = preferences.updateLastUploadedEpoch(System.currentTimeMillis());
        }
    }

    int getBatteryPercentage() {
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = Parse.getApplicationContext().registerReceiver(null, batteryIntentFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);

        return Math.round((level / (float)scale) * 100);
    }

    void setHasEnoughBattery(String intentAction) {
        hasEnoughBattery = intentAction.equals(Intent.ACTION_BATTERY_OKAY);
    }

    private boolean getHasEnoughBattery() {
        return hasEnoughBattery || getBatteryPercentage() > 20;
    }

    BuddyConnectivityStatus getConnectivityStatus() {
        return connectivityStatus;
    }

    void setConnectivityStatus(BuddyConnectivityStatus status) {
        connectivityStatus = status;
    }

    BuddyPowerConnectionStatus getPowerStatus() {
        return powerStatus;
    }

    void setPowerStatus(BuddyPowerConnectionStatus status) {
        powerStatus = status;
    }

    public boolean canUpload() {
        boolean result = false;

        if (!isUploading) {
            if (System.currentTimeMillis() > configuration.getLastUploadedEpoch() + milliSecondsPerDay) {
                if (connectivityStatus == BuddyConnectivityStatus.WifiConnected ||
                        connectivityStatus == BuddyConnectivityStatus.CellularConnected) {
                    if (powerStatus == BuddyPowerConnectionStatus.Connected || getHasEnoughBattery()) {
                        result = true;
                    }
                }
            }
            else {
                // within 24 hours
                if (powerStatus == BuddyPowerConnectionStatus.Connected) {
                    if (connectivityStatus == BuddyConnectivityStatus.WifiConnected) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    public void updateInitialPowerStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = Parse.getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) {
            setPowerStatus(BuddyPowerConnectionStatus.Connected);
        }
        else {
            setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
        }
    }

    public synchronized void startUpload() {
        uploadJobsCount++;
        if (!isUploading) {
            isUploading = true;
        }
        PLog.i(TAG, "startUpload - isUploading = " + String.valueOf(isUploading) + ", jobs = " + String.valueOf(uploadJobsCount));
    }

    public synchronized void endUpload() {
        configuration = preferences.updateLastUploadedEpoch(System.currentTimeMillis());
        if (isUploading) {
            uploadJobsCount--;
            if (uploadJobsCount == 0) {
                isUploading = false;
            }
        }
        else {
            uploadJobsCount = 0;
        }

        PLog.i(TAG, "endUpload - isUploading = " + String.valueOf(isUploading) + ", jobs = " + String.valueOf(uploadJobsCount));
    }
}

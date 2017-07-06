package com.parse;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;


class BuddyUploadCriteria {
    private BuddyPowerConnectionStatus powerStatus = BuddyPowerConnectionStatus.Unknown;
    private BuddyConnectivityStatus connectivityStatus = BuddyConnectivityStatus.Unknown;
    private boolean hasEnoughBattery = false;
    private static int uploadJobsCount = 0;
    private final int milliSecondsPerDay = 24 * 60 * 60 * 1000;
    public static final String TAG = "com.parse.BuddyUploadCriteria";

    BuddyUploadCriteria ()
    {
        uploadJobsCount = 0;
    }

    public int getBatteryPercentage(Context context) {
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryIntentFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);

        return Math.round((level / (float)scale) * 100);
    }

    public void setHasEnoughBattery(String intentAction) {
        hasEnoughBattery = intentAction.equals(Intent.ACTION_BATTERY_OKAY);
    }

    public boolean getHasEnoughBattery(Context context) {
        return hasEnoughBattery || getBatteryPercentage(context) > 20;
    }

    public BuddyConnectivityStatus getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(BuddyConnectivityStatus status) {
        connectivityStatus = status;
    }

    public BuddyPowerConnectionStatus getPowerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(BuddyPowerConnectionStatus status) {
        powerStatus = status;
    }

    public boolean canUpload(Context context, BuddyConfiguration configuration) {
        boolean result = false;

        if (uploadJobsCount == 0) {
            if (System.currentTimeMillis() > configuration.getLastUploadedEpoch() + milliSecondsPerDay) {
                if (connectivityStatus == BuddyConnectivityStatus.WifiConnected ||
                        connectivityStatus == BuddyConnectivityStatus.CellularConnected) {
                    if (powerStatus == BuddyPowerConnectionStatus.Connected || getHasEnoughBattery(context)) {
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

    public void updateInitialPowerStatus(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) {
            setPowerStatus(BuddyPowerConnectionStatus.Connected);
        }
        else {
            setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
        }
    }

    public synchronized int startUpload() {
        uploadJobsCount++;
        PLog.i(TAG, "startUpload - jobs = " + String.valueOf(uploadJobsCount));

        return uploadJobsCount;
    }

    public synchronized int endUpload(Context context) {
        BuddyPreferenceService.updateLastUploadedEpoch(context, System.currentTimeMillis());
        uploadJobsCount--;

        PLog.i(TAG, "endUpload - jobs = " + String.valueOf(uploadJobsCount));
        return uploadJobsCount;
    }
}

package com.parse;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;


class BuddyUploadCriteria {
    private BuddyPowerConnectionStatus powerStatus = BuddyPowerConnectionStatus.Unknown;
    private BuddyConnectivityStatus connectivityStatus = BuddyConnectivityStatus.Unknown;
    private boolean hasEnoughBattery = false;
    private boolean isUploading = false;
    private long lastUploadedEpoch = 0;
    private final int milliSecondsPerDay = 24 * 60 * 60 * 1000;
    private final String PREFS_BUDDY_LOCATION_TRACKER = "BuddyPreferences 09977e39-d487-48fb-97f1-1c018ea5e095"; // UUID to prevent name collision
    private final String PREF_LAST_UPLOADED_EPOCH = "lastUploadedEpoch";
    private SharedPreferences sharedPreferences;

    BuddyUploadCriteria() {
        sharedPreferences = Parse.getApplicationContext().getSharedPreferences(PREFS_BUDDY_LOCATION_TRACKER, 0);

        long lastUploaded = sharedPreferences.getLong(PREF_LAST_UPLOADED_EPOCH, 0);
        if (lastUploaded == 0) {
            // first time, so save the current time.
            setLastUploadedEpoch(System.currentTimeMillis());
        }
        else {
            lastUploadedEpoch = lastUploaded;
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

    boolean canUpload() {
        boolean result = false;

        if (!isUploading) {
            if (System.currentTimeMillis() > getLastUploadedEpoch() + milliSecondsPerDay) {
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

    void startUpload() {
        isUploading = true;
    }

    void endUpload() {
        setLastUploadedEpoch(System.currentTimeMillis());
        isUploading = false;
    }

    private long getLastUploadedEpoch() {
        return lastUploadedEpoch;
    }

    private void setLastUploadedEpoch(long lastUploaded) {
        lastUploadedEpoch = lastUploaded;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_LAST_UPLOADED_EPOCH, lastUploaded);
        editor.apply();
    }
}

package com.parse;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;


class BuddyUploadCriteria {
    private BuddyPowerConnectionStatus powerStatus = BuddyPowerConnectionStatus.Unknown;
    private BuddyWifiConnectivityStatus wifiConnectivityStatus = BuddyWifiConnectivityStatus.Unknown;
    private BuddyCellularConnectivityStatus cellularConnectivityStatus = BuddyCellularConnectivityStatus.Unknown;
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
        BuddyConnectivityStatus connectivityStatus = BuddyConnectivityStatus.Unknown;

        if (cellularConnectivityStatus != BuddyCellularConnectivityStatus.Connected &&
                wifiConnectivityStatus != BuddyWifiConnectivityStatus.Connected) {
            connectivityStatus = BuddyConnectivityStatus.Disconnected;
        }
        else if (cellularConnectivityStatus != BuddyCellularConnectivityStatus.Connected &&
                wifiConnectivityStatus == BuddyWifiConnectivityStatus.Connected) {
            connectivityStatus = BuddyConnectivityStatus.Wifi;
        }
        else if (cellularConnectivityStatus == BuddyCellularConnectivityStatus.Connected &&
                wifiConnectivityStatus != BuddyWifiConnectivityStatus.Connected) {
            connectivityStatus = BuddyConnectivityStatus.Cellular;
        }
        else if (cellularConnectivityStatus == BuddyCellularConnectivityStatus.Connected &&
                wifiConnectivityStatus == BuddyWifiConnectivityStatus.Connected) {
            connectivityStatus = BuddyConnectivityStatus.CellularAndWifi;
        }

        return connectivityStatus;
    }

    public BuddyPowerConnectionStatus getPowerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(BuddyPowerConnectionStatus status) {
        powerStatus = status;
    }

    public boolean canUpload(Context context, BuddyConfiguration configuration) {
        boolean result = false;
        BuddyConnectivityStatus connectivityStatus = getConnectivityStatus();

        if (uploadJobsCount == 0) {
            if ((powerStatus == BuddyPowerConnectionStatus.Connected && connectivityStatus == BuddyConnectivityStatus.Wifi) || (System.currentTimeMillis() > configuration.getLastUploadedEpoch() + milliSecondsPerDay && (connectivityStatus == BuddyConnectivityStatus.Wifi ||
                    connectivityStatus == BuddyConnectivityStatus.Cellular || connectivityStatus == BuddyConnectivityStatus.CellularAndWifi) && (powerStatus == BuddyPowerConnectionStatus.Connected || getHasEnoughBattery(context)))) {
                // wifi and power connected, or
                // long time since last upload, connected to network and power or has enough battery
                result = true;
                PLog.i(TAG, "in canUpload true");
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

    public void setWifiConnectivityStatus(BuddyWifiConnectivityStatus wifiConnectivityStatus) {
        this.wifiConnectivityStatus = wifiConnectivityStatus;
    }

    public void setCellularConnectivityStatus(BuddyCellularConnectivityStatus cellularConnectivityStatus) {
        this.cellularConnectivityStatus = cellularConnectivityStatus;
    }
}

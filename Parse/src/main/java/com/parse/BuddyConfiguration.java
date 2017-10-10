package com.parse;

public class BuddyConfiguration {
    private long version;
    private long commonLocationPushBatchSize;
    private long commonCellularPushBatchSize;
    private long commonMaxLocationRecords;
    private long commonMaxCellularRecords;
    private long commonMaxErrorRecords;
    private long commonMaxBatteryRecords;
    private long commonMaxRecordsToDelete;
    private long androidLocationPowerAccuracy;
    private long androidLocationFastestUpdateIntervalMs;
    private long androidLocationUpdateIntervalMs;
    private boolean androidLogCellular;
    private boolean androidLogLocation;
    private boolean androidLogBattery;
    private boolean androidUploadCellular;
    private boolean androidUploadLocation;
    private boolean androidUploadBattery;
    private long androidCellularLogTimeoutMs;
    private long lastUploadedEpoch;
    private long androidActivityMonitoringTimeoutMs;
    private long commonBatteryPushBatchSize;
    private long androidBatteryLogTimeoutMs;
    private long androidUploadTimeoutMinutes;
    public static final String TAG = "com.parse.BuddyConfiguration";

    public long getCommonCellularPushBatchSize() {
        return commonCellularPushBatchSize;
    }

    public void setCommonCellularPushBatchSize(long commonCellularPushBatchSize) {
        this.commonCellularPushBatchSize = commonCellularPushBatchSize;
    }

    public long getAndroidCellularLogTimeoutMs() {
        return androidCellularLogTimeoutMs;
    }

    public long getAndroidTimeout() {
        long timeout = androidCellularLogTimeoutMs;

        if (androidBatteryLogTimeoutMs < androidCellularLogTimeoutMs) {
            timeout = androidBatteryLogTimeoutMs;
        }

        long uploadTimeoutMs = androidUploadTimeoutMinutes*60*1000;
        if (uploadTimeoutMs < timeout) {
            timeout = uploadTimeoutMs;
        }
        PLog.i(TAG, "timer set to " + timeout);
        return timeout;
    }

    public void setAndroidCellularLogTimeoutMs(long androidCellularLogTimeoutMs) {
        this.androidCellularLogTimeoutMs = androidCellularLogTimeoutMs;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getCommonLocationPushBatchSize() {
        return commonLocationPushBatchSize;
    }

    public void setCommonLocationPushBatchSize(long commonLocationPushBatchSize) {
        this.commonLocationPushBatchSize = commonLocationPushBatchSize;
    }

    public long getAndroidLocationPowerAccuracy() {
        return androidLocationPowerAccuracy;
    }

    public void setAndroidLocationPowerAccuracy(long androidLocationPowerAccuracy) {
        this.androidLocationPowerAccuracy = androidLocationPowerAccuracy;
    }

    public long getAndroidLocationFastestUpdateIntervalMs() {
        return androidLocationFastestUpdateIntervalMs;
    }

    public void setAndroidLocationFastestUpdateIntervalMs(long androidLocationFastestUpdateIntervalMs) {
        this.androidLocationFastestUpdateIntervalMs = androidLocationFastestUpdateIntervalMs;
    }

    public long getAndroidLocationUpdateIntervalMs() {
        return androidLocationUpdateIntervalMs;
    }

    public void setAndroidLocationUpdateIntervalMs(long androidLocationUpdateIntervalMs) {
        this.androidLocationUpdateIntervalMs = androidLocationUpdateIntervalMs;
    }

    public boolean shouldLogCellular() {
        return androidLogCellular;
    }

    public void setLogCellular(boolean androidLogCellular) {
        this.androidLogCellular = androidLogCellular;
    }

    public boolean shouldLogLocation() {
        return androidLogLocation;
    }

    public void setLogLocation(boolean androidLogLocation) {
        this.androidLogLocation = androidLogLocation;
    }

    public boolean shouldUploadCellular() {
        return androidUploadCellular;
    }

    public void setUploadCellular(boolean androidUploadCellular) {
        this.androidUploadCellular = androidUploadCellular;
    }

    public boolean shouldUploadLocation() {
        return androidUploadLocation;
    }

    public void setUploadLocation(boolean androidUploadLocation) {
        this.androidUploadLocation = androidUploadLocation;
    }

    public long getLastUploadedEpoch() {
        return lastUploadedEpoch;
    }

    public void setLastUploadedEpoch(long lastUploadedEpoch) {
        this.lastUploadedEpoch = lastUploadedEpoch;
    }

    public long getCommonMaxLocationRecords() {
        return commonMaxLocationRecords;
    }

    public void setCommonMaxLocationRecords(long commoonMaxLocationRecords) {
        this.commonMaxLocationRecords = commoonMaxLocationRecords;
    }

    public long getCommonMaxCellularRecords() {
        return commonMaxCellularRecords;
    }

    public void setCommonMaxCellularRecords(long commonMaxCellularRecords) {
        this.commonMaxCellularRecords = commonMaxCellularRecords;
    }

    public long getCommonMaxRecordsToDelete() {
        return commonMaxRecordsToDelete;
    }

    public void setCommonMaxRecordsToDelete(long commonMaxRecordsToDelete) {
        this.commonMaxRecordsToDelete = commonMaxRecordsToDelete;
    }

    public long getCommonMaxErrorRecords() {
        return commonMaxErrorRecords;
    }

    public void setCommonMaxErrorRecords(long commonMaxErrorRecords) {
        this.commonMaxErrorRecords = commonMaxErrorRecords;
    }

    public long getAndroidActivityMonitoringTimeoutMs() {
        return androidActivityMonitoringTimeoutMs;
    }

    public void setAndroidActivityMonitoringTimeoutMs(long androidActivityMonitoringTimeoutMs) {
        this.androidActivityMonitoringTimeoutMs = androidActivityMonitoringTimeoutMs;
    }

    public long getCommonBatteryPushBatchSize() {
        return commonBatteryPushBatchSize;
    }

    public void setCommonBatteryPushBatchSize(long commonBatteryPushBatchSize) {
        this.commonBatteryPushBatchSize = commonBatteryPushBatchSize;
    }

    public long getCommonMaxBatteryRecords() {
        return commonMaxBatteryRecords;
    }

    public void setCommonMaxBatteryRecords(long commonMaxBatteryRecords) {
        this.commonMaxBatteryRecords = commonMaxBatteryRecords;
    }

    public boolean shouldLogBattery() {
        return androidLogBattery;
    }

    public void setLogBattery(boolean androidLogBattery) {
        this.androidLogBattery = androidLogBattery;
    }

    public boolean shouldUploadBattery() {
        return androidUploadBattery;
    }

    public void setUploadBattery(boolean androidUploadBattery) {
        this.androidUploadBattery = androidUploadBattery;
    }

    public long getAndroidBatteryLogTimeoutMs() {
        return androidBatteryLogTimeoutMs;
    }

    public void setAndroidBatteryLogTimeoutMs(long androidBatteryLogTimeoutMs) {
        this.androidBatteryLogTimeoutMs = androidBatteryLogTimeoutMs;
    }

    public long getAndroidUploadTimeoutMinutes() {
        return androidUploadTimeoutMinutes;
    }

    public void setAndroidUploadTimeoutMinutes(long androidUploadTimeoutMinutes) {
        this.androidUploadTimeoutMinutes = androidUploadTimeoutMinutes;
    }
}

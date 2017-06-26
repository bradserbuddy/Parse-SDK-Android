package com.parse;

public class BuddyConfiguration {
    private long version;
    private long commonLocationPushBatchSize;
    private long commonCellularLogTimeout;
    private long commonCellularPushBatchSize;
    private long commonMaxLocationRecords;
    private long commonMaxCellularRecords;
    private long commonMaxRecordsToDelete;
    private long androidLocationPowerAccuracy;
    private long androidLocationFastestUpdateInterval;
    private long androidLocationUpdateInterval;
    private boolean androidLogCellular;
    private boolean androidLogLocation;
    private boolean androidUploadCellular;
    private boolean androidUploadLocation;
    private long lastUploadedEpoch;

    public long getCommonCellularPushBatchSize() {
        return commonCellularPushBatchSize;
    }

    public void setCommonCellularPushBatchSize(long commonCellularPushBatchSize) {
        this.commonCellularPushBatchSize = commonCellularPushBatchSize;
    }

    public long getCommonCellularLogTimeout() {
        return commonCellularLogTimeout;
    }

    public void setCommonCellularLogTimeout(long commonCellularLogTimeout) {
        this.commonCellularLogTimeout = commonCellularLogTimeout;
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

    public long getAndroidLocationFastestUpdateInterval() {
        return androidLocationFastestUpdateInterval;
    }

    public void setAndroidLocationFastestUpdateInterval(long androidLocationFastestUpdateInterval) {
        this.androidLocationFastestUpdateInterval = androidLocationFastestUpdateInterval;
    }

    public long getAndroidLocationUpdateInterval() {
        return androidLocationUpdateInterval;
    }

    public void setAndroidLocationUpdateInterval(long androidLocationUpdateInterval) {
        this.androidLocationUpdateInterval = androidLocationUpdateInterval;
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
}

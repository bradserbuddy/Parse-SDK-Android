package com.parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuddyPreferences {

    public static final String TAG = "com.parse.BuddyPreferences";
    private static final long  lastVersionDefault = 1;
    private static final long locationsBatchSizeDefault = 100;
    private static final long batteryBatchSizeDefault = 100;
    private static final long cellularBatchSizeDefault = 100;
    private static final long cellularLogTimeoutMsDefault = 600000;
    private static final long locationPowerAccuracyDefault = 102;
    private static final long locationUpdateIntervalDefault = 600000;
    private static final long locationFastestUpdateIntervalDefault = 60000;
    private static final long locationsMaxRecordsDefault = 100000;
    private static final long cellularMaxRecordsDefault = 100000;
    private static final long errorMaxRecordsDefault = 100000;
    private static final long batteryMaxRecordsDefault = 100000;
    private static final long maxRecordsToDeleteDefault = 1000;
    private static final long activityMonitoringTimeoutMsDefault = 600000;
    private static final boolean shouldLogLocationDefault = true;
    private static final boolean shouldLogCellularDefault = true;
    private static final boolean shouldUploadLocationDefault = true;
    private static final boolean shouldUploadCellularDefault = true;
    private static final boolean shouldLogBatteryDefault = true;
    private static final boolean shouldUploadBatteryDefault = true;
    private static final long batteryLogTimeoutMsDefault = 60000;
    private static final long uploadTimeoutMinutesDefault = 360;

    public static BuddyConfiguration getConfig(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
        BuddyConfiguration configuration = new BuddyConfiguration();

        long lastUploaded = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, 0);
        configuration.setLastUploadedEpoch(lastUploaded == 0 ? System.currentTimeMillis() : lastUploaded);

        long lastVersion = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigVersion, 0);
        configuration.setVersion(lastVersion == 0 ? lastVersionDefault : lastVersion);

        long locationsBatchSize = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, 0);
        configuration.setCommonLocationPushBatchSize(locationsBatchSize == 0 ? locationsBatchSizeDefault : locationsBatchSize);

        long batteryBatchSize = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigBatteryPushBatch, 0);
        configuration.setCommonBatteryPushBatchSize(batteryBatchSize == 0 ? batteryBatchSizeDefault : batteryBatchSize);

        long cellularBatchSize = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, 0);
        configuration.setCommonCellularPushBatchSize(cellularBatchSize == 0 ? cellularBatchSizeDefault : cellularBatchSize);

        long cellularLogTimeout = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, 0);
        configuration.setAndroidCellularLogTimeoutMs(cellularLogTimeout == 0 ? cellularLogTimeoutMsDefault : cellularLogTimeout);

        long locationPowerAccuracy = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, 0);
        configuration.setAndroidLocationPowerAccuracy(locationPowerAccuracy == 0 ? locationPowerAccuracyDefault : locationPowerAccuracy);

        long locationUpdateInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, 0);
        configuration.setAndroidLocationUpdateIntervalMs(locationUpdateInterval == 0 ? locationUpdateIntervalDefault : locationUpdateInterval);

        long locationFastestUpdateInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, 0);
        configuration.setAndroidLocationFastestUpdateIntervalMs(locationFastestUpdateInterval == 0 ? locationFastestUpdateIntervalDefault : locationFastestUpdateInterval);

        boolean shouldLogCellular = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, shouldLogCellularDefault);
        configuration.setLogCellular(shouldLogCellular);

        boolean shouldLogLocation = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, shouldLogLocationDefault);
        configuration.setLogLocation(shouldLogLocation);

        boolean shouldLogBattery = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigLogBattery, shouldLogBatteryDefault);
        configuration.setLogBattery(shouldLogBattery);

        boolean shouldUploadCellular = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, shouldUploadCellularDefault);
        configuration.setUploadCellular(shouldUploadCellular);

        boolean shouldUploadLocation = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, shouldUploadLocationDefault);
        configuration.setUploadLocation(shouldUploadLocation);

        boolean shouldUploadBattery = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadBattery, shouldUploadBatteryDefault);
        configuration.setUploadBattery(shouldUploadBattery);

        long locationsMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, 0);
        configuration.setCommonMaxLocationRecords(locationsMaxRecords == 0 ? locationsMaxRecordsDefault : locationsMaxRecords);

        long cellularMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, 0);
        configuration.setCommonMaxCellularRecords(cellularMaxRecords == 0 ? cellularMaxRecordsDefault : cellularMaxRecords);

        long errorMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, 0);
        configuration.setCommonMaxErrorRecords(errorMaxRecords == 0 ? errorMaxRecordsDefault : errorMaxRecords);

        long batteryMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigBatteryMaxRecords, 0);
        configuration.setCommonMaxBatteryRecords(batteryMaxRecords == 0 ? batteryMaxRecordsDefault : batteryMaxRecords);

        long maxRecordsToDelete = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, 0);
        configuration.setCommonMaxRecordsToDelete(maxRecordsToDelete == 0 ? maxRecordsToDeleteDefault : maxRecordsToDelete);

        long activityMonitoringInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorLogTimeoutMs, 0);
        configuration.setAndroidActivityMonitoringTimeoutMs(activityMonitoringInterval == 0 ? activityMonitoringTimeoutMsDefault : activityMonitoringInterval);

        long batteryLogTimeoutMs = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigBatteryLogTimeoutMs, 0);
        configuration.setAndroidBatteryLogTimeoutMs(batteryLogTimeoutMs == 0 ? batteryLogTimeoutMsDefault : batteryLogTimeoutMs);

        long uploadTimeoutMinutes = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigUploadTimeoutMinutes, 0);
        configuration.setAndroidUploadTimeoutMinutes(uploadTimeoutMinutes == 0 ? uploadTimeoutMinutesDefault : uploadTimeoutMinutes);

        return configuration;
    }

    public static void updateLastUploadedEpoch(Context context, long lastUploadedEpoch) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, lastUploadedEpoch);
        editor.apply();
    }

    private static long getValueWithDefault(JSONObject config, String key, long defaultValue) {
        long result = defaultValue;
        if (config.has(key)) {
            try {
                result = config.getLong(key);
            } catch (JSONException e) {
                // could be an invalid value
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
        else {
            BuddySqliteHelper.getInstance().logError(TAG, new Resources.NotFoundException("missing " + key + " remote config"));
        }

        return result;
    }

    private static boolean getValueWithDefault(JSONObject config, String key, boolean defaultValue) {
        boolean result = defaultValue;
        if (config.has(key)) {
            try {
                result = config.getBoolean(key);
            } catch (JSONException e) {
                // could be an invalid value
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
        else {
            BuddySqliteHelper.getInstance().logError(TAG, new Resources.NotFoundException("missing " + key + " remote config"));
        }

        return result;
    }

    public static BuddyConfiguration update(Context context, JSONObject configJson, String applicationId) {
        BuddyConfiguration savedConfig = getConfig(context);

        try {
            JSONObject commonConfigItems = configJson.getJSONObject("common");
            JSONArray androidConfigItems = configJson.getJSONArray("android");

            long version = configJson.getLong(BuddyPreferenceKeys.preferenceConfigVersion);
            if (version > savedConfig.getVersion()) {
                // upgraded, so update
                long locationPushBatch = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigLocationPushBatch, locationsBatchSizeDefault);
                long cellularPushBatch = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigCellularPushBatch, cellularBatchSizeDefault);
                long batteryPushBatch = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigBatteryPushBatch, batteryBatchSizeDefault);
                long locationsMaxRecords = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, locationsMaxRecordsDefault);
                long cellularMaxRecords = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, cellularMaxRecordsDefault);
                long errorMaxRecords = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, errorMaxRecordsDefault);
                long batteryMaxRecords = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigBatteryMaxRecords, batteryMaxRecordsDefault);
                long maxRecordsToDelete = getValueWithDefault(commonConfigItems, BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, maxRecordsToDeleteDefault);

                String deviceModel = Build.MANUFACTURER;
                int apiLevel = android.os.Build.VERSION.SDK_INT;

                BuddyRemoteConfigurations configurations = new BuddyRemoteConfigurations();

                for(int i=0; i<androidConfigItems.length(); i++){
                    JSONObject androidConfigItem =  androidConfigItems.getJSONObject(i);
                    BuddyRemoteConfiguration configuration = new BuddyRemoteConfiguration(androidConfigItem, deviceModel, apiLevel, applicationId);
                    configurations.add(configuration);
                }
                JSONObject usedConfig = configurations.getValidConfig();

                if (usedConfig != null) {
                    long locationPowerAccuracy = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy,locationPowerAccuracyDefault);
                    long locationUpdateInterval =  getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, locationUpdateIntervalDefault);
                    long locationFastestUpdateInterval =  getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs,locationFastestUpdateIntervalDefault);
                    boolean shouldLogCellular = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigLogCellular,shouldLogCellularDefault);
                    boolean shouldLogLocation = getValueWithDefault(usedConfig,BuddyPreferenceKeys.preferenceConfigLogLocation, shouldLogLocationDefault);
                    boolean shouldLogBattery = getValueWithDefault(usedConfig,BuddyPreferenceKeys.preferenceConfigLogBattery, shouldLogBatteryDefault);
                    boolean shouldUploadCellular = getValueWithDefault(usedConfig,BuddyPreferenceKeys.preferenceConfigUploadCellular, shouldUploadCellularDefault);
                    boolean shouldUploadLocation = getValueWithDefault(usedConfig,BuddyPreferenceKeys.preferenceConfigUploadLocation, shouldUploadLocationDefault);
                    boolean shouldUploadBattery = getValueWithDefault(usedConfig,BuddyPreferenceKeys.preferenceConfigUploadBattery, shouldUploadBatteryDefault);
                    long activityMonitoringTimeout = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigActivityMonitorLogTimeoutMs, activityMonitoringTimeoutMsDefault);
                    long cellularLogTimeout = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, cellularLogTimeoutMsDefault);
                    long batteryLogTimeout = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigBatteryLogTimeoutMs, batteryLogTimeoutMsDefault);
                    long uploadTimeout = getValueWithDefault(usedConfig, BuddyPreferenceKeys.preferenceConfigUploadTimeoutMinutes, uploadTimeoutMinutesDefault);

                    SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigVersion, version);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, locationPushBatch);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, cellularPushBatch);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigBatteryPushBatch, batteryPushBatch);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, cellularLogTimeout);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, locationsMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, cellularMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, errorMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigBatteryMaxRecords, batteryMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, locationPowerAccuracy);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, locationUpdateInterval);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, locationFastestUpdateInterval);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, shouldLogCellular);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, shouldLogLocation);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigLogBattery, shouldLogBattery);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, shouldUploadCellular);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, shouldUploadLocation);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigUploadBattery, shouldUploadBattery);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, maxRecordsToDelete);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorLogTimeoutMs, activityMonitoringTimeout);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigBatteryLogTimeoutMs, batteryLogTimeout);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigUploadTimeoutMinutes, uploadTimeout);
                    editor.apply();

                    savedConfig.setVersion(version);
                    savedConfig.setCommonLocationPushBatchSize(locationPushBatch);
                    savedConfig.setCommonCellularPushBatchSize(cellularPushBatch);
                    savedConfig.setCommonBatteryPushBatchSize(batteryPushBatch);
                    savedConfig.setAndroidCellularLogTimeoutMs(cellularLogTimeout);
                    savedConfig.setCommonMaxLocationRecords(locationsMaxRecords);
                    savedConfig.setCommonMaxCellularRecords(cellularMaxRecords);
                    savedConfig.setCommonMaxBatteryRecords(batteryMaxRecords);
                    savedConfig.setCommonMaxErrorRecords(errorMaxRecords);
                    savedConfig.setCommonMaxRecordsToDelete(maxRecordsToDelete);
                    savedConfig.setAndroidActivityMonitoringTimeoutMs(activityMonitoringTimeout);
                    savedConfig.setAndroidBatteryLogTimeoutMs(batteryLogTimeout);
                    savedConfig.setAndroidLocationPowerAccuracy(locationPowerAccuracy);
                    savedConfig.setAndroidLocationUpdateIntervalMs(locationUpdateInterval);
                    savedConfig.setAndroidLocationFastestUpdateIntervalMs(locationFastestUpdateInterval);
                    savedConfig.setLogCellular(shouldLogCellular);
                    savedConfig.setLogLocation(shouldLogLocation);
                    savedConfig.setLogBattery(shouldLogBattery);
                    savedConfig.setUploadCellular(shouldUploadCellular);
                    savedConfig.setUploadLocation(shouldUploadLocation);
                    savedConfig.setUploadBattery(shouldUploadBattery);
                    savedConfig.setAndroidUploadTimeoutMinutes(uploadTimeout);
                }
            }
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return savedConfig;
    }
}

package com.parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuddyPreferenceService {

    public static final String TAG = "com.parse.BuddyPreferenceService";

    public static BuddyConfiguration getConfig(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
        BuddyConfiguration configuration = new BuddyConfiguration();

        long lastUploaded = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, 0);
        configuration.setLastUploadedEpoch(lastUploaded == 0 ? System.currentTimeMillis() : lastUploaded);

        long lastVersion = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigVersion, 0);
        configuration.setVersion(lastVersion == 0 ? 1 : lastVersion);

        long locationsBatchSize = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, 0);
        configuration.setCommonLocationPushBatchSize(locationsBatchSize == 0 ? 100 : locationsBatchSize);

        long cellularBatchSize = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, 0);
        configuration.setCommonCellularPushBatchSize(cellularBatchSize == 0 ? 100 : cellularBatchSize);

        long cellularLogTimeout = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, 0);
        configuration.setCommonCellularLogTimeout(cellularLogTimeout == 0 ? 1000 : cellularLogTimeout);

        long locationPowerAccuracy = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, 0);
        configuration.setAndroidLocationPowerAccuracy(locationPowerAccuracy == 0 ? 102 : locationPowerAccuracy);

        long locationUpdateInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, 0);
        configuration.setAndroidLocationUpdateInterval(locationUpdateInterval == 0 ? 1000 : locationUpdateInterval);

        long locationFastestUpdateInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, 0);
        configuration.setAndroidLocationFastestUpdateInterval(locationFastestUpdateInterval == 0 ? 500 : locationFastestUpdateInterval);

        boolean shouldLogCellular = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, true);
        configuration.setLogCellular(shouldLogCellular);

        boolean shouldLogLocation = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, true);
        configuration.setLogLocation(shouldLogLocation);

        boolean shouldUploadCellular = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, true);
        configuration.setUploadCellular(shouldUploadCellular);

        boolean shouldUploadLocation = sharedPreferences.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, true);
        configuration.setUploadLocation(shouldUploadLocation);

        long locationsMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, 0);
        configuration.setCommonMaxLocationRecords(locationsMaxRecords == 0 ? 100000 : locationsMaxRecords);

        long cellularMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, 0);
        configuration.setCommonMaxCellularRecords(cellularMaxRecords == 0 ? 100000 : cellularMaxRecords);

        long errorMaxRecords = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, 0);
        configuration.setCommonMaxErrorRecords(errorMaxRecords == 0 ? 100000 : errorMaxRecords);

        long maxRecordsToDelete = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, 0);
        configuration.setCommonMaxRecordsToDelete(maxRecordsToDelete == 0 ? 1000 : maxRecordsToDelete);

        long activityMonitoringInterval = sharedPreferences.getLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorInterval, 0);
        configuration.setAndroidActivityMonitoringInterval(activityMonitoringInterval == 0 ? 3000 : activityMonitoringInterval);

        return configuration;
    }

    public static void updateLastUploadedEpoch(Context context, long lastUploadedEpoch) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, lastUploadedEpoch);
        editor.apply();
    }

    public static BuddyConfiguration update(Context context, JSONObject configJson) {
        BuddyConfiguration savedConfig = getConfig(context);

        try {
            JSONObject commonConfigItems = configJson.getJSONObject("common");
            JSONArray androidConfigItems = configJson.getJSONArray("android");

            long version = configJson.getLong(BuddyPreferenceKeys.preferenceConfigVersion);
            if (version > savedConfig.getVersion()) {
                // upgraded, so update
                long locationPushBatch = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch);
                long cellularPushBatch = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch);
                long cellularLogTimeout = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs);
                long locationsMaxRecords = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords);
                long cellularMaxRecords = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords);
                long errorMaxRecords = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords);
                long maxRecordsToDelete = commonConfigItems.getLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete);


                JSONObject deviceConfig = null;
                JSONObject defaultConfig = null;
                JSONObject usedConfig = null;

                String deviceModel = Build.MANUFACTURER;
                int apiLevel = android.os.Build.VERSION.SDK_INT;

                for(int i=0; i<androidConfigItems.length(); i++){
                    JSONObject androidConfigItem =  androidConfigItems.getJSONObject(i);
                    String model = androidConfigItem.getString("model");

                    if (model.equalsIgnoreCase("*") && androidConfigItem.getString("api-level").equalsIgnoreCase("*")) {
                        defaultConfig = androidConfigItem;
                    }
                    else if (model.equalsIgnoreCase(deviceModel) && androidConfigItem.getInt("api-level") == apiLevel) {
                        deviceConfig = androidConfigItem;
                    }
                }

                if (deviceConfig != null) {
                    usedConfig = deviceConfig;
                }
                else if (defaultConfig != null) {
                    usedConfig = defaultConfig;
                }

                if (usedConfig != null) {
                    long locationPowerAccuracy = usedConfig.getLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy);
                    long locationUpdateInterval =  usedConfig.getLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs);
                    long locationFastestUpdateInterval =  usedConfig.getLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs);
                    boolean shouldLogCellular = usedConfig.getBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular);
                    boolean shouldLogLocation = usedConfig.getBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation);
                    boolean shouldUploadCellular = usedConfig.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular);
                    boolean shouldUploadLocation = usedConfig.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation);
                    long activityMonitoringInterval = usedConfig.getLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorInterval);

                    SharedPreferences sharedPreferences = context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigVersion, version);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, locationPushBatch);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, cellularPushBatch);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, cellularLogTimeout);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, locationsMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, cellularMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, errorMaxRecords);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, locationPowerAccuracy);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, locationUpdateInterval);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, locationFastestUpdateInterval);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, shouldLogCellular);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, shouldLogLocation);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, shouldUploadCellular);
                    editor.putBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, shouldUploadLocation);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, maxRecordsToDelete);
                    editor.putLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorInterval, activityMonitoringInterval);
                    editor.apply();

                    savedConfig.setVersion(version);
                    savedConfig.setCommonLocationPushBatchSize(locationPushBatch);
                    savedConfig.setCommonCellularPushBatchSize(cellularPushBatch);
                    savedConfig.setCommonCellularLogTimeout(cellularLogTimeout);
                    savedConfig.setCommonMaxLocationRecords(locationsMaxRecords);
                    savedConfig.setCommonMaxCellularRecords(cellularMaxRecords);
                    savedConfig.setCommonMaxErrorRecords(errorMaxRecords);
                    savedConfig.setCommonMaxRecordsToDelete(maxRecordsToDelete);
                    savedConfig.setAndroidActivityMonitoringInterval(activityMonitoringInterval);
                    savedConfig.setAndroidLocationPowerAccuracy(locationPowerAccuracy);
                    savedConfig.setAndroidLocationUpdateInterval(locationUpdateInterval);
                    savedConfig.setAndroidLocationFastestUpdateInterval(locationFastestUpdateInterval);
                    savedConfig.setLogCellular(shouldLogCellular);
                    savedConfig.setLogLocation(shouldLogLocation);
                    savedConfig.setUploadCellular(shouldUploadCellular);
                    savedConfig.setUploadLocation(shouldUploadLocation);
                }
            }
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        return savedConfig;
    }
}

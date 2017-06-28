package com.parse;

import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuddyPreferences {

    private SharedPreferences sharedPreferences;
    private final String PREFS_BUDDY_LOCATION_TRACKER = "BuddyPreferences 09977e39-d487-48fb-97f1-1c018ea5e095"; // UUID to prevent name collision
    private final String PREF_LAST_UPLOADED_EPOCH = "lastUploadedEpoch";
    private final String PREF_CONFIG_VERSION = "version";
    private final String PREF_CONFIG_LOCATION_PUSH_BATCH = "location-push-batch";
    private final String PREF_CONFIG_CELLULAR_PUSH_BATCH = "cellular-push-batch";
    private final String PREF_CONFIG_CELLULAR_LOG_TIMEOUT_MS = "cellular-log-timeout-ms";
    private final String PREF_CONFIG_LOCATION_POWER_ACCURACY = "location-power-accuracy";
    private final String PREF_CONFIG_LOCATION_FASTEST_UPDATE_INTERVAL_MS = "location-fastest-update-interval-ms";
    private final String PREF_CONFIG_LOCATION_UPDATE_INTERVAL_MS = "location-update-interval-ms";
    private final String PREF_CONFIG_LOG_CELLULAR = "log-cellular";
    private final String PREF_CONFIG_LOG_LOCATION = "log-location";
    private final String PREF_CONFIG_UPLOAD_CELLULAR = "upload-cellular";
    private final String PREF_CONFIG_UPLOAD_LOCATION = "upload-location";
    private final String PREF_CONFIG_CELLULAR_MAX_RECORDS = "max-cellular-records";
    private final String PREF_CONFIG_LOCATION_MAX_RECORDS = "max-location-records";
    private final String PREF_CONFIG_ERROR_MAX_RECORDS = "max-error-records";
    private final String PREF_CONFIG_MAX_RECORDS_TO_DELETE = "max-records-to-delete";
    public static final String TAG = "com.parse.BuddyPreferences";

    public BuddyPreferences() {
        sharedPreferences = Parse.getApplicationContext().getSharedPreferences(PREFS_BUDDY_LOCATION_TRACKER, 0);
    }

    public BuddyConfiguration getConfig() {
        BuddyConfiguration configuration = new BuddyConfiguration();

        long lastUploaded = sharedPreferences.getLong(PREF_LAST_UPLOADED_EPOCH, 0);
        configuration.setLastUploadedEpoch(lastUploaded == 0 ? System.currentTimeMillis() : lastUploaded);

        long lastVersion = sharedPreferences.getLong(PREF_CONFIG_VERSION, 0);
        configuration.setVersion(lastVersion == 0 ? 1 : lastVersion);

        long locationsBatchSize = sharedPreferences.getLong(PREF_CONFIG_LOCATION_PUSH_BATCH, 0);
        configuration.setCommonLocationPushBatchSize(locationsBatchSize == 0 ? 100 : locationsBatchSize);

        long cellularBatchSize = sharedPreferences.getLong(PREF_CONFIG_CELLULAR_PUSH_BATCH, 0);
        configuration.setCommonCellularPushBatchSize(cellularBatchSize == 0 ? 100 : cellularBatchSize);

        long cellularLogTimeout = sharedPreferences.getLong(PREF_CONFIG_CELLULAR_LOG_TIMEOUT_MS, 0);
        configuration.setCommonCellularLogTimeout(cellularLogTimeout == 0 ? 1000 : cellularLogTimeout);

        long locationPowerAccuracy = sharedPreferences.getLong(PREF_CONFIG_LOCATION_POWER_ACCURACY, 0);
        configuration.setAndroidLocationPowerAccuracy(locationPowerAccuracy == 0 ? 102 : locationPowerAccuracy);

        long locationUpdateInterval = sharedPreferences.getLong(PREF_CONFIG_LOCATION_UPDATE_INTERVAL_MS, 0);
        configuration.setAndroidLocationUpdateInterval(locationPowerAccuracy == 0 ? 1000 : locationUpdateInterval);

        long locationFastestUpdateInterval = sharedPreferences.getLong(PREF_CONFIG_LOCATION_FASTEST_UPDATE_INTERVAL_MS, 0);
        configuration.setAndroidLocationFastestUpdateInterval(locationFastestUpdateInterval == 0 ? 500 : locationFastestUpdateInterval);

        boolean shouldLogCellular = sharedPreferences.getBoolean(PREF_CONFIG_LOG_CELLULAR, true);
        configuration.setLogCellular(shouldLogCellular);

        boolean shouldLogLocation = sharedPreferences.getBoolean(PREF_CONFIG_LOG_LOCATION, true);
        configuration.setLogLocation(shouldLogLocation);

        boolean shouldUploadCellular = sharedPreferences.getBoolean(PREF_CONFIG_UPLOAD_CELLULAR, true);
        configuration.setUploadCellular(shouldUploadCellular);

        boolean shouldUploadLocation = sharedPreferences.getBoolean(PREF_CONFIG_UPLOAD_LOCATION, true);
        configuration.setUploadLocation(shouldUploadLocation);

        long locationsMaxRecords = sharedPreferences.getLong(PREF_CONFIG_LOCATION_MAX_RECORDS, 0);
        configuration.setCommonMaxLocationRecords(locationsMaxRecords == 0 ? 100000 : locationsMaxRecords);

        long cellularMaxRecords = sharedPreferences.getLong(PREF_CONFIG_CELLULAR_MAX_RECORDS, 0);
        configuration.setCommonMaxCellularRecords(cellularMaxRecords == 0 ? 100000 : cellularMaxRecords);

        long errorMaxRecords = sharedPreferences.getLong(PREF_CONFIG_ERROR_MAX_RECORDS, 0);
        configuration.setCommonMaxErrorRecords(errorMaxRecords == 0 ? 100000 : errorMaxRecords);

        long maxRecordsToDelete = sharedPreferences.getLong(PREF_CONFIG_MAX_RECORDS_TO_DELETE, 0);
        configuration.setCommonMaxRecordsToDelete(maxRecordsToDelete == 0 ? 1000 : maxRecordsToDelete);

        return configuration;
    }

    private void save(String key, Object value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof Long) {
            editor.putLong(key, (long)value);
        }

        editor.apply();
    }
    public BuddyConfiguration updateLastUploadedEpoch(long lastUploadedEpoch) {
        save(PREF_LAST_UPLOADED_EPOCH,lastUploadedEpoch);
        return getConfig();
    }

    public BuddyConfiguration update(JSONObject configJson) {
        BuddyConfiguration savedConfig = getConfig();

        try {
            JSONObject commonConfigItems = configJson.getJSONObject("common");
            JSONArray androidConfigItems = configJson.getJSONArray("android");

            long version = configJson.getLong(PREF_CONFIG_VERSION);
            if (version > savedConfig.getVersion()) {
                // upgraded, so update
                long locationPushBatch = commonConfigItems.getLong(PREF_CONFIG_LOCATION_PUSH_BATCH);
                long cellularPushBatch = commonConfigItems.getLong(PREF_CONFIG_CELLULAR_PUSH_BATCH);
                long cellularLogTimeout = commonConfigItems.getLong(PREF_CONFIG_CELLULAR_LOG_TIMEOUT_MS);
                long locationsMaxRecords = commonConfigItems.getLong(PREF_CONFIG_LOCATION_MAX_RECORDS);
                long cellularMaxRecords = commonConfigItems.getLong(PREF_CONFIG_CELLULAR_MAX_RECORDS);
                long errorMaxRecords = commonConfigItems.getLong(PREF_CONFIG_ERROR_MAX_RECORDS);
                long maxRecordsToDelete = commonConfigItems.getLong(PREF_CONFIG_MAX_RECORDS_TO_DELETE);

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
                    long locationPowerAccuracy = usedConfig.getLong(PREF_CONFIG_LOCATION_POWER_ACCURACY);
                    long locationUpdateInterval =  usedConfig.getLong(PREF_CONFIG_LOCATION_UPDATE_INTERVAL_MS);
                    long locationFastestUpdateInterval =  usedConfig.getLong(PREF_CONFIG_LOCATION_FASTEST_UPDATE_INTERVAL_MS);
                    boolean shouldLogCellular = usedConfig.getBoolean(PREF_CONFIG_LOG_CELLULAR);
                    boolean shouldLogLocation = usedConfig.getBoolean(PREF_CONFIG_LOG_LOCATION);
                    boolean shouldUploadCellular = usedConfig.getBoolean(PREF_CONFIG_UPLOAD_CELLULAR);
                    boolean shouldUploadLocation = usedConfig.getBoolean(PREF_CONFIG_UPLOAD_LOCATION);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(PREF_CONFIG_VERSION, version);
                    editor.putLong(PREF_CONFIG_LOCATION_PUSH_BATCH, locationPushBatch);
                    editor.putLong(PREF_CONFIG_CELLULAR_PUSH_BATCH, cellularPushBatch);
                    editor.putLong(PREF_CONFIG_CELLULAR_LOG_TIMEOUT_MS, cellularLogTimeout);
                    editor.putLong(PREF_CONFIG_LOCATION_MAX_RECORDS, locationsMaxRecords);
                    editor.putLong(PREF_CONFIG_CELLULAR_MAX_RECORDS, cellularMaxRecords);
                    editor.putLong(PREF_CONFIG_ERROR_MAX_RECORDS, errorMaxRecords);
                    editor.putLong(PREF_CONFIG_LOCATION_POWER_ACCURACY, locationPowerAccuracy);
                    editor.putLong(PREF_CONFIG_LOCATION_UPDATE_INTERVAL_MS, locationUpdateInterval);
                    editor.putLong(PREF_CONFIG_LOCATION_FASTEST_UPDATE_INTERVAL_MS, locationFastestUpdateInterval);
                    editor.putBoolean(PREF_CONFIG_LOG_CELLULAR, shouldLogCellular);
                    editor.putBoolean(PREF_CONFIG_LOG_LOCATION, shouldLogLocation);
                    editor.putBoolean(PREF_CONFIG_UPLOAD_CELLULAR, shouldUploadCellular);
                    editor.putBoolean(PREF_CONFIG_UPLOAD_LOCATION, shouldUploadLocation);
                    editor.putLong(PREF_CONFIG_MAX_RECORDS_TO_DELETE, maxRecordsToDelete);
                    editor.apply();

                    savedConfig.setVersion(version);
                    savedConfig.setCommonLocationPushBatchSize(locationPushBatch);
                    savedConfig.setCommonCellularPushBatchSize(cellularPushBatch);
                    savedConfig.setCommonCellularLogTimeout(cellularLogTimeout);
                    savedConfig.setCommonMaxLocationRecords(locationsMaxRecords);
                    savedConfig.setCommonMaxCellularRecords(cellularMaxRecords);
                    savedConfig.setCommonMaxErrorRecords(errorMaxRecords);
                    savedConfig.setCommonMaxRecordsToDelete(maxRecordsToDelete);
                    savedConfig.setAndroidLocationPowerAccuracy(locationPowerAccuracy);
                    savedConfig.setAndroidLocationUpdateInterval(locationUpdateInterval);
                    savedConfig.setAndroidLocationFastestUpdateInterval(locationFastestUpdateInterval);
                    savedConfig.setLogCellular(shouldLogCellular);
                    savedConfig.setLogLocation(shouldLogLocation);
                    savedConfig.setUploadCellular(shouldUploadCellular);
                    savedConfig.setUploadLocation(shouldUploadLocation);
                }
            }
        } catch (JSONException e) {
            BuddyDBHelper.getInstance().logError(TAG, e.getMessage());
        }

        return savedConfig;
    }
}

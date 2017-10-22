package com.parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyPreferenceTest {
    private SharedPreferences sharedPrefs;
    private Context context;
    private long epoch = 100;
    private long lastVersion = 101;
    private long locationsBatchSize = 102;
    private long cellularBatchSize = 103;
    private long cellularLogTimeout = 104;
    private long locationPowerAccuracy = 105;
    private long locationUpdateInterval = 106;
    private long locationFastestUpdateInterval = 107;
    private boolean shouldLogCellular = true;
    private boolean shouldLogLocation = false;
    private boolean shouldUploadCellular = true;
    private boolean shouldUploadLocation = false;
    private long locationsMaxRecords = 108;
    private long cellularMaxRecords = 109;
    private long errorMaxRecords = 110;
    private long maxRecordsToDelete = 111;
    private long androidActivityMonitoringInterval = 112;
    private long batteryMaxRecords = 113;
    private long batteryBatchSize = 114;
    private long uploadTimeoutMin = 500;
    private String appId1 = "12d87a75-5d00-4140-b701-4fea351d05a8";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sharedPrefs = Mockito.mock(SharedPreferences.class);
        context = Mockito.mock(Context.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetConfigMaxRecordsToDelete() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, 0)).thenReturn(maxRecordsToDelete);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(maxRecordsToDelete, configuration.getCommonMaxRecordsToDelete());
    }

    @Test
    public void testGetConfigErrorMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, 0)).thenReturn(errorMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(errorMaxRecords, configuration.getCommonMaxErrorRecords());
    }

    @Test
    public void testGetConfigBatteryMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigBatteryMaxRecords, 0)).thenReturn(batteryMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(batteryMaxRecords, configuration.getCommonMaxBatteryRecords());
    }

    @Test
    public void testGetConfigCellularMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, 0)).thenReturn(cellularMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(cellularMaxRecords, configuration.getCommonMaxCellularRecords());
    }

    @Test
    public void testGetConfigLocationsMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, 0)).thenReturn(locationsMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(locationsMaxRecords, configuration.getCommonMaxLocationRecords());
    }

    @Test
    public void testGetConfigShouldUploadLocation() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, false)).thenReturn(shouldUploadLocation);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(shouldUploadLocation, configuration.shouldUploadLocation());
    }

    @Test
    public void testGetConfigShouldUploadCellular() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, true)).thenReturn(shouldUploadCellular);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(shouldUploadCellular, configuration.shouldUploadCellular());
    }

    @Test
    public void testGetConfigShouldLogLocation() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, false)).thenReturn(shouldLogLocation);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(shouldLogLocation, configuration.shouldLogLocation());
    }

    @Test
    public void testGetConfigShouldLogCellular() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, true)).thenReturn(shouldLogCellular);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(shouldLogCellular, configuration.shouldLogCellular());
    }

    @Test
    public void testGetConfigLocationFastestUpdateInterval() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, 0)).thenReturn(locationFastestUpdateInterval);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(locationFastestUpdateInterval, configuration.getAndroidLocationFastestUpdateIntervalMs());
    }

    @Test
    public void testGetConfigLocationUpdateInterval() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, 0)).thenReturn(locationUpdateInterval);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(locationUpdateInterval, configuration.getAndroidLocationUpdateIntervalMs());
    }

    @Test
    public void testGetConfigLocationPowerAccuracy() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, 0)).thenReturn(locationPowerAccuracy);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(locationPowerAccuracy, configuration.getAndroidLocationPowerAccuracy());
    }

    @Test
    public void testGetConfigCellularLogTimeout() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, 0)).thenReturn(cellularLogTimeout);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(cellularLogTimeout, configuration.getAndroidCellularLogTimeoutMs());
    }

    @Test
    public void testGetConfigCellularBatchSize() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, 0)).thenReturn(cellularBatchSize);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(cellularBatchSize, configuration.getCommonCellularPushBatchSize());
    }

    @Test
    public void testGetConfigLocationBatchSize() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, 0)).thenReturn(locationsBatchSize);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(locationsBatchSize, configuration.getCommonLocationPushBatchSize());
    }

    @Test
    public void testGetConfigBatteryBatchSize() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigBatteryPushBatch, 0)).thenReturn(batteryBatchSize);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(batteryBatchSize, configuration.getCommonBatteryPushBatchSize());
    }

    @Test
    public void testGetConfigLastVersion() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigVersion, 0)).thenReturn(lastVersion);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(lastVersion, configuration.getVersion());
    }

    @Test
    public void testGetConfigEpoch() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, 0)).thenReturn(epoch);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(epoch, configuration.getLastUploadedEpoch());
    }

    @Test
    public void testGetConfigActivityMonitorInterval() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigActivityMonitorLogTimeoutMs, 0)).thenReturn(androidActivityMonitoringInterval);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(androidActivityMonitoringInterval, configuration.getAndroidActivityMonitoringTimeoutMs());
    }

    @Test
    public void testGetConfigUploadTimeout() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigUploadTimeoutMinutes, 0)).thenReturn(uploadTimeoutMin);

        // act
        BuddyConfiguration configuration = BuddyPreferences.getConfig(context);

        // assert
        assertEquals(uploadTimeoutMin, configuration.getAndroidUploadTimeoutMinutes());
    }

    @Test
    public void testUpdateWithDefaultAndroidConfiguration() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationOnly();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config,appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(107, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(108, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(109, configuration.getAndroidLocationUpdateIntervalMs());
        assertTrue(configuration.shouldLogCellular());
        assertTrue(configuration.shouldLogLocation());
        assertTrue(configuration.shouldLogBattery());
        assertFalse(configuration.shouldUploadCellular());
        assertFalse(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldUploadBattery());
        assertEquals(110, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationOnly() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndOneSingleConfig() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndOneSingleConfig();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(111, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(112, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(113, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(114, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndOneSingleConfig() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoSingleConfigs() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoSingleConfigs();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 22);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(115, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(116, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(117, configuration.getAndroidLocationUpdateIntervalMs());
        assertTrue(configuration.shouldLogCellular());
        assertTrue(configuration.shouldLogLocation());
        assertFalse(configuration.shouldUploadCellular());
        assertFalse(configuration.shouldUploadLocation());
        assertTrue(configuration.shouldLogBattery());
        assertFalse(configuration.shouldUploadBattery());
        assertEquals(118, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoSingleConfigs() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndApiLevel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndApiLevel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(115, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(116, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(117, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(118, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndApiLevel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"12d87a75-5d00-4140-b701-4fea351d05a8\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndModel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndModel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(111, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(112, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(113, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(114, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoSingleConfigsWithAppIdAndModel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"12d87a75-5d00-4140-b701-4fea351d05a8\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoSingleConfigsWithApiLevelAndModel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoSingleConfigsWithApiLevelAndModel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(111, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(112, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(113, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(114, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoSingleConfigsWithApiLevelAndModel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndOneDoubleConfig() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndOneDoubleConfig();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(115, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(116, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(117, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(118, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndOneDoubleConfig() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndApiLevel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndApiLevel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(119, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(120, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(121, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(122, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndApiLevel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"12d87a75-5d00-4140-b701-4fea351d05a8\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 123,\n" +
                    "            \"location-fastest-update-interval-ms\": 124,\n" +
                    "            \"location-update-interval-ms\": 125,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 126\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndModel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndModel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(119, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(120, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(121, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(122, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoDoubleConfigAppIdAndModel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"12d87a75-5d00-4140-b701-4fea351d05a8\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 123,\n" +
                    "            \"location-fastest-update-interval-ms\": 124,\n" +
                    "            \"location-update-interval-ms\": 125,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 126\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoDoubleConfigApiLevelAndModel() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoDoubleConfigApiLevelAndModel();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(115, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(116, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(117, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(118, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoDoubleConfigApiLevelAndModel() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 123,\n" +
                    "            \"location-fastest-update-interval-ms\": 124,\n" +
                    "            \"location-update-interval-ms\": 125,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 126\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Test
    public void testUpdateWithDefaultAndroidConfigurationAndTwoTripleConfig() throws Exception {
        // arrange
        JSONObject config = getDefaultAndroidConfigurationAndTwoTrippleConfig();
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPrefs.edit()).thenReturn(editor);
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 23);
        setFinalStatic(Build.class.getField("MANUFACTURER"), "samsung");

        // act
        BuddyConfiguration configuration = BuddyPreferences.update(context, config, appId1);

        // assert
        assertEquals(100, configuration.getCommonLocationPushBatchSize());
        assertEquals(101, configuration.getCommonCellularPushBatchSize());
        assertEquals(102, configuration.getAndroidCellularLogTimeoutMs());
        assertEquals(103, configuration.getCommonMaxLocationRecords());
        assertEquals(104, configuration.getCommonMaxCellularRecords());
        assertEquals(105, configuration.getCommonMaxErrorRecords());
        assertEquals(106, configuration.getCommonMaxRecordsToDelete());
        assertEquals(121, configuration.getCommonBatteryPushBatchSize());
        assertEquals(120, configuration.getCommonMaxBatteryRecords());
        // android stuff
        assertEquals(119, configuration.getAndroidLocationPowerAccuracy());
        assertEquals(120, configuration.getAndroidLocationFastestUpdateIntervalMs());
        assertEquals(121, configuration.getAndroidLocationUpdateIntervalMs());
        assertFalse(configuration.shouldLogCellular());
        assertFalse(configuration.shouldLogLocation());
        assertTrue(configuration.shouldUploadCellular());
        assertTrue(configuration.shouldUploadLocation());
        assertFalse(configuration.shouldLogBattery());
        assertTrue(configuration.shouldUploadBattery());
        assertEquals(122, configuration.getAndroidActivityMonitoringTimeoutMs());
        assertEquals(127, configuration.getAndroidBatteryLogTimeoutMs());
        assertEquals(500, configuration.getAndroidUploadTimeoutMinutes());
    }

    private JSONObject getDefaultAndroidConfigurationAndTwoTrippleConfig() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\n" +
                    "  \"version\": 4,\n" +
                    "  \"common\": {\n" +
                    "      \"location-push-batch\": 100,\n" +
                    "      \"cellular-push-batch\": 101,\n" +
                    "      \"battery-push-batch\": 121, \n" +
                    "      \"max-location-records\": 103,\n" +
                    "      \"max-cellular-records\": 104,\n" +
                    "      \"max-error-records\": 105,\n" +
                    "      \"max-battery-records\": 120,\n" +
                    "      \"max-records-to-delete\" : 106\n" +
                    "  },\n" +
                    "  \"ios\": {},\n" +
                    "  \"android\": [\n" +
                    "        {\n" +
                    "            \"api-level\": \"*\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 107,\n" +
                    "            \"location-fastest-update-interval-ms\": 108,\n" +
                    "            \"location-update-interval-ms\": 109,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 110\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 111,\n" +
                    "            \"location-fastest-update-interval-ms\": 112,\n" +
                    "            \"location-update-interval-ms\": 113,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 114\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 115,\n" +
                    "            \"location-fastest-update-interval-ms\": 116,\n" +
                    "            \"location-update-interval-ms\": 117,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 118\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"23\",\n" +
                    "            \"model\": \"samsung\",\n" +
                    "            \"app-id\": \"12d87a75-5d00-4140-b701-4fea351d05a8\",\n" +
                    "            \"location-power-accuracy\": 119,\n" +
                    "            \"location-fastest-update-interval-ms\": 120,\n" +
                    "            \"location-update-interval-ms\": 121,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 127,\n" +
                    "            \"log-cellular\": false,\n" +
                    "            \"log-location\": false,\n" +
                    "            \"log-battery\": false,\n" +
                    "            \"upload-cellular\": true,\n" +
                    "            \"upload-location\": true,\n" +
                    "            \"upload-battery\": true,\n" +
                    "            \"upload-timeout-min\": 500,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 122\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"api-level\": \"22\",\n" +
                    "            \"model\": \"*\",\n" +
                    "            \"app-id\": \"*\",\n" +
                    "            \"location-power-accuracy\": 123,\n" +
                    "            \"location-fastest-update-interval-ms\": 124,\n" +
                    "            \"location-update-interval-ms\": 125,\n" +
                    "      \"cellular-log-timeout-ms\": 102,\n" +
                    "      \"battery-log-timeout-ms\": 128,\n" +
                    "            \"log-cellular\": true,\n" +
                    "            \"log-location\": true,\n" +
                    "            \"log-battery\": true,\n" +
                    "            \"upload-cellular\": false,\n" +
                    "            \"upload-location\": false,\n" +
                    "            \"upload-battery\": false,\n" +
                    "            \"upload-timeout-min\": 360,\n" +
                    "            \"activity-monitor-log-timeout-ms\": 126\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}

package com.parse;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddySqliteHelperTest {
    private BuddySqliteHelper dbHelper;
    private double latitude = 123;
    private double longitude = 456;
    private double accuracy = 10;
    private double bearing = 20;
    private double bearingAccuracy = 15;
    private double altitude = 23;
    private double speed = 100;
    private double speedAccuracyMetersPerSecond = 230;
    private double verticalAccuracyMeters = 10;
    private final String TAG = "location tracker";
    private final String message = "broken";
    private final String savedError = message;
    private final String cellularBody = "{\"cellinfo\": \"here\"}";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dbHelper = new BuddySqliteHelper(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
//        dbHelper.cleanUp(BuddySqliteTableType.Cellular);
//        dbHelper.cleanUp(BuddySqliteTableType.Error);
//        dbHelper.cleanUp(BuddySqliteTableType.Location);
    }

    @Test
    public void testLogError() throws Exception {
        // arrange

        // act
        saveSampleError();

        // assert
        long count = dbHelper.rowCount(BuddySqliteTableType.Error);
        assertEquals(1, count);

        JSONObject result = dbHelper.get(BuddySqliteTableType.Error, 10);
        Boolean exists = result.has("items") && result.has("ids");
        assertTrue(exists);

        final JSONArray items = (JSONArray) result.get("items");
        assertEquals(1, items.length());

        final String[] ids = (String[]) result.get("ids");
        assertEquals(1, ids.length);

        JSONObject item = items.getJSONObject(0);
        assertEquals(savedError, item.get("message"));
        assertEquals(TAG, item.get("tag"));
        assertNotNull(item.get("stacktrace"));
    }

    @Test
    public void testCleanUpLocations() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleLocation();
        }

        BuddyConfiguration configuration = setupPreferences();

        // act
        dbHelper.cleanUp(configuration);

        // assert
        long locationCount = dbHelper.rowCount(BuddySqliteTableType.Location);
        assertEquals(35, locationCount);
    }

    @NonNull
    private BuddyConfiguration setupPreferences() {
        BuddyConfiguration configuration = new BuddyConfiguration();
        configuration.setCommonMaxErrorRecords(50);
        configuration.setCommonMaxCellularRecords(20);
        configuration.setCommonMaxLocationRecords(30);
        configuration.setCommonMaxRecordsToDelete(5);
        return configuration;
    }

    @Test
    public void testCleanUpErrors() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleError();
        }

        BuddyConfiguration configuration = setupPreferences();

        // act
        dbHelper.cleanUp(configuration);

        // assert
        long errorCount = dbHelper.rowCount(BuddySqliteTableType.Error);
        assertEquals(40, errorCount);
    }


    @Test
    public void testCleanUpCellular() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }

        BuddyConfiguration configuration = setupPreferences();

        // act
        dbHelper.cleanUp(configuration);

        // assert
        long cellularCount = dbHelper.rowCount(BuddySqliteTableType.Cellular);
        assertEquals(35, cellularCount);
    }


    @Test
    public void testCleanUpTable() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }

        // act
        dbHelper.cleanUpTable(BuddySqliteTableType.Cellular);

        // assert
        long cellularCount = dbHelper.rowCount(BuddySqliteTableType.Cellular);
        assertEquals(0, cellularCount);
    }

    @Test
    public void testGetAll() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }

        // act
        dbHelper.get(BuddySqliteTableType.Cellular, 0);

        // assert
        long cellularCount = dbHelper.rowCount(BuddySqliteTableType.Cellular);
        assertEquals(40, cellularCount);
    }

    @Test
    public void testGetBatch() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }

        // act
        JSONObject result =  dbHelper.get(BuddySqliteTableType.Cellular, 30);

        // assert
        Boolean exists = result.has("items") && result.has("ids");
        assertTrue(exists);

        final JSONArray items = (JSONArray) result.get("items");
        assertEquals(30, items.length());

        final String[] ids = (String[]) result.get("ids");
        assertEquals(30, ids.length);
    }

    @Test
    public void testRowCounts() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }

        // act
        long cellularCount = dbHelper.rowCount(BuddySqliteTableType.Cellular);

        // assert
        assertEquals(40, cellularCount);
    }

    @Test
    public void testDeleteRows() throws Exception {
        // arrange
        for (int i=0; i< 40; i++) {
            saveSampleCellular();
        }
        JSONObject result =  dbHelper.get(BuddySqliteTableType.Cellular, 40);
        final String[] ids = (String[]) result.get("ids");

        // act
        long cellularCount = dbHelper.delete(BuddySqliteTableType.Cellular, ids);

        // assert
        assertEquals(40, cellularCount);
    }

    private void saveSampleCellular() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BuddySqliteCellularTableKeys.Uuid, UUID.randomUUID().toString());
        contentValues.put(BuddySqliteCellularTableKeys.Body, cellularBody);

        dbHelper.save(BuddySqliteTableType.Cellular,contentValues);
    }

    private void saveSampleError() {
        dbHelper.logError(TAG, new Exception(message));
    }

    private void saveSampleLocation() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BuddySqliteLocationTableKeys.Uuid, UUID.randomUUID().toString());
        contentValues.put(BuddySqliteLocationTableKeys.Latitude,latitude);
        contentValues.put(BuddySqliteLocationTableKeys.Longitude,longitude);
        contentValues.put(BuddySqliteLocationTableKeys.Accuracy,accuracy);
        contentValues.put(BuddySqliteLocationTableKeys.Altitude,altitude);
        contentValues.put(BuddySqliteLocationTableKeys.Bearing,bearing);
        contentValues.put(BuddySqliteLocationTableKeys.BearingAccuracy,bearingAccuracy);
        contentValues.put(BuddySqliteLocationTableKeys.Speed,speed);
        contentValues.put(BuddySqliteLocationTableKeys.SpeedAccuracy,speedAccuracyMetersPerSecond);
        contentValues.put(BuddySqliteLocationTableKeys.VerticalAccuracy,verticalAccuracyMeters);

        dbHelper.save(BuddySqliteTableType.Location, contentValues);
    }
}

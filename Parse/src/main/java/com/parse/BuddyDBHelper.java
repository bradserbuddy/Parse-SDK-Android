package com.parse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


class BuddyDBHelper extends SQLiteOpenHelper {
    public static final String TAG = "com.parse.BuddyDBHelper";
    private static final String DATABASE_NAME = "Buddy.db";
    private static final String LOCATIONS_TABLE_NAME = "locations";
    private static final String LOCATIONS_COLUMN_UUID = "uuid";
    private static final String LOCATIONS_COLUMN_TIMESTAMP = "timestamp";
    private static final String LOCATIONS_COLUMN_LATITUDE = "latitude";
    private static final String LOCATIONS_COLUMN_LONGITUDE = "longitude";
    private static final String LOCATIONS_COLUMN_ACCURACY = "accuracy";
    private static final String LOCATIONS_COLUMN_ALTITUDE = "altitude";
    private static final String LOCATIONS_COLUMN_BEARING = "bearing";
    private static final String LOCATIONS_COLUMN_BEARING_ACCURACY = "bearingAccuracy";
    private static final String LOCATIONS_COLUMN_SPEED = "speed";
    private static final String LOCATIONS_COLUMN_SPEED_ACCURACY = "speedAccuracyMetersPerSecond";
    private static final String LOCATIONS_COLUMN_VERTICAL_ACCURACY = "verticalAccuracyMeters";

    private static final String CELLULAR_TABLE_NAME = "cellular";
    private static final String CELLULAR_COLUMN_UUID = "uuid";
    private static final String CELLULAR_COLUMN_TIMESTAMP = "timestamp";
    private static final String CELLULAR_COLUMN_BODY = "body";
    private static SQLiteDatabase db;

    BuddyDBHelper(Context context) {
        super(context, DATABASE_NAME , null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CreateLocationsTable(db);
        CreateCellularTable(db);
    }

    private void CreateLocationsTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s real, %s real, %s real, %s real, " +
                        "%s real, %s real, %s real, %s real, %s real )",
                LOCATIONS_TABLE_NAME, LOCATIONS_COLUMN_UUID, LOCATIONS_COLUMN_TIMESTAMP,
                LOCATIONS_COLUMN_LATITUDE, LOCATIONS_COLUMN_LONGITUDE, LOCATIONS_COLUMN_ACCURACY,
                LOCATIONS_COLUMN_ALTITUDE, LOCATIONS_COLUMN_BEARING, LOCATIONS_COLUMN_BEARING_ACCURACY,
                LOCATIONS_COLUMN_SPEED, LOCATIONS_COLUMN_SPEED_ACCURACY, LOCATIONS_COLUMN_VERTICAL_ACCURACY);

        db.execSQL(query);
    }

    private void CreateCellularTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s text )",
                CELLULAR_TABLE_NAME, CELLULAR_COLUMN_UUID, CELLULAR_COLUMN_TIMESTAMP,
                CELLULAR_COLUMN_BODY);

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = String.format("drop table if exists %s", LOCATIONS_TABLE_NAME);
        db.execSQL(query);

        query = String.format("drop table if exists %s", CELLULAR_TABLE_NAME);
        db.execSQL(query);

        onCreate(db);
    }

    boolean openDatabase() {
        boolean isSuccessful = true;
        if (db == null) {
            try {
                db = this.getWritableDatabase();
            }
            catch (Exception e) {
                isSuccessful = false;
                PLog.e(TAG, e.getMessage());
            }
        }

        return isSuccessful;
    }

    long insertLocation (String uuid, Double latitude, Double longitude,
                         float accuracy, double altitude, float bearing,
                         float bearingAccuracy, float speed,
                         float speedAccuracyMetersPerSecond,
                         float verticalAccuracyMeters) {

        long result = 0;

        if (openDatabase()) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(LOCATIONS_COLUMN_UUID, uuid);
                contentValues.put(LOCATIONS_COLUMN_LATITUDE, latitude);
                contentValues.put(LOCATIONS_COLUMN_LONGITUDE, longitude);
                contentValues.put(LOCATIONS_COLUMN_ACCURACY, accuracy);
                contentValues.put(LOCATIONS_COLUMN_ALTITUDE, altitude);
                contentValues.put(LOCATIONS_COLUMN_BEARING, bearing);
                contentValues.put(LOCATIONS_COLUMN_SPEED, speed);
                contentValues.put(LOCATIONS_COLUMN_BEARING_ACCURACY, bearingAccuracy);
                contentValues.put(LOCATIONS_COLUMN_SPEED_ACCURACY, speedAccuracyMetersPerSecond);
                contentValues.put(LOCATIONS_COLUMN_VERTICAL_ACCURACY, verticalAccuracyMeters);

                result = db.insert(LOCATIONS_TABLE_NAME, null, contentValues);

            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return result;
    }

    long insertCellInformation (String uuid, String body) {
        long result = 0;

        if (openDatabase()) {
            try {
                ContentValues contentValues = new ContentValues();

                contentValues.put(CELLULAR_COLUMN_UUID, uuid);
                contentValues.put(CELLULAR_COLUMN_BODY, body);
                result = db.insert(CELLULAR_TABLE_NAME, null, contentValues);
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return result;
    }

    public void cleanUp() {
        BuddyPreferences preferences = new BuddyPreferences();
        BuddyConfiguration configuration = preferences.getConfig();

        if (openDatabase()) {
            try {
                if (cellularRowCount() > configuration.getCommonMaxCellularRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", CELLULAR_TABLE_NAME,
                            CELLULAR_COLUMN_UUID, CELLULAR_COLUMN_UUID, CELLULAR_TABLE_NAME, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }

                if (locationsRowCount() > configuration.getCommonMaxLocationRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", LOCATIONS_TABLE_NAME,
                            LOCATIONS_COLUMN_UUID, LOCATIONS_COLUMN_UUID, LOCATIONS_TABLE_NAME, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }
    }

    ArrayList<BuddyLocation> getLocationsBatch(long locationsBatchSize) {
        ArrayList<BuddyLocation> locations = new ArrayList<>();

        if (openDatabase()) {
            try {
                String query = String.format(Locale.US, "select * from %s limit %d", LOCATIONS_TABLE_NAME, locationsBatchSize);
                Cursor res =  db.rawQuery( query, null );
                res.moveToFirst();

                while (!res.isAfterLast()) {
                    BuddyLocation location = new BuddyLocation();
                    String uuid = res.getString(res.getColumnIndex(LOCATIONS_COLUMN_UUID));
                    location.setUuid(uuid);

                    long timestamp = res.getLong(res.getColumnIndex(LOCATIONS_COLUMN_TIMESTAMP));
                    location.setTimestamp(timestamp);

                    double latitude = res.getDouble(res.getColumnIndex(LOCATIONS_COLUMN_LATITUDE));
                    location.setLatitude(latitude);

                    double longitude = res.getDouble(res.getColumnIndex(LOCATIONS_COLUMN_LONGITUDE));
                    location.setLongitude(longitude);

                    float accuracy = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_ACCURACY));
                    location.setAccuracy(accuracy);

                    double altitude = res.getDouble(res.getColumnIndex(LOCATIONS_COLUMN_ALTITUDE));
                    location.setAltitude(altitude);

                    float bearing = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_BEARING));
                    location.setBearing(bearing);

                    float speed  = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_SPEED));
                    location.setSpeed(speed);

                    float bearingAccuracy = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_BEARING_ACCURACY));
                    location.setBearingAccuracy(bearingAccuracy);

                    float speedAccuracy  = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_SPEED_ACCURACY));
                    location.setSpeedAccuracyMetersPerSecond(speedAccuracy);

                    float verticalAccuracy = res.getFloat(res.getColumnIndex(LOCATIONS_COLUMN_VERTICAL_ACCURACY));
                    location.setVerticalAccuracyMeters(verticalAccuracy);

                    locations.add(location);

                    res.moveToNext();
                }

                res.close();
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return locations;
    }

    JSONArray getCellularBatch(long cellularBatchSize) {
        JSONArray cellularInfoItems = new JSONArray();
        if (openDatabase()) {
            try {
                String query = String.format(Locale.US, "select * from %s limit %d", CELLULAR_TABLE_NAME, cellularBatchSize);
                Cursor res =  db.rawQuery( query, null );
                res.moveToFirst();

                while (!res.isAfterLast()) {
                    JSONObject cellularInfo = new JSONObject();

                    try {
                        String uuid = res.getString(res.getColumnIndex(CELLULAR_COLUMN_UUID));
                        cellularInfo.put("uuid", uuid);
                        long timestamp = res.getLong(res.getColumnIndex(CELLULAR_COLUMN_TIMESTAMP));
                        cellularInfo.put("timestamp", timestamp);
                        String body = res.getString(res.getColumnIndex(CELLULAR_COLUMN_BODY));
                        JSONObject cellInfo = new JSONObject(body);
                        cellularInfo.put("cellInfo", cellInfo);
                    } catch (JSONException e) {
                        PLog.e(TAG, e.getMessage());
                    }

                    cellularInfoItems.put(cellularInfo);

                    res.moveToNext();
                }

                res.close();
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return cellularInfoItems;
    }

    int locationsRowCount(){
        int numRows = 0;

        if (openDatabase()) {
            try {
                numRows = (int) DatabaseUtils.queryNumEntries(db, LOCATIONS_TABLE_NAME);            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

    int cellularRowCount(){
        int numRows = 0;

        if (openDatabase()) {
            try {
                numRows = (int) DatabaseUtils.queryNumEntries(db, CELLULAR_TABLE_NAME);           }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

    long deleteLocations(String[] locationIds) {
        int numRows = 0;

        if (openDatabase()) {
            try {
                String args = "'" + TextUtils.join("', '", locationIds) + "'";

                numRows = db.delete(LOCATIONS_TABLE_NAME,
                        LOCATIONS_COLUMN_UUID + " in ( " + args + ")",
                        null);
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

    public long deleteCellular(ArrayList<String> uploadableCellularIds) {
        int numRows = 0;

        if (openDatabase()) {
            try {
                String args = "'" + TextUtils.join("', '", uploadableCellularIds) + "'";

                numRows = db.delete(CELLULAR_TABLE_NAME,
                        CELLULAR_COLUMN_UUID + " in ( " + args + ")",
                        null);
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }
}
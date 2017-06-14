package com.parse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

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
    void openDatabase() {
        if (db == null) {
            db = this.getWritableDatabase();
        }
    }

    long insertLocation (String uuid, Double latitude, Double longitude,
                                float accuracy, double altitude, float bearing,
                                float bearingAccuracy, float speed,
                                float speedAccuracyMetersPerSecond,
                                float verticalAccuracyMeters) {
        openDatabase();
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
        long insertResult = db.insert(LOCATIONS_TABLE_NAME, null, contentValues);

        return insertResult;
    }

    long insertCellInformation (String uuid, String body) {
        openDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CELLULAR_COLUMN_UUID, uuid);
        contentValues.put(CELLULAR_COLUMN_BODY, body);
        long insertResult = db.insert(CELLULAR_TABLE_NAME, null, contentValues);

        return insertResult;
    }

    ArrayList<BuddyLocation> getLocationsBatch(long locationsBatchSize) {
        ArrayList<BuddyLocation> locations = new ArrayList<>();
        openDatabase();

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

        return locations;
    }

    ArrayList<JSONObject> getCellularBatch(long cellularBatchSize) {
        ArrayList<JSONObject> cellularInfoItems = new ArrayList<>();
        openDatabase();

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

            cellularInfoItems.add(cellularInfo);

            res.moveToNext();
        }

        res.close();

        return cellularInfoItems;
    }

    int locationsRowCount(){
        openDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LOCATIONS_TABLE_NAME);

        return numRows;
    }

    int cellularRowCount(){
        openDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CELLULAR_TABLE_NAME);

        return numRows;
    }

    long deleteLocations(String[] locationIds) {
        openDatabase();
        String args = "'" + TextUtils.join("', '", locationIds) + "'";

        int deleteRowCount = db.delete(LOCATIONS_TABLE_NAME,
                LOCATIONS_COLUMN_UUID + " in ( " + args + ")",
                null);

        return deleteRowCount;
    }

    public long deleteCellular(String[] uploadableCellularIds) {
        openDatabase();
        String args = "'" + TextUtils.join("', '", uploadableCellularIds) + "'";

        int deleteRowCount = db.delete(CELLULAR_TABLE_NAME,
                CELLULAR_COLUMN_UUID + " in ( " + args + ")",
                null);

        return deleteRowCount;
    }
}
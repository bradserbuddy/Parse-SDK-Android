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
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class BuddyDBHelper extends SQLiteOpenHelper {
    private static BuddyDBHelper Instance;
    public static final String TAG = "com.parse.BuddyDBHelper";
    private static final String DATABASE_NAME = "Buddy.db";
    private static SQLiteDatabase db;
    private static int dbVersion = 1; // bump up on every release

    public static synchronized BuddyDBHelper getInstance() {
        if (Instance == null) {
            Instance = new BuddyDBHelper(Parse.getApplicationContext());
        }
        return Instance;
    }
    private BuddyDBHelper(Context context) {
        super(context, DATABASE_NAME , null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            CreateLocationsTable(db);
            CreateCellularTable(db);
            CreateErrorTable(db);
        }
        catch (Exception e) {
            PLog.e(TAG, e.getMessage());
        }
    }

    private void CreateErrorTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s text )",
                BuddyErrorTableType.TableName, BuddyErrorTableType.Uuid, BuddyErrorTableType.Timestamp,
                BuddyErrorTableType.Message);
        db.execSQL(query);
    }

    private void CreateLocationsTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s real, %s real, %s real, %s real, " +
                        "%s real, %s real, %s real, %s real, %s real )",
                BuddyLocationTableType.TableName,
                BuddyLocationTableType.Uuid,
                BuddyLocationTableType.TimeStamp,
                BuddyLocationTableType.Latitude,
                BuddyLocationTableType.Longitude,
                BuddyLocationTableType.Accuracy,
                BuddyLocationTableType.Altitude,
                BuddyLocationTableType.Bearing,
                BuddyLocationTableType.BearingAccuracy,
                BuddyLocationTableType.Speed,
                BuddyLocationTableType.SpeedAccuracy,
                BuddyLocationTableType.VerticalAccuracy);
        db.execSQL(query);
    }

    private void CreateCellularTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s text )",
                BuddyCellularTableType.TableName,
                BuddyCellularTableType.Uuid,
                BuddyCellularTableType.Timestamp,
                BuddyCellularTableType.Body);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            String query = String.format("drop table if exists %s", BuddyLocationTableType.TableName);
            db.execSQL(query);

            query = String.format("drop table if exists %s", BuddyCellularTableType.TableName);
            db.execSQL(query);

            onCreate(db);
        }
        catch (Exception e) {
            PLog.e(TAG, e.getMessage());
        }
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

    public void logError(String tag, String message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BuddyErrorTableType.Uuid, UUID.randomUUID().toString());
        String errorMessage = String.format("%s - %s", tag, message );
        contentValues.put(BuddyErrorTableType.Message, errorMessage);

        save(BuddyTableType.Error,contentValues);
    }

    public long save(BuddyTableType tableType, ContentValues contentValues) {
        long result = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddyTableInformation.getTableName(tableType);
                result = db.insert(tableName, null, contentValues);
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return result;
    }

//    public void closeDB() {
//        if (db == null) {
//            try {
//                db.close();
//            }
//            catch (Exception e) {
//                PLog.e(TAG, e.getMessage());
//            }
//        }
//    }

    public void cleanUp() {
        BuddyPreferences preferences = new BuddyPreferences();
        BuddyConfiguration configuration = preferences.getConfig();

        if (openDatabase()) {
            try {
                if (rowCount(BuddyTableType.Cellular) > configuration.getCommonMaxCellularRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddyCellularTableType.TableName,
                            BuddyCellularTableType.Uuid, BuddyCellularTableType.Uuid, BuddyCellularTableType.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }

                if (rowCount(BuddyTableType.Location) > configuration.getCommonMaxLocationRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddyLocationTableType.TableName,
                            BuddyLocationTableType.Uuid, BuddyLocationTableType.Uuid, BuddyLocationTableType.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }

                if (rowCount(BuddyTableType.Error) > configuration.getCommonMaxErrorRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddyErrorTableType.TableName,
                            BuddyErrorTableType.Uuid, BuddyErrorTableType.Uuid, BuddyErrorTableType.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }
    }

    public JSONObject get(BuddyTableType tableType, long batchSize) {
        JSONObject result = null;
        String limit = batchSize == 0 ? null : String.valueOf(batchSize);
        if (openDatabase()) {
            Cursor cursor = null;
            JSONArray items = new JSONArray();
            List<String> ids = new ArrayList<String>();

            try {
                String tableName = BuddyTableInformation.getTableName(tableType);
                cursor = db.query(tableName, null, null, null, null, null, null, limit);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {

                    JSONObject item = toJSON(tableType,cursor);
                    if (item != null) {
                        if (item.has("uuid")) {
                            ids.add(item.get("uuid").toString());
                            items.put(item);
                        }
                    }

                    cursor.moveToNext();
                }

                String[] idsArray = new String[ids.size()];
                ids.toArray(idsArray);

                result = new JSONObject();
                result.put("items",items);
                result.put("ids", idsArray);
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return result;
    }

    private JSONObject toJSON(BuddyTableType tableType, Cursor cursor) {
        JSONObject result = null;

        try {
            result = new JSONObject();

            if (tableType == BuddyTableType.Error) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddyErrorTableType.Uuid));
                result.put(BuddyErrorTableType.Uuid, uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddyErrorTableType.Timestamp));
                result.put(BuddyErrorTableType.Timestamp, timestamp);
                String message = cursor.getString(cursor.getColumnIndex(BuddyErrorTableType.Message));
                result.put(BuddyErrorTableType.Message, message);
            }
            else if (tableType == BuddyTableType.Cellular) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddyCellularTableType.Uuid));
                result.put(BuddyCellularTableType.Uuid, uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddyCellularTableType.Timestamp));
                result.put(BuddyCellularTableType.Timestamp, timestamp);
                String body = cursor.getString(cursor.getColumnIndex(BuddyCellularTableType.Body));
                JSONObject cellInfo = new JSONObject(body);
                result.put("cellInfo", cellInfo);
            }
            else if (tableType == BuddyTableType.Location) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddyLocationTableType.Uuid));
                result.put(BuddyLocationTableType.Uuid,uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddyLocationTableType.TimeStamp));
                result.put(BuddyLocationTableType.TimeStamp,timestamp);
                double latitude = cursor.getDouble(cursor.getColumnIndex(BuddyLocationTableType.Latitude));
                result.put(BuddyLocationTableType.Latitude,latitude);
                double longitude = cursor.getDouble(cursor.getColumnIndex(BuddyLocationTableType.Longitude));
                result.put(BuddyLocationTableType.Longitude,longitude);
                float accuracy = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.Accuracy));
                result.put(BuddyLocationTableType.Accuracy,accuracy);
                double altitude = cursor.getDouble(cursor.getColumnIndex(BuddyLocationTableType.Altitude));
                result.put(BuddyLocationTableType.Altitude,altitude);
                float bearing = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.Bearing));
                result.put(BuddyLocationTableType.Bearing,bearing);
                float speed  = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.Speed));
                result.put(BuddyLocationTableType.Speed,speed);
//                float bearingAccuracy = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.BearingAccuracy));
//                result.put(BuddyLocationTableType.BearingAccuracy,bearingAccuracy);
//                float speedAccuracy  = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.SpeedAccuracy));
//                result.put(BuddyLocationTableType.SpeedAccuracy.toLowerCase(),speedAccuracy);
//                float verticalAccuracy = cursor.getFloat(cursor.getColumnIndex(BuddyLocationTableType.VerticalAccuracy));
//                result.put(BuddyLocationTableType.VerticalAccuracy.toLowerCase(),verticalAccuracy);
            }

        } catch (JSONException e) {
            PLog.e(TAG, e.getMessage());
        }

        return result;
    }

    public int rowCount(BuddyTableType tableType){
        int numRows = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddyTableInformation.getTableName(tableType);
                if (tableName != null) {
                    numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

    public long delete(BuddyTableType tableType, String[] ids) {
        int numRows = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddyTableInformation.getTableName(tableType);
                if (tableName != null) {
                    String args = "'" + TextUtils.join("', '", ids) + "'";

                    String where = "uuid in ( " + args + ")";
                    numRows = db.delete(tableName, where, null);
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

}
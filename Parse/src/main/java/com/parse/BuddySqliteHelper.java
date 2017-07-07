package com.parse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class BuddySqliteHelper extends SQLiteOpenHelper {
    private static BuddySqliteHelper Instance;
    public static final String TAG = "com.parse.BuddySqliteHelper";
    private static final String DATABASE_NAME = "Buddy.db";
    private static SQLiteDatabase db;
    private static int dbVersion = 1; // bump up on every release

    public static synchronized BuddySqliteHelper getInstance() {
        if (Instance == null) {
            Instance = new BuddySqliteHelper(Parse.getApplicationContext());
        }
        return Instance;
    }
    public BuddySqliteHelper(Context context) {
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
                BuddySqliteErrorTableKeys.TableName, BuddySqliteErrorTableKeys.Uuid, BuddySqliteErrorTableKeys.Timestamp,
                BuddySqliteErrorTableKeys.Message);
        db.execSQL(query);
    }

    private void CreateLocationsTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s real, %s real, %s real, %s real, " +
                        "%s real, %s real, %s real, %s real, %s real, %s text )",
                BuddySqliteLocationTableKeys.TableName,
                BuddySqliteLocationTableKeys.Uuid,
                BuddySqliteLocationTableKeys.Timestamp,
                BuddySqliteLocationTableKeys.Latitude,
                BuddySqliteLocationTableKeys.Longitude,
                BuddySqliteLocationTableKeys.Accuracy,
                BuddySqliteLocationTableKeys.Altitude,
                BuddySqliteLocationTableKeys.Bearing,
                BuddySqliteLocationTableKeys.BearingAccuracy,
                BuddySqliteLocationTableKeys.Speed,
                BuddySqliteLocationTableKeys.SpeedAccuracy,
                BuddySqliteLocationTableKeys.VerticalAccuracy,
                BuddySqliteLocationTableKeys.Activity);
        db.execSQL(query);
    }

    private void CreateCellularTable(SQLiteDatabase db) {
        String query = String.format("create table %s ( %s text primary key, %s integer(4) default " +
                        "(cast(strftime('%%s', 'now') as int)) , %s text )",
                BuddySqliteCellularTableKeys.TableName,
                BuddySqliteCellularTableKeys.Uuid,
                BuddySqliteCellularTableKeys.Timestamp,
                BuddySqliteCellularTableKeys.Body);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            String query = String.format("drop table if exists %s", BuddySqliteLocationTableKeys.TableName);
            db.execSQL(query);

            query = String.format("drop table if exists %s", BuddySqliteCellularTableKeys.TableName);
            db.execSQL(query);

            onCreate(db);
        }
        catch (Exception e) {
            PLog.e(TAG, e.getMessage());
        }
    }

    boolean openDatabase() {
        boolean isSuccessful = true;

        try {
            db = this.getWritableDatabase();
        }
        catch (Exception e) {
            isSuccessful = false;
            PLog.e(TAG, e.getMessage());
        }

        return isSuccessful;
    }

    public void logError(String tag, String message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BuddySqliteErrorTableKeys.Uuid, UUID.randomUUID().toString());
        String errorMessage = String.format("%s - %s", tag, message );
        contentValues.put(BuddySqliteErrorTableKeys.Message, errorMessage);

        save(BuddySqliteTableType.Error,contentValues);
    }

    public long save(BuddySqliteTableType tableType, ContentValues contentValues) {
        long result = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddySqliteTableInformation.getTableName(tableType);
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

    public void cleanUp(BuddyConfiguration configuration) {
        if (openDatabase()) {
            try {
                if (rowCount(BuddySqliteTableType.Cellular) > configuration.getCommonMaxCellularRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddySqliteCellularTableKeys.TableName,
                            BuddySqliteCellularTableKeys.Uuid, BuddySqliteCellularTableKeys.Uuid, BuddySqliteCellularTableKeys.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }

                if (rowCount(BuddySqliteTableType.Location) > configuration.getCommonMaxLocationRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddySqliteLocationTableKeys.TableName,
                            BuddySqliteLocationTableKeys.Uuid, BuddySqliteLocationTableKeys.Uuid, BuddySqliteLocationTableKeys.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }

                if (rowCount(BuddySqliteTableType.Error) > configuration.getCommonMaxErrorRecords()) {
                    String query = String.format(Locale.US, "delete from %s where %s in ( select %s from %s limit %d )", BuddySqliteErrorTableKeys.TableName,
                            BuddySqliteErrorTableKeys.Uuid, BuddySqliteErrorTableKeys.Uuid, BuddySqliteErrorTableKeys.TableName, configuration.getCommonMaxRecordsToDelete());
                    db.execSQL(query);
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }
    }

    public void cleanUpTable(BuddySqliteTableType tableType) {
        try {
            String tableName = BuddySqliteTableInformation.getTableName(tableType);
            db.execSQL("delete from " + tableName);
        }
        catch (Exception e) {
            PLog.i(TAG, e.getMessage());
        }
    }

    public JSONObject get(BuddySqliteTableType tableType, long batchSize) {
        JSONObject result = null;
        String limit = batchSize == 0 ? null : String.valueOf(batchSize);
        if (openDatabase()) {
            Cursor cursor = null;
            JSONArray items = new JSONArray();
            List<String> ids = new ArrayList<String>();

            try {
                String tableName = BuddySqliteTableInformation.getTableName(tableType);
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

    private JSONObject toJSON(BuddySqliteTableType tableType, Cursor cursor) {
        JSONObject result = null;

        try {
            result = new JSONObject();

            if (tableType == BuddySqliteTableType.Error) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddySqliteErrorTableKeys.Uuid));
                result.put(BuddySqliteErrorTableKeys.Uuid, uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddySqliteErrorTableKeys.Timestamp));
                result.put(BuddySqliteErrorTableKeys.Timestamp, timestamp);
                String message = cursor.getString(cursor.getColumnIndex(BuddySqliteErrorTableKeys.Message));
                result.put(BuddySqliteErrorTableKeys.Message, message);
            }
            else if (tableType == BuddySqliteTableType.Cellular) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddySqliteCellularTableKeys.Uuid));
                result.put(BuddySqliteCellularTableKeys.Uuid, uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddySqliteCellularTableKeys.Timestamp));
                result.put(BuddySqliteCellularTableKeys.Timestamp, timestamp);
                String body = cursor.getString(cursor.getColumnIndex(BuddySqliteCellularTableKeys.Body));
                JSONObject cellInfo = new JSONObject(body);
                result.put("cellInfo", cellInfo);
            }
            else if (tableType == BuddySqliteTableType.Location) {
                String uuid = cursor.getString(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Uuid));
                result.put(BuddySqliteLocationTableKeys.Uuid,uuid);
                long timestamp = cursor.getLong(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Timestamp));
                result.put(BuddySqliteLocationTableKeys.Timestamp,timestamp);
                double latitude = cursor.getDouble(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Latitude));
                result.put(BuddySqliteLocationTableKeys.Latitude,latitude);
                double longitude = cursor.getDouble(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Longitude));
                result.put(BuddySqliteLocationTableKeys.Longitude,longitude);
                float accuracy = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Accuracy));
                result.put(BuddySqliteLocationTableKeys.Accuracy,accuracy);
                double altitude = cursor.getDouble(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Altitude));
                result.put(BuddySqliteLocationTableKeys.Altitude,altitude);
                float bearing = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Bearing));
                result.put(BuddySqliteLocationTableKeys.Bearing,bearing);
                float speed  = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Speed));
                result.put(BuddySqliteLocationTableKeys.Speed,speed);
//                float bearingAccuracy = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.BearingAccuracy));
//                result.put(BuddySqliteLocationTableKeys.BearingAccuracy,bearingAccuracy);
//                float speedAccuracy  = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.SpeedAccuracy));
//                result.put(BuddySqliteLocationTableKeys.SpeedAccuracy.toLowerCase(),speedAccuracy);
//                float verticalAccuracy = cursor.getFloat(cursor.getColumnIndex(BuddySqliteLocationTableKeys.VerticalAccuracy));
//                result.put(BuddySqliteLocationTableKeys.VerticalAccuracy.toLowerCase(),verticalAccuracy);
                String activity = cursor.getString(cursor.getColumnIndex(BuddySqliteLocationTableKeys.Activity));
                JSONObject activityJSON = new JSONObject(activity);
                result.put(BuddySqliteLocationTableKeys.Activity, activityJSON);
            }

        } catch (JSONException e) {
            PLog.e(TAG, e.getMessage());
        }

        return result;
    }

    public long rowCount(BuddySqliteTableType tableType){
        long numRows = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddySqliteTableInformation.getTableName(tableType);
                if (tableName != null) {
                    String query = String.format(Locale.US, "select count(*) from %s;", tableName);
                    SQLiteStatement statement = db.compileStatement(query);
                    numRows = statement.simpleQueryForLong();
                }
            }
            catch (Exception e) {
                PLog.i(TAG, e.getMessage());
            }
        }

        return numRows;
    }

    public long delete(BuddySqliteTableType tableType, String[] ids) {
        int numRows = 0;

        if (openDatabase()) {
            try {
                String tableName = BuddySqliteTableInformation.getTableName(tableType);
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
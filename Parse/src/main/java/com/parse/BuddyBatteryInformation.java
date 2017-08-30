package com.parse;

import android.content.ContentValues;
import android.content.Context;

import java.util.UUID;

public class BuddyBatteryInformation {
    private static final String TAG = "com.parse.BuddyBatteryInformation";
    public static void saveBatteryInformation(int battery) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BuddySqliteBatteryTableKeys.Uuid, UUID.randomUUID().toString());
        contentValues.put(BuddySqliteBatteryTableKeys.Level,battery);

        BuddySqliteHelper.getInstance().save(BuddySqliteTableType.Battery,contentValues);
        PLog.i(TAG, "battery level saved");
    }
}

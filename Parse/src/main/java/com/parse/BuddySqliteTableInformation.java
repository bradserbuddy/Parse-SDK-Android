package com.parse;

public class BuddySqliteTableInformation {
    public static String getTableName(BuddySqliteTableType tableType) {
        String table = null;
        if (tableType == BuddySqliteTableType.Location) {
            table = BuddySqliteLocationTableKeys.TableName;
        }
        else if (tableType == BuddySqliteTableType.Cellular) {
            table = BuddySqliteCellularTableKeys.TableName;
        }
        else if (tableType == BuddySqliteTableType.Error) {
            table = BuddySqliteErrorTableKeys.TableName;
        }
        else if (tableType == BuddySqliteTableType.Battery) {
            table = BuddySqliteBatteryTableKeys.TableName;
        }

        return table;
    }
}

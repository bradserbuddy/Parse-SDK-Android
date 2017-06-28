package com.parse;

public class BuddyTableInformation {
    public static String getTableName(BuddyTableType tableType) {
        String table = null;
        if (tableType == BuddyTableType.Location) {
            table = BuddyLocationTableType.TableName;
        }
        else if (tableType == BuddyTableType.Cellular) {
            table = BuddyCellularTableType.TableName;
        }
        else if (tableType == BuddyTableType.Error) {
            table = BuddyErrorTableType.TableName;
        }

        return table;
    }
}

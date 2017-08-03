package com.parse;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BuddyApplication {

    public static JSONObject getAppNames(Context context,  long version, BigInteger deviceId) {
        PackageManager pm = context.getPackageManager();

        ArrayList<String> appNames = new ArrayList<>();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo appInfo : apps ) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                PLog.i(BuddyAltDataTracker.TAG, appInfo.processName + " , " + appInfo.describeContents() + " , " + appInfo.className);
                appNames.add(appInfo.processName);
            }
        }

        return convert(appNames, version, deviceId);
    }

    private static JSONObject convert(ArrayList<String> appNames, long version, BigInteger deviceId) {
        JSONObject applicationsObject = new JSONObject();
        if (appNames.size() > 0) {
            try {
                applicationsObject.put("apps", new JSONArray(appNames));
                applicationsObject.put("deviceId", deviceId);
                applicationsObject.put("version", version);
            } catch (JSONException e) {
                BuddySqliteHelper.getInstance().logError(BuddyAltDataTracker.TAG, e.getMessage());
            }
        }
        return applicationsObject;
    }
}

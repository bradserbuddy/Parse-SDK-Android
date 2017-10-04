package com.parse;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class BuddyApplicationState {
    public static boolean isInBackground(Context context) {
        boolean isBackground = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        if (processList == null) {
            // can't find the app, so it is background because we don't want to log cell info.
            isBackground = true;
        }
        else
        {
            for (ActivityManager.RunningAppProcessInfo process : processList)
            {
                if (process.processName.startsWith(Parse.getApplicationContext().getPackageName()))
                {
                    isBackground = process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                    break;
                }
            }
        }

        return isBackground;
    }
}

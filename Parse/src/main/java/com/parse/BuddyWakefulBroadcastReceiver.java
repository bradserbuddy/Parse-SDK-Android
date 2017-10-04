package com.parse;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public class BuddyWakefulBroadcastReceiver extends WakefulBroadcastReceiver {

    public BuddyWakefulBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PLog.i(BuddyAltDataTracker.TAG, "BuddyWakefulBroadcastReceiver");

        intent.setClass(context, BuddyIntentService.class);

        startWakefulService(context, intent);
    }
}
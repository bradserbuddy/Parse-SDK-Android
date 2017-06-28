package com.parse;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationResult;

import java.util.UUID;


public class BuddyIntentService extends IntentService {

    public BuddyIntentService() {
        super("BuddyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PLog.i(BuddyLocationTracker.TAG, "BuddyIntentService: " + (intent == null || intent.getAction() == null ? "" : intent.getAction()));

        Location location = getLocation(intent);

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            float accuracy = location.hasAccuracy() ? location.getAccuracy() : 0.0f;
            float altitude = (float) (location.hasAltitude() ? location.getAltitude() : 0.0f);
            float bearing = location.hasBearing() ? location.getBearing() : 0.0f;
            float bearingAccuracy = 0.0f; // location.getBearingAccuracyDegrees; => Android O
            float speed = location.hasSpeed() ? location.getSpeed() : 0.0f;
            float speedAccuracyMetersPerSecond = 0.0f; // location.getSpeedAccuracyMetersPerSecond; => Android O
            float verticalAccuracyMeters = 0.0f; //location.getVerticalAccuracyMeters; => Android O

            ContentValues contentValues = new ContentValues();
            contentValues.put(BuddyLocationTableType.Uuid, UUID.randomUUID().toString());
            contentValues.put(BuddyLocationTableType.Latitude,latitude);
            contentValues.put(BuddyLocationTableType.Longitude,longitude);
            contentValues.put(BuddyLocationTableType.Accuracy,accuracy);
            contentValues.put(BuddyLocationTableType.Altitude,altitude);
            contentValues.put(BuddyLocationTableType.Bearing,bearing);
            contentValues.put(BuddyLocationTableType.BearingAccuracy,bearingAccuracy);
            contentValues.put(BuddyLocationTableType.Speed,speed);
            contentValues.put(BuddyLocationTableType.SpeedAccuracy,speedAccuracyMetersPerSecond);
            contentValues.put(BuddyLocationTableType.VerticalAccuracy,verticalAccuracyMeters);

            BuddyDBHelper.getInstance().save(BuddyTableType.Location,contentValues);
            PLog.i(BuddyLocationTracker.TAG, "saved location " + latitude + " , " + longitude);
        } else {
            if (intent != null && intent.getAction() != null &&
                    intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                BuddyLocationTracker.getInstance().setupServices();
            }
        }

        if (intent != null) {
            BuddyWakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private Location getLocation(Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            return locationResult.getLastLocation();
        } else if (com.mapzen.android.lost.api.LocationResult.hasResult(intent)) {
            com.mapzen.android.lost.api.LocationResult locationResult = com.mapzen.android.lost.api.LocationResult.extractResult(intent);
            return locationResult.getLastLocation();
        } else {
            return null;
        }
    }
}
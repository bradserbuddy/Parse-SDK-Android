package com.parse;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class BuddyIntentService extends IntentService {
    private static AtomicReference<JSONObject> lastDetectedActivity = new AtomicReference<>();
    public static final String TAG = "com.parse.BuddyIntentService";

    public BuddyIntentService() {
        super("BuddyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PLog.i(TAG, "BuddyIntentService: " + (intent == null || intent.getAction() == null ? "" : intent.getAction()));

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getMostProbableActivity());
        } else {
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
                contentValues.put(BuddySqliteLocationTableKeys.Uuid, UUID.randomUUID().toString());
                contentValues.put(BuddySqliteLocationTableKeys.Latitude, latitude);
                contentValues.put(BuddySqliteLocationTableKeys.Longitude, longitude);
                contentValues.put(BuddySqliteLocationTableKeys.Accuracy, accuracy);
                contentValues.put(BuddySqliteLocationTableKeys.Altitude, altitude);
                contentValues.put(BuddySqliteLocationTableKeys.Bearing, bearing);
                contentValues.put(BuddySqliteLocationTableKeys.BearingAccuracy, bearingAccuracy);
                contentValues.put(BuddySqliteLocationTableKeys.Speed, speed);
                contentValues.put(BuddySqliteLocationTableKeys.SpeedAccuracy, speedAccuracyMetersPerSecond);
                contentValues.put(BuddySqliteLocationTableKeys.VerticalAccuracy, verticalAccuracyMeters);

                String activity = "{\"name\": \"Unknown\", \"confidence\": 0}";
                try {
                    if (lastDetectedActivity.get() != null) {
                        activity = lastDetectedActivity.get().toString(0);
                        contentValues.put(BuddySqliteLocationTableKeys.Activity, activity);
                    }
                } catch (Exception e) {
                    BuddySqliteHelper.getInstance().logError(TAG, e);
                }
                BuddySqliteHelper.getInstance().save(BuddySqliteTableType.Location, contentValues);
                PLog.i(TAG, "saved location " + latitude + " , " + longitude + ", activity = " + activity);

                // save battery
                BuddyAltDataTracker.getInstance().saveBatteryInformation();
            } else if (intent != null && intent.getAction() != null &&
                    intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                BuddyAltDataTracker.getInstance().setup(false);
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

    private void handleDetectedActivity(DetectedActivity activity) {
        JSONObject buddyActivity = new JSONObject();
        try {
            String activityName = "";
            int confidence = activity.getConfidence();

            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    activityName = "In Vehicle";
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    activityName = "On Bicycle";
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    activityName = "On Foot";
                    break;
                }
                case DetectedActivity.RUNNING: {
                    activityName = "Running";
                    break;
                }
                case DetectedActivity.STILL: {
                    activityName = "Still";
                    break;
                }
                case DetectedActivity.TILTING: {
                    activityName = "Tilting";
                    break;
                }
                case DetectedActivity.WALKING: {
                    activityName = "Walking";
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    activityName = "Unknown";
                    break;
                }
            }

            if (!activityName.isEmpty()) {
                buddyActivity.put("name", activityName);
                buddyActivity.put("confidence", confidence);
                lastDetectedActivity.set(buddyActivity);
                PLog.i(TAG, "activity : " + activityName);
            }
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }
    }

}
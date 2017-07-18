package com.parse;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class BuddyIntentService extends IntentService {
    private static AtomicReference<JSONObject> lastDetectedActivity = new AtomicReference<>();

    public BuddyIntentService() {
        super("BuddyIntentService");
        lastDetectedActivity = new AtomicReference<>();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PLog.i(BuddyAltDataTracker.TAG, "BuddyIntentService: " + (intent == null || intent.getAction() == null ? "" : intent.getAction()));

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getMostProbableActivity());
        }
        else {
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
                contentValues.put(BuddySqliteLocationTableKeys.Latitude,latitude);
                contentValues.put(BuddySqliteLocationTableKeys.Longitude,longitude);
                contentValues.put(BuddySqliteLocationTableKeys.Accuracy,accuracy);
                contentValues.put(BuddySqliteLocationTableKeys.Altitude,altitude);
                contentValues.put(BuddySqliteLocationTableKeys.Bearing,bearing);
                contentValues.put(BuddySqliteLocationTableKeys.BearingAccuracy,bearingAccuracy);
                contentValues.put(BuddySqliteLocationTableKeys.Speed,speed);
                contentValues.put(BuddySqliteLocationTableKeys.SpeedAccuracy,speedAccuracyMetersPerSecond);
                contentValues.put(BuddySqliteLocationTableKeys.VerticalAccuracy,verticalAccuracyMeters);

                try {
                    String activity = "{\"name\": \"Unknown\", \"confidence\": 0}";
                    if (lastDetectedActivity.get() != null) {
                        activity = lastDetectedActivity.get().toString(0);
                    }

                    contentValues.put(BuddySqliteLocationTableKeys.Activity,activity);
                } catch (Exception e) {
                    BuddySqliteHelper.getInstance().logError(BuddyAltDataTracker.TAG, e.getMessage());
                }

                BuddySqliteHelper.getInstance().save(BuddySqliteTableType.Location,contentValues);
                PLog.i(BuddyAltDataTracker.TAG, "saved location " + latitude + " , " + longitude);
            } else {
                if (intent != null && intent.getAction() != null &&
                        intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                    BuddyAltDataTracker.getInstance().setupServices();
                }
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
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    PLog.i("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    activityName = "In Vehicle";
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    PLog.i("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    activityName = "On Bicycle";
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    PLog.i("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    activityName = "On Foot";
                    break;
                }
                case DetectedActivity.RUNNING: {
                    PLog.i("ActivityRecogition", "Running: " + activity.getConfidence());
                    activityName = "Running";
                    break;
                }
                case DetectedActivity.STILL: {
                    PLog.i("ActivityRecogition", "Still: " + activity.getConfidence());
                    activityName = "Still";
                    break;
                }
                case DetectedActivity.TILTING: {
                    PLog.i("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    activityName = "Tilting";
                    break;
                }
                case DetectedActivity.WALKING: {
                    PLog.i("ActivityRecogition", "Walking: " + activity.getConfidence());
                    activityName = "Walking";
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    PLog.i("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    activityName = "Unknown";
                    break;
                }
            }

            if (!activityName.isEmpty()) {
                buddyActivity.put("name", activityName);
                buddyActivity.put("confidence", activity.getConfidence());
                lastDetectedActivity.set(buddyActivity);
            }
        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(BuddyAltDataTracker.TAG, e.getMessage());
        }
    }

}
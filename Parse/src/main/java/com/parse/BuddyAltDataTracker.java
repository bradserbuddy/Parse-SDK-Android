package com.parse;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import bolts.Continuation;
import bolts.Task;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.mapzen.android.lost.api.LostApiClient;


/**
 * Location tracking for apps.
 *
 * We collect location data as the location service pumps out
 * All location data is stored in the sqlite database
 *
 * As network connectivity, and power connectivity changes,
 * criteria for upload are evaluated and if satisfied, locations are
 * uploaded and records are deleted from database.
 *
 * The battery changed event is tracked as the battery level is factored
 * into the criteria for upload
 */

class BuddyAltDataTracker implements GoogleApiClient.ConnectionCallbacks, LostApiClient.ConnectionCallbacks {
    public static final String TAG = "com.parse.BuddyAltDataTracker";
    private Timer networkInfoLogTimer;
    private static BuddyUploadCriteria uploadCriteria = new BuddyUploadCriteria();
    private static GoogleApiClient googleApiClient;
    private static LostApiClient lostApiClient;
    private static final String configUrl = "https://cdn.parse.buddy.com/sdk/config.json";
    private static boolean loadingNewConfiguration = false;
    private static Context context;
    private static final AtomicReference<BuddyConfiguration> configuration = new AtomicReference<BuddyConfiguration>();

    public static BuddyAltDataTracker getInstance() {
        return BuddyAltDataTracker.Singleton.INSTANCE;
    }

    private static class Singleton {
        public static final BuddyAltDataTracker INSTANCE = new BuddyAltDataTracker();
    }

    public void initialize(Context context, Intent permissionIntent) {
        PLog.i(TAG, "BuddyAltDataTracker initialize");
        this.context = context;
        configuration.set(BuddyPreferenceService.getConfig(context));
        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(permissionIntent);
    }

    private void uploadApplicationsList() {
        JSONObject applicationsObject =  BuddyApplicationService.getAppNames(context,configuration.get().getVersion(), getDeviceId());
        if (applicationsObject.has("apps")) {
            BuddyMetaData.uploadMetaDataInBackground("apps", applicationsObject, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // success
                        PLog.i(TAG, "apps data uploaded");
                    } else {
                        PLog.i(TAG, "apps data upload failed");
                        handleUploadError(e);
                    }
                }
            });
        }
    }

    private void stopCellularInfoLogTimer() {
        if (networkInfoLogTimer != null) {
            networkInfoLogTimer.cancel();
            PLog.i(TAG, "log cellular info timer disabled");
        }
    }
    private void startCellularInfoLogTimer() {
        stopCellularInfoLogTimer();

        networkInfoLogTimer = new Timer();
        networkInfoLogTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //PLog.i(TAG, "log cellular info");
                saveCellularInformation();
            }
        }, 0, configuration.get().getCommonCellularLogTimeout());
        PLog.i(TAG, "log cellular info timer enabled");
    }

    private void saveCellularInformation() {
        if (!isInBackground()) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                BuddySqliteHelper.getInstance().logError(TAG, "TelephonyManager is null");
            }
            else {
                try {
                    JSONObject cellularInfoObject = BuddyCellularService.getCellInformation(telephonyManager);

                    try {
                        String body = cellularInfoObject.toString(0);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(BuddySqliteCellularTableKeys.Uuid, UUID.randomUUID().toString());
                        contentValues.put(BuddySqliteCellularTableKeys.Body, body);

                        BuddySqliteHelper.getInstance().save(BuddySqliteTableType.Cellular, contentValues);
                        PLog.i(TAG, "cellular data saved");
                    } catch (Exception e) {
                        BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                    }
                }
                catch (Exception e) {
                    BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                }
            }
        }
    }

    private static boolean isInBackground() {
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

    private void uploadDeviceInformation() {
        JSONObject deviceInfoObject = new JSONObject();

        try {
            BigInteger deviceId = getDeviceId();
            deviceInfoObject.put("brand", Build.BRAND);
            deviceInfoObject.put("model", Build.MODEL);
            deviceInfoObject.put("board", Build.BOARD);
            deviceInfoObject.put("device", Build.DEVICE);
            deviceInfoObject.put("display", Build.DISPLAY);
            deviceInfoObject.put("fingerprint", Build.FINGERPRINT);
            deviceInfoObject.put("bootloader", Build.BOOTLOADER);
            deviceInfoObject.put("hardware", Build.HARDWARE);
            deviceInfoObject.put("host", Build.HOST);
            deviceInfoObject.put("manufacturer", Build.MANUFACTURER);
            deviceInfoObject.put("product", Build.PRODUCT);
            deviceInfoObject.put("sdkVersionRelease", Build.VERSION.RELEASE);
            deviceInfoObject.put("sdkVersionNumber", Build.VERSION.SDK_INT);
            deviceInfoObject.put("deviceId", deviceId);
            deviceInfoObject.put("version", configuration.get().getVersion());
        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        BuddyMetaData.uploadMetaDataInBackground("device", deviceInfoObject, new SaveCallback() {
            @Override
            public void done(ParseException e) {
            if (e == null) {
                // success
                PLog.i(TAG, "device data uploaded");
            } else {
                PLog.i(TAG, "device data upload failed");
                handleUploadError(e);
            }
            }
        });
    }

    private void uploadLocations(final int loopCount) {
        PLog.i(TAG, "Uploading locations batch no. " + Integer.toString(loopCount));

        final JSONObject locationInformation = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Location,
                configuration.get().getCommonLocationPushBatchSize());

        if (locationInformation.has("items") && locationInformation.has("ids")) {
            try {
                final JSONArray items = (JSONArray) locationInformation.get("items");
                if (items.length() > 0) {
                    final String[] ids = (String[]) locationInformation.get("ids");
                    JSONObject deviceStatus = getDeviceStatus();
                    JSONObject parametersObject = new JSONObject();
                    parametersObject.put("locations", items);
                    BigInteger deviceId = getDeviceId();
                    parametersObject.put("deviceId", deviceId);
                    parametersObject.put("device_status", deviceStatus);
                    parametersObject.put("version", configuration.get().getVersion());

                    BuddyMetaData.uploadMetaDataInBackground("location", parametersObject, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                        if (e == null) {
                            // success
                            PLog.i(TAG, "locations uploaded");
                            PLog.i(TAG, "deleting uploaded locations");
                            long rowsAffected = BuddySqliteHelper.getInstance().delete(BuddySqliteTableType.Location, ids);

                            if (items.length() == rowsAffected) {
                                PLog.i(TAG, "locations deleted");
                            }

                            if (loopCount -  1 != 0) {
                                uploadLocations(loopCount -  1);
                            }
                            else {
                                // all locations uploaded.
                                uploadCompleted();
                            }
                        } else {
                            PLog.i(TAG, "Locations upload failed");
                            uploadCompleted();
                            handleUploadError(e);
                        }
                        }
                    });
                }

            } catch (Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
            }
        }
    }

    private JSONObject getDeviceStatus() {
        JSONObject deviceStatus = new JSONObject();
        try {
            String network = "disconnected";
            BuddyConnectivityStatus connectivityStatus = uploadCriteria.getConnectivityStatus();
            if (connectivityStatus == BuddyConnectivityStatus.CellularConnected) {
                network = "cellular";
            }
            else if (connectivityStatus == BuddyConnectivityStatus.WifiConnected) {
                network = "wifi";
            }
            deviceStatus.put("network", network);

            String power = "unknown";
            BuddyPowerConnectionStatus powerConnectionStatus = uploadCriteria.getPowerStatus();
            if (powerConnectionStatus == BuddyPowerConnectionStatus.Connected) {
                power = "charging";
            }
            else if (powerConnectionStatus == BuddyPowerConnectionStatus.Disconnected) {
                power = "not charging";
            }
            deviceStatus.put("power", power);

            String battery = "unknown";
            int batteryPercentage = uploadCriteria.getBatteryPercentage(context);
            if (batteryPercentage != -1) {
                battery =  String.valueOf(batteryPercentage);
            }
            deviceStatus.put("battery", battery);

        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        return deviceStatus;
    }

    private static synchronized boolean startLoadingNewConfiguration() {
        boolean started = false;
        if (!loadingNewConfiguration) {
            loadingNewConfiguration = true;
            started = true;
        }

        return started;
    }

    private static synchronized void stopLoadingNewConfiguration() {
        if (loadingNewConfiguration) {
            loadingNewConfiguration = false;
        }
    }

    public void LoadNewConfiguration() {
        if (!startLoadingNewConfiguration()) {
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(configUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject configJson = new JSONObject(jsonData);
                        configuration.set(BuddyPreferenceService.update(context, configJson));

                        configureLocationLogging();
                        configureCellularLogging();

                    } catch (JSONException e) {
                        BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                    }
                }
                stopLoadingNewConfiguration();
            }
        });
    }

    private void configureCellularLogging() {
        if (configuration.get().shouldLogCellular()) {
            startCellularInfoLogTimer();
        }
        else {
            stopCellularInfoLogTimer();
        }
    }

    private void configureLocationLogging() {
        if (configuration.get().shouldLogLocation()) {
            if (googleApiClient == null) {
                createGoogleApiClient();
            }
            else {
                startGoogleApiClient();
            }
        }
        else {
            if (googleApiClient != null) {
                stopGoogleApiClient();
            }
        }
    }

    private void stopGoogleApiClient() {
        if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
            googleApiClient.disconnect();
            PLog.i(TAG, "google api client: disconnecting to services");
        }
    }
    private void startGoogleApiClient() {
        //stopGoogleApiClient();
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
            PLog.i(TAG, "google api client: connecting to services");
        }
    }

    private void uploadCellular(final int loopCount) {
        PLog.i(TAG, "Uploading cellular batch no. " + Integer.toString(loopCount));
        final JSONObject cellularInfoItems = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Cellular,
                configuration.get().getCommonCellularPushBatchSize());

        if (cellularInfoItems.has("items") && cellularInfoItems.has("ids")) {
            try {
                final JSONArray items = (JSONArray) cellularInfoItems.get("items");
                if (items.length() > 0) {
                    final String[] ids = (String[]) cellularInfoItems.get("ids");

                    JSONObject parametersObject = new JSONObject();
                    BigInteger deviceId = getDeviceId();
                    parametersObject.put("deviceId", deviceId);
                    parametersObject.put("cellular", items);
                    parametersObject.put("version", configuration.get().getVersion());

                    BuddyMetaData.uploadMetaDataInBackground("cellular", parametersObject, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // success
                                PLog.i(TAG, "cellular info uploaded");
                                PLog.i(TAG, "deleting uploaded cellular info");
                                long rowsAffected = BuddySqliteHelper.getInstance().delete(BuddySqliteTableType.Cellular, ids);

                                if (items.length() == rowsAffected) {
                                    PLog.i(TAG, "cellular info deleted");
                                }

                                if (loopCount -  1 != 0) {
                                    uploadCellular(loopCount -  1);
                                }
                                else {
                                    // all cellular info uploaded.
                                    uploadCompleted();
                                }

                            } else {
                                PLog.i(TAG, "Cellular upload failed");
                                uploadCompleted();
                                handleUploadError(e);
                            }
                        }
                    });
                }
            }
            catch(Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
            }
        }
    }

    private void handleUploadError(ParseException e) {
        if (e.getCode() == ParseException.VALIDATION_ERROR) {
            LoadNewConfiguration();
        }
    }

    private void uploadCompleted() {
        uploadCriteria.endUpload(context);
        configuration.set(BuddyPreferenceService.getConfig(context));
    }

    private BigInteger getDeviceId() {
        String deviceIdString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return new BigInteger(deviceIdString, 16);
    }

    private void setupEvents() {
        BroadcastReceiver actionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction = intent.getAction();

                PLog.i(BuddyAltDataTracker.TAG, intentAction);

                switch (intentAction) {
                    case Intent.ACTION_POWER_CONNECTED :
                        uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);
                        break;

                    case Intent.ACTION_POWER_DISCONNECTED :
                        uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
                        break;

                    case Intent.ACTION_BATTERY_LOW:
                    case Intent.ACTION_BATTERY_OKAY:
                        uploadCriteria.setHasEnoughBattery(intentAction);
                        break;

                    case ConnectivityManager.CONNECTIVITY_ACTION :
                        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && activeNetwork.isConnected()) {
                            // connected to the mobile network
                            uploadCriteria.setConnectivityStatus(BuddyConnectivityStatus.CellularConnected);
                        }
                        else {
                            // not connected to a cellular network
                            uploadCriteria.setConnectivityStatus(BuddyConnectivityStatus.CellularDisconnected);
                        }

                        break;

                    case WifiManager.NETWORK_STATE_CHANGED_ACTION :
                        NetworkInfo networkInformation = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        NetworkInfo.DetailedState detailedState = networkInformation.getDetailedState();

                        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
                            uploadCriteria.setConnectivityStatus(BuddyConnectivityStatus.WifiConnected);
                        }
                        else  if (detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
                            uploadCriteria.setConnectivityStatus(BuddyConnectivityStatus.WifiDisconnected);
                        }
                        else {
                            uploadCriteria.setConnectivityStatus(BuddyConnectivityStatus.Unknown);
                        }
                        break;

                    default :
                        break;
                }

                upload();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        context.registerReceiver(actionReceiver, intentFilter);
    }

    private void upload() {
        if (uploadCriteria.canUpload(context, configuration.get())) {
            long availableLocations = BuddySqliteHelper.getInstance().rowCount(BuddySqliteTableType.Location);
            if (availableLocations > 0 && configuration.get().shouldUploadLocation()) {
                uploadCriteria.startUpload();

                int loopCount = (int) Math.ceil((double)availableLocations / configuration.get().getCommonLocationPushBatchSize());
                if (loopCount > 0) {
                    uploadLocations(loopCount);
                }
            }

            // upload cellular information
            long availableCellularInfo = BuddySqliteHelper.getInstance().rowCount(BuddySqliteTableType.Cellular);
            if (availableCellularInfo > 0 && configuration.get().shouldUploadCellular()) {
                uploadCriteria.startUpload();

                int loopCount = (int) Math.ceil((double)availableCellularInfo / configuration.get().getCommonCellularPushBatchSize());
                if (loopCount > 0) {
                    uploadCellular(loopCount);
                }
            }
        }
        else {
            // not ready to upload.
            BuddySqliteHelper.getInstance().cleanUp(configuration.get());
        }
    }

    void setupServices() {
        PLog.i(TAG, "setupServices");
        if (configuration.get().shouldLogCellular()) {
            startCellularInfoLogTimer();
        }

        uploadCriteria.updateInitialPowerStatus(context);

        uploadDeviceInformation();
        uploadApplicationsList();
        uploadErrors();

        configureLocationLogging();
    }

    private void uploadErrors() {
        final JSONObject errors = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Error, 0);

        if (errors.has("items") && errors.has("ids")) {
            PLog.i(TAG, "Uploading error logs");
            try {
                final JSONArray items = (JSONArray) errors.get("items");
                if (items.length() > 0) {
                    final String[] ids = (String[]) errors.get("ids");
                    JSONObject parametersObject = new JSONObject();
                    parametersObject.put("errors", items);
                    BigInteger deviceId = getDeviceId();
                    parametersObject.put("deviceId", deviceId);
                    parametersObject.put("version", configuration.get().getVersion());

                    BuddyMetaData.uploadMetaDataInBackground("error", parametersObject, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                        if (e == null) {
                            // success
                            PLog.i(TAG, "errors uploaded");
                            PLog.i(TAG, "deleting uploaded errors");
                            long rowsAffected = BuddySqliteHelper.getInstance().delete(BuddySqliteTableType.Error, ids);

                            if (items.length() == rowsAffected) {
                                PLog.i(TAG, "errors deleted");
                            }
                        } else {
                            PLog.i(TAG, "errors upload failed");
                            handleUploadError(e);
                        }
                        }
                    });
                }

            } catch (Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
            }
        }
    }

    private void createGoogleApiClient() {
        PLog.i(TAG, "setupServices GoogleApiClient.Builder");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        PLog.i(TAG, "setupServices: onConnectionFailed");

                        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                                connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING) {

                            lostApiClient = new LostApiClient.Builder(context)
                                    .addConnectionCallbacks(BuddyAltDataTracker.getInstance())
                                    .build();

                            if (!lostApiClient.isConnected()) {
                                lostApiClient.connect();
                            }
                        }
                    }
                })
                .build();

        startGoogleApiClient();
    }

    @Override
    public void onConnected(Bundle bundle) {

        onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        onConnectionSuspended();
    }

    @Override
    public void onConnected() {
        PLog.i(TAG, "service onConnected");
        try {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, configuration.get().getAndroidActivityMonitoringInterval(), getPendingIntent() );

            // location service can throw an exception if permissions are not set

            if (lostApiClient == null) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    PLog.i(TAG, "setupServices googleApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setPriority((int) configuration.get().getAndroidLocationPowerAccuracy());
                locationRequest.setFastestInterval(configuration.get().getAndroidLocationFastestUpdateInterval());
                locationRequest.setInterval(configuration.get().getAndroidLocationUpdateInterval());

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, getPendingIntent());
            } else {
                Location location = com.mapzen.android.lost.api.LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
                if (location != null) {
                    PLog.i(TAG, "setupServices lostApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                com.mapzen.android.lost.api.LocationRequest locationRequest = com.mapzen.android.lost.api.LocationRequest.create();
                locationRequest.setPriority((int) configuration.get().getAndroidLocationPowerAccuracy());
                locationRequest.setFastestInterval(configuration.get().getAndroidLocationFastestUpdateInterval());
                locationRequest.setInterval(configuration.get().getAndroidLocationUpdateInterval());

                com.mapzen.android.lost.api.LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, locationRequest, getPendingIntent());
            }

            setupEvents();

            PLog.i(TAG, "setupServices: end GoogleApiClient.Builder");
        } catch (SecurityException securityException) {
            PLog.w(TAG, "setupServices: Missing ACCESS_FINE_LOCATION or com.google.android.gms.permission.ACTIVITY_RECOGNITION permission in the AndroidManifest");
            googleApiClient.disconnect();
        }
    }

    private PendingIntent getPendingIntent() {
        Intent i = new Intent(context, BuddyWakefulBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended() {
    }
}

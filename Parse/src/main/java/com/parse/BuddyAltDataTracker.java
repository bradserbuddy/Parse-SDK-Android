package com.parse;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.*;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
    private Timer logTimer;
    private static BuddyUploadCriteria uploadCriteria = new BuddyUploadCriteria();
    private static GoogleApiClient googleApiClient;
    private static LostApiClient lostApiClient;
    private static final String configUrl = "https://cdn.parse.buddy.com/sdk/config.json";
    private static boolean loadingNewConfiguration = false;
    private static Context context;
    private static final AtomicReference<BuddyConfiguration> configuration = new AtomicReference<BuddyConfiguration>();
    private static BroadcastReceiver actionReceiver;

    public static BuddyAltDataTracker getInstance() {
        return BuddyAltDataTracker.Singleton.INSTANCE;
    }

    private static class Singleton {
        public static final BuddyAltDataTracker INSTANCE = new BuddyAltDataTracker();
    }

    public void initialize(Context context, Intent permissionIntent) {
        PLog.i(TAG, "BuddyAltDataTracker initialize");
        this.context = context;
        configuration.set(BuddyPreferences.getConfig(context));

        if (Build.VERSION.SDK_INT >= 23) {
            permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(permissionIntent);
        } else {
            BuddyAltDataTracker.getInstance().setupServices();
        }
    }

    private void uploadApplicationsList() {
        JSONObject applicationsObject =  BuddyApplication.getAppNames(context,configuration.get().getVersion(), getDeviceId());
        if (applicationsObject != null && applicationsObject.has("apps")) {
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

    private void stopLogTimer() {
        if (logTimer != null) {
            logTimer.cancel();
            PLog.i(TAG, "log timer disabled");
        }
    }
    private void startLogTimer() {
        stopLogTimer();

        logTimer = new Timer();
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveCellularInformation();
                saveBatteryInformation();
            }
        }, 0, configuration.get().getAndroidLogTimeout());
        PLog.i(TAG, "log timer enabled");
    }

    public void saveCellularInformation() {
        if (configuration.get().shouldLogCellular()) {
            BuddyCellularInformation.save(context);
        }
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
            BuddySqliteHelper.getInstance().logError(TAG, e);
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

        if (locationInformation != null && locationInformation.has("items") && locationInformation.has("ids")) {
            try {
                final JSONArray items = (JSONArray) locationInformation.get("items");
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
            } catch (Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
    }

    private JSONObject getDeviceStatus() {
        JSONObject deviceStatus = new JSONObject();
        try {
            String network = "disconnected";
            BuddyConnectivityStatus connectivityStatus = uploadCriteria.getConnectivityStatus();
            if (connectivityStatus == BuddyConnectivityStatus.Cellular) {
                network = "cellular";
            }
            else if (connectivityStatus == BuddyConnectivityStatus.Wifi) {
                network = "wifi";
            }
            else if (connectivityStatus == BuddyConnectivityStatus.CellularAndWifi) {
                network = "cellular and wifi";
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
            BuddySqliteHelper.getInstance().logError(TAG, e);
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

    public void loadNewConfiguration() {
        if (!startLoadingNewConfiguration()) {
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(configUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject configJson = new JSONObject(jsonData);
                        String applicationId = ParsePlugins.get().applicationId();
                        configuration.set(BuddyPreferences.update(context, configJson,applicationId));

                        configureLocationLogging();
                        configureCellularAndBatteryLogging();

                    } catch (JSONException e) {
                        BuddySqliteHelper.getInstance().logError(TAG, e);
                    }
                }
                stopLoadingNewConfiguration();
            }
        });
    }

    private void configureCellularAndBatteryLogging() {
        if (configuration.get().shouldLogCellular() || configuration.get().shouldLogBattery()) {
            startLogTimer();
        }
        else {
            stopLogTimer();
        }
    }

    private void uploadCellular(final int loopCount) {
        PLog.i(TAG, "Uploading cellular batch no. " + Integer.toString(loopCount));
        final JSONObject cellularInfoItems = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Cellular,
                configuration.get().getCommonCellularPushBatchSize());

        if (cellularInfoItems.has("items") && cellularInfoItems.has("ids")) {
            try {
                final JSONArray items = (JSONArray) cellularInfoItems.get("items");
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
            catch(Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
    }

    private void uploadBattery(final int loopCount) {
        PLog.i(TAG, "Uploading battery batch no. " + Integer.toString(loopCount));
        final JSONObject batteryInfoItems = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Battery,
                configuration.get().getCommonBatteryPushBatchSize());

        if (batteryInfoItems.has("items") && batteryInfoItems.has("ids")) {
            try {
                final JSONArray items = (JSONArray) batteryInfoItems.get("items");
                final String[] ids = (String[]) batteryInfoItems.get("ids");

                JSONObject parametersObject = new JSONObject();
                BigInteger deviceId = getDeviceId();
                parametersObject.put("deviceId", deviceId);
                parametersObject.put("battery", items);
                parametersObject.put("version", configuration.get().getVersion());

                BuddyMetaData.uploadMetaDataInBackground("battery", parametersObject, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            // success
                            PLog.i(TAG, "battery info uploaded");
                            PLog.i(TAG, "deleting uploaded battery info");
                            long rowsAffected = BuddySqliteHelper.getInstance().delete(BuddySqliteTableType.Battery, ids);

                            if (items.length() == rowsAffected) {
                                PLog.i(TAG, "battery info deleted");
                            }

                            if (loopCount -  1 != 0) {
                                uploadBattery(loopCount -  1);
                            }
                            else {
                                // all cellular info uploaded.
                                uploadCompleted();
                            }

                        } else {
                            PLog.i(TAG, "battery upload failed");
                            uploadCompleted();
                            handleUploadError(e);
                        }
                    }
                });

            }
            catch(Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
    }

    private void handleUploadError(ParseException e) {
        if (e.getCode() == ParseException.VALIDATION_ERROR) {
            loadNewConfiguration();
        }
    }

    private void uploadCompleted() {
        uploadCriteria.endUpload(context);
        configuration.set(BuddyPreferences.getConfig(context));
    }

    private BigInteger getDeviceId() {
        String deviceIdString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return new BigInteger(deviceIdString, 16);
    }

    private void setupEvents() {
        actionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction = intent.getAction();

                PLog.i(BuddyAltDataTracker.TAG, intentAction);

                switch (intentAction) {
                    case Intent.ACTION_POWER_CONNECTED :
                        uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);
                        PLog.i(TAG, "power connected ");
                        break;

                    case Intent.ACTION_POWER_DISCONNECTED :
                        uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
                        PLog.i(TAG, "power disconnected ");
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
                            uploadCriteria.setCellularConnectivityStatus(BuddyCellularConnectivityStatus.Connected);
                            PLog.i(TAG, "cellular connected ");
                        }
                        else {
                            // not connected to a cellular network
                            uploadCriteria.setCellularConnectivityStatus(BuddyCellularConnectivityStatus.Disconnected);
                            PLog.i(TAG, "cellular disconnected ");
                        }

                        break;

                    case WifiManager.NETWORK_STATE_CHANGED_ACTION :
                        NetworkInfo networkInformation = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        NetworkInfo.DetailedState detailedState = networkInformation.getDetailedState();

                        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
                            uploadCriteria.setWifiConnectivityStatus(BuddyWifiConnectivityStatus.Connected);
                            PLog.i(TAG, "wifi connected ");
                        }
                        else {
                            uploadCriteria.setWifiConnectivityStatus(BuddyWifiConnectivityStatus.Disconnected);
                            PLog.i(TAG, "wifi disconnected ");
                        }
                        break;

                    default :
                        break;
                }

                handleEvent();
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

    private void handleEvent() {
        saveCellularInformation();
        saveBatteryInformation();

        upload();
    }

    public void saveBatteryInformation() {
        if (configuration.get().shouldLogBattery()) {
            int batteryPercentage = uploadCriteria.getBatteryPercentage(context);
            BuddyBatteryInformation.saveBatteryInformation(batteryPercentage);
        }
    }

    private void upload() {
        if (uploadCriteria.canUpload(context, configuration.get())) {
            uploadLocations();
            uploadCellular();
            uploadBattery();
        }
        else {
            // not ready to upload.
            BuddySqliteHelper.getInstance().cleanUp(configuration.get());
        }
    }

    private void uploadBattery() {
        if (configuration.get().shouldUploadBattery()) {
            long availableBatteryInfo = BuddySqliteHelper.getInstance().rowCount(BuddySqliteTableType.Battery);
            uploadCriteria.startUpload();

            int loopCount = (int) Math.ceil((double)availableBatteryInfo / configuration.get().getCommonBatteryPushBatchSize());
            if (loopCount == 0) {
                // make the push with empty battery data, so updated config gets downloaded
                loopCount = 1;
            }
            uploadBattery(loopCount);
        }
    }

    private void uploadCellular() {
        if (configuration.get().shouldUploadCellular()) {
            long availableCellularInfo = BuddySqliteHelper.getInstance().rowCount(BuddySqliteTableType.Cellular);
            uploadCriteria.startUpload();

            int loopCount = (int) Math.ceil((double)availableCellularInfo / configuration.get().getCommonCellularPushBatchSize());
            if (loopCount == 0) {
                // make the push with empty cellular data, so updated config gets downloaded
                loopCount = 1;
            }
            uploadCellular(loopCount);
        }
    }

    private void uploadLocations() {
        if (configuration.get().shouldUploadLocation()) {
            long availableLocations = BuddySqliteHelper.getInstance().rowCount(BuddySqliteTableType.Location);
            uploadCriteria.startUpload();

            int loopCount = (int) Math.ceil((double)availableLocations / configuration.get().getCommonLocationPushBatchSize());
            if (loopCount == 0) {
                // make the push with empty locations, so updated config gets downloaded
                loopCount = 1;
            }
            uploadLocations(loopCount);
        }
    }

    private void uploadErrors() {
        final JSONObject errors = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Error, 0);

        if (errors != null && errors.has("items") && errors.has("ids")) {
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
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
    }

    void setupServices() {
        PLog.i(TAG, "setupServices");
        uploadCriteria.updateInitialPowerStatus(context);

        uploadDeviceInformation();
        uploadApplicationsList();
        uploadErrors();

        configureLocationLogging();
        configureCellularAndBatteryLogging();
    }

    private void configureLocationLogging() {
        if (configuration.get().shouldLogLocation()) {
            if (googleApiClient == null) {
                createApiClient();
            }
            else {
                startApiClient();
            }
        }
        else {
            if (googleApiClient != null) {
                stopApiClient();
            }
        }
    }

    private void stopApiClient() {
        stopLocationService();
        stopActivityMonitoring();

        if (lostApiClient == null) {
            googleApiClient.disconnect();
            PLog.i(TAG, "googleApiClient: disconnecting from services");
        } else {
            lostApiClient.disconnect();
            PLog.i(TAG, "lostApiClient: disconnecting from services");
        }
    }

    private void startApiClient() {
        if (lostApiClient == null) {
            googleApiClient.connect();
            PLog.i(TAG, "googleApiClient: connecting to services");
        }
        else
        {
            lostApiClient.connect();
            PLog.i(TAG, "lostApiClient: connecting to services");
        }
    }

    private void createApiClient() {
        PLog.i(TAG, "createApiClient");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        PLog.i(TAG, "createApiClient: GoogleApiClient onConnectionFailed");

                        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                                connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING) {

                            lostApiClient = new LostApiClient.Builder(context)
                                    .addConnectionCallbacks(BuddyAltDataTracker.getInstance())
                                    .build();

                            lostApiClient.connect();
                        }
                    }
                })
                .build();

        googleApiClient.connect();
    }

    // GoogleApiClient
    @Override
    public void onConnected(Bundle bundle) {
        Assert.assertNull (lostApiClient);
        PLog.i(TAG, "GoogleApiClient onConnected");
        initializeServices();
    }

    // LostApiClient
    @Override
    public void onConnected() {
        PLog.i(TAG, "LostApiClient onConnected");
        initializeServices();
    }

    // GoogleApiClient
    @Override
    public void onConnectionSuspended(int i) {
        onConnectionSuspended();
    }

    // LostApiClient
    @Override
    public void onConnectionSuspended() {
    }

    private void initializeServices() {
        initializeActivityMonitoring();
        initializeLocationService();
        setupEvents();
    }

    private void stopLocationService() {
        try {
            if (lostApiClient == null) {
                // undo updates
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, getPendingIntent());
            } else {
                // undo any updates
                com.mapzen.android.lost.api.LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, getPendingIntent());
            }
            PLog.i(TAG, "location service stopped");
        } catch (IllegalStateException e) {
            PLog.w(TAG, "Location service stop error because it is already stopped");
        }
    }

    private void initializeLocationService() {
        try {
            if (lostApiClient == null) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    PLog.i(TAG, "initializeLocationService googleApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setPriority((int) configuration.get().getAndroidLocationPowerAccuracy());
                locationRequest.setFastestInterval(configuration.get().getAndroidLocationFastestUpdateIntervalMs());
                locationRequest.setInterval(configuration.get().getAndroidLocationUpdateIntervalMs());

                // undo updates
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, getPendingIntent());
                // enable
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, getPendingIntent());
            } else {
                Location location = com.mapzen.android.lost.api.LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
                if (location != null) {
                    PLog.i(TAG, "initializeLocationService lostApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                final com.mapzen.android.lost.api.LocationRequest locationRequest = com.mapzen.android.lost.api.LocationRequest.create();
                locationRequest.setPriority((int) configuration.get().getAndroidLocationPowerAccuracy());
                locationRequest.setFastestInterval(configuration.get().getAndroidLocationFastestUpdateIntervalMs());
                locationRequest.setInterval(configuration.get().getAndroidLocationUpdateIntervalMs());

                // undo any updates
                com.mapzen.android.lost.api.LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, getPendingIntent());

                // enable
                com.mapzen.android.lost.api.LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, locationRequest, getPendingIntent());
            }
            PLog.i(TAG, "initializeLocationService: end");
        } catch (SecurityException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
            PLog.w(TAG, "initializeLocationService: Missing ACCESS_FINE_LOCATION or com.google.android.gms.permission.ACTIVITY_RECOGNITION permission in the AndroidManifest");
            googleApiClient.disconnect();
        }
    }

    private void initializeActivityMonitoring() {
        if (lostApiClient == null) {
            long activityMonitoringInterval = configuration.get().getAndroidActivityMonitoringTimeoutMs();
            // undo any updates
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( googleApiClient, getPendingIntent() );
            // redo updates
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, activityMonitoringInterval, getPendingIntent() );
        }
    }

    private void stopActivityMonitoring() {

        try {
            if (lostApiClient == null) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( googleApiClient, getPendingIntent() );
            }
            PLog.i(TAG, "activity monitoring stopped");
        } catch (IllegalStateException e) {
            PLog.w(TAG, "Activity monitoring stop error because it is already stopped");
        }
    }

    private PendingIntent getPendingIntent() {
        Intent i = new Intent(context, BuddyWakefulBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

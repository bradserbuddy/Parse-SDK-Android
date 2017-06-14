package com.parse;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;

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

class BuddyLocationTracker implements GoogleApiClient.ConnectionCallbacks, LostApiClient.ConnectionCallbacks {
    public static final String TAG = "com.parse.BuddyLocationTracker";
    private static long locationsBatchSize = 100; // 100 locations to be batched up in a post
    private static long cellularBatchSize = 100; // 100 cellular info to be batched up in a post
    private Timer networkInfoLogTimer;
    private static BuddyUploadCriteria uploadCriteria = new BuddyUploadCriteria();
    private static BuddyDBHelper dbHelper;
    private static GoogleApiClient googleApiClient;
    private static LostApiClient lostApiClient;
    private static final long cellularInfoLogTimeout = 1000; // how often to log. default 1sec

    public static BuddyLocationTracker getInstance() {
        return BuddyLocationTracker.Singleton.INSTANCE;
    }

    private static class Singleton {
        public static final BuddyLocationTracker INSTANCE = new BuddyLocationTracker();
    }

    public void initialize() {
        PLog.i(TAG, "BuddyLocationTracker initialize");

        Intent permissionIntent = new Intent(Parse.getApplicationContext(), BuddyLocationRequestPermissionActivity.class);
        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Parse.getApplicationContext().getApplicationContext().startActivity(permissionIntent);
    }

    private void uploadApplicationsList() {
        PackageManager pm = Parse.getApplicationContext().getPackageManager();

        ArrayList<String> appNames = new ArrayList<>();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo appInfo : apps ) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                PLog.i(TAG, appInfo.processName + " , " + appInfo.describeContents() + " , " + appInfo.className);
                appNames.add(appInfo.processName);
            }
        }

        JSONObject applicationsObject = new JSONObject();
        try {
            applicationsObject.put("apps", new JSONArray(appNames));
            long deviceIdLong = getDeviceId();
            applicationsObject.put("deviceId", deviceIdLong);
        } catch (JSONException e) {
            PLog.e(TAG, e.getMessage());
        }

        trackEventInBackground("apps", applicationsObject, new SaveCallback() {
            @Override
            public void done(ParseException e) {
            if (e == null) {
                // success
                PLog.i(TAG, "apps data uploaded");
            } else {
                PLog.i(TAG, "apps data upload failed");
            }
            }
        });
    }

    private void setupCellularInfoLogTimer() {
        networkInfoLogTimer = new Timer();
        networkInfoLogTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PLog.i(TAG, "log cellular info");
                logCellularInformation();
            }
        }, 0, cellularInfoLogTimeout);
        PLog.i(TAG, "log cellular info timer enabled");
    }

    private void logCellularInformation() {
        TelephonyManager telephonyManager = (TelephonyManager) Parse.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    SaveCellularInfoForApi17Plus(telephonyManager);
                }
                else {
                    // older apis
                }
            }
            catch (Exception exception) {
                PLog.e(TAG, exception.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void SaveCellularInfoForApi17Plus(TelephonyManager telephonyManager) {
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        if (allCellInfo != null) {
            for (CellInfo cellInfo : allCellInfo) {
                JSONObject cellularInfoObject = new JSONObject();
                try {
                    cellularInfoObject.put("IsRegistered", cellInfo.isRegistered());
                } catch (JSONException e) {
                    PLog.e(TAG, e.getMessage());
                }

                if (cellInfo instanceof CellInfoGsm) {
                    PLog.i(TAG, "GSM network");
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm identityGSM = cellInfoGsm.getCellIdentity();
                    CellSignalStrengthGsm signalStrengthGsm = cellInfoGsm.getCellSignalStrength();

                    try {
                        cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.GSM.toString());
                        cellularInfoObject.put("Cid", identityGSM.getCid());
                        cellularInfoObject.put("Mcc", identityGSM.getMcc());
                        cellularInfoObject.put("Lac", identityGSM.getLac());
                        cellularInfoObject.put("Mnc", identityGSM.getMnc());

                        cellularInfoObject.put("AsuLevel", signalStrengthGsm.getAsuLevel());
                        cellularInfoObject.put("Dbm", signalStrengthGsm.getDbm());
                        cellularInfoObject.put("Level", signalStrengthGsm.getLevel());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cellularInfoObject.put("Arfcn", identityGSM.getArfcn());
                            cellularInfoObject.put("Bsic", identityGSM.getBsic());
                        }

                    } catch (JSONException e) {
                        PLog.e(TAG, e.getMessage());
                    }
                } else if (cellInfo instanceof CellInfoCdma) {
                    PLog.i(TAG, "CDMA network");
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                    CellIdentityCdma identityCdma = cellInfoCdma.getCellIdentity();
                    CellSignalStrengthCdma signalStrengthCdma = cellInfoCdma.getCellSignalStrength();

                    try {
                        cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.CDMA.toString());
                        cellularInfoObject.put("Latitude", identityCdma.getLatitude());
                        cellularInfoObject.put("BasestationId", identityCdma.getBasestationId());
                        cellularInfoObject.put("Longitude", identityCdma.getLongitude());
                        cellularInfoObject.put("NetworkId", identityCdma.getNetworkId());
                        cellularInfoObject.put("SystemId", identityCdma.getSystemId());
                        cellularInfoObject.put("AsuLevel", signalStrengthCdma.getAsuLevel());
                        cellularInfoObject.put("Dbm", signalStrengthCdma.getDbm());
                        cellularInfoObject.put("Level", signalStrengthCdma.getLevel());

                    } catch (JSONException e) {
                        PLog.e(TAG, e.getMessage());
                    }

                } else if (cellInfo instanceof CellInfoWcdma) {
                    PLog.i(TAG, "WCDMA network");
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma identityWcdma = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        identityWcdma = cellInfoWcdma.getCellIdentity();
                        CellSignalStrengthWcdma signalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                        try {
                            cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.WCDMA.toString());
                            cellularInfoObject.put("Cid", identityWcdma.getCid());
                            cellularInfoObject.put("Mcc", identityWcdma.getMcc());
                            cellularInfoObject.put("Lac", identityWcdma.getLac());
                            cellularInfoObject.put("Mnc", identityWcdma.getMnc());
                            cellularInfoObject.put("Psc", identityWcdma.getPsc());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cellularInfoObject.put("Uarfcn", identityWcdma.getUarfcn());
                            }

                            cellularInfoObject.put("AsuLevel", signalStrengthWcdma.getAsuLevel());
                            cellularInfoObject.put("Dbm", signalStrengthWcdma.getDbm());
                            cellularInfoObject.put("Level", signalStrengthWcdma.getLevel());

                        } catch (JSONException e) {
                            PLog.e(TAG, e.getMessage());
                        }
                    }
                } else if (cellInfo instanceof CellInfoLte) {
                    PLog.i(TAG, "Other Lte network");
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();

                    try {
                        cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.LTE.toString());
                        cellularInfoObject.put("Ci", cellIdentityLte.getCi());
                        cellularInfoObject.put("Mcc", cellIdentityLte.getMcc());
                        cellularInfoObject.put("Mnc", cellIdentityLte.getMnc());
                        cellularInfoObject.put("Pci", cellIdentityLte.getPci());
                        cellularInfoObject.put("Tac", cellIdentityLte.getTac());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cellularInfoObject.put("Earfcn", cellIdentityLte.getEarfcn());
                        }

                        cellularInfoObject.put("AsuLevel", signalStrengthLte.getAsuLevel());
                        cellularInfoObject.put("Dbm", signalStrengthLte.getDbm());
                        cellularInfoObject.put("Level", signalStrengthLte.getLevel());
                        cellularInfoObject.put("TimingAdvance", signalStrengthLte.getTimingAdvance());

                    } catch (JSONException e) {
                        PLog.e(TAG, e.getMessage());
                    }
                }

                if (cellularInfoObject.length() > 0) {
                    try {
                        String cellInfoJson = cellularInfoObject.toString(0);
                        String uuid = UUID.randomUUID().toString();
                        dbHelper.insertCellInformation(uuid, cellInfoJson);
                        PLog.i(TAG, "network info saved to db ");
                    } catch (Exception exception) {
                        PLog.e(TAG, exception.getMessage());
                    }
                }
            }
        }
    }
    private void uploadDeviceInformation() {
        JSONObject deviceInfoObject = new JSONObject();
        try {
            long deviceIdLong = getDeviceId();
            deviceInfoObject.put("deviceBrand", android.os.Build.BRAND);
            deviceInfoObject.put("deviceModel", android.os.Build.MODEL);
            deviceInfoObject.put("sdkVersion", android.os.Build.VERSION.RELEASE);
            deviceInfoObject.put("sdkVersionNumber", android.os.Build.VERSION.SDK_INT);
            deviceInfoObject.put("deviceId", deviceIdLong);
        } catch (JSONException e) {
            PLog.e(TAG, e.getMessage());
        }

        trackEventInBackground("device", deviceInfoObject, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // success
                    PLog.i(TAG, "device data uploaded");
                } else {
                    PLog.i(TAG, "device data upload failed");
                }
            }
        });
    }

    private void uploadLocations(final int loopCount) {
        PLog.i(TAG, "Uploading batch no. " + Integer.toString(loopCount));
        final ArrayList<BuddyLocation> locations = dbHelper.getLocationsBatch(locationsBatchSize);
        final int uploadableLocationCount = locations.size();

        if (uploadableLocationCount > 0) {
            PLog.i(TAG, "Push " + uploadableLocationCount + " locations");

            final String[] uploadableLocationIds = new String[uploadableLocationCount];
            JSONObject parametersObject = new JSONObject();
            JSONArray parametersArray = new JSONArray();

            for (int i = 0; i < uploadableLocationCount; i++) {
                BuddyLocation location = locations.get(i);
                double longitude = location.getLongitude();
                long timestamp = location.getTimestamp();
                double latitude = location.getLatitude();
                String locationUuid = location.getUuid();
                float accuracy = location.getAccuracy();
                double altitude = location.getAltitude();
                float bearing = location.getBearing();

                float speed = location.getSpeed();
                // float bearingAccuracy = location.getBearingAccuracy();
                // float speedAccuracyMetersPerSecond = location.getSpeedAccuracyMetersPerSecond();
                // float verticalAccuracyMeters = location.getVerticalAccuracyMeters();

                //PLog.i(TAG, location.getUuid() + " , " + location.getLatitude() + " , " + location.getLongitude() + " , " + location.getTimestamp());
                uploadableLocationIds[i] = locationUuid;
                JSONObject locationObject = new JSONObject();
                try {
                    locationObject.put("uuid", locationUuid);
                    locationObject.put("latitude", latitude);
                    locationObject.put("longitude", longitude);
                    locationObject.put("timestamp", timestamp);
                    locationObject.put("accuracy", accuracy);
                    locationObject.put("altitude", altitude);
                    locationObject.put("bearing", bearing);
                    locationObject.put("speed", speed);
                    // locationObject.put("bearingAccuracy",bearingAccuracy);
                    // locationObject.put("speedAccuracyMetersPerSecond",speedAccuracyMetersPerSecond);
                    // locationObject.put("verticalAccuracyMeters",verticalAccuracyMeters);
                    parametersArray.put(i, locationObject);
                } catch (JSONException e) {
                    PLog.e(TAG, e.getMessage());
                }
            }

            // device data for testing
            JSONObject deviceStatusObject = new JSONObject();
            try {
                String network = "disconnected";
                BuddyConnectivityStatus connectivityStatus = uploadCriteria.getConnectivityStatus();
                if (connectivityStatus == BuddyConnectivityStatus.CellularConnected) {
                    network = "cellular";
                }
                else if (connectivityStatus == BuddyConnectivityStatus.WifiConnected) {
                    network = "wifi";
                }
                deviceStatusObject.put("network", network);

                String power = "unknown";
                BuddyPowerConnectionStatus powerConnectionStatus = uploadCriteria.getPowerStatus();
                if (powerConnectionStatus == BuddyPowerConnectionStatus.Connected) {
                    power = "charging";
                }
                else if (powerConnectionStatus == BuddyPowerConnectionStatus.Disconnected) {
                    power = "not charging";
                }
                deviceStatusObject.put("power", power);

                String battery = "unknown";
                int batteryPercentage = uploadCriteria.getBatteryPercentage();
                if (batteryPercentage != -1) {
                    battery =  String.valueOf(batteryPercentage);
                }
                deviceStatusObject.put("battery", battery);

            } catch (JSONException e) {
                PLog.e(TAG, e.getMessage());
            }

            try {
                parametersObject.put("locations", parametersArray);
                long deviceIdLong = getDeviceId();
                parametersObject.put("deviceId", deviceIdLong);
                parametersObject.put("device_status", deviceStatusObject);
            } catch (JSONException e) {
                PLog.e(TAG, e.getMessage());
            }

            trackEventInBackground("location", parametersObject, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // success
                        PLog.i(TAG, "locations uploaded");
                        PLog.i(TAG, "deleting uploaded locations");
                        long rowsAffected = dbHelper.deleteLocations(uploadableLocationIds);

                        if (uploadableLocationCount == rowsAffected) {
                            PLog.i(TAG, "locations deleted");
                        }

                        if (loopCount -  1 != 0) {
                            uploadLocations(loopCount -  1);
                        }
                        else {
                            // all locations uploaded.
                            uploadCriteria.endUpload();
                        }

                    } else {
                        PLog.i(TAG, "Locations upload failed");
                    }
                }
            });
        }
    }

    private void uploadCellular(final int loopCount) {
        PLog.i(TAG, "Uploading cellular batch no. " + Integer.toString(loopCount));
        final ArrayList<JSONObject> cellularInfoItems = dbHelper.getCellularBatch(cellularBatchSize);
        final int uploadableCellularItemsCount = cellularInfoItems.size();

        if (uploadableCellularItemsCount > 0) {
            PLog.i(TAG, "Push " + uploadableCellularItemsCount + " cellular records");

            final String[] uploadableCellularIds = new String[uploadableCellularItemsCount];

            for (int i = 0; i < uploadableCellularItemsCount; i++) {
                JSONObject cellularInfoItem = cellularInfoItems.get(i);
                try {
                    String uuid = (String) cellularInfoItem.get("uuid");
                    uploadableCellularIds[i] = uuid;
                } catch (JSONException e) {
                    PLog.e(TAG, e.getMessage());
                }
            }

            JSONObject parametersObject = new JSONObject();
            try {

                long deviceIdLong = getDeviceId();
                parametersObject.put("deviceId", deviceIdLong);
                parametersObject.put("cellular", cellularInfoItems);
            } catch (JSONException e) {
                PLog.e(TAG, e.getMessage());
            }

            trackEventInBackground("cellular", parametersObject, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // success
                        PLog.i(TAG, "cellular info uploaded");
                        PLog.i(TAG, "deleting uploaded cellular info");
                        long rowsAffected = dbHelper.deleteCellular(uploadableCellularIds);

                        if (uploadableCellularItemsCount == rowsAffected) {
                            PLog.i(TAG, "cellular info deleted");
                        }

                        if (loopCount -  1 != 0) {
                            uploadCellular(loopCount -  1);
                        }
                        else {
                            // all cellular info uploaded.
                            uploadCriteria.endUpload();
                        }

                    } else {
                        PLog.i(TAG, "Cellular upload failed");
                    }
                }
            });
        }
    }

    private long getDeviceId() {
        String deviceIdString = Settings.Secure.getString(Parse.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return Long.parseLong(deviceIdString, 16);
    }

    void trackEventInBackground(String name, JSONObject parametersObject, SaveCallback callback) {
        ParseTaskUtils.callbackOnMainThreadAsync(trackEventInBackground(name, parametersObject), callback);
    }

    Task<Void> trackEventInBackground(final String name,
                                                    final JSONObject parametersObject) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("A name for the custom event must be provided.");
        }

        return ParseUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
            @Override
            public Task<Void> then(Task<String> task) throws Exception {
                String sessionToken = task.getResult();
                return getLocationsController().trackLocationEventInBackground(name, parametersObject, sessionToken);
            }
        });
    }

    /* package for test */ BuddyLocationsController getLocationsController() {
        return ParseCorePlugins.getInstance().getLocationsController();
    }

    private void setupEvents() {
        BroadcastReceiver actionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction = intent.getAction();

                PLog.i(BuddyLocationTracker.TAG, intentAction);

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

        Parse.getApplicationContext().registerReceiver(actionReceiver, intentFilter);
    }

    private void upload() {
        if (uploadCriteria.canUpload()) {
            int availableLocations = dbHelper.locationsRowCount();
            if (availableLocations > 0) {
                uploadCriteria.startUpload();

                int loopCount = (int) Math.ceil((double)availableLocations / locationsBatchSize);
                if (loopCount > 0) {
                    uploadLocations(loopCount);
                }
            }

            // upload cellular information
            int availableCellularInfo = dbHelper.cellularRowCount();
            if (availableCellularInfo > 0) {
                uploadCriteria.startUpload();

                int loopCount = (int) Math.ceil((double)availableCellularInfo / cellularBatchSize);
                if (loopCount > 0) {
                    uploadCellular(loopCount);
                }
            }
        }
    }

    void setupLocationService() {
        PLog.i(TAG, "setupLocationService");
        dbHelper = new BuddyDBHelper(Parse.getApplicationContext());
        setupCellularInfoLogTimer();
        updateInitialPowerStatus();

        uploadDeviceInformation();
        uploadApplicationsList();

        if (googleApiClient == null) {

            PLog.i(TAG, "setupLocationService GoogleApiClient.Builder");
            googleApiClient = new GoogleApiClient.Builder(Parse.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        PLog.i(TAG, "setupLocationService: onConnectionFailed");

                        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                                connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING) {

                            lostApiClient = new LostApiClient.Builder(Parse.getApplicationContext())
                                    .addConnectionCallbacks(BuddyLocationTracker.getInstance())
                                    .build();

                            if (!lostApiClient.isConnected()) {
                                lostApiClient.connect();
                            }
                        }
                    }
                }).addApi(LocationServices.API).build();

            if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                googleApiClient.connect();
                PLog.i(TAG, "setupLocationService: connecting to services");
            }
        }
    }

    private void updateInitialPowerStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = Parse.getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) {
            uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);
        }
        else {
            uploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
        }
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
        PLog.i(TAG, "setupLocationService onConnected");

        try {
            // location service can throw an exception if permissions are not set

            if (lostApiClient == null) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    PLog.i(TAG, "setupLocationService googleApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest.setFastestInterval(1000 * 60);
                locationRequest.setInterval(1000 * 60 * 10);
                //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                //locationRequest.setFastestInterval(1000 * 1);
                //locationRequest.setInterval(1000 * 1);

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, getPendingIntent());
            } else {
                Location location = com.mapzen.android.lost.api.LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
                if (location != null) {
                    PLog.i(TAG, "setupLocationService lostApiClient start location is " + location.getLatitude() + " , " + location.getLongitude());
                }

                com.mapzen.android.lost.api.LocationRequest locationRequest = com.mapzen.android.lost.api.LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest.setFastestInterval(1000 * 60);
                locationRequest.setInterval(1000 * 60 * 10);
                //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                //locationRequest.setFastestInterval(1000 * 1);
                //locationRequest.setInterval(1000 * 1);

                com.mapzen.android.lost.api.LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, locationRequest, getPendingIntent());
            }

            setupEvents();

            PLog.i(TAG, "setupLocationService: end GoogleApiClient.Builder");
        } catch (SecurityException securityException) {
            PLog.w(TAG, "setupLocationService: Missing ACCESS_FINE_LOCATION permission in the AndroidManifest");
            googleApiClient.disconnect();
        }
    }

    private PendingIntent getPendingIntent() {
        Intent i = new Intent(Parse.getApplicationContext(), BuddyWakefulBroadcastReceiver.class);
        return PendingIntent.getBroadcast(Parse.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended() {
    }
}

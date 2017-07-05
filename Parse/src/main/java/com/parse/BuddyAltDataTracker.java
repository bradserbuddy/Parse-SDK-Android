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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private static ActivityManager activityManager;
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
        PackageManager pm = context.getPackageManager();

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
            applicationsObject.put("buddySdkVersion", configuration.get().getVersion());
        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        trackEventInBackground("apps", applicationsObject, new SaveCallback() {
            @Override
            public void done(ParseException e) {
            if (e == null) {
                // success
                PLog.i(TAG, "apps data uploaded");
            } else {
                PLog.i(TAG, "apps data upload failed");
                LoadNewConfiguration();
            }
            }
        });
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
            if (telephonyManager != null) {
                try {
                    JSONObject cellularInfoObject = getCellInformation(telephonyManager);

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

    private JSONObject getCellInformation(TelephonyManager telephonyManager) {
        JSONObject cellInformation = new JSONObject();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            int dataNetworkType = telephonyManager.getDataNetworkType();
            try {
                cellInformation.put("DataNetworkType",dataNetworkType);
            } catch (JSONException e) {
                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
            }
        }

        JSONArray cellInformationArray = new JSONArray();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();

            if (allCellInfo != null) {
                for (CellInfo cellInfo : allCellInfo) {
                    JSONObject cellularInfoObject = new JSONObject();
                    try {
                        cellularInfoObject.put("IsRegistered", cellInfo.isRegistered());
                    } catch (JSONException e) {
                        BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                    }

                    if (cellInfo instanceof CellInfoGsm) {
                        //PLog.i(TAG, "GSM network");
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellIdentityGsm identityGSM = cellInfoGsm.getCellIdentity();
                        CellSignalStrengthGsm signalStrengthGsm = cellInfoGsm.getCellSignalStrength();

                        try {
                            cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.GSM.toString());
                            cellularInfoObject.put("CellId", identityGSM.getCid());
                            cellularInfoObject.put("MobileCountryCode", identityGSM.getMcc());
                            cellularInfoObject.put("LocationAreaCode", identityGSM.getLac());
                            cellularInfoObject.put("MobileNetworkCode", identityGSM.getMnc());

                            cellularInfoObject.put("AsuLevel", signalStrengthGsm.getAsuLevel());
                            cellularInfoObject.put("SignalStrengthDbm", signalStrengthGsm.getDbm());
                            cellularInfoObject.put("SignalLevel", signalStrengthGsm.getLevel());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cellularInfoObject.put("AbsoluteRFChannelNo", identityGSM.getArfcn());
                                cellularInfoObject.put("BaseStationIdCode", identityGSM.getBsic());
                            }

                        } catch (JSONException e) {
                            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                        }
                    } else if (cellInfo instanceof CellInfoCdma) {
                        //PLog.i(TAG, "CDMA network");
                        CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                        CellIdentityCdma identityCdma = cellInfoCdma.getCellIdentity();
                        CellSignalStrengthCdma signalStrengthCdma = cellInfoCdma.getCellSignalStrength();

                        try {
                            cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.CDMA.toString());
                            cellularInfoObject.put("Latitude", identityCdma.getLatitude());
                            cellularInfoObject.put("BaseStationId", identityCdma.getBasestationId());
                            cellularInfoObject.put("Longitude", identityCdma.getLongitude());
                            cellularInfoObject.put("NetworkId", identityCdma.getNetworkId());
                            cellularInfoObject.put("SystemId", identityCdma.getSystemId());
                            cellularInfoObject.put("AsuLevel", signalStrengthCdma.getAsuLevel());
                            cellularInfoObject.put("SignalStrengthDbm", signalStrengthCdma.getDbm());
                            cellularInfoObject.put("SignalLevel", signalStrengthCdma.getLevel());

                        } catch (JSONException e) {
                            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                        }

                    } else if (cellInfo instanceof CellInfoWcdma) {
                        //PLog.i(TAG, "WCDMA network");
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        CellIdentityWcdma identityWcdma = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            identityWcdma = cellInfoWcdma.getCellIdentity();
                            CellSignalStrengthWcdma signalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                            try {
                                cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.WCDMA.toString());
                                cellularInfoObject.put("CellId", identityWcdma.getCid());
                                cellularInfoObject.put("MobileCountryCode", identityWcdma.getMcc());
                                cellularInfoObject.put("LocationAreaCode", identityWcdma.getLac());
                                cellularInfoObject.put("MobileNetworkCode", identityWcdma.getMnc());
                                cellularInfoObject.put("PrimaryScramblingCode", identityWcdma.getPsc());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cellularInfoObject.put("UMTSAbsoluteRFChannelNo", identityWcdma.getUarfcn());
                                }

                                cellularInfoObject.put("AsuLevel", signalStrengthWcdma.getAsuLevel());
                                cellularInfoObject.put("SignalStrengthDbm", signalStrengthWcdma.getDbm());
                                cellularInfoObject.put("SignalLevel", signalStrengthWcdma.getLevel());

                            } catch (JSONException e) {
                                BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                            }
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        //PLog.i(TAG, "Other Lte network");
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                        CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();

                        try {
                            cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.LTE.toString());
                            cellularInfoObject.put("CellId", cellIdentityLte.getCi());
                            cellularInfoObject.put("MobileCountryCode", cellIdentityLte.getMcc());
                            cellularInfoObject.put("MobileNetworkCode", cellIdentityLte.getMnc());
                            cellularInfoObject.put("PhysicalCellId", cellIdentityLte.getPci());
                            cellularInfoObject.put("TrackingAreaCode", cellIdentityLte.getTac());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cellularInfoObject.put("AbsoluteRFChannelNo", cellIdentityLte.getEarfcn());
                            }

                            cellularInfoObject.put("AsuLevel", signalStrengthLte.getAsuLevel());
                            cellularInfoObject.put("SignalStrengthDbm", signalStrengthLte.getDbm());
                            cellularInfoObject.put("SignalLevel", signalStrengthLte.getLevel());
                            cellularInfoObject.put("TimingAdvance", signalStrengthLte.getTimingAdvance());

                        } catch (JSONException e) {
                            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                        }
                    }

                    if (cellularInfoObject.length() > 0) {
                        cellInformationArray.put(cellularInfoObject);
                    }
                }
            }
        }
        else {
            // other api levels
            CellLocation cellLocation = telephonyManager.getCellLocation();

            if (cellLocation instanceof  CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)cellLocation;
                JSONObject cellularInfoObject = new JSONObject();

                try {
                    cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.CDMA.toString());
                    cellularInfoObject.put("Latitude", cdmaCellLocation.getBaseStationLatitude());
                    cellularInfoObject.put("BaseStationId", cdmaCellLocation.getBaseStationId());
                    cellularInfoObject.put("Longitude", cdmaCellLocation.getBaseStationLongitude());
                    cellularInfoObject.put("NetworkId", cdmaCellLocation.getNetworkId());
                    cellularInfoObject.put("SystemId", cdmaCellLocation.getSystemId());

                    cellInformationArray.put(cellularInfoObject);
                } catch (JSONException e) {
                    BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                }
            }
            else  if (cellLocation instanceof  GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
                JSONObject cellularInfoObject = new JSONObject();

                try {
                    cellularInfoObject.put("NetworkType", BuddyCellularNetworkType.GSM.toString());
                    cellularInfoObject.put("CellId", gsmCellLocation.getCid());
                    cellularInfoObject.put("PrimaryScramblingCode", gsmCellLocation.getPsc());
                    cellularInfoObject.put("LocationAreaCode", gsmCellLocation.getLac());

                    cellInformationArray.put(cellularInfoObject);
                } catch (JSONException e) {
                    BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
                }
            }
        }
        try {
            cellInformation.put("data",cellInformationArray);
        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        return cellInformation;
    }

    private static boolean isInBackground() {
        boolean isBackground = false;
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
            long deviceIdLong = getDeviceId();
            deviceInfoObject.put("deviceBrand", android.os.Build.BRAND);
            deviceInfoObject.put("deviceModel", android.os.Build.MODEL);
            deviceInfoObject.put("sdkVersion", android.os.Build.VERSION.RELEASE);
            deviceInfoObject.put("sdkVersionNumber", android.os.Build.VERSION.SDK_INT);
            deviceInfoObject.put("deviceId", deviceIdLong);
            deviceInfoObject.put("buddySdkVersion", configuration.get().getVersion());
        } catch (JSONException e) {
            BuddySqliteHelper.getInstance().logError(TAG, e.getMessage());
        }

        trackEventInBackground("device", deviceInfoObject, new SaveCallback() {
            @Override
            public void done(ParseException e) {
            if (e == null) {
                // success
                PLog.i(TAG, "device data uploaded");
            } else {
                PLog.i(TAG, "device data upload failed");
                LoadNewConfiguration();
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
                    long deviceIdLong = getDeviceId();
                    parametersObject.put("deviceId", deviceIdLong);
                    parametersObject.put("device_status", deviceStatus);
                    parametersObject.put("buddySdkVersion", configuration.get().getVersion());

                    trackEventInBackground("location", parametersObject, new SaveCallback() {
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
                            LoadNewConfiguration();
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
                    long deviceIdLong = getDeviceId();
                    parametersObject.put("deviceId", deviceIdLong);
                    parametersObject.put("cellular", items);
                    parametersObject.put("buddySdkVersion", configuration.get().getVersion());

                    trackEventInBackground("cellular", parametersObject, new SaveCallback() {
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
                                LoadNewConfiguration();
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

    private void uploadCompleted() {
        uploadCriteria.endUpload(context);
        configuration.set(BuddyPreferenceService.getConfig(context));
    }

    private long getDeviceId() {
        String deviceIdString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
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
                return getLocationsController().trackMetaInBackground(name, parametersObject, sessionToken);
            }
        });
    }

    /* package for test */ BuddyAltDataController getLocationsController() {
        return ParseCorePlugins.getInstance().getLocationsController();
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
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

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
        PLog.i(TAG, "Uploading error logs");
        final JSONObject errors = BuddySqliteHelper.getInstance().get(BuddySqliteTableType.Error, 0);

        if (errors.has("items") && errors.has("ids")) {
            try {
                final JSONArray items = (JSONArray) errors.get("items");
                if (items.length() > 0) {
                    final String[] ids = (String[]) errors.get("ids");
                    JSONObject parametersObject = new JSONObject();
                    parametersObject.put("errors", items);
                    long deviceIdLong = getDeviceId();
                    parametersObject.put("deviceId", deviceIdLong);
                    parametersObject.put("buddySdkVersion", configuration.get().getVersion());

                    trackEventInBackground("error", parametersObject, new SaveCallback() {
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
                            uploadCompleted();
                        } else {
                            PLog.i(TAG, "errors upload failed");
                            uploadCompleted();
                            LoadNewConfiguration();
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
                }).addApi(LocationServices.API).build();

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
        PLog.i(TAG, "location service onConnected");
        try {
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
            PLog.w(TAG, "setupServices: Missing ACCESS_FINE_LOCATION permission in the AndroidManifest");
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

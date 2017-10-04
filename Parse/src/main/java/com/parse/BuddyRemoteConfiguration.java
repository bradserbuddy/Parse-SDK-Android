package com.parse;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

public class BuddyRemoteConfiguration {
    private final String deviceModel;
    private final int apiLevel;
    private final String applicationId;
    private BuddyRemoteConfigValidity apiLevelValidity = null;
    private BuddyRemoteConfigValidity modelValidity = null;
    private JSONObject config;
    private BuddyRemoteConfigValidity appIdValidity = null;
    private static final String TAG = "com.parse.BuddyRemoteConfiguration";

    public JSONObject getConfiguration() {
        return this.config;
    }

    public BuddyRemoteConfiguration(JSONObject config, String deviceModel, int apiLevel, String applicationId) {
        this.config = config;
        this.deviceModel = deviceModel;
        this.apiLevel = apiLevel;
        this.applicationId = applicationId;

        appIdValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigAppId);
        apiLevelValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigApiLevel);
        modelValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigModel);
    }

    public boolean getApiLevelValidity() {
        boolean result = false;
        try {
            result = apiLevelValidity == BuddyRemoteConfigValidity.Specific && Integer.toString(apiLevel).equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigApiLevel));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    public boolean getAppIdValidity() {
        boolean result = false;
        try {
            result = appIdValidity == BuddyRemoteConfigValidity.Specific && applicationId.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigAppId));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    public boolean getModelValidity() {
        boolean result = false;
        try {
            result = modelValidity == BuddyRemoteConfigValidity.Specific && deviceModel.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigModel));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    private BuddyRemoteConfigValidity getValidity(String preferenceConfig) {
        BuddyRemoteConfigValidity validity = BuddyRemoteConfigValidity.Any;

        try {
            if (!this.config.getString(preferenceConfig).equalsIgnoreCase("*")) {
                validity = BuddyRemoteConfigValidity.Specific;
            }
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return validity;
    }

    public Integer validKeys() {
        Integer count = 0;
        if (appIdValidity == BuddyRemoteConfigValidity.Specific) {
            count++;
        }
        if (apiLevelValidity == BuddyRemoteConfigValidity.Specific) {
            count++;
        }
        if (modelValidity == BuddyRemoteConfigValidity.Specific) {
            count++;
        }

        return count;
    }

    public boolean isAppIdAndApiLevelSet()
    {
        boolean result = false;
        try {
            result = appIdValidity == BuddyRemoteConfigValidity.Specific && applicationId.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigAppId))
                    && apiLevelValidity == BuddyRemoteConfigValidity.Specific && Integer.toString(apiLevel).equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigApiLevel));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    public boolean isAppIdAndModelSet()
    {
        boolean result = false;
        try {
            result = appIdValidity == BuddyRemoteConfigValidity.Specific && applicationId.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigAppId))
                    && modelValidity == BuddyRemoteConfigValidity.Specific && deviceModel.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigModel));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    public boolean isApiLevelAndModelSet()
    {
        boolean result = false;
        try {
            result = apiLevelValidity == BuddyRemoteConfigValidity.Specific && Integer.toString(apiLevel).equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigApiLevel))
                    && modelValidity == BuddyRemoteConfigValidity.Specific && deviceModel.equalsIgnoreCase(config.getString(BuddyPreferenceKeys.preferenceConfigModel));
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }

    public boolean isAppIdAndApiLevelAndModelSet()
    {
        boolean result = false;
        try {
            String configAppId = config.getString(BuddyPreferenceKeys.preferenceConfigAppId);
            String configApiLevel = config.getString(BuddyPreferenceKeys.preferenceConfigApiLevel);
            String configModel = config.getString(BuddyPreferenceKeys.preferenceConfigModel);
            result = appIdValidity == BuddyRemoteConfigValidity.Specific && applicationId.equalsIgnoreCase(configAppId)
                    && apiLevelValidity == BuddyRemoteConfigValidity.Specific && Integer.toString(apiLevel).equalsIgnoreCase(configApiLevel)
                    && modelValidity == BuddyRemoteConfigValidity.Specific && deviceModel.equalsIgnoreCase(configModel);
        } catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(TAG, e);
        }

        return result;
    }
}

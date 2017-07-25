package com.parse;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

public class BuddyRemoteConfiguration {
    private BuddyRemoteConfigValidity apiLevelValidity = null;
    private BuddyRemoteConfigValidity modelValidity = null;
    private JSONObject config;
    private BuddyRemoteConfigValidity appIdValidity = null;

    public JSONObject getConfiguration() {
        return this.config;
    }

    public BuddyRemoteConfiguration(JSONObject config, String deviceModel, int apiLevel, String applicationId) {
        this.config = config;
        appIdValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigAppId, applicationId);
        apiLevelValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigApiLevel, apiLevel);
        modelValidity =  getValidity(BuddyPreferenceKeys.preferenceConfigModel, deviceModel);
    }

    public boolean getApiLevelValidity() {
        return apiLevelValidity == BuddyRemoteConfigValidity.Specific;
    }

    public boolean getAppIdValidity() {
        return appIdValidity == BuddyRemoteConfigValidity.Specific;
    }

    public boolean getModelValidity() {
        return modelValidity == BuddyRemoteConfigValidity.Specific;
    }

    private BuddyRemoteConfigValidity getValidity(String preferenceConfig, Object value) {
        BuddyRemoteConfigValidity validity = BuddyRemoteConfigValidity.Any;

        try {
            if (this.config.getString(preferenceConfig).equalsIgnoreCase(value.toString())) {
                validity = BuddyRemoteConfigValidity.Specific;
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
        return appIdValidity == BuddyRemoteConfigValidity.Specific && apiLevelValidity == BuddyRemoteConfigValidity.Specific;
    }

    public boolean isAppIdAndModelSet()
    {
        return appIdValidity == BuddyRemoteConfigValidity.Specific && modelValidity == BuddyRemoteConfigValidity.Specific;
    }

    public boolean isApiLevelAndModelSet()
    {
        return apiLevelValidity == BuddyRemoteConfigValidity.Specific && modelValidity == BuddyRemoteConfigValidity.Specific;
    }
}

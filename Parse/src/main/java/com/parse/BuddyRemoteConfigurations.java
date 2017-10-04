package com.parse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuddyRemoteConfigurations {
    private Map<Integer, List<BuddyRemoteConfiguration>> configurations = new HashMap<Integer, List<BuddyRemoteConfiguration>>();
    private int numberOfKeys = 3;

    public void add(BuddyRemoteConfiguration configuration) {
        Integer validKeyCount = configuration.validKeys();

        List<BuddyRemoteConfiguration> configs = configurations.get(validKeyCount);

        if (configs == null) {
            configs = new ArrayList<>();
        }
        configs.add(configuration);
        configurations.put(validKeyCount, configs);
    }

    public JSONObject getValidConfig() {
        List<BuddyRemoteConfiguration> configs = configurations.get(numberOfKeys);

        // first check if we have any configs with all 3 set
        if (configs != null && configs.size() > 0) {
            // find the one that matches this app id
            for (BuddyRemoteConfiguration config : configs) {
                if (config.isAppIdAndApiLevelAndModelSet() ) {
                    return config.getConfiguration();
                }
            }
        }

        // secondly check if we have any configs with 2 set
        configs = configurations.get(--numberOfKeys);

        if (configs != null && configs.size() > 0) {
            // first check if we have appid and api level set
            JSONObject result = null;

            for (BuddyRemoteConfiguration config : configs) {
                if (config.isAppIdAndApiLevelSet() ) {
                    return config.getConfiguration();
                }
            }

            for (BuddyRemoteConfiguration config : configs) {
                if (config.isAppIdAndModelSet() ) {
                    return config.getConfiguration();
                }
            }

            for (BuddyRemoteConfiguration config : configs) {
                if (config.isApiLevelAndModelSet() ) {
                    return config.getConfiguration();
                }
            }
        }

        // thirdly, check if we have configs with 1 set
        configs = configurations.get(--numberOfKeys);

        if (configs != null && configs.size() > 0) {
            // first check if we have appid  set
            JSONObject result = null;

            for (BuddyRemoteConfiguration config : configs) {
                if (config.getAppIdValidity() ) {
                    return config.getConfiguration();
                }
            }

            for (BuddyRemoteConfiguration config : configs) {
                if (config.getApiLevelValidity() ) {
                    return config.getConfiguration();
                }
            }

            for (BuddyRemoteConfiguration config : configs) {
                if (config.getModelValidity() ) {
                    return config.getConfiguration();
                }
            }
        }

        // finally none set
        configs = configurations.get(--numberOfKeys);
        if (configs != null && configs.size() > 0) {
            return configs.get(0).getConfiguration();
        }

        return null;
    }
}

package com.parse;

import java.util.List;

public class BuddyRemoteConfigHandler {
    private java.util.Map<Integer, List<BuddyRemoteConfiguration>> configurations;

    public void addConfig(BuddyRemoteConfiguration configuration) {
        Integer validKeys = configuration.validKeys();
    }
}



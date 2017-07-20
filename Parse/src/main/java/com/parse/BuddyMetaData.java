package com.parse;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

public class BuddyMetaData {
    private static final String TAG = "com.parse.BuddyMetaData";

    /* package for test */ static BuddyAltDataController getAltDataController() {
        return ParseCorePlugins.getInstance().getAltDataController();
    }

    public static void uploadMetaDataInBackground(String name, JSONObject parametersObject, SaveCallback callback) {
        ParseTaskUtils.callbackOnMainThreadAsync(uploadMetaDataInBackground(name, parametersObject), callback);
    }

    public static Task<Void> uploadMetaDataInBackground(final String name,
                                          final JSONObject parametersObject) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("A name for the custom event must be provided.");
        }

        return ParseUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
            @Override
            public Task<Void> then(Task<String> task) throws Exception {
                String sessionToken = task.getResult();
                return getAltDataController().trackMetaInBackground(name, parametersObject, sessionToken);
            }
        });
    }

}

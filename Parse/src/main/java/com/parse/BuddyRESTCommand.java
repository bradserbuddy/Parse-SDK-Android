package com.parse;

import android.net.Uri;

import com.parse.http.ParseHttpRequest;

import org.json.JSONObject;


class BuddyRESTCommand extends ParseRESTCommand {

    /* package for test */
    private static final String META_PATH = "meta/%s";

    /* package */ static BuddyRESTCommand trackMetaCommand(
            String eventName, JSONObject parametersObject, String sessionToken) {

        String httpPath = String.format(META_PATH, Uri.encode(eventName));

        return new BuddyRESTCommand(
                httpPath, ParseHttpRequest.Method.POST, parametersObject, sessionToken);
    }

    private BuddyRESTCommand(
            String httpPath,
            ParseHttpRequest.Method httpMethod,
            JSONObject parameters,
            String sessionToken) {
        super(httpPath, httpMethod, parameters, sessionToken);
    }
}

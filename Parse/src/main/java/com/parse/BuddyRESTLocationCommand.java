package com.parse;

import android.net.Uri;

import com.parse.http.ParseHttpRequest;

import org.json.JSONObject;


class BuddyRESTLocationCommand extends ParseRESTCommand {

    /* package for test */
    private static final String LOCATIONPATH = "meta/%s";

    /* package */ static BuddyRESTLocationCommand trackLocationEventCommand (
            String eventName, JSONObject parametersObject, String sessionToken) {

        String httpPath = String.format(LOCATIONPATH, Uri.encode(eventName));

        return new BuddyRESTLocationCommand(
                httpPath, ParseHttpRequest.Method.POST, parametersObject, sessionToken);
    }

    private BuddyRESTLocationCommand(
            String httpPath,
            ParseHttpRequest.Method httpMethod,
            JSONObject parameters,
            String sessionToken) {
        super(httpPath, httpMethod, parameters, sessionToken);
    }
}

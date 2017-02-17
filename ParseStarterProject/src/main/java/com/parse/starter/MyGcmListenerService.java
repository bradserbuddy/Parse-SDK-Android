package com.parse.starter;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
//import com.android.volley.*;
//import com.android.volley.toolbox.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by bradleyserbus on 12/17/16.
 */

public class MyGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {

        /*RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://app.datadoghq.com/api/v1/events?api_key=d2fecbda4fff5fe1e0dd82980bae9492&application_key=16c917955e87bd526ff12ecd27d256c9ee3a9f66";

        JSONObject json = null;
        try {
            json = new JSONObject("{\"title\":\"Parse Push\", \"text\": \"Success\", \"tags\": [\"application:Parse\", \"platform:GCM\"]}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
            }
        };

        queue.add(stringRequest);*/
        int i = 1;
    }
}

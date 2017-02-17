/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

    // Enable Local Datastore.
    //Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.Configuration.Builder builder = new Parse.Configuration.Builder(this);
    builder.applicationId("c1f754e7-bfd5-4fb6-b416-896ba0c4a0c0");
    builder.server("https://api.parse.buddy.com/parse/");
    builder.clientKey("");
    Parse.initialize(builder.build());

    /*ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);*/

    ParseInstallation pi = ParseInstallation.getCurrentInstallation();
    pi.saveInBackground();
  }
}

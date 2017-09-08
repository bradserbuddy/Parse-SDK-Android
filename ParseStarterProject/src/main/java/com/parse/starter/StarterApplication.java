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

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;


public class StarterApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (Parse.Buddy.skipApplicationOnCreate(this)) {
      return;
    }

    Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    Parse.initialize(new Parse.Configuration.Builder(this)
            .applicationId("Parse on Buddy AppId goes here")
            .server("https://api.parse.buddy.com/parse/")
            .build());
    //ParseFacebookUtils.initialize(getApplicationContext());

    //ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

    ParseInstallation.getCurrentInstallation().saveInBackground();

  }
}

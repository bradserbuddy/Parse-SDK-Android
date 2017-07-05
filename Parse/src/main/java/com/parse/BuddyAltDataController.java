package com.parse;


import org.json.JSONObject;

import bolts.Task;


class BuddyAltDataController {

    ParseEventuallyQueue eventuallyQueue;

    BuddyAltDataController(ParseEventuallyQueue eventuallyQueue) {
        this.eventuallyQueue = eventuallyQueue;
    }

    Task<Void> trackMetaInBackground(final String name,
                                     JSONObject parametersObject, String sessionToken) {
        ParseRESTCommand command = BuddyRESTCommand.trackMetaCommand(name, parametersObject,
                sessionToken);

        Task<JSONObject> eventuallyTask = eventuallyQueue.enqueueEventuallyAsync(command, null);

        return eventuallyTask.makeVoid();
    }
}

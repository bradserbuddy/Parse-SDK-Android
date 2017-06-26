package com.parse;


import org.json.JSONObject;

import bolts.Task;


class BuddyLocationsController {

    /* package for test */ ParseEventuallyQueue eventuallyQueue;

    BuddyLocationsController(ParseEventuallyQueue eventuallyQueue) {
        this.eventuallyQueue = eventuallyQueue;
    }

    Task<Void> trackLocationEventInBackground(final String name,
                                                     JSONObject parametersObject, String sessionToken) {
        ParseRESTCommand command = BuddyRESTCommand.trackLocationEventCommand(name, parametersObject,
                sessionToken);

        Task<JSONObject> eventuallyTask = eventuallyQueue.enqueueEventuallyAsync(command, null);

        return eventuallyTask.makeVoid();
    }
}

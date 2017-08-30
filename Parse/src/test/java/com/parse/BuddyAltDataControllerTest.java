package com.parse;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import bolts.Task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyAltDataControllerTest {

    @Before
    public void setUp() throws MalformedURLException {
        ParseRESTCommand.server = new URL("https://parse.buddy/parse");
    }

    @After
    public void tearDown() {
        ParseRESTCommand.server = null;
    }


    @Test
    public void testTrackMeta() throws Exception {
        // Mock eventually queue
        ParseEventuallyQueue queue = mock(ParseEventuallyQueue.class);
        when(queue.enqueueEventuallyAsync(any(BuddyRESTCommand.class), any(ParseObject.class)))
                .thenReturn(Task.forResult(new JSONObject()));

        // Execute
        BuddyAltDataController controller = new BuddyAltDataController(queue);
        JSONObject params = new JSONObject();
        params.put("hello","there");
        ParseTaskUtils.wait(controller.trackMetaInBackground("locations", params, "sessionToken"));

        // Verify eventuallyQueue.enqueueEventuallyAsync
        ArgumentCaptor<BuddyRESTCommand> command = ArgumentCaptor.forClass(BuddyRESTCommand.class);
        ArgumentCaptor<ParseObject> object = ArgumentCaptor.forClass(ParseObject.class);
        verify(queue, times(1)).enqueueEventuallyAsync(command.capture(), object.capture());

        // Verify eventuallyQueue.enqueueEventuallyAsync object parameter
        assertNull(object.getValue());

        // Verify eventuallyQueue.enqueueEventuallyAsync command parameter
        assertTrue(command.getValue() instanceof BuddyRESTCommand);
        assertTrue(command.getValue().httpPath.contains("meta"));
        assertEquals("sessionToken", command.getValue().getSessionToken());
        JSONObject payload = command.getValue().jsonParameters;
        assertEquals("there", payload.get("hello"));
        assertEquals(1, command.getValue().jsonParameters.length());
    }
}
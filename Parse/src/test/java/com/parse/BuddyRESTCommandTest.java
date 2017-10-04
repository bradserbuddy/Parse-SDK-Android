package com.parse;

import com.parse.http.ParseHttpRequest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class BuddyRESTCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testInitialRequestHasMetaEndpointURL() throws Exception {
        // arrange
        JSONObject payload = new JSONObject();
        String url = "https://parse.buddy.com/parse/";
        BuddyRESTCommand.server = new URL(url);

        // act
        BuddyRESTCommand command = BuddyRESTCommand.trackMetaCommand ("locations", payload, "");

        // assert
        assertEquals(url + "meta/locations", command.url);
    }
}

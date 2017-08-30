package com.parse;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import bolts.Task;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyMetaDataTest {

    BuddyAltDataController controller;

    @Before
    public void setUp() {
        ParseTestUtils.setTestParseUser();

        // Mock ParseAnalyticsController
        controller = mock(BuddyAltDataController.class);
        when(controller.trackMetaInBackground(
                anyString(),
                any(JSONObject.class),
                anyString())).thenReturn(Task.<Void>forResult(null));

        ParseCorePlugins.getInstance().registerAltDataController(controller);
    }

    @After
    public void tearDown() {
        ParseCorePlugins.getInstance().reset();
        controller = null;
    }

    @Test
    public void testGetAltDataController() throws Exception{
        assertSame(controller, BuddyMetaData.getAltDataController());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadMetaDataInBackgroundNullName() throws Exception {
        ParseTaskUtils.wait(BuddyMetaData.uploadMetaDataInBackground(null, new JSONObject()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadMetaDataInBackgroundEmptyName() throws Exception {
        ParseTaskUtils.wait(BuddyMetaData.uploadMetaDataInBackground("", new JSONObject()));
    }

    @Test
    public void testUploadMetaDataInBackgroundNormalName() throws Exception{
        JSONObject payload = new JSONObject();
        String location = "location";
        ParseTaskUtils.wait(BuddyMetaData.uploadMetaDataInBackground(location, payload));

        verify(controller, times(1)).trackMetaInBackground(
                eq(location), Matchers.eq(payload), isNull(String.class));
    }

    @Test
    public void testUploadMetaDataInBackgroundNullCallback() throws Exception{
        String location = "location";
        JSONObject payload = new JSONObject();

        BuddyMetaData.uploadMetaDataInBackground(location, payload, null);

        verify(controller, times(1)).trackMetaInBackground(
                eq(location), eq(payload), isNull(String.class));
    }

    @Test
    public void testUploadMetaDataInBackgroundNormalCallback() throws Exception {
        String location = "location";
        JSONObject payload = new JSONObject();
        final Semaphore done = new Semaphore(0);
        BuddyMetaData.uploadMetaDataInBackground(location, payload,
                new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        assertNull(e);
                        done.release();
                    }
                });

        // Make sure the callback is called
        assertTrue(done.tryAcquire(1, 10, TimeUnit.SECONDS));
        verify(controller, times(1)).trackMetaInBackground(
                eq(location), eq(payload), isNull(String.class));
    }
}

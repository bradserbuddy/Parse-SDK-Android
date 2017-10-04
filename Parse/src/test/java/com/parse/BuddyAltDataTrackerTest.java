package com.parse;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyAltDataTrackerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private SharedPreferences sharedPrefs;
    private Context context;
    private Application appContext;

    @Before
    public void setUp() throws Exception {
        sharedPrefs = Mockito.mock(SharedPreferences.class);
        context = Mockito.mock(Context.class);
        appContext = Mockito.mock(RuntimeEnvironment.application.getClass());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSingleton() throws Exception {
        // arrange


        // act
        BuddyAltDataTracker tracker = BuddyAltDataTracker.getInstance();

        // assert
        assertNotNull(tracker);
    }

    @Test
    public void testInitialize() throws Exception {
        // arrange
        when(context.getApplicationContext()).thenReturn(appContext);

        // act
        BuddyAltDataTracker.getInstance().initialize(appContext);

        // assert
        verify(appContext).startActivity(any(Intent.class));
    }

//    @Test
//    public void testLoadNewConfiguration() throws Exception {
//        // arrange
//
//
//        // act
//        BuddyAltDataTracker.getInstance().loadNewConfiguration();
//
//        // assert
//
//    }
}
package com.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyUploadStatusTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private BuddyUploadCriteria buddyUploadCriteria;
    private java.lang.Integer level;
    private java.lang.Integer scale;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Before
    public void setUp() throws Exception {
        buddyUploadCriteria = new BuddyUploadCriteria();
        context = Mockito.mock(Context.class);
    }

    @After
    public void tearDown() throws Exception {
        buddyUploadCriteria = null;
        context = null;
    }

    @Test
    public void testGetSetIsUploading() throws Exception {
        // arrange
        BuddyUploadStatus status = new BuddyUploadStatus();
        boolean isUploading = true;

        // act
        status.setUploading(isUploading);

        // assert
        assertEquals(isUploading, status.isUploading());
    }

    @Test
    public void testGetSetJobsCount() throws Exception {
        // arrange
        BuddyUploadStatus status = new BuddyUploadStatus();

        // act
        long jobsCount = 10;
        status.setJobsCount(jobsCount);

        // assert
        assertEquals(jobsCount, status.getJobsCount());
    }
}
package com.parse;

import android.content.Context;
import android.content.SharedPreferences;

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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyConfigurationTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {


    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetAndGetUploadedEpoch() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long lastUploadedEpoch = 21;

        // act
        configuration.setLastUploadedEpoch(lastUploadedEpoch);

        // assert
        assertEquals(lastUploadedEpoch, configuration.getLastUploadedEpoch());
    }

    @Test
    public void testSetAndGetUploadLocation() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        boolean androidUploadLocation = true;

        // act
        configuration.setUploadLocation(androidUploadLocation);

        // assert
        assertEquals(androidUploadLocation, configuration.shouldUploadLocation());
    }

    @Test
    public void testSetAndGetUploadCellular() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        boolean androidUploadCellular = true;

        // act
        configuration.setUploadCellular(androidUploadCellular);

        // assert
        assertEquals(androidUploadCellular, configuration.shouldUploadCellular());
    }

    @Test
    public void testSetAndGetLogLocation() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        boolean androidLogLocation = true;

        // act
        configuration.setLogLocation(androidLogLocation);

        // assert
        assertEquals(androidLogLocation, configuration.shouldLogLocation());
    }

    @Test
    public void testSetAndGetLogCellular() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        boolean androidLogCellular = true;

        // act
        configuration.setLogCellular(androidLogCellular);

        // assert
        assertEquals(androidLogCellular, configuration.shouldLogCellular());
    }

    @Test
    public void testSetAndGetAndroidLocationUpdateInterval() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long androidLocationUpdateInterval = 20;

        // act
        configuration.setAndroidLocationUpdateInterval(androidLocationUpdateInterval);

        // assert
        assertEquals(androidLocationUpdateInterval, configuration.getAndroidLocationUpdateInterval());
    }

    @Test
    public void testSetAndGetAndroidLocationFastestUpdateInterval() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long androidLocationFastestUpdateInterval = 19;

        // act
        configuration.setAndroidLocationFastestUpdateInterval(androidLocationFastestUpdateInterval);

        // assert
        assertEquals(androidLocationFastestUpdateInterval, configuration.getAndroidLocationFastestUpdateInterval());
    }

    @Test
    public void testSetAndGetAndroidLocationPowerAccuracy() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long androidLocationPowerAccuracy = 18;

        // act
        configuration.setAndroidLocationPowerAccuracy(androidLocationPowerAccuracy);

        // assert
        assertEquals(androidLocationPowerAccuracy, configuration.getAndroidLocationPowerAccuracy());
    }

    @Test
    public void testSetAndGetCommonMaxRecordsToDelete() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonMaxRecordsToDelete = 17;

        // act
        configuration.setCommonMaxRecordsToDelete(commonMaxRecordsToDelete);

        // assert
        assertEquals(commonMaxRecordsToDelete, configuration.getCommonMaxRecordsToDelete());
    }

    @Test
    public void testSetAndGetCommonMaxErrorRecords() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonMaxErrorRecords = 16;

        // act
        configuration.setCommonMaxErrorRecords(commonMaxErrorRecords);

        // assert
        assertEquals(commonMaxErrorRecords, configuration.getCommonMaxErrorRecords());
    }

    @Test
    public void testSetAndGetCommonMaxCellularRecords() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonMaxCellularRecords = 15;

        // act
        configuration.setCommonMaxCellularRecords(commonMaxCellularRecords);

        // assert
        assertEquals(commonMaxCellularRecords, configuration.getCommonMaxCellularRecords());
    }

    @Test
    public void testSetAndGetCommonMaxLocationRecords() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonMaxLocationRecords = 14;

        // act
        configuration.setCommonMaxLocationRecords(commonMaxLocationRecords);

        // assert
        assertEquals(commonMaxLocationRecords, configuration.getCommonMaxLocationRecords());
    }

    @Test
    public void testSetAndGetCommonCellularPushBatchSize() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonCellularPushBatchSize = 13;

        // act
        configuration.setCommonCellularPushBatchSize(commonCellularPushBatchSize);

        // assert
        assertEquals(commonCellularPushBatchSize, configuration.getCommonCellularPushBatchSize());
    }

    @Test
    public void testSetAndGetCommonCellularLogTimeout() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonCellularLogTimeout = 12;

        // act
        configuration.setCommonCellularLogTimeout(commonCellularLogTimeout);

        // assert
        assertEquals(commonCellularLogTimeout, configuration.getCommonCellularLogTimeout());
    }

    @Test
    public void testSetAndGetCommonLocationPushBatchSize() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long commonLocationPushBatchSize = 11;

        // act
        configuration.setCommonLocationPushBatchSize(commonLocationPushBatchSize);

        // assert
        assertEquals(commonLocationPushBatchSize, configuration.getCommonLocationPushBatchSize());
    }

    @Test
    public void testSetAndGetVersion() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        long version = 10;

        // act
        configuration.setVersion(version);

        // assert
        assertEquals(version, configuration.getVersion());
    }

}

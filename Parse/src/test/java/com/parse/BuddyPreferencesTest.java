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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyPreferencesTest {
    private SharedPreferences sharedPrefs;
    private Context context;
    //private BuddyPreferenceService preferences;
    private long epoch = 100;
    private long lastVersion = 101;
    private long locationsBatchSize = 102;
    private long cellularBatchSize = 103;
    private long cellularLogTimeout = 104;
    private long locationPowerAccuracy = 105;
    private long locationUpdateInterval = 106;
    private long locationFastestUpdateInterval = 107;
    private boolean shouldLogCellular = true;
    private boolean shouldLogLocation = false;
    private boolean shouldUploadCellular = true;
    private boolean shouldUploadLocation = false;
    private long locationsMaxRecords = 108;
    private long cellularMaxRecords = 109;
    private long errorMaxRecords = 110;
    private long maxRecordsToDelete = 111;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sharedPrefs = Mockito.mock(SharedPreferences.class);
        context = Mockito.mock(Context.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetConfigMaxRecordsToDelete() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigMaxRecordsToDelete, 0)).thenReturn(maxRecordsToDelete);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(maxRecordsToDelete, configuration.getCommonMaxRecordsToDelete());
    }

    @Test
    public void testGetConfigErrorMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigErrorMaxRecords, 0)).thenReturn(errorMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(errorMaxRecords, configuration.getCommonMaxErrorRecords());
    }

    @Test
    public void testGetConfigCellularMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularMaxRecords, 0)).thenReturn(cellularMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(cellularMaxRecords, configuration.getCommonMaxCellularRecords());
    }

    @Test
    public void testGetConfigLocationsMaxRecords() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationMaxRecords, 0)).thenReturn(locationsMaxRecords);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(locationsMaxRecords, configuration.getCommonMaxLocationRecords());
    }

    @Test
    public void testGetConfigShouldUploadLocation() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadLocation, false)).thenReturn(shouldUploadLocation);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(shouldUploadLocation, configuration.shouldUploadLocation());
    }

    @Test
    public void testGetConfigShouldUploadCellular() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigUploadCellular, true)).thenReturn(shouldUploadCellular);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(shouldUploadCellular, configuration.shouldUploadCellular());
    }

    @Test
    public void testGetConfigShouldLogLocation() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigLogLocation, false)).thenReturn(shouldLogLocation);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(shouldLogLocation, configuration.shouldLogLocation());
    }

    @Test
    public void testGetConfigShouldLogCellular() throws Exception {
        // arrange
        when(sharedPrefs.getBoolean(BuddyPreferenceKeys.preferenceConfigLogCellular, true)).thenReturn(shouldLogCellular);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(shouldLogCellular, configuration.shouldLogCellular());
    }

    @Test
    public void testGetConfigLocationFastestUpdateInterval() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationFastestUpdateIntervalMs, 0)).thenReturn(locationFastestUpdateInterval);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(locationFastestUpdateInterval, configuration.getAndroidLocationFastestUpdateInterval());
    }

    @Test
    public void testGetConfigLocationUpdateInterval() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationUpdateIntervalMs, 0)).thenReturn(locationUpdateInterval);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(locationUpdateInterval, configuration.getAndroidLocationUpdateInterval());
    }

    @Test
    public void testGetConfigLocationPowerAccuracy() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationPowerAccuracy, 0)).thenReturn(locationPowerAccuracy);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(locationPowerAccuracy, configuration.getAndroidLocationPowerAccuracy());
    }

    @Test
    public void testGetConfigCellularLogTimeout() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularLogTimeoutMs, 0)).thenReturn(cellularLogTimeout);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(cellularLogTimeout, configuration.getCommonCellularLogTimeout());
    }

    @Test
    public void testGetConfigCellularBatchSize() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigCellularPushBatch, 0)).thenReturn(cellularBatchSize);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(cellularBatchSize, configuration.getCommonCellularPushBatchSize());
    }

    @Test
    public void testGetConfigLocationBatchSize() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigLocationPushBatch, 0)).thenReturn(locationsBatchSize);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(locationsBatchSize, configuration.getCommonLocationPushBatchSize());
    }

    @Test
    public void testGetConfigLastVersion() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceConfigVersion, 0)).thenReturn(lastVersion);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(lastVersion, configuration.getVersion());
    }

    @Test
    public void testGetConfigEpoch() throws Exception {
        // arrange
        when(sharedPrefs.getLong(BuddyPreferenceKeys.preferenceLastUpdatedEpoch, 0)).thenReturn(epoch);

        // act
        BuddyConfiguration configuration = BuddyPreferenceService.getConfig(context);

        // assert
        assertEquals(epoch, configuration.getLastUploadedEpoch());
    }
}

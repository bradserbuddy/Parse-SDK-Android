package com.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.support.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyUploadCriteriaTest {
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
    public void testGeInitialBatteryPercentage() throws Exception {
        // arrange
        Intent intent = Mockito.mock(Intent.class);
        when(context.registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class))).thenReturn(intent);
        level = 2;
        scale = 5;
        when(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)).thenReturn(level);
        when(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)).thenReturn(scale);

        // act
        int percentage = buddyUploadCriteria.getBatteryPercentage(context);

        // assert
        assertEquals(40, percentage);
    }

    @Test
    public void testSetHasEnoughBatteryWhenBatteryOK() throws Exception {
        // arrange

        // act
        buddyUploadCriteria.setHasEnoughBattery(Intent.ACTION_BATTERY_OKAY);

        // assert
        boolean hasEnough = buddyUploadCriteria.getHasEnoughBattery(context);
        assertEquals(true, hasEnough);
    }

    @Test
    public void testSetHasEnoughBatteryWhenBatteryLowAndPercentageIsOver20() throws Exception {
        // arrange
        // set battery to 40%
        Intent intent = Mockito.mock(Intent.class);
        when(context.registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class))).thenReturn(intent);
        level = 2;
        scale = 5;
        when(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)).thenReturn(level);
        when(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)).thenReturn(scale);

        // act
        buddyUploadCriteria.setHasEnoughBattery(Intent.ACTION_BATTERY_LOW);

        // assert
        boolean hasEnough = buddyUploadCriteria.getHasEnoughBattery(context);
        assertEquals(true, hasEnough);
    }

    @Test
    public void testSetHasEnoughBatteryWhenBatteryLowAndPercentageIsBelow20() throws Exception {
        // arrange
        // set battery to 10%
        Intent intent = Mockito.mock(Intent.class);
        when(context.registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class))).thenReturn(intent);
        level = 1;
        scale = 10;
        when(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)).thenReturn(level);
        when(intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)).thenReturn(scale);

        // act
        buddyUploadCriteria.setHasEnoughBattery(Intent.ACTION_BATTERY_LOW);

        // assert
        boolean hasEnough = buddyUploadCriteria.getHasEnoughBattery(context);
        assertEquals(false, hasEnough);
    }

    @Test
    public void testSetAndGetWifiConnectivityStatus() throws Exception {
        // arrange
        BuddyWifiConnectivityStatus status = BuddyWifiConnectivityStatus.Connected;

        // act
        buddyUploadCriteria.setWifiConnectivityStatus(status);

        // assert
        BuddyConnectivityStatus savedStatus = buddyUploadCriteria.getConnectivityStatus();
        assertEquals(BuddyConnectivityStatus.Wifi, savedStatus);
    }

    @Test
    public void testSetAndGetPowerStatus() throws Exception {
        // arrange
        BuddyPowerConnectionStatus status = BuddyPowerConnectionStatus.Connected;

        // act
        buddyUploadCriteria.setPowerStatus(status);

        // assert
        BuddyPowerConnectionStatus savedStatus = buddyUploadCriteria.getPowerStatus();
        assertEquals(status, savedStatus);
    }

    @Test
    public void testWhenUploadingReturnsFalse() throws Exception {
        // arrange
        buddyUploadCriteria.startUpload();

        // act
        boolean canUpload = buddyUploadCriteria.canUpload(context,null,false);

        // assert
        assertEquals(false, canUpload);
    }

    @Test
    public void testWhenNotUploadingAndTimeElapsedAndOnWifiAndChargingReturnsTrue() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        // set last uploaded to a couple of days ago
        configuration.setLastUploadedEpoch(System.currentTimeMillis()-2*24*3600*1000);
        // wifi connected
        buddyUploadCriteria.setWifiConnectivityStatus(BuddyWifiConnectivityStatus.Connected);
        buddyUploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);

        // act
        boolean canUpload = buddyUploadCriteria.canUpload(context,configuration,false);

        // assert
        assertEquals(true, canUpload);
    }

    @Test
    public void testWhenNotUploadingAndTimeElapsedAndOnCellularAndChargingReturnsTrue() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        // set last uploaded to a couple of days ago
        configuration.setLastUploadedEpoch(System.currentTimeMillis()-2*24*3600*1000);
        // wifi connected
        buddyUploadCriteria.setCellularConnectivityStatus(BuddyCellularConnectivityStatus.Connected);
        buddyUploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);

        // act
        boolean canUpload = buddyUploadCriteria.canUpload(context,configuration,false);

        // assert
        assertEquals(true, canUpload);
    }

    @Test
    public void testWhenNotUploadingAndTimeElapsedAndOnCellularAndNotChargingButEnoughBatteryReturnsTrue() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        // set last uploaded to a couple of days ago
        configuration.setLastUploadedEpoch(System.currentTimeMillis()-2*24*3600*1000);
        // wifi connected
        buddyUploadCriteria.setCellularConnectivityStatus(BuddyCellularConnectivityStatus.Connected);
        buddyUploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
        buddyUploadCriteria.setHasEnoughBattery(Intent.ACTION_BATTERY_OKAY);

        // act
        boolean canUpload = buddyUploadCriteria.canUpload(context,configuration,false);

        // assert
        assertEquals(true, canUpload);
    }

    @Test
    public void testWhenNotUploadingAndTimeNotElapsedButOnWifiAndChargingReturnsTrue() throws Exception {
        // arrange
        BuddyConfiguration configuration = new BuddyConfiguration();
        // set last uploaded to a couple of days ago
        configuration.setLastUploadedEpoch(System.currentTimeMillis());
        // wifi connected
        buddyUploadCriteria.setWifiConnectivityStatus(BuddyWifiConnectivityStatus.Connected);
        buddyUploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Connected);

        // act
        boolean canUpload = buddyUploadCriteria.canUpload(context,configuration,false);

        // assert
        assertEquals(true, canUpload);
    }

    @Test
    public void testUpdateInitialPowerStatusToCharging() throws Exception {
        // arrange
        buddyUploadCriteria.setPowerStatus(BuddyPowerConnectionStatus.Disconnected);
        Intent intent = Mockito.mock(Intent.class);
        when(context.registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class))).thenReturn(intent);
        when(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)).thenReturn(BatteryManager.BATTERY_STATUS_CHARGING);

        // act
        buddyUploadCriteria.updateInitialPowerStatus(context);

        // assert
        BuddyPowerConnectionStatus connectionStatus = buddyUploadCriteria.getPowerStatus();
        assertEquals(BuddyPowerConnectionStatus.Connected, connectionStatus);
    }

    @Test
    public void testStartUploadIncrementsJobs() throws Exception {
        // arrange

        // act
        int jobsCount = buddyUploadCriteria.startUpload();

        // assert
        assertEquals(1, jobsCount);
    }

    @Test
    public void testStartUploadWhileUploadingIncrementsJobs() throws Exception {
        // arrange
        buddyUploadCriteria.startUpload();

        // act
        int jobsCount = buddyUploadCriteria.startUpload();

        // assert
        assertEquals(2, jobsCount);
    }

    @Test
    public void testEndUploadDecrementsJobsCount() throws Exception {
        // arrange
        setupSharedPreferencesAndEditorMocks();
        buddyUploadCriteria.startUpload();

        // act
        int jobsCount = buddyUploadCriteria.endUpload(context);

        // assert
        assertEquals(0, jobsCount);
    }

    @Test
    public void testStartUploadWithMultipleThreads() throws Exception {
        // arrange
        setupSharedPreferencesAndEditorMocks();
        final int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // act
        for (int i = 0; i < threads; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    buddyUploadCriteria.startUpload();
                }
            });
        }

        executor.awaitTermination(5, TimeUnit.SECONDS);
        int jobsCount = buddyUploadCriteria.endUpload(context);

        // assert
        assertEquals(9, jobsCount);
    }

    private void setupSharedPreferencesAndEditorMocks() {
        sharedPreferences = Mockito.mock(SharedPreferences.class);
        editor = Mockito.mock(SharedPreferences.Editor.class);
        when(context.getSharedPreferences(BuddyPreferenceKeys.preferenceBuddyLocationTracker, 0)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
    }
}
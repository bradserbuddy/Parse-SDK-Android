package com.parse;

import android.content.Context;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyCellularServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private TelephonyManager telephonyManager;
    private Build build;

    @Before
    public void setUp() throws Exception {
        telephonyManager = Mockito.mock(TelephonyManager.class);
        build = Mockito.mock(android.os.Build.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetGsmCellularInfoForNougat() throws Exception {
        Context context = Mockito.mock(Context.class);

        // arrange
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 25);
        setFinalStatic(Build.VERSION_CODES.class.getField("N"), 20);
        List<CellInfo> cellInfos = new ArrayList<>();
        CellInfoGsm cellInfoGsm = Mockito.mock(CellInfoGsm.class);
        boolean isRegistered = true;
        when(cellInfoGsm.isRegistered()).thenReturn(isRegistered);
        cellInfos.add(cellInfoGsm);
        CellIdentityGsm cellIdentityGsm = Mockito.mock(CellIdentityGsm.class);
        when(telephonyManager.getAllCellInfo()).thenReturn(cellInfos);
        when(cellInfoGsm.getCellIdentity()).thenReturn(cellIdentityGsm);
        CellSignalStrengthGsm signalStrengthGsm = Mockito.mock(CellSignalStrengthGsm.class);
        when(cellInfoGsm.getCellSignalStrength()).thenReturn(signalStrengthGsm);
        int cid = 101;
        int mcc = 102;
        int lac = 103;
        int mnc = 104;
        int asu = 105;
        int level = 106;
        int dbm = 107;
        int bsic = 108;
        int arfcn = 109;
        when(cellIdentityGsm.getCid()).thenReturn(cid);
        when(cellIdentityGsm.getMcc()).thenReturn(mcc);
        when(cellIdentityGsm.getLac()).thenReturn(lac);
        when(cellIdentityGsm.getMnc()).thenReturn(mnc);
        when(signalStrengthGsm.getAsuLevel()).thenReturn(asu);
        when(signalStrengthGsm.getLevel()).thenReturn(level);
        when(signalStrengthGsm.getDbm()).thenReturn(dbm);
        when(cellIdentityGsm.getBsic()).thenReturn(bsic);
        when(cellIdentityGsm.getArfcn()).thenReturn(arfcn);

        // act
        JSONObject gsmCellInfo = BuddyCellularInformation.getCellInformation(context, telephonyManager);

        // assert
        JSONArray data = (JSONArray)gsmCellInfo.get("data");
        JSONObject first = (JSONObject) data.get(0);
        assertTrue((Boolean) first.get("isRegistered"));
        assertEquals(BuddyCellularNetworkType.GSM.toString(), first.get("networkType"));
        assertEquals(cid, first.get("cellId"));
        assertEquals(mcc, first.get("mobileCountryCode"));
        assertEquals(lac, first.get("locationAreaCode"));
        assertEquals(mnc, first.get("mobileNetworkCode"));
        assertEquals(asu, first.get("asuLevel"));
        assertEquals(dbm, first.get("signalStrengthDbm"));
        assertEquals(level, first.get("signalLevel"));
        assertEquals(arfcn, first.get("absoluteRFChannelNo"));
        assertEquals(bsic, first.get("baseStationIdCode"));
    }

    @Test
    public void testGetCdmaCellularInfoForNougat() throws Exception {
        Context context = Mockito.mock(Context.class);

        // arrange
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 25);
        setFinalStatic(Build.VERSION_CODES.class.getField("N"), 20);
        List<CellInfo> cellInfos = new ArrayList<>();
        CellInfoCdma cellInfoCdma = Mockito.mock(CellInfoCdma.class);
        boolean isRegistered = true;
        when(cellInfoCdma.isRegistered()).thenReturn(isRegistered);
        cellInfos.add(cellInfoCdma);
        CellIdentityCdma cellIdentityCdma = Mockito.mock(CellIdentityCdma.class);
        when(telephonyManager.getAllCellInfo()).thenReturn(cellInfos);
        when(cellInfoCdma.getCellIdentity()).thenReturn(cellIdentityCdma);
        CellSignalStrengthCdma cellSignalStrengthCdma = Mockito.mock(CellSignalStrengthCdma.class);
        when(cellInfoCdma.getCellSignalStrength()).thenReturn(cellSignalStrengthCdma);
        int latitude = 101;
        int baseStationId = 102;
        int longitude = 103;
        int networkId = 104;
        int systemId = 105;
        int level = 106;
        int dbm = 107;
        int asu = 108;

        when(cellIdentityCdma.getLatitude()).thenReturn(latitude);
        when(cellIdentityCdma.getBasestationId()).thenReturn(baseStationId);
        when(cellIdentityCdma.getLongitude()).thenReturn(longitude);
        when(cellIdentityCdma.getNetworkId()).thenReturn(networkId);
        when(cellSignalStrengthCdma.getAsuLevel()).thenReturn(asu);
        when(cellSignalStrengthCdma.getLevel()).thenReturn(level);
        when(cellSignalStrengthCdma.getDbm()).thenReturn(dbm);
        when(cellIdentityCdma.getSystemId()).thenReturn(systemId);

        // act
        JSONObject gsmCellInfo = BuddyCellularInformation.getCellInformation(context, telephonyManager);

        // assert
        JSONArray data = (JSONArray)gsmCellInfo.get("data");
        JSONObject first = (JSONObject) data.get(0);
        assertTrue((Boolean) first.get("isRegistered"));
        assertEquals(BuddyCellularNetworkType.CDMA.toString(), first.get("networkType"));
        assertEquals(latitude, first.get("latitude"));
        assertEquals(baseStationId, first.get("baseStationId"));
        assertEquals(longitude, first.get("longitude"));
        assertEquals(networkId, first.get("networkId"));
        assertEquals(asu, first.get("asuLevel"));
        assertEquals(systemId, first.get("systemId"));
        assertEquals(dbm, first.get("signalStrengthDbm"));
        assertEquals(level, first.get("signalLevel"));
    }

    @Test
    public void testGetWcdmaCellularInfoForNougat() throws Exception {
        Context context = Mockito.mock(Context.class);

        // arrange
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 25);
        setFinalStatic(Build.VERSION_CODES.class.getField("N"), 20);
        List<CellInfo> cellInfos = new ArrayList<>();
        CellInfoWcdma cellInfoWcdma = Mockito.mock(CellInfoWcdma.class);
        boolean isRegistered = true;
        when(cellInfoWcdma.isRegistered()).thenReturn(isRegistered);
        cellInfos.add(cellInfoWcdma);
        CellIdentityWcdma cellIdentityWcdma = Mockito.mock(CellIdentityWcdma.class);
        when(telephonyManager.getAllCellInfo()).thenReturn(cellInfos);
        when(cellInfoWcdma.getCellIdentity()).thenReturn(cellIdentityWcdma);
        CellSignalStrengthWcdma cellSignalStrengthWcdma = Mockito.mock(CellSignalStrengthWcdma.class);
        when(cellInfoWcdma.getCellSignalStrength()).thenReturn(cellSignalStrengthWcdma);
        int cid = 101;
        int mcc = 102;
        int lac = 103;
        int mnc = 104;
        int psc = 105;
        int uarfcn = 109;
        int level = 106;
        int dbm = 107;
        int asu = 108;

        when(cellIdentityWcdma.getCid()).thenReturn(cid);
        when(cellIdentityWcdma.getMcc()).thenReturn(mcc);
        when(cellIdentityWcdma.getLac()).thenReturn(lac);
        when(cellIdentityWcdma.getMnc()).thenReturn(mnc);
        when(cellIdentityWcdma.getPsc()).thenReturn(psc);
        when(cellIdentityWcdma.getUarfcn()).thenReturn(uarfcn);
        when(cellSignalStrengthWcdma.getAsuLevel()).thenReturn(asu);
        when(cellSignalStrengthWcdma.getLevel()).thenReturn(level);
        when(cellSignalStrengthWcdma.getDbm()).thenReturn(dbm);

        // act
        JSONObject gsmCellInfo = BuddyCellularInformation.getCellInformation(context, telephonyManager);

        // assert
        JSONArray data = (JSONArray)gsmCellInfo.get("data");
        JSONObject first = (JSONObject) data.get(0);
        assertTrue((Boolean) first.get("isRegistered"));
        assertEquals(BuddyCellularNetworkType.WCDMA.toString(), first.get("networkType"));
        assertEquals(cid, first.get("cellId"));
        assertEquals(mcc, first.get("mobileCountryCode"));
        assertEquals(lac, first.get("locationAreaCode"));
        assertEquals(mnc, first.get("mobileNetworkCode"));
        assertEquals(psc, first.get("primaryScramblingCode"));
        assertEquals(uarfcn, first.get("umtsAbsoluteRFChannelNo"));
        assertEquals(asu, first.get("asuLevel"));
        assertEquals(dbm, first.get("signalStrengthDbm"));
        assertEquals(level, first.get("signalLevel"));
    }

    @Test
    public void testGetCdmaCellularInfoForJellyBean() throws Exception {
        Context context = Mockito.mock(Context.class);

        // arrange
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);
        setFinalStatic(Build.VERSION_CODES.class.getField("N"), 20);
        CdmaCellLocation cdmaCellLocation = Mockito.mock(CdmaCellLocation.class);
        when(telephonyManager.getCellLocation()).thenReturn(cdmaCellLocation);


        int latitude = 101;
        int baseStationId = 102;
        int longitude = 103;
        int networkId = 104;
        int systemId = 105;

        when(cdmaCellLocation.getBaseStationLatitude()).thenReturn(latitude);
        when(cdmaCellLocation.getBaseStationId()).thenReturn(baseStationId);
        when(cdmaCellLocation.getBaseStationLongitude()).thenReturn(longitude);
        when(cdmaCellLocation.getNetworkId()).thenReturn(networkId);
        when(cdmaCellLocation.getSystemId()).thenReturn(systemId);

        // act
        JSONObject gsmCellInfo = BuddyCellularInformation.getCellInformation(context, telephonyManager);

        // assert
        JSONArray data = (JSONArray)gsmCellInfo.get("data");
        JSONObject first = (JSONObject) data.get(0);
        assertEquals(BuddyCellularNetworkType.CDMA.toString(), first.get("networkType"));
        assertEquals(latitude, first.get("latitude"));
        assertEquals(baseStationId, first.get("baseStationId"));
        assertEquals(longitude, first.get("longitude"));
        assertEquals(networkId, first.get("networkId"));
        assertEquals(systemId, first.get("systemId"));
    }

    @Test
    public void testGetGsmCellularInfoForJellyBean() throws Exception {
        Context context = Mockito.mock(Context.class);

        // arrange
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);
        setFinalStatic(Build.VERSION_CODES.class.getField("N"), 20);
        GsmCellLocation gsmCellLocation = Mockito.mock(GsmCellLocation.class);
        when(telephonyManager.getCellLocation()).thenReturn(gsmCellLocation);

        int cid = 101;
        int psc = 102;
        int lac = 103;

        when(gsmCellLocation.getCid()).thenReturn(cid);
        when(gsmCellLocation.getLac()).thenReturn(lac);
        when(gsmCellLocation.getPsc()).thenReturn(psc);

        // act
        JSONObject gsmCellInfo = BuddyCellularInformation.getCellInformation(context, telephonyManager);

        // assert
        JSONArray data = (JSONArray)gsmCellInfo.get("data");
        JSONObject first = (JSONObject) data.get(0);
        assertEquals(BuddyCellularNetworkType.GSM.toString(), first.get("networkType"));
        assertEquals(cid, first.get("cellId"));
        assertEquals(psc, first.get("primaryScramblingCode"));
        assertEquals(lac, first.get("locationAreaCode"));
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}

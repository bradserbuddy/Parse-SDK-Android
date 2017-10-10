package com.parse;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class BuddyCellularInformation {
    private static final String TAG = "com.parse.BuddyCellularInformation";

    public static JSONObject getCellInformation(Context context, TelephonyManager telephonyManager) {

        JSONObject cellInformation = new JSONObject();

        try {
            /*if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                int dataNetworkType = telephonyManager.getDataNetworkType();
                cellInformation.put("DataNetworkType", dataNetworkType);
            }*/

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                JSONArray cellInformationArray = new JSONArray();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                    List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();

                    if (allCellInfo != null) {
                        for (CellInfo cellInfo : allCellInfo) {
                            JSONObject cellularInfoObject = new JSONObject();
                            cellularInfoObject.put("isRegistered", cellInfo.isRegistered());

                            if (cellInfo instanceof CellInfoGsm) {
                                //PLog.i(TAG, "GSM network");
                                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                                CellIdentityGsm identityGSM = cellInfoGsm.getCellIdentity();
                                CellSignalStrengthGsm signalStrengthGsm = cellInfoGsm.getCellSignalStrength();

                                cellularInfoObject.put("networkType", BuddyCellularNetworkType.GSM.toString());
                                cellularInfoObject.put("cellId", identityGSM.getCid());
                                cellularInfoObject.put("mobileCountryCode", identityGSM.getMcc());
                                cellularInfoObject.put("locationAreaCode", identityGSM.getLac());
                                cellularInfoObject.put("mobileNetworkCode", identityGSM.getMnc());
                                cellularInfoObject.put("psc", identityGSM.getPsc());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cellularInfoObject.put("arfcn", identityGSM.getArfcn());
                                    cellularInfoObject.put("bsic", identityGSM.getBsic());
                                }

                                cellularInfoObject.put("asuLevel", signalStrengthGsm.getAsuLevel());
                                cellularInfoObject.put("signalStrengthDbm", signalStrengthGsm.getDbm());
                                cellularInfoObject.put("signalLevel", signalStrengthGsm.getLevel());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cellularInfoObject.put("absoluteRFChannelNo", identityGSM.getArfcn());
                                    cellularInfoObject.put("baseStationIdCode", identityGSM.getBsic());
                                }
                            } else if (cellInfo instanceof CellInfoCdma) {
                                //PLog.i(TAG, "CDMA network");
                                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                                CellIdentityCdma identityCdma = cellInfoCdma.getCellIdentity();
                                CellSignalStrengthCdma signalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                                cellularInfoObject.put("networkType", BuddyCellularNetworkType.CDMA.toString());
                                cellularInfoObject.put("latitude", identityCdma.getLatitude());
                                cellularInfoObject.put("baseStationId", identityCdma.getBasestationId());
                                cellularInfoObject.put("longitude", identityCdma.getLongitude());
                                cellularInfoObject.put("networkId", identityCdma.getNetworkId());
                                cellularInfoObject.put("systemId", identityCdma.getSystemId());
                                cellularInfoObject.put("asuLevel", signalStrengthCdma.getAsuLevel());
                                cellularInfoObject.put("signalStrengthDbm", signalStrengthCdma.getDbm());
                                cellularInfoObject.put("signalLevel", signalStrengthCdma.getLevel());

                                cellularInfoObject.put("cdmaLevel", signalStrengthCdma.getCdmaLevel());
                                cellularInfoObject.put("evdoLevel", signalStrengthCdma.getEvdoLevel());
                                cellularInfoObject.put("dbm", signalStrengthCdma.getDbm());
                                cellularInfoObject.put("cdmaDbm", signalStrengthCdma.getCdmaDbm());
                                cellularInfoObject.put("cdmaEcio", signalStrengthCdma.getCdmaEcio());
                                cellularInfoObject.put("evdoDbm", signalStrengthCdma.getEvdoDbm());
                                cellularInfoObject.put("evdoEcio", signalStrengthCdma.getEvdoEcio());
                                cellularInfoObject.put("evdoSnr", signalStrengthCdma.getEvdoSnr());
                            } else if (cellInfo instanceof CellInfoWcdma) {
                                //PLog.i(TAG, "WCDMA network");
                                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                                CellIdentityWcdma identityWcdma = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                    identityWcdma = cellInfoWcdma.getCellIdentity();
                                    CellSignalStrengthWcdma signalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                                    cellularInfoObject.put("networkType", BuddyCellularNetworkType.WCDMA.toString());
                                    cellularInfoObject.put("cellId", identityWcdma.getCid());
                                    cellularInfoObject.put("mobileCountryCode", identityWcdma.getMcc());
                                    cellularInfoObject.put("locationAreaCode", identityWcdma.getLac());
                                    cellularInfoObject.put("mobileNetworkCode", identityWcdma.getMnc());
                                    cellularInfoObject.put("primaryScramblingCode", identityWcdma.getPsc());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        cellularInfoObject.put("umtsAbsoluteRFChannelNo", identityWcdma.getUarfcn());
                                    }

                                    cellularInfoObject.put("asuLevel", signalStrengthWcdma.getAsuLevel());
                                    cellularInfoObject.put("signalStrengthDbm", signalStrengthWcdma.getDbm());
                                    cellularInfoObject.put("signalLevel", signalStrengthWcdma.getLevel());
                                }
                            } else if (cellInfo instanceof CellInfoLte) {
                                //PLog.i(TAG, "Other Lte network");
                                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                                CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();
                                cellularInfoObject.put("networkType", BuddyCellularNetworkType.LTE.toString());
                                cellularInfoObject.put("cellId", cellIdentityLte.getCi());
                                cellularInfoObject.put("mobileCountryCode", cellIdentityLte.getMcc());
                                cellularInfoObject.put("mobileNetworkCode", cellIdentityLte.getMnc());
                                cellularInfoObject.put("physicalCellId", cellIdentityLte.getPci());
                                cellularInfoObject.put("trackingAreaCode", cellIdentityLte.getTac());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cellularInfoObject.put("absoluteRFChannelNo", cellIdentityLte.getEarfcn());
                                }

                                cellularInfoObject.put("asuLevel", signalStrengthLte.getAsuLevel());
                                cellularInfoObject.put("signalStrengthDbm", signalStrengthLte.getDbm());
                                cellularInfoObject.put("signalLevel", signalStrengthLte.getLevel());
                                cellularInfoObject.put("timingAdvance", signalStrengthLte.getTimingAdvance());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    cellularInfoObject.put("rsrq", signalStrengthLte.getRsrq());
                                    cellularInfoObject.put("rssnr", signalStrengthLte.getRssnr());
                                    cellularInfoObject.put("rsrp", signalStrengthLte.getRsrp());
                                    cellularInfoObject.put("cqi", signalStrengthLte.getCqi());
                                }
                            }

                            if (cellularInfoObject.length() > 0) {
                                cellInformationArray.put(cellularInfoObject);
                            }
                        }
                    }
                } else {
                    // other api levels
                    CellLocation cellLocation = telephonyManager.getCellLocation();

                    if (cellLocation instanceof CdmaCellLocation) {
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                        JSONObject cellularInfoObject = new JSONObject();
                        cellularInfoObject.put("networkType", BuddyCellularNetworkType.CDMA.toString());
                        cellularInfoObject.put("latitude", cdmaCellLocation.getBaseStationLatitude());
                        cellularInfoObject.put("baseStationId", cdmaCellLocation.getBaseStationId());
                        cellularInfoObject.put("longitude", cdmaCellLocation.getBaseStationLongitude());
                        cellularInfoObject.put("networkId", cdmaCellLocation.getNetworkId());
                        cellularInfoObject.put("systemId", cdmaCellLocation.getSystemId());

                        cellInformationArray.put(cellularInfoObject);
                    } else if (cellLocation instanceof GsmCellLocation) {
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                        JSONObject cellularInfoObject = new JSONObject();
                        cellularInfoObject.put("networkType", BuddyCellularNetworkType.GSM.toString());
                        cellularInfoObject.put("cellId", gsmCellLocation.getCid());
                        cellularInfoObject.put("primaryScramblingCode", gsmCellLocation.getPsc());
                        cellularInfoObject.put("locationAreaCode", gsmCellLocation.getLac());

                        cellInformationArray.put(cellularInfoObject);
                    }
                }

                cellInformation.put("data", cellInformationArray);
                // extra fields
                cellInformation.put("callState", telephonyManager.getCallState());
                cellInformation.put("dataActivity", telephonyManager.getDataActivity());
                cellInformation.put("dataState", telephonyManager.getDataState());
                cellInformation.put("getNetworkCountryIso", telephonyManager.getNetworkCountryIso());
                cellInformation.put("networkOperator", telephonyManager.getNetworkOperator()); // mobile country code + mobile network code http://www.mcc-mnc.com/
                cellInformation.put("networkOperatorName", telephonyManager.getNetworkOperatorName());
                cellInformation.put("getNetworkType", telephonyManager.getNetworkType());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cellInformation.put("phoneCount", telephonyManager.getPhoneCount());
                }
                cellInformation.put("phoneType", telephonyManager.getPhoneType());
                cellInformation.put("simCountryIso", telephonyManager.getSimCountryIso());
                cellInformation.put("simOperator", telephonyManager.getSimOperator());
                cellInformation.put("simOperatorName", telephonyManager.getSimOperatorName());
                cellInformation.put("simState", telephonyManager.getSimState());
                cellInformation.put("hasIccCard", telephonyManager.hasIccCard());
                cellInformation.put("isNetworkRoaming", telephonyManager.isNetworkRoaming());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    cellInformation.put("mmsUAProfUrl", telephonyManager.getMmsUAProfUrl());
                    cellInformation.put("mmsUserAgent", telephonyManager.getMmsUserAgent());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cellInformation.put("smsCapable", telephonyManager.isSmsCapable());
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    cellInformation.put("hasCarrierPrivileges", telephonyManager.hasCarrierPrivileges());
                    cellInformation.put("isVoiceCapable", telephonyManager.isVoiceCapable());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    cellInformation.put("isConcurrentVoiceAndDataSupported", telephonyManager.isConcurrentVoiceAndDataSupported());
                    cellInformation.put("networkSpecifier", telephonyManager.getNetworkSpecifier());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cellInformation.put("isHearingAidCompatibilitySupported", telephonyManager.isHearingAidCompatibilitySupported());
                    cellInformation.put("isTtyModeSupported", telephonyManager.isTtyModeSupported());
                }
            }
        }
        catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(BuddyAltDataTracker.TAG, e);
        }

        return cellInformation;
    }

    public static void save(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager == null) {
            BuddySqliteHelper.getInstance().logError(TAG, new Exception("TelephonyManager is null"));
        } else {
            try {
                JSONObject cellularInfoObject = BuddyCellularInformation.getCellInformation(context, telephonyManager);

                try {
                    String body = cellularInfoObject.toString(0);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BuddySqliteCellularTableKeys.Uuid, UUID.randomUUID().toString());
                    contentValues.put(BuddySqliteCellularTableKeys.Body, body);

                    BuddySqliteHelper.getInstance().save(BuddySqliteTableType.Cellular, contentValues);
                    PLog.i(TAG, "cellular data saved");
                } catch (Exception e) {
                    BuddySqliteHelper.getInstance().logError(TAG, e);
                }
            } catch (Exception e) {
                BuddySqliteHelper.getInstance().logError(TAG, e);
            }
        }
    }
}

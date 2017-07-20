package com.parse;

import android.os.Build;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BuddyCellularService {

    public static JSONObject getCellInformation(TelephonyManager telephonyManager) {

        JSONObject cellInformation = new JSONObject();

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                int dataNetworkType = telephonyManager.getDataNetworkType();
                cellInformation.put("DataNetworkType",dataNetworkType);
            }

            JSONArray cellInformationArray = new JSONArray();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
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
                        }

                        if (cellularInfoObject.length() > 0) {
                            cellInformationArray.put(cellularInfoObject);
                        }
                    }
                }
            }
            else {
                // other api levels
                CellLocation cellLocation = telephonyManager.getCellLocation();

                if (cellLocation instanceof CdmaCellLocation) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)cellLocation;
                    JSONObject cellularInfoObject = new JSONObject();
                    cellularInfoObject.put("networkType", BuddyCellularNetworkType.CDMA.toString());
                    cellularInfoObject.put("latitude", cdmaCellLocation.getBaseStationLatitude());
                    cellularInfoObject.put("baseStationId", cdmaCellLocation.getBaseStationId());
                    cellularInfoObject.put("longitude", cdmaCellLocation.getBaseStationLongitude());
                    cellularInfoObject.put("networkId", cdmaCellLocation.getNetworkId());
                    cellularInfoObject.put("systemId", cdmaCellLocation.getSystemId());

                    cellInformationArray.put(cellularInfoObject);
                }
                else  if (cellLocation instanceof GsmCellLocation) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
                    JSONObject cellularInfoObject = new JSONObject();
                    cellularInfoObject.put("networkType", BuddyCellularNetworkType.GSM.toString());
                    cellularInfoObject.put("cellId", gsmCellLocation.getCid());
                    cellularInfoObject.put("primaryScramblingCode", gsmCellLocation.getPsc());
                    cellularInfoObject.put("locationAreaCode", gsmCellLocation.getLac());

                    cellInformationArray.put(cellularInfoObject);
                }
            }

            cellInformation.put("data",cellInformationArray);
        }
        catch (Exception e) {
            BuddySqliteHelper.getInstance().logError(BuddyAltDataTracker.TAG, e.getMessage());
        }

        return cellInformation;
    }

}

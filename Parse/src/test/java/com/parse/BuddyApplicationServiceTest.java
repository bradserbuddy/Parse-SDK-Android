package com.parse;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddyApplicationServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private Context context;


    @Before
    public void setUp() throws Exception {
        context = Mockito.mock(Context.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetApplicationNames() throws Exception {
        // arrange
        PackageManager packageManager = Mockito.mock(PackageManager.class);
        when(context.getPackageManager()).thenReturn(packageManager);

        List<ApplicationInfo> applicationInfos = new ArrayList<>();
        ApplicationInfo appInfo = new ApplicationInfo();
        appInfo.flags = 0;
        String appName = "hello";
        appInfo.processName = appName;
        applicationInfos.add(appInfo);
        when(packageManager.getInstalledApplications(0)).thenReturn(applicationInfos);

        BigInteger deviceId = BigInteger.valueOf(121);
        long version = 122;


        // act

        JSONObject gsmCellInfo = BuddyApplication.getAppNames(context, version, deviceId);

        // assert
        JSONArray apps = (JSONArray)gsmCellInfo.get("apps");
        String firstAppName = (String) apps.get(0);
        assertEquals(appName, firstAppName);
    }

}

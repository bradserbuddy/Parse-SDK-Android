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
public class BuddySqliteCellularTableKeysTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetTableNameKey() throws Exception {
        // arrange

        // act
        String tableName = BuddySqliteCellularTableKeys.TableName;

        // assert
        assertEquals("cellular", tableName);
    }

    @Test
    public void testGetTableBodyKey() throws Exception {
        // arrange

        // act
        String body = BuddySqliteCellularTableKeys.Body;

        // assert
        assertEquals("body", body);
    }

    @Test
    public void testGetTableTimestampKey() throws Exception {
        // arrange

        // act
        String timestamp = BuddySqliteCellularTableKeys.Timestamp;

        // assert
        assertEquals("timestamp", timestamp);
    }

    @Test
    public void testGetTableUuidKey() throws Exception {
        // arrange

        // act
        String uuid = BuddySqliteCellularTableKeys.Uuid;

        // assert
        assertEquals("uuid", uuid);
    }
}

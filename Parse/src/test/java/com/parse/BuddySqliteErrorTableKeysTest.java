package com.parse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = TestHelper.ROBOLECTRIC_SDK_VERSION)
public class BuddySqliteErrorTableKeysTest {
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
        String tableName = BuddySqliteErrorTableKeys.TableName;

        // assert
        assertEquals("error", tableName);
    }

    @Test
    public void testGetTableTimestampKey() throws Exception {
        // arrange

        // act
        String timestamp = BuddySqliteErrorTableKeys.Timestamp;

        // assert
        assertEquals("timestamp", timestamp);
    }

    @Test
    public void testGetTableUuidKey() throws Exception {
        // arrange

        // act
        String uuid = BuddySqliteErrorTableKeys.Uuid;

        // assert
        assertEquals("uuid", uuid);
    }

    @Test
    public void testGetTableMessageKey() throws Exception {
        // arrange

        // act
        String message = BuddySqliteErrorTableKeys.Message;

        // assert
        assertEquals("message", message);
    }
}
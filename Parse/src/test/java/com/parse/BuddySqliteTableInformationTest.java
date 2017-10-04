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
public class BuddySqliteTableInformationTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLocationTableName() throws Exception {
        // arrange

        // act
        String tableName = BuddySqliteTableInformation.getTableName(BuddySqliteTableType.Location);

        // assert
        assertEquals(BuddySqliteLocationTableKeys.TableName, tableName);
    }

    @Test
    public void testGetErrorTableName() throws Exception {
        // arrange

        // act
        String tableName = BuddySqliteTableInformation.getTableName(BuddySqliteTableType.Error);

        // assert
        assertEquals(BuddySqliteErrorTableKeys.TableName, tableName);
    }

    @Test
    public void testGetCellularTableName() throws Exception {
        // arrange

        // act
        String tableName = BuddySqliteTableInformation.getTableName(BuddySqliteTableType.Cellular);

        // assert
        assertEquals(BuddySqliteCellularTableKeys.TableName, tableName);
    }
}
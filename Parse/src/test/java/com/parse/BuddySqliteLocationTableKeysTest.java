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
public class BuddySqliteLocationTableKeysTest {
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
        String tableName = BuddySqliteLocationTableKeys.TableName;

        // assert
        assertEquals("locations", tableName);
    }

    @Test
    public void testGetTableTimestampKey() throws Exception {
        // arrange

        // act
        String timestamp = BuddySqliteLocationTableKeys.Timestamp;

        // assert
        assertEquals("timestamp", timestamp);
    }

    @Test
    public void testGetTableUuidKey() throws Exception {
        // arrange

        // act
        String uuid = BuddySqliteLocationTableKeys.Uuid;

        // assert
        assertEquals("uuid", uuid);
    }

    @Test
    public void testGetTableAccuracyKey() throws Exception {
        // arrange

        // act
        String accuracy = BuddySqliteLocationTableKeys.Accuracy;

        // assert
        assertEquals("accuracy", accuracy);
    }

    @Test
    public void testGetTableAltitudeKey() throws Exception {
        // arrange

        // act
        String altitude = BuddySqliteLocationTableKeys.Altitude;

        // assert
        assertEquals("altitude", altitude);
    }

    @Test
    public void testGetTableBearingKey() throws Exception {
        // arrange

        // act
        String bearing = BuddySqliteLocationTableKeys.Bearing;

        // assert
        assertEquals("bearing", bearing);
    }

    @Test
    public void testGetTableBearingAccuracyKey() throws Exception {
        // arrange

        // act
        String bearingAccuracy = BuddySqliteLocationTableKeys.BearingAccuracy;

        // assert
        assertEquals("bearingAccuracy", bearingAccuracy);
    }

    @Test
    public void testGetTableLatitudeKey() throws Exception {
        // arrange

        // act
        String latitude = BuddySqliteLocationTableKeys.Latitude;

        // assert
        assertEquals("latitude", latitude);
    }

    @Test
    public void testGetTableLongitudeKey() throws Exception {
        // arrange

        // act
        String longitude = BuddySqliteLocationTableKeys.Longitude;

        // assert
        assertEquals("longitude", longitude);
    }

    @Test
    public void testGetTableSpeedKey() throws Exception {
        // arrange

        // act
        String speed = BuddySqliteLocationTableKeys.Speed;

        // assert
        assertEquals("speed", speed);
    }

    @Test
    public void testGetTableSpeedAccuracyKey() throws Exception {
        // arrange

        // act
        String speedAccuracy = BuddySqliteLocationTableKeys.SpeedAccuracy;

        // assert
        assertEquals("speedAccuracyMetersPerSecond", speedAccuracy);
    }

    @Test
    public void testGetTableVerticalAccuracyKey() throws Exception {
        // arrange

        // act
        String verticalAccuracy = BuddySqliteLocationTableKeys.VerticalAccuracy;

        // assert
        assertEquals("verticalAccuracyMeters", verticalAccuracy);
    }
}

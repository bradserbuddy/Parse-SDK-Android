package com.parse;


class BuddyLocation {
    private String uuid;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private long timestamp;
    private float accuracy;
    private double altitude;
    private float bearing;
    private float speed;
    private float bearingAccuracy;
    private float speedAccuracyMetersPerSecond;
    private float verticalAccuracyMeters;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    void setLatitude(double latitude) {
        if (latitude > 90.0 || latitude < -90.0) {
            throw new IllegalArgumentException("Latitude must be within the range (-90.0, 90.0).");
        }
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        if (longitude > 180.0 || longitude < -180.0) {
            throw new IllegalArgumentException("Longitude must be within the range (-180.0, 180.0).");
        }
        this.longitude = longitude;
    }

    double getLongitude() {
        return longitude;
    }

    long getTimestamp() {
        return timestamp;
    }

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    float getAccuracy() {
        return accuracy;
    }

    void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    double getAltitude() {
        return altitude;
    }

    void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    float getBearing() {
        return bearing;
    }

    void setBearing(float bearing) {
        this.bearing = bearing;
    }

    float getSpeed() {
        return speed;
    }

    void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearingAccuracy() {
        return bearingAccuracy;
    }

    void setBearingAccuracy(float bearingAccuracy) {
        this.bearingAccuracy = bearingAccuracy;
    }

    public float getSpeedAccuracyMetersPerSecond() {
        return speedAccuracyMetersPerSecond;
    }

    void setSpeedAccuracyMetersPerSecond(float speedAccuracyMetersPerSecond) {
        this.speedAccuracyMetersPerSecond = speedAccuracyMetersPerSecond;
    }

    public float getVerticalAccuracyMeters() {
        return verticalAccuracyMeters;
    }

    void setVerticalAccuracyMeters(float verticalAccuracyMeters) {
        this.verticalAccuracyMeters = verticalAccuracyMeters;
    }
}

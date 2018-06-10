package com.breakevenpoint.model;

import android.location.Location;

import java.util.Date;

public class AthleteLocation extends Object {


    private Location location;
    private Date timeStamp;
    private String athleteId;


    //code to be removed
    private String riderName;
    private  double lat;
    private double longitude;


    private  String bibNo;

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    private  Date lastUpdated;



    public AthleteLocation(Location location, Date timeStamp, String athleteId, String bibNo, String locationId) {
        this.location = location;
        this.timeStamp = timeStamp;
        this.athleteId = athleteId;
        this.bibNo = bibNo;
        this.locationId = locationId;
    }

    private String locationId;


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(String athleteId) {
        this.athleteId = athleteId;
    }

    public String getBibNo() {
        return bibNo;
    }

    public void setBibNo(String bibNo) {
        this.bibNo = bibNo;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderName() {
        return riderName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "AthleteLocation{" +
                "location=" + location +
                ", timeStamp=" + timeStamp +
                ", athleteId='" + athleteId + '\'' +
                ", riderName='" + riderName + '\'' +
                ", lat=" + lat +
                ", longitude=" + longitude +
                ", bibNo='" + bibNo + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", locationId='" + locationId + '\'' +
                '}';
    }
}

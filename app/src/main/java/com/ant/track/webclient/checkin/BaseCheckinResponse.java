package com.ant.track.webclient.checkin;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class BaseCheckinResponse extends BaseCheckin {


    @SerializedName("_id")
    public String _id;

    @SerializedName("date")
    public String date;

    @SerializedName("title")
    public String title;

    @SerializedName("address")

    public String address;

    @SerializedName("adress_medium")
    public String adress_medium;

    @SerializedName("zona_id")
    public String zona_id;

    public String getUser_id() {
        return user_id;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getCourse() {
        return course;
    }

    public float getSpeed() {
        return speed;
    }

    public String getMeasured() {
        return measured;
    }

    public String getImage_id() {
        return image_id;
    }

    public String getCompany() {
        return company;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getPoi() {
        return poi;
    }

    public String getWifi() {
        return wifi;
    }


    public String getZona_id() {
        return zona_id;
    }

    public String get_id() {
        return _id;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getAdress_medium() {
        return adress_medium;
    }

    public boolean isOptimized() {
        return optimized;
    }


}

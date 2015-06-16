package com.ant.track.webclient.checkin;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Toader on 6/4/2015.
 */
public class BaseCheckin {
    @SerializedName("user_id")
    public String user_id;

    @SerializedName("long")
    public float longitude;

    @SerializedName("lat")
    public float latitude;

    @SerializedName("accuracy")
    public Integer accuracy;

    @SerializedName("course")
    public float course;

    @SerializedName("speed")
    public float speed;

    @SerializedName("measured")
    public String measured;

    @SerializedName("image_id")
    public String image_id;

    @SerializedName("company")
    public String company;

    @SerializedName("annotation")
    public String annotation;

    @SerializedName("poi")
    public String poi;

    @SerializedName("wifi")
    public String wifi;

    @SerializedName("optimized")
    public boolean optimized;

    public BaseCheckin() {

    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setMeasured(String measured) {
        this.measured = measured;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public void setCourse(float course) {
        this.course = course;
    }

}

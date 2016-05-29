package com.ant.track.lib.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Toader on 6/4/2015.
 */
public class UserAddress implements Parcelable {

    public static final Creator<UserAddress> CREATOR
            = new Creator<UserAddress>() {
        public UserAddress createFromParcel(Parcel in) {
            return new UserAddress(in);
        }

        public UserAddress[] newArray(int size) {
            return new UserAddress[size];
        }
    };
    private Location location;
    private String zona_id;
    private String date;
    private String title;
    private String address;
    private String poi;
    private String address_medium;


    public UserAddress(Location _loc, String _title, String _address, String _address_medium, String _zona_id, String _date, String _poi) {
        this.location = _loc;
        this.title = _title;
        this.address = _address;
        this.address_medium = _address_medium;
        this.zona_id = _zona_id;
        this.date = _date;
        this.poi = _poi;
    }

    private UserAddress(Parcel in) {
        title = in.readString();
        address = in.readString();
        address_medium = in.readString();
        zona_id = in.readString();
        date = in.readString();
        poi = in.readString();
        location = Location.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(address);
        out.writeString(address_medium);
        out.writeString(zona_id);
        out.writeString(date);
        out.writeString(poi);
        location.writeToParcel(out, flags);
    }

    public String getZona_id() {
        return zona_id;
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

    public String getPoi() {
        return poi;
    }

    public String getAddress_medium() {
        return address_medium;
    }

    public Location getLocation() {
        return location;

    }
}

package com.ant.track.lib.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Toader on 6/4/2015.
 */
public class User implements Parcelable {


    public static final Creator<User> CREATOR
            = new Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    String user_id;
    String auth_token;
    UserAddress userAddress;

    public User(String _user_id, String _auth_token, UserAddress userAddress) {
        this.user_id = _user_id;
        this.auth_token = _auth_token;
        this.userAddress = userAddress;
    }


    private User(Parcel in) {
        user_id = in.readString();
        auth_token = in.readString();
        userAddress = UserAddress.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(user_id);
        out.writeString(auth_token);
        userAddress.writeToParcel(out, flags);
    }

    public String getUser_id() {
        return user_id;
    }

    public String getAuth_token() {
        return auth_token;
    }

    public UserAddress getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(UserAddress loc) {
        this.userAddress = loc;
    }
}

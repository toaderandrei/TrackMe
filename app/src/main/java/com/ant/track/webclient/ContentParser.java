package com.ant.track.webclient;

import android.util.Log;

import com.ant.track.webclient.checkin.BaseCheckin;
import com.ant.track.webclient.checkin.CheckinPostResponse;
import com.ant.track.webclient.session.SessionRequest;
import com.ant.track.webclient.session.SessionResponse;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Toader on 6/4/2015.
 */
public class ContentParser {


    private static final String TAG = ContentParser.class.getSimpleName();
    private static ContentParser instance;
    Gson gson;

    protected ContentParser() {
        gson = new Gson();
    }

    public static ContentParser getInstance() {
        if (instance == null) {
            instance = new ContentParser();
        }
        return instance;
    }

    public String getJsonBody(SessionRequest client) {
        String json = null;
        try {
            json = gson.toJson(client);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage(), ex);
        }
        return json;
    }

    public String getJsonBody(BaseCheckin client) {
        String json = null;
        try {
            json = gson.toJson(client);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage(), ex);
        }
        return json;
    }

    public CheckinPostResponse parseCheckinStream(InputStream stream) {
        CheckinPostResponse response = null;
        try {
            if (stream != null) {
                InputStreamReader reader = new InputStreamReader(stream);
                if (reader != null) {
                    response = gson.fromJson(reader, CheckinPostResponse.class);
                }
            }
        } catch (JsonIOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } catch (JsonSyntaxException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);

        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
            }
        }
        return response;
    }

    public SessionResponse parseSessionStream(InputStream stream) {
        SessionResponse response = null;
        try {
            if (stream != null) {
                InputStreamReader reader = new InputStreamReader(stream);
                if (reader != null) {
                    response = gson.fromJson(reader, SessionResponse.class);
                }
            }
        } catch (JsonIOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } catch (JsonSyntaxException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);

        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
            }
        }
        return response;
    }
}

package com.ant.track.webclient;

import android.util.Log;

import com.ant.track.constants.Constants;
import com.ant.track.models.User;
import com.ant.track.provider.ICheckinListener;
import com.ant.track.provider.ISessionCreated;
import com.ant.track.webclient.checkin.BaseCheckin;
import com.ant.track.webclient.checkin.CheckinPostResponse;
import com.ant.track.webclient.session.SessionRequest;
import com.ant.track.webclient.session.SessionResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by Toader on 6/4/2015.
 */
public class HttpClient {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = HttpClient.class.getSimpleName();
    private static HttpClient instance = null;
    OkHttpClient okHttpClient;

    protected HttpClient() {
        okHttpClient = new OkHttpClient();
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    private void initConnTimeout() {
        okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(5, TimeUnit.SECONDS);
    }

    /**
     * creates a session to the server and if the data returned from the server is valid, returns using
     * the listener callback.
     * The listener callback.
     *
     * @param listener
     */
    public void createSession(final ISessionCreated listener) {
        SessionResponse response = null;
        initConnTimeout();
        SessionRequest sessionRequest = ContentTranslator.getSessionRequest();
        String body = getContentParser().getJsonBody(sessionRequest);
        if (body != null) {
            try {
                RequestBody requestBody = RequestBody.create(JSON, body);
                Request request = new Request.Builder().url(getSessionUrl()).post(requestBody).build();
                Response resp = okHttpClient.newCall(request).execute();
                if (resp != null) {
                    switch (resp.code()) {
                        case 200:
                            final ResponseBody responseBody = resp.body();
                            final InputStream stream = responseBody.byteStream();
                            if (stream != null) {
                                response = getContentParser().parseSessionStream(stream);
                            }
                            break;
                        default:
                            Log.w(TAG, "Message is:" + resp.code());
                            break;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        }
        if (listener != null && response != null && response.isStatusOK()) {
            listener.sessionCreated(response);
        }
    }

    /**
     * creates a session to the server and if the data returned from the server is valid, returns using
     * the listener callback.
     * The listener callback.
     *
     * @param listener
     */
    public void sendCheckIn(final ICheckinListener listener, User user) {
        CheckinPostResponse response = null;
        initConnTimeout();
        BaseCheckin checkin = ContentTranslator.getCheckinForUserToServer(user);
        String body = getContentParser().getJsonBody(checkin);
        if (body != null) {
            try {
                RequestBody requestBody = RequestBody.create(JSON, body);
                Request request = new Request.Builder().url(getCheckinUrl(user.getAuth_token())).post(requestBody).build();

                Response resp = okHttpClient.newCall(request).execute();
                if (resp != null) {
                    switch (resp.code()) {
                        case 200:
                            final ResponseBody responseBody = resp.body();
                            final InputStream stream = responseBody.byteStream();
                            if (stream != null) {
                                response = getContentParser().parseCheckinStream(stream);
                            }
                            break;
                        default:
                            Log.w(TAG, "Message is:" + resp.code());
                            break;
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        }

        if (listener != null && response != null && response.isStatusOK()) {
            ContentTranslator.translateResponse(listener, user, response);
        }
    }

    private ContentParser getContentParser() {
        return ContentParser.getInstance();
    }

    private String getSessionUrl() {
        return Constants.BASE_URL + Constants.SESSION + "/";
    }

    private String getCheckinUrl(String auth_token) {
        return Constants.BASE_URL + Constants.CHECKIN + "/" + "?auth_token=" + auth_token;
    }


}

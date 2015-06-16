package com.ant.track.provider;

import com.ant.track.webclient.session.SessionResponse;

/**
 * Created by Toader on 6/4/2015.
 */
public interface ISessionCreated {
    /**
     * callback from the Httpclient after the session has been created.
     *
     * @param response the parsed response from the server.
     */
    void sessionCreated(SessionResponse response);
}

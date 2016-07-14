package com.ant.track.lib.publisher;

import com.ant.track.lib.model.Route;

/**
 * listener used to notify listeners
 */
public interface NotifyListener {

    void notifyUI(Route route);
}

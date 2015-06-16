package com.ant.track.publisher;

import com.ant.track.models.User;

/**
 * Created by Toader on 6/4/2015.
 */
public interface IContentPublisher {
    void registerListener(INotifyUIListener listener);

    void unregisterListener(INotifyUIListener listener);

    void notifyListeners(User user);
}

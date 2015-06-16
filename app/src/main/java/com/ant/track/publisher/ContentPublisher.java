package com.ant.track.publisher;

import com.ant.track.models.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Toader on 6/4/2015.
 */
public class ContentPublisher implements IContentPublisher {

    private static IContentPublisher instance = null;
    private List<INotifyUIListener> listeners = new ArrayList<>();

    public static IContentPublisher getInstance() {
        if (instance == null) {
            instance = new ContentPublisher();
        }
        return instance;
    }

    @Override
    public void registerListener(final INotifyUIListener listener) {
        synchronized (listeners) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    @Override
    public void unregisterListener(INotifyUIListener listener) {
        synchronized (listeners) {
            if (listener != null) {
                Iterator<INotifyUIListener> it = listeners.iterator();
                while ((it.hasNext())) {
                    INotifyUIListener mListener = it.next();
                    if (mListener.equals(listener)) {
                        it.remove();
                    }
                }
            }
        }
    }

    @Override
    public void notifyListeners(User user) {
        if (listeners.isEmpty()) {
            return;
        }
        for (INotifyUIListener listener : listeners) {
            if (listener != null) {
                listener.notifyUI(user);
            }
        }
    }
}

package com.ant.track.lib.content.publisher;

import android.util.Log;

import com.ant.track.lib.content.datasource.RouteType;
import com.ant.track.lib.content.datasource.RouteDataListener;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controller that is used for manipulating data contents.
 */
public class DataContentPublisherImpl implements DataContentPublisher {

    private static final String TAG = DataContentPublisherImpl.class.getCanonicalName();
    private Map<RouteDataListener, EnumSet<RouteType>> listenerToEnumSetMap = new HashMap<>();

    private Map<RouteType, Set<RouteDataListener>> typeToListenersMap = new EnumMap<>(RouteType.class);

    public DataContentPublisherImpl() {

    }

    @Override
    public void registerListener(RouteDataListener listener, EnumSet<RouteType> types) {
        if (listener == null) {
            Log.d(TAG, "null data.");
            return;
        }
        if (listenerToEnumSetMap.containsKey(listener)) {
            Log.d(TAG, "key is already there.");
            return;
        }
        listenerToEnumSetMap.put(listener, types);
        //we now put the other way round.
        for (RouteType type : types) {
            typeToListenersMap.get(type).add(listener);
        }
    }

    @Override
    public void unregisterListener(RouteDataListener listener) {
        if (listener == null) {
            Log.d(TAG, "null data.");
            return;
        }

        if (!listenerToEnumSetMap.containsKey(listener)) {
            Log.d(TAG, "key was removed");
            return;
        }

        EnumSet<RouteType> removed = listenerToEnumSetMap.remove(listener);

        for (RouteType routeType : removed) {
            if (typeToListenersMap.containsKey(routeType)) {
                typeToListenersMap.get(routeType).remove(listener);
            }
        }
    }

    @Override
    public Set<RouteDataListener> getListeners() {
        Set<RouteDataListener> listeners = new HashSet<>();
        for (RouteType type : typeToListenersMap.keySet()) {
            listeners.addAll(typeToListenersMap.get(type));
        }
        return listeners;
    }

    @Override
    public EnumSet<RouteType> getAllRouteTypes() {
        EnumSet<RouteType> listeners = EnumSet.noneOf(RouteType.class);
        for (RouteDataListener type : listenerToEnumSetMap.keySet()) {
            listeners.addAll(listenerToEnumSetMap.get(type));
        }
        return listeners;
    }

    @Override
    public Set<RouteDataListener> getListenersByType(RouteType type) {
        return typeToListenersMap.get(type);
    }

    @Override
    public EnumSet<RouteType> getRouteTypes(RouteDataListener listener) {
        if (listener == null) {
            return null;
        }
        return EnumSet.copyOf(listenerToEnumSetMap.get(listener));
    }

    @Override
    public int getRouteTypesCount() {
        return typeToListenersMap.keySet().size();
    }

    @Override
    public int getRouteListenersCount() {
        return listenerToEnumSetMap.values().size();
    }

    @Override
    public void clearAll() {
        if (listenerToEnumSetMap != null && !listenerToEnumSetMap.isEmpty()) {
            listenerToEnumSetMap.clear();
        }

        if (typeToListenersMap != null && !typeToListenersMap.isEmpty()) {
            typeToListenersMap.clear();
        }
    }
}

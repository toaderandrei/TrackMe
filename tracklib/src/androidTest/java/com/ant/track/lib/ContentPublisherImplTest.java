package com.ant.track.lib;

import android.support.test.runner.AndroidJUnit4;

import com.ant.track.lib.model.Route;
import com.ant.track.lib.publisher.ContentPublisherImpl;
import com.ant.track.lib.publisher.NotifyListener;
import com.ant.track.lib.stats.RouteStats;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 *
 * Test class for ContenPublisherImpl.
 */
@RunWith(AndroidJUnit4.class)
public class ContentPublisherImplTest {
    private boolean userNotified;
    private Route routeUpdated;
    private ContentPublisherImpl contentPublisher;


    private NotifyListener notifyListener = new NotifyListener() {
        @Override
        public void notifyUI(Route route) {
            userNotified = true;
            routeUpdated = route;
        }
    };

    @Before
    public void setup(){
        contentPublisher = ContentPublisherImpl.getInstance();
    }


    @After
    public void tearDown(){
        contentPublisher = null;
    }


    @Test
    public void testNonNull(){
        assertNotNull(contentPublisher);
    }


    @Test
    public void testRegister(){
        RouteStats stats = mock(RouteStats.class);
        Route route = new Route(stats);
        contentPublisher.registerListener(notifyListener);
        contentPublisher.notifyListeners(route);
        assertEquals(routeUpdated, route);
        //register null
        contentPublisher.unregisterListener(notifyListener);
        contentPublisher.registerListener(null);
        userNotified = false;
        routeUpdated = null;
        contentPublisher.notifyListeners(route);
        assertNull(routeUpdated);
        assertFalse(userNotified);
    }

    @Test
    public void testUnregister(){
        RouteStats stats = mock(RouteStats.class);
        Route route = new Route(stats);
        contentPublisher.registerListener(notifyListener);
        contentPublisher.notifyListeners(route);
        userNotified = false;
        routeUpdated = null;
        contentPublisher.unregisterListener(notifyListener);

        assertFalse(userNotified);
        assertNull(routeUpdated);
        //unregister null
        contentPublisher.unregisterListener(null);
    }
}

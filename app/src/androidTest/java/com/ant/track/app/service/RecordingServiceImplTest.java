package com.ant.track.app.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;

/**
 * Test class for the RecordingServiceImpl.
 */
public class RecordingServiceImplTest extends BaseServiceTest {

    @Rule
    public ServiceTestRule mServiceRule;


    @Override
    protected void init() {
        mServiceRule = new ServiceTestRule();
    }

    @Override
    protected void destroy() {
        //todo destroy something anything.
    }


    @Test
    public void testWithBoundService() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), RecordingServiceImpl.class);

        // Data can be passed to the service via the Intent.

        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call public methods on the binder directly.
        assertNotNull(binder);
    }
}

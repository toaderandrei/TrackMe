package com.ant.track.app.service;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Test class for service tests
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseServiceTest {

    @Before
    public void setup() {
        init();
    }

    @After
    public void tearDown() {
        destroy();
    }

    /**
     * called inside the setup method. first time call.
     */
    protected abstract void init();

    /**
     * called the last after tearDown.
     */
    protected abstract void destroy();

}

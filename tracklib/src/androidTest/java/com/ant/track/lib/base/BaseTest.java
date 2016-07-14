package com.ant.track.lib.base;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base test for all the android test
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {

    @Before
    public void setup() {
        init();
    }

    @After
    public void tearDown() {
        destroy();
    }

    /**
     * method called after setup.
     */
    protected abstract void init();


    /**
     * method called in in teardown
     */
    protected abstract void destroy();

}

package com.ant.track.lib.stats;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertNotNull;

/**
 * Test class for the DataBuffer.
 */
@RunWith(JUnit4.class)
public class DataBufferTest {


    private static final int DEFAULT_MAX_SIZE = 10;
    private DataBufferImpl dataBuffer;

    @Before
    public void setup() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE);
    }

    @After
    public void tearDown() {
        dataBuffer = null;
    }

    @Test
    public void testObject() {
        assertNotNull(dataBuffer);

    }

}

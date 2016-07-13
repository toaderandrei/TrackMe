package com.ant.track.lib.stats;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for the DataBuffer.
 */
@RunWith(JUnit4.class)
public class DataBufferTest {


    private static final int DEFAULT_MAX_SIZE = 10;
    public static final int DEFAULT_MAX_SIZE_1 = 3;
    private DataBufferImpl dataBuffer;

    @Before
    public void setup() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE_1);
    }

    @After
    public void tearDown() {
        dataBuffer = null;
    }

    @Test
    public void testObject() {
        assertNotNull(dataBuffer);
    }

    @Test
    public void testAverage() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE_1);
        double value1 = 10.0d;
        double value2 = 20.0d;

        dataBuffer.setNext(value1, false);
        dataBuffer.setNext(value2, false);
        assertEquals(15.0d, dataBuffer.getAverage());
    }

    @Test
    public void testVariance() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE_1);
        double value1 = 10.0d;
        double value2 = 20.0d;

        dataBuffer.setNext(value1, false);
        dataBuffer.setNext(value2, false);
        //we check first to see it is not null
        assertTrue(dataBuffer.getVariance() > 0);
    }

    @Test
    public void testMin() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE_1);
        double value1 = 10.0d;
        double value2 = 20.0d;

        dataBuffer.setNext(value1, false);
        dataBuffer.setNext(value2, false);
        assertEquals(value1, dataBuffer.getMin());
    }


    @Test
    public void testMax() {
        dataBuffer = new DataBufferImpl(DEFAULT_MAX_SIZE_1);
        double value1 = 10.0d;
        double value2 = 20.0d;

        dataBuffer.setNext(value1, false);
        dataBuffer.setNext(value2, false);
        assertEquals(value2, dataBuffer.getMax());
    }


}

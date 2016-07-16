package com.ant.track.lib.base;

import android.test.ProviderTestCase2;

import com.ant.track.lib.db.provider.TrackMeDbProvider;

import org.junit.After;
import org.junit.Before;

/**
 * base test class for provider.
 */
public abstract class BaseProviderTest extends ProviderTestCase2<TrackMeDbProvider> {

    /**
     * Constructor.
     *
     * @param providerClass     The class name of the provider under test
     * @param providerAuthority The provider's authority string
     */
    public BaseProviderTest(Class<TrackMeDbProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Before
    public void setup() {
        init();
    }

    @After
    public void tearDown() {
        destroy();
    }

    protected abstract void init();

    protected abstract void destroy();
}

/**
 * Copyright (C) 2017 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt;

import org.ldmud.jldmud.rt.net.RuntimeNetModule;
import org.ldmud.jldmud.rt.object.RuntimeObjectModule;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice configuration class.
 */
public class RuntimeModule extends AbstractModule {

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        install(new RuntimeNetModule());
        install(new RuntimeObjectModule());
        bind(GameLoop.class).in(Singleton.class);
        bind(GameStateSignals.class).in(Singleton.class);
        bind(MemoryReserve.class).in(Singleton.class);
    }
}

/**
 * Copyright (C) 2017 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice configuration class.
 */
public class ConfigurationModule extends AbstractModule {

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(Configuration.class).in(Singleton.class);
    }
}

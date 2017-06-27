/**
 * Copyright (C) 2017 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import org.ldmud.jldmud.config.ConfigurationModule;
import org.ldmud.jldmud.rt.RuntimeModule;

import com.google.inject.AbstractModule;

/**
 * Guice configuration class.
 */
public class MainModule extends AbstractModule {

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        install(new ConfigurationModule());
        install(new RuntimeModule());
    }

}

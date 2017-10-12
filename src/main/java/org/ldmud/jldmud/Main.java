/**
 * Copyright (C) 2013 jLDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.config.CommandLineArguments;
import org.ldmud.jldmud.config.Configuration;
import org.ldmud.jldmud.config.ConfigurationLoader;
import org.ldmud.jldmud.config.Version;
import org.ldmud.jldmud.log.Logging;
import org.ldmud.jldmud.rt.GameLoop;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The entry point into the Game Driver.<p/>
 *
 * This class initializes the driver and then hands off control to the main game loop.
 */
public final class Main {
    private static Logger log;

    /**
     * Main method
     */
    public static void main( String[] args )
    {
        CommandLineArguments cliArgs = new CommandLineArguments();
    	if (cliArgs.parseCommandline(args)) {
    	    System.exit(cliArgs.getExitCode());
    	}

        System.out.println(Version.DRIVER_NAME + " " + Version.getVersionString());

        Injector injector = Guice.createInjector(new MainModule());
        Configuration config = injector.getInstance(Configuration.class);

        // Read the driver's configuration, then jettison the utility classes by GC.
        {
            ConfigurationLoader configurator = new ConfigurationLoader();

            if (!configurator.loadProperties(cliArgs.getSettingsFilename(), cliArgs.getConfigSettings(), config)) {
                System.exit(1);
            }

            if (cliArgs.getPrintConfiguration()) {
                configurator.printEffectiveSettings();
                System.exit(0);
            }
        }

        // Java doesn't directly allow to change the current working directory of the process itself;
        // but by changing the 'user.dir' system property the same effect is achieved as the property
        // is used when the Java runtime resolves relative paths.
        if (null == System.setProperty("user.dir", config.getMudDirectory().getAbsolutePath())) {
            System.err.println("Error: Can't set the 'user.dir' system property to the mud directory ('" + config.getMudDirectory().getAbsolutePath() + "')");
            System.exit(1);
        }

        Logging.config(config);
        log = LogManager.getLogger(Main.class);

        Logging.mudLog.info(Version.DRIVER_NAME + " " + Version.getVersionString() + " starting up.");
        log.info(Version.DRIVER_NAME + " " + Version.getVersionString() + " starting up.");

        GameLoop gameLoop = injector.getInstance(GameLoop.class);
        gameLoop.run();

        /* TODO: More stuff here? */

        Logging.mudLog.info(Version.DRIVER_NAME + " " + Version.getVersionString() + " stopped.");
        log.info(Version.DRIVER_NAME + " " + Version.getVersionString() + " stopped.");
    }
}

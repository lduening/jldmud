/**
 * Copyright (C) 2013 jLDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud;


/**
 * The entry point into the Game Driver.<p/>
 *
 * This class initializes the driver and then hands off control to the main game loop.
 */
public final class Main {

    /**
     * Main method
     */
    public static void main( String[] args )
    {
        CommandLineArguments cliArgs = new CommandLineArguments();
    	if (cliArgs.parseCommandline(args)) {
    	    System.exit(cliArgs.getExitCode());
    	}

        System.out.println(Version.DRIVER_NAME+" "+Version.getVersionString());
        GameConfiguration config = new GameConfiguration();

        if (! config.loadProperties(cliArgs.getSettingsFilename(), cliArgs.getConfigSettings())) {
            System.exit(1);
        }

        if (cliArgs.getPrintConfiguration()) {
            config.printEffectiveSettings();
            System.exit(0);
        }

        // Java doesn't directly allow to change the current working directory of the process itself;
        // but by changing the 'user.dir' system property the same effect is achieved as the property
        // is used when the Java runtime resolves relative paths.
        if (null == System.setProperty("user.dir", config.getMudRoot().getAbsolutePath())) {
            System.err.println("Error: Can't set the 'user.dir' system property to the mud directory ('" + config.getMudRoot().getAbsolutePath() + "')");
            System.exit(1);
        }
    }
}

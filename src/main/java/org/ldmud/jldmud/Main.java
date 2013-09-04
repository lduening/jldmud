/**
 * Copyright (C) 2013 LDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.IOException;

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
    	cliArgs.parseCommandline(args);

        System.out.println(Version.DRIVER_NAME+" "+Version.getVersionString());
        if (! GameConfiguration.loadProperties(cliArgs.getPropertiesFilename(), cliArgs.getConfigProperties())) {
            System.exit(1);
        }
        if (cliArgs.getPrintConfiguration()) {
            GameConfiguration.printEffectiveProperties();
            System.exit(0);
        }

        // Java doesn't directly allow to change the current working directory of the process itself;
        // but by changing the 'user.dir' system property the same effect is achieved as the property
        // is used when the Java runtime resolves relative paths.
        try {
            final String dirPath = GameConfiguration.getMudDirectory().getCanonicalPath();
            if (null == System.setProperty("user.dir", dirPath)) {
                System.err.println("Error: Can't set the 'user.dir' system property to the mud directory ('" + dirPath + "')");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Error: Can't set the 'user.dir' system property to the mud directory ('"+GameConfiguration.getMudDirectory().getPath()+"'): "+e);
            System.exit(1);
        }
    }
}

/**
 * Copyright (C) 2013 LDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

/**
 * The entry point into the Game Driver.<p/>
 *
 * This class initializes the driver and then hands off control to the mud loop.
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
        if (! MudProperties.loadProperties(cliArgs.getPropertiesFilename(), cliArgs.getConfigProperties())) {
            System.exit(1);
        }
        if (cliArgs.getPrintConfiguration()) {
            MudProperties.printEffectiveProperties();
            System.exit(0);
        }
    }
}

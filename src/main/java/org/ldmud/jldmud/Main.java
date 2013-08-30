/* Copyright (C) LDMud developers */
package org.ldmud.jldmud;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The entry point into the Game Driver.<p/>
 *
 * This class initializes the driver and then hands off control to the mud loop.
 */
public final class Main {
	// The official name of the project.
	private static final String DRIVER_NAME = "jLDMud";

	/* Command line Arguments */
	private static String propertiesFilename = "mud.properties";

    /**
     * Main method
     */
    public static void main( String[] args )
    {
    	parseCommandline(args);

    	System.out.println(DRIVER_NAME+" "+Version.getVersionString());
    }

    /**
     * Parse the commandline and set the associated globals.
     * This method may exit the process, especially if an argument error is detected.
     */
    @SuppressWarnings("static-access")
    private static void parseCommandline(String [] args) {
        boolean needToExit = false;

        try {
            Option help = OptionBuilder.withLongOpt("help").withDescription("Prints the help text and exits.").create("h");
            Option helpConfig = OptionBuilder.withLongOpt("help-config").withDescription("Prints the <config properties> help text and exits.").create();
            Option version = OptionBuilder.withLongOpt("version").withDescription("Prints the driver version and exits").create("V");

            Options options = new Options();
            options.addOption(help);
            options.addOption(helpConfig);
            options.addOption(version);

            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(help.getOpt())) {
                final PrintWriter systemOut = new PrintWriter(System.out, true);

            	HelpFormatter formatter = new HelpFormatter();

                System.out.println("Usage: "+DRIVER_NAME+" [options] [<config properties>]");
                System.out.println();
                formatter.printWrapped(systemOut, formatter.getWidth(), "The <config properties> is a file containing the mud settings; if not specified, it defaults to '"+propertiesFilename+"'.");
                System.out.println();
                formatter.printOptions(systemOut, formatter.getWidth(), options, formatter.getLeftPadding(), formatter.getDescPadding());
                needToExit = true;
            }

            if (line.hasOption(helpConfig.getLongOpt())) {
                System.out.println("No help for config properties yet.");
                needToExit = true;
            }

            if (line.hasOption(version.getOpt())) {
            	System.out.println(DRIVER_NAME+" "+Version.getVersionString()+" - a LPMud Game Driver.");
                needToExit = true;
            }

            if (line.getArgs().length > 1) {
                System.err.println("Error: Too many arguments given.");
                System.exit(1);
            }

            if (line.getArgs().length == 1) {
                propertiesFilename = line.getArgs()[0];
            }

        } catch (ParseException e) {
            System.err.println("Parse exception: "+e);
            System.exit(1);
        }

        if (needToExit) {
            System.exit(0);
        }
    }
}

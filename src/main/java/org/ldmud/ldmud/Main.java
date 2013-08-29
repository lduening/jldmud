/* Copyright (C) LDMud developers */
package org.ldmud.ldmud;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
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
public final class Main
{
	/* Command line Arguments */
	private static String propertiesFilename = "mud.properties";

    /**
     * Main method
     */
    public static void main( String[] args )
    {
    	parseCommandline(args);
    }

    /**
     * Parse the commandline and set the associated globals.
     * This method may exit the process, especially if an argument error is detected.
     */
    @SuppressWarnings("static-access")
    private static void parseCommandline(String [] args) {
        try {
            Option help = OptionBuilder.withLongOpt("help").withDescription("prints this message").create("h");
            Options options = new Options();
            options.addOption(help);
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
            	HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("LDMud [options] [<mud properties file>]", options);
                System.exit(0);
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
    }
}

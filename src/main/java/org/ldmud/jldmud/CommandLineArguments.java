/**
 * Copyright (C) 2013 LDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This class parses and holds the commandline arguments.<p/>
 *
 * Not only does this neatly keep the wall of help text out of the main
 * class; it also allows to discard of the CLI arguments once they are no longer needed.
 */
public class CommandLineArguments {

    /* -- Parsed command line Arguments -- */

    /* Name of the properties */
    private String propertiesFilename = MudProperties.PROPERTIES_FILE;

    /* Manually provided configuration properties */
    private Properties configProperties = new Properties();

    /* Print the effective configuration properties after the system is loaded. */
    private boolean printConfiguration = false;

    /**
     * Constructor
     */
    public CommandLineArguments() {
        super();
    }

    /**
     * Parse the commandline and set the associated globals.
     * This method may exit the process, especially if an argument error is detected.
     */
    @SuppressWarnings("static-access")
    public void parseCommandline(String [] args) {
        boolean needToExit = false;

        try {
            Option configProperty = OptionBuilder.withLongOpt("config").withArgName("property=value").hasArgs(2).withValueSeparator()
                                                 .withDescription("Set the configuration property to the given value (overrides any setting in the <config properties> file). Unsupported properties are ignored.")
                                                 .create("C");
            Option help = OptionBuilder.withLongOpt("help").withDescription("Print the help text and exits.").create("h");
            Option helpConfig = OptionBuilder.withLongOpt("help-config").withDescription("Print the <config properties> help text and exits.").create();
            Option version = OptionBuilder.withLongOpt("version").withDescription("Print the driver version and exits").create("V");
            Option printConfig = OptionBuilder.withLongOpt("print-config").withDescription("Print the effective configuration properties to stdout and exit.").create();

            Options options = new Options();
            options.addOption(help);
            options.addOption(helpConfig);
            options.addOption(version);
            options.addOption(configProperty);
            options.addOption(printConfig);

            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);

            /* Handle the print-help-and-exit options first, to allow them to be chained in a nice way. */

            if (line.hasOption(version.getOpt())) {
                System.out.println(Version.DRIVER_NAME+" "+Version.getVersionString()+" - a LPMud Game Driver.");
                needToExit = true;
            }

            if (line.hasOption(help.getOpt())) {
                final PrintWriter systemOut = new PrintWriter(System.out, true);

                HelpFormatter formatter = new HelpFormatter();

                if (needToExit) {
                    System.out.println();
                }
                System.out.println("Usage: "+Version.DRIVER_NAME+" [options] [<config properties>]");
                System.out.println();
                formatter.printWrapped(systemOut, formatter.getWidth(), "The <config properties> is a file containing the mud settings; if not specified, it defaults to '"+MudProperties.PROPERTIES_FILE+"'. "+
                                                                        "The properties file must exist if no configuration property is specified via commandline argument.");
                System.out.println();
                formatter.printOptions(systemOut, formatter.getWidth(), options, formatter.getLeftPadding(), formatter.getDescPadding());
                needToExit = true;
            }

            if (line.hasOption(helpConfig.getLongOpt())) {
                if (needToExit) {
                    System.out.println();
                }
                MudProperties.printTemplate();
                needToExit = true;
            }

            if (needToExit) {
                System.exit(0);
            }

            if (line.hasOption(configProperty.getLongOpt())) {
                configProperties = line.getOptionProperties(configProperty.getLongOpt());
            }

            if (line.hasOption(printConfig.getLongOpt())) {
                printConfiguration = true;
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

    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    public Properties getConfigProperties() {
        return configProperties;
    }

    public boolean getPrintConfiguration() {
        return printConfiguration;
    }
}

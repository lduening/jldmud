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
 * This class parses and holds the command line arguments.<p/>
 *
 * Not only does this neatly keep the wall of help text out of the main
 * class; it also allows to discard of the CLI arguments once they are no longer needed.
 */
public class CommandLineArguments {

    /* -- Parsed command line Arguments -- */

    /* Name of the settings file */
    private String settingsFilename = GameConfiguration.DEFAULT_SETTINGS_FILE;

    /* Manually provided configuration settings */
    private Properties configSettings = new Properties();

    /* Print the effective configuration after the system is loaded. */
    private boolean printConfiguration = false;

    /* -- Other variables -- */
    private int exitCode = 0;

    /**
     * Constructor
     */
    public CommandLineArguments() {
        super();
    }

    /**
     * Parse the commandline and set the associated globals.
     *
     * @return {@code true} if the main program should exit; in that case {@link @getExitCode()} provides the suggest exit code.
     */
    @SuppressWarnings("static-access")
    public boolean parseCommandline(String [] args) {

        try {
            Option configSetting = OptionBuilder.withLongOpt("config").withArgName("setting=value").hasArgs(2).withValueSeparator()
                                                 .withDescription("Set the configuration setting to the given value (overrides any setting in the <config settings> file). Unsupported settings are ignored.")
                                                 .create("C");
            Option help = OptionBuilder.withLongOpt("help").withDescription("Print the help text and exits.").create("h");
            Option helpConfig = OptionBuilder.withLongOpt("help-config").withDescription("Print the <config settings> help text and exits.").create();
            Option version = OptionBuilder.withLongOpt("version").withDescription("Print the driver version and exits").create("V");
            Option printConfig = OptionBuilder.withLongOpt("print-config").withDescription("Print the effective configuration settings to stdout and exit.").create();

            Options options = new Options();
            options.addOption(help);
            options.addOption(helpConfig);
            options.addOption(version);
            options.addOption(configSetting);
            options.addOption(printConfig);

            CommandLineParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args);

            /* Handle the print-help-and-exit options first, to allow them to be chained in a nice way. */

            boolean helpOptionsGiven = false;

            if (line.hasOption(version.getOpt())) {
                System.out.println(Version.DRIVER_NAME+" "+Version.getVersionString()+" - a LPMud Game Driver.");
                helpOptionsGiven = true;
            }

            if (line.hasOption(help.getOpt())) {
                final PrintWriter systemOut = new PrintWriter(System.out, true);

                HelpFormatter formatter = new HelpFormatter();

                if (helpOptionsGiven) {
                    System.out.println();
                }
                System.out.println("Usage: "+Version.DRIVER_NAME+" [options] [<config settings>]");
                System.out.println();
                formatter.printWrapped(systemOut, formatter.getWidth(), "The <config settings> is a file containing the game settings; if not specified, it defaults to '"+GameConfiguration.DEFAULT_SETTINGS_FILE+"'. "+
                                                                        "The settings file must exist if no configuration setting is specified via commandline argument.");
                System.out.println();
                formatter.printOptions(systemOut, formatter.getWidth(), options, formatter.getLeftPadding(), formatter.getDescPadding());
                helpOptionsGiven = true;
            }

            if (line.hasOption(helpConfig.getLongOpt())) {
                if (helpOptionsGiven) {
                    System.out.println();
                }
                GameConfiguration.printTemplate();
                helpOptionsGiven = true;
            }

            if (helpOptionsGiven) {
                exitCode = 0;
                return true;
            }

            /* Parse the real options */

            /* TODO: If we get many real options, it would be useful to implement a more general system like {@link GameConfiguration#SettingBase} */

            if (line.hasOption(configSetting.getLongOpt())) {
                configSettings = line.getOptionProperties(configSetting.getLongOpt());
            }

            if (line.hasOption(printConfig.getLongOpt())) {
                printConfiguration = true;
            }

            if (line.getArgs().length > 1) {
                throw new ParseException("Too many arguments");
            }

            if (line.getArgs().length == 1) {
                settingsFilename = line.getArgs()[0];
            }

            return false;
        } catch (ParseException e) {
            System.err.println("Error: "+e.getMessage());
            exitCode = 1;
            return true;
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getSettingsFilename() {
        return settingsFilename;
    }

    public Properties getConfigSettings() {
        return configSettings;
    }

    public boolean getPrintConfiguration() {
        return printConfiguration;
    }
}

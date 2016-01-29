/**
 * Copyright (C) 2013 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * The Mud's configuration settings.<p/>
 *
 * The properties are primarily read from a configuration file, but the
 * class allows for the manual override from a {@code Properties} instance.<p/>
 *
 * In addition to storing the values, the class also attempts to generalize
 * the way the settings are defined, to simplify the definition itself,
 * and the generation of help texts.<p/>
 *
 * Due to the global nature of configuration settings, the instance of this class
 * is passed to all modules needing configueration. The modules can then pick which values
 * they need. And all the memory used for this instance can be
 * released once initialization is complete.
 */
public class GameConfiguration {

    /**
     * Default configuration filename.
     */
    public final static String DEFAULT_SETTINGS_FILE = "mud.properties";

    /*
     * The settings themselves.
     */
    private final DirectorySetting mudDirectory = new DirectorySetting("mud.dir",
            "The root directory of the mud lib, which may be specified relative to the driver process' working directory.", true);
    private final GameDirectorySetting driverLogDirectory = new GameDirectorySetting(
            "mud.dir.driverlog",
            "The directory in which to keep the driver logs, which may be specified relative to the driver process' working directory. "+
            "If the path name starts with '${mud.dir}', it is interpreted relative to the mud.dir setting.",
            true, mudDirectory);
    private final GameDirectorySetting mudLogDirectory = new GameDirectorySetting(
            "mud.dir.gamelog",
            "The directory in which to keep the game logs, which may be specified relative to the driver process' working directory. "+
            "If the path name starts with '${mud.dir}', it is interpreted relative to the mud.dir setting.",
            true, mudDirectory);

    /*
     * This list tracks all settings as they are defined.
     */
    List<SettingBase<?>> allSettings = new ArrayList<>();

    public GameConfiguration() {
        super();
        allSettings.add(mudDirectory);
        allSettings.add(driverLogDirectory);
        allSettings.add(mudLogDirectory);
    }

    /**
     * Load the settings from the given input source and/or the override properties, and validate them.
     * If the validation fails, an error message is printed to stderr.
     *
     * @param propertyFileName The name of the settings file in properties format.
     * @param overrideProperties A manually created set of properties, overriding those in the settings file. If this set
     *          is not empty, the {@code propertyFileName} need not exist.
     * @return {@code true} if the file was successfully loaded.
     */
    public boolean loadProperties(String propertyFileName, Properties overrideProperties) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(propertyFileName)) {
            properties.load(in);
        } catch (IOException ioe) {
            final String message = (ioe instanceof FileNotFoundException) ? "File not found" : ioe.toString();
            if (overrideProperties.isEmpty()) {
                System.err.println("Error: Problem loading ".concat(propertyFileName).concat(": ").concat(message));
                return false;
            }
            System.err.println("Warning: Problem loading ".concat(propertyFileName).concat(": ").concat(message));
        }

        List<String> errors = loadProperties(properties, overrideProperties, allSettings);

        if (!errors.isEmpty()) {
            System.err.println("Error: Property validation problems loading the configuration '" + propertyFileName + "':");
            for (String entry : errors) {
                System.err.println("  " + entry);
            }
        }

        return errors.isEmpty();
    }

    /**
     * Load the given properties and validate them.
     *
     * @param properties The properties file read from the input source.
     * @param overrideProperties A manually created set of properties, overriding those in the properties file.
     * @param propertyList The list of property instances to load the values into.
     * @return A list of errors, if any property value failed to validate.
     */
     List<String> loadProperties(Properties properties, Properties overrideProperties, List<SettingBase<?>> propertyList) {
        List<String> errors = new ArrayList<>();

        for (SettingBase<?> entry : propertyList) {
            String value = overrideProperties.containsKey(entry.name) ? overrideProperties.getProperty(entry.name) : properties.getProperty(entry.name);
            String error = null;
            if (value != null) {
                error = entry.parseValue(StringUtils.strip(value));
            }
            if (StringUtils.isEmpty(error) && entry.value == null && entry.required) {
                error = "Setting is required.";
            }
            if (!StringUtils.isEmpty(error)) {
                errors.add("Setting '" + entry.name + "': " + error);
            }
        }
        return errors;
    }

    /**
     * Wrap the given text to a line length of 70, while preserving hard line
     * breaks. Wrapped lines will be prepended with '# '.
     *
     * @param lines The text to wrap, each String representing a line.
     * @return The wrapped text, joined into one string, with a trailing line break.
     */
    static String wrap(String[] lines) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lines.length; ++i) {
            sb.append(WordUtils.wrap(lines[i], 70, System.lineSeparator() + "# ", false)).append(System.lineSeparator());
        }
        return sb.toString();
    }


    /**
     * Wrap the given text to a line length of 70, while preserving hard line
     * breaks. Wrapped lines will be prepended with '# '.
     *
     * @param in The string to wrap.
     * @return The wrapped string, with a trailing line break.
     */
    static String wrap(String in) {
        return wrap(StringUtils.split(in, System.lineSeparator()));
    }

    /**
     * Using the registered options, print an an example template.
     */
    public static void printTemplate() {
        System.out.print(wrap(new String[] {
          "# This is a template Mud settings file.",
          "#",
          "# Settings with defaults will have their default value printed as example value.",
          "#",
          "# All settings can also be specified as arguments on the commandline; in that case, any commandline value overrides a corresponding properties file value."
        }));
        System.out.println();
        for (SettingBase<?> entry : new GameConfiguration().allSettings) {
            System.out.print(entry.describe());
            System.out.println();
        }
    }

    /**
     * Using the registered options, print the currently effective settings.
     */
    public void printEffectiveSettings() {
        System.out.print(wrap(new String[] {
          "# -- Effective Mud configuration settings --",
          "# If the effective setting is different from the originally provided configuration value, that original value will be printed as comment."
        }));
        System.out.println();
        for (SettingBase<?> entry : allSettings) {
            System.out.print(entry.effective());
            System.out.println();
        }
        System.out.println("# -- END of effective Mud configuration properties --");
    }

    /* --------------------- Configuration Property Accessors ------------------------------ */

    /**
     * The configured mud directory may have been specified relative to the initial working
     * directory, so its absolute path may no longer be correct once the startup is complete. Instead, create
     * paths relative to the value of {@link GameConfiguration#getMudRoot() getMudRoot()}.
     * TODO: Is this setting ever used directly?
     *
     * @return The configured mud directory (may be relative to the initial working directory).
     */
    public File getMudDirectory() {
        return mudDirectory.value;
    }

    /**
     * @return The effective absolute root directory of the mud.
     */
    public File getMudRoot() {
        return mudDirectory.getEffectiveValue();
    }

    /**
     * The configured log directory may have been specified relative to the initial working
     * directory, so its absolute path may no longer be correct once the startup is complete. Instead, create
     * paths relative to the value of {@link GameConfiguration#getDriverLogRoot() getDriverLogRoot()}.
     * TODO: Is this setting ever used directly?
     *
     * @return The configured driver log directory (may be relative to the initial working directory).
     */
    public File getDriverLogDirectory() {
        return driverLogDirectory.value;
    }

    /**
     * @return The effective absolute directory holding the driver logs.
     */
    public File getDriverLogRoot() {
        return driverLogDirectory.getEffectiveValue();
    }

    /**
     * The configured log directory may have been specified relative to the initial working
     * directory, so its absolute path may no longer be correct once the startup is complete. Instead, create
     * paths relative to the value of {@link GameConfiguration#getDriverLogRoot() getDriverLogRoot()}.
     * TODO: Is this setting ever used directly?
     *
     * @return The configured game log directory (may be relative to the initial working directory).
     */
    public File getGameLogDirectory() {
        return mudLogDirectory.value;
    }

    /**
     * @return The effective absolute directory holding the game logs.
     */
    public File getGameLogRoot() {
        return mudLogDirectory.getEffectiveValue();
    }
}
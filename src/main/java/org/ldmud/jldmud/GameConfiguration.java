/**
 * Copyright (C) 2013 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

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
 * Due to the global nature of configuration settings, instances of this class
 * (ok, the one instance) are not passed to the modules needing the values. Instead,
 * either the individual values are passed by the Main module explicitly, or
 * the modules can define their own 'configuration settings' classes, which this global
 * class can then generate as needed. That way, the business modules aren't tempted to
 * look at other modules parameters. And all the memory used for this instance can be
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
    private final DirectorySetting mudDirectory = new DirectorySetting("mud.dir", "The root directory of the mud lib, which may be specified relative to the driver process' working directory.", true);

    /*
     * This list tracks all settings as they are defined.
     */
    List<SettingBase<?>> allSettings;

    /**
     * Base class describing a setting.<p/>
     *
     * The class allows options to create an effective value based
     * upon the given value - to this end the retrieval of the
     * final effective value is done through the method {@link #getEffectiveValue()},
     * which just happens in its default implementation to return the
     * given values.
     *
     * @param <T> The target type of the setting value.
     */
    abstract class SettingBase<T> {
        // The name of the setting, e.g. 'mud.directory'
        protected String name;

        // The description of the setting, e.g. "The mud directory"
        // The help printer will prepend '#' and possibly 'Optional: ', and
        // take care of the wrapping. Embedded line feeds will be preserved.
        protected String description;

        // Flag whether the setting is required
        protected boolean required;

        // Flag set to true if this setting was ever explicitly set.
        protected boolean wasSet = false;

        // The parsed value
        protected T value;

        // The default value (optional)
        protected T defaultValue;

        public SettingBase(String name, String description, boolean required) {
            super();
            this.name = name;
            this.description = description;
            this.required = required;

            registerThis();
        }

        /**
         * Register this Setting instance with the allSettings list in the GameConfiguration class instance.
         * The list is created if necessary.
         */
        private void registerThis() {
            if (allSettings == null) {
                allSettings = new ArrayList<>();
            }
            allSettings.add(this);
        }

        public SettingBase(String name, String description, T defaultValue) {
            super();
            this.name = name;
            this.description = description;
            this.required = false;
            this.defaultValue = defaultValue;
            this.value = defaultValue;

            registerThis();
        }

        /**
         * Create the self-description string suitable for a properties template file.
         *
         * @return The multi-line self description string, with a trailing line break.
         */
        public String describe() {
            return wrap("# "+(required ? "" : "Optional: ")+description+System.lineSeparator()+
                   name+"="+(defaultValue != null ? defaultValue : "")+System.lineSeparator());
        }

        /**
         * Print the given and effective setting of the setting, inclusive description.
         *
         * @return The multi-line string describing the setting setting, with a trailing line break.
         */
        public String effective() {
            StringBuilder sb = new StringBuilder("# ");
            if (!required) {
                sb.append("Optional: ");
            }
            sb.append(description).append(System.lineSeparator());
            if (!wasSet) {
                if (value == null) {
                    sb.append("# Using default value (unset).").append(System.lineSeparator());
                } else {
                    sb.append("# Using default value: ").append(value).append(System.lineSeparator());
                }
            } else if (value == null && getEffectiveValue() != null) {
                sb.append("# Configured value unset.").append(System.lineSeparator());
            } else if (value != null && !value.equals(getEffectiveValue())) {
                sb.append("# Configured value: ").append(value).append(System.lineSeparator());
            }

            if (getEffectiveValue() == null) {
                sb.append("# ").append(name).append("=");
            } else {
                sb.append(name).append("=").append(getEffectiveValue());
            }
            sb.append(System.lineSeparator());
            return wrap(sb.toString());
        }

        /**
         * Parse the given string for the desired value, and set the {@link #value} member from it.
         * The actual work of the parsing is done by {@link #parseValueImpl(String)}, this method
         * just does some housekeeping.
         *
         * @param v The setting value string to parse
         * @return An error message, or {@code null} if the value could be set.
         */
        public String parseValue(String v) {
            String rc = parseValueImpl(v);
            if (rc == null) {
                wasSet = true;
            }
            return rc;
        }

        /**
         * Parse the given string for the desired value, and set the {@link #value} member from it.
         * This method is called as part of the {@link #parseValue(String)} processing.
         *
         * @param v The property string to parse
         * @return An error message, or {@code null} if the value could be set.
         */
        protected abstract String parseValueImpl(String v);

        /**
         * @return the effective value of this property.
         */
        public T getEffectiveValue() {
            return value;
        }
    }

    /**
     * A setting holding an directory, which must exist.
     * The effective value will be the absolute canonical file path.
     */
    class DirectorySetting extends SettingBase<File> {

        File effectiveValue;

        public DirectorySetting(String name, String description, File defaultValue) {
            super(name, description, defaultValue);
        }

        public DirectorySetting(String name, String description,
                boolean required) {
            super(name, description, required);
        }

        @Override
        public String parseValueImpl(String v) {
            if (! StringUtils.isEmpty(v)) {
                File f = new File(v);
                if (!f.isDirectory()) {
                    return "'" + v + "' doesn't exist, or is not a directory.";
                }
                value = f;
                try {
                    effectiveValue = f.getAbsoluteFile().getCanonicalFile();
                } catch (IOException e) {
                    return "'" + v +"' can't be resolved to a canonical path.";
                }
            }

            return null;
        }

        @Override
        public File getEffectiveValue() {
            return effectiveValue;
        }
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
}
/**
 * Copyright (C) 2013 LDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * The Mud's configuration properties.<p/>
 *
 * The properties are primarily read from a configuration file, but the
 * class allows for the manual override from a {@code Properties} instance.<p/>
 *
 * In addition to storing the values, the class also attempts to generalize
 * the way the properties are defined.
 */
public class GameConfiguration {

    /**
     * Default configuration filename.
     */
    public final static String PROPERTIES_FILE = "mud.properties";

    /**
     * Helper constant for creating multi-line descriptions.
     */
    @SuppressWarnings("unused")
    private final static String NL = System.lineSeparator()+"# ";

    /**
     * Base class describing a property.
     *
     * @param <T> The target type of the property value.
     */
    abstract static class PropertyBase<T> {
        // The name of the property, e.g. 'mud.directory'
        String name;

        // The description of the property, e.g. "The mud directory"
        // The help printer will prepend '#' and possibly 'Optional: '; but
        // for multi-line descriptions the wrapping with additional newlines and '#'
        // need to be done by hand.
        String description;

        // Flag whether the property is required
        boolean required;

        // The parsed value
        T value;

        // The default value (optional)
        T defaultValue;

        // This list tracks all properties as they are defined.
        static List<PropertyBase<?>> allProperties = new ArrayList<>();

        public PropertyBase(String name, String description, boolean required) {
            super();
            this.name = name;
            this.description = description;
            this.required = required;

            allProperties.add(this);
        }

        public PropertyBase(String name, String description, T defaultValue) {
            super();
            this.name = name;
            this.description = description;
            this.required = false;
            this.defaultValue = defaultValue;
            this.value = defaultValue;

            allProperties.add(this);
        }

        /**
         * Create the self-description string suitable for a properties template file.
         *
         * @return The (multi-line) self description string
         */
        public String describe() {
            return "# "+(required ? "" : "Optional: ")+description+System.lineSeparator()+
                   name+"="+(defaultValue != null ? defaultValue : "");
        }

        /**
         * Print the effective setting of the property, inclusive description.
         *
         * @return The (multi-line) string describing the property setting.
         */
        public String effective() {
            return "# "+(required ? "" : "Optional: ")+description+System.lineSeparator()+
                   (value == null ? "#" : "") + name+"="+(value != null ? value : "");
        }

        /**
         * Parse the given string for the desired value, and set the {@link #value} member from it.
         *
         * @param v The property string to parse
         * @return An error message, or {@code null} if the value could be set.
         */
        public abstract String parseValue(String v);
    }

    /**
     * A property holding a directory, which must exist.
     */
    static class DirectoryProperty extends PropertyBase<File> {

        public DirectoryProperty(String name, String description, File defaultValue) {
            super(name, description, defaultValue);
        }

        public DirectoryProperty(String name, String description,
                boolean required) {
            super(name, description, required);
        }

        @Override
        public String parseValue(String v) {
            if (! StringUtils.isEmpty(v)) {
                File f = new File(v);
                if (!f.isDirectory()) {
                    return "'" + v + "' doesn't exist, or is not a directory.";
                }
                value = f;
            }

            return null;
        }
    }

    /*
     * The properties themselves.
     */
    private static final DirectoryProperty mudDirectory = new DirectoryProperty("mud.dir", "The root directory of the mud lib, which is also the working directory of the driver process.", true);

    /**
     * Load the properties from the given input source and/or the override properties, and validate them.
     * If the validation fails, an error message is printed to stderr.
     *
     * @param propertyFileName The name of the properties file
     * @param overrideProperties A manually created set of properties, overriding those in the properties file. If this set
     *          is not empty, the {@code propertyFileName} need not exist.
     * @return {@code true} if the file was successfully loaded.
     */
    public static boolean loadProperties(String propertyFileName, Properties overrideProperties) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(propertyFileName)) {
            properties.load(in);
        } catch (IOException ioe) {
            if (overrideProperties.isEmpty()) {
                System.err.println("Error: Problem loading ".concat(propertyFileName).concat(": ").concat(ioe.toString()));
                return false;
            }
            System.err.println("Warning: Problem loading ".concat(propertyFileName).concat(": ").concat(ioe.toString()));
        }

        List<String> errors = loadProperties(properties, overrideProperties, PropertyBase.allProperties);

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
    static List<String> loadProperties(Properties properties, Properties overrideProperties, List<PropertyBase<?>> propertyList) {
        List<String> errors = new ArrayList<>();

        for (PropertyBase<?> entry : propertyList) {
            String value = overrideProperties.containsKey(entry.name) ? overrideProperties.getProperty(entry.name) : properties.getProperty(entry.name);
            String error = null;
            if (value != null) {
                error = entry.parseValue(value);
            }
            if (StringUtils.isEmpty(error) && entry.value == null && entry.required) {
                error = "Property is required.";
            }
            if (!StringUtils.isEmpty(error)) {
                errors.add("Property '" + entry.name + "': " + error);
            }
        }
        return errors;
    }

    /**
     * Using the registered options, print an an example template.
     */
    public static void printTemplate() {
        System.out.println("# This is a template Mud properties file.");
        System.out.println("# Properties with defaults will have their default value printed as example value.");
        System.out.println("# All properties can also be specified as arguments on the commandline; in that case, any");
        System.out.println("# commandline value overrides a corresponding properties file value.");
        System.out.println();
        for (PropertyBase<?> entry : PropertyBase.allProperties) {
            System.out.println(entry.describe());
            System.out.println();
        }
    }

    /**
     * Using the registered options, print the currently effective settings.
     */
    public static void printEffectiveProperties() {
        System.out.println("# -- Effective Mud configuration properties --");
        System.out.println();
        for (PropertyBase<?> entry : PropertyBase.allProperties) {
            System.out.println(entry.effective());
            System.out.println();
        }
        System.out.println("# -- END of effective Mud configuration properties --");
    }

    /* ------------------------- Property Accessors ---------------------------------- */

    public static File getMudDirectory() {
        return mudDirectory.value;
    }

}
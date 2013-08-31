/**
 * Copyright (C) 2013 LDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * The Mud's properties, are read from the configuration file.<p/>
 *
 * In addition to storing the values, the class also attempts to generalize
 * the way the properties are defined, as well as generating a template property
 * file.
 */
public class MudProperties {

    /**
     * Default mud properties filename.
     */
    public final static String PROPERTIES_FILE = "mud.properties";

    /**
     * Helper constant for creating multiline descriptions.
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
         * Parse the given string for the desired value, and set the {@link #value} member from it.
         *
         * @param v The property string to parse
         * @return An error message, or {@code null} if the value could be set.
         */
        public abstract String parseValue(String v);
    }

    /**
     * A property holding a directory.
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
    public static final DirectoryProperty mudDirectory = new DirectoryProperty("mud.dir", "The root directory of the mud lib.", true);

    /**
     * Load the properties from the given input source, and validate them. If the validation fails,
     * an error message is printed to stderr.
     *
     * @param propertyFileName The name of the properties file, for error messages
     * @param propertyFile The properties file
     * @return {@code true} if the file was successfully loaded.
     */
    public static boolean loadProperties(String propertyFileName, InputStream propertyFile) {
        try {

            Properties properties = new Properties();
            properties.load(propertyFile);

            List<String> errors = loadProperties(properties, PropertyBase.allProperties);

            if (! errors.isEmpty()) {
                System.out.println();
                System.err.println("Problems loading the configuration '"+propertyFileName+"':");
                for (String entry : errors) {
                    System.err.println("  "+entry);
                }
            }

            return ! errors.isEmpty();
        } catch (IOException ioe) {
            System.out.println();
            System.err.println("Error: Problem loading ".concat(propertyFileName).concat(":").concat(ioe.toString()));
            return false;
        }
    }

    /**
     * Load the given properties and validate them.
     *
     * @param properties The properties file read from the input source.
     * @param propertyList The list of property instances to load the values into.
     * @return A list of errors, if any property value failed to validate.
     */
    static List<String> loadProperties(Properties properties, List<PropertyBase<?>> propertyList) {
        List<String> errors = new ArrayList<>();

        for (PropertyBase<?> entry : propertyList) {
            String value = properties.getProperty(entry.name);
            String error = null;
            if (value == null && entry.required) {
                error = "Property is required.";
            } else {
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
        System.out.println("# Properties with defaults will have their default value filled in.");
        System.out.println();
        for (PropertyBase<?> entry : PropertyBase.allProperties) {
            System.out.println(entry.describe());
            System.out.println();
        }
    }
}
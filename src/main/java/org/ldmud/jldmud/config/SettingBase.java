/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;


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
    }

    public SettingBase(String name, String description, T defaultValue) {
        super();
        this.name = name;
        this.description = description;
        this.required = false;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    /**
     * Create the self-description string suitable for a properties template file.
     *
     * @return The multi-line self description string, with a trailing line break.
     */
    public String describe() {
        return GameConfiguration.wrap("# "+(required ? "" : "Optional: ")+description+System.lineSeparator()+
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
        return GameConfiguration.wrap(sb.toString());
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
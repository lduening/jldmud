/**
 * Copyright (C) 2013 jLDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Version information about the driver, read from the version.properties resource.
 * An argument could be made that this class, too, should not use static globals.
 */
public class Version {
    // The official name of the project.
    public static final String DRIVER_NAME = "jLDMud";

    public static String ReleaseType;
	public static String ReleaseLongType;
	public static String ReleaseDate;
	public static String Version;
	public static String Major;
	public static String Minor;
	public static String Micro;
	public static String Tag;
	public static String Copyright;
    public static String License;

    static {
        loadProperties();
    }

    /**
     * Load the version information from the properties file.
     */
    private static void loadProperties() {
        boolean loaded = false;
        final String versionProperties = "/org/ldmud/jldmud/version.properties";
        try (InputStream in = Version.class.getResourceAsStream(versionProperties)) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                loaded = true;
                ReleaseType = properties.getProperty("version.release.type");
                ReleaseLongType = properties.getProperty("version.release.longtype");
                ReleaseDate = properties.getProperty("version.release.date");
                Version = properties.getProperty("version");
                Copyright = properties.getProperty("version.copyright");
                License = properties.getProperty("version.license");
            }
        } catch (IOException ioe) {
            System.err.println("Error: Problem loading ".concat(versionProperties).concat(": ").concat(ioe.toString()));
        }
        if (!loaded) {
            throw new IllegalArgumentException("Unable to load ".concat(versionProperties));
        }

        /* Take apart the full version string. */

        Pattern pattern = Pattern.compile("(?<major>[^.]+)" + "\\.(?<minor>[^.-]+)" + "(?:\\.(?<micro>[^-]+))?" + "(?:\\-(?<tag>.+))?");
        Matcher matcher = pattern.matcher(Version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(versionProperties+": Version string '"+Version+"' is malformed - {major}.{minor}[.{micro}][-{tag}] format expected.");
        }
        Major = matcher.group("major");
        Minor = matcher.group("major");
        Micro = matcher.group("micro");
        if (Micro == null) {
            Micro = "";
        }
        Tag = matcher.group("tag");
        if (Tag == null) {
            Tag = "";
        }
    }

    /**
     * @return The full version string for printing purposes.
     */
    public static String getVersionString() {
    	return Version + getDriverTag();
    }

    /**
     * @return the 'driver tag', ie. the string " (releaseLongType)" if
     * the release long type is not empty. Otherwise, return an empty string.
     */
    public static String getDriverTag() {
    	if (! StringUtils.isEmpty(ReleaseLongType)) {
    		return " ("+ReleaseLongType+")";
    	}

    	return "";
    }
}

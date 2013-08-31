/**
 * Copyright (C) 2013 LDMud developers
 * This file is free software under the MIT license - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Version information about the driver, read from the version.properties resource.
 */
public class Version {
	public static String ReleaseType;
	public static String ReleaseLongType;
	public static String ReleaseDate;
	public static String Version;
	public static String Major;
	public static String Minor;
	public static String Micro;

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
                ReleaseType = properties.getProperty("ldmud.version.release.type");
                ReleaseLongType = properties.getProperty("ldmud.version.release.longtype");
                ReleaseDate = properties.getProperty("ldmud.version.release.date");
                Version = properties.getProperty("ldmud.version");
                Major = properties.getProperty("ldmud.version.major");
                Minor = properties.getProperty("ldmud.version.minor");
                Micro = properties.getProperty("ldmud.version.micro");
            }
        } catch (IOException ioe) {
            System.err.println("Error: Problem loading ".concat(versionProperties).concat(":"));
            ioe.printStackTrace();
        }
        if (!loaded) {
            throw new RuntimeException("Unable to load ".concat(versionProperties));
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

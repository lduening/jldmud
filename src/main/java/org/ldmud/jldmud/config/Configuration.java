/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import java.io.File;

import com.google.inject.Singleton;

/**
 * Class holding various configuration parameters.
 */
@Singleton
public class Configuration {
    private File mudDirectory;
    private File driverLogDirectory;
    private File mudLogDirectory;

    /**
     * @return The absolute root directory of the mud.
     */
    public File getMudDirectory() {
        return mudDirectory;
    }

    /**
     * @return The absolute directory holding the driver logs.
     */
    public File getDriverLogDirectory() {
        return driverLogDirectory;
    }

    /**
     * @return The absolute directory holding the game logs.
     */
    public File getMudLogDirectory() {
        return mudLogDirectory;
    }

    /**
     * @param mudDirectory The absolute directory of the mud
     */
    public void setMudDirectory(File mudDirectory) {
        this.mudDirectory = mudDirectory;
    }

    /**
     * @param driverLogDirectory The absolute directory for the driver logs.
     */
    public void setDriverLogDirectory(File driverLogDirectory) {
        this.driverLogDirectory = driverLogDirectory;
    }

    /**
     * @param mudLogDirectory The absolute directory holding the game logs.
     */
    public void setMudLogDirectory(File mudLogDirectory) {
        this.mudLogDirectory = mudLogDirectory;
    }

}

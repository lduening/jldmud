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
    private File driverDirectory;
    private File driverLogDirectory;
    private File mudLogDirectory;
    private Long memoryReserve;

    /**
     * @return The absolute root directory of the mud library.
     */
    public File getMudDirectory() {
        return mudDirectory;
    }

    /**
     * @return The absolute root directory for driver files.
     */
    public File getDriverDirectory() {
        return driverDirectory;
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
     * @return The memory reserve in Bytes.
     */
    public Long getMemoryReserve() {
        return memoryReserve;
    }

    /**
     * @param memoryReserve The memory reserve in Bytes.
     */
    public void setMemoryReserve(Long memoryReserve) {
        this.memoryReserve = memoryReserve;
    }

    /**
     * @param mudDirectory The absolute directory of the mud library.
     */
    public void setMudDirectory(File mudDirectory) {
        this.mudDirectory = mudDirectory;
    }

    /**
     * @param driverDirectory The absolute directory of the driver files.
     */
    public void setDriverDirectory(File driverDirectory) {
        this.driverDirectory = mudDirectory;
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

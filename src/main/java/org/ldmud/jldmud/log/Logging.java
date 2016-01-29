/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.config.GameConfiguration;

/**
 * Main class to configure the logging subsystem, and to provide standard loggers.<p>
 *
 * This is the first class touching the logging subsystem,
 * allowing us to set up the file path properties first.
 */
public class Logging {
    public static Logger mudLog;

    /**
     * Configure and 'start' the logging subsystem.
     *
     * @param configuration The configuration object providing the file paths.
     */
    public static void config(GameConfiguration configuration) {
        System.setProperty("mud.logdir.game", configuration.getGameLogRoot().toString());
        System.setProperty("mud.logdir.driver", configuration.getDriverLogRoot().toString());

        mudLog = LogManager.getLogger("MudLog");
    }
}

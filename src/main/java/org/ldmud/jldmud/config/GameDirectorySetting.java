/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * A setting holding an directory, which does not need to exist, and
 * and can also be relative to given root directories.
 * The effective value will be the absolute canonical file path.
 */
public class GameDirectorySetting extends DirectorySetting {

    public final static String MUDROOT_PREFIX = "${mud.dir}";

    String defaultValueString;
    File effectiveValue;
    DirectorySetting rootDirectories[];

    public GameDirectorySetting(String name, String description, File defaultValue, DirectorySetting... rootDirectories) {
        super(name, description, defaultValue);
        this.rootDirectories = rootDirectories;
        try {
            effectiveValue = defaultValue.getAbsoluteFile().getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Error creating a GameDirectorySetting instance for '" + name + "' with default value "+defaultValue, e);
        }
    }

    public GameDirectorySetting(String name, String description, boolean required, DirectorySetting... rootDirectories) {
        super(name, description, required);
        this.rootDirectories = rootDirectories;
    }

    public GameDirectorySetting(String name, String description, String defaultValue, DirectorySetting... rootDirectories) {
        super(name, description, new File(defaultValue));
        this.rootDirectories = rootDirectories;
        defaultValueString = defaultValue;
    }

    @Override
    public String parseValueImpl(String v) {
        if (!StringUtils.isEmpty(v)) {
            File f = null;
            for (DirectorySetting entry : rootDirectories) {
                String prefix = "${"+entry.name+"}";
                if (v.startsWith(prefix)) {
                    if (entry.getEffectiveValue() == null) {
                        return "'" + v + "' references uninitialized setting '" + entry.name+"'.";
                    }
                    if (v.startsWith(prefix+"/")) {
                        f = new File(entry.getEffectiveValue(), v.substring(prefix.length()+1));
                    } else {
                        f = new File(entry.getEffectiveValue(), v.substring(prefix.length()));
                    }

                    break;
                }
            }
            if (f == null) {
                if (v.startsWith("${")) {
                    return "'" + v + "' references an undefined setting.";
                }
                f = new File(v);
            }
            if (f.exists() && !f.isDirectory()) {
                return "'" + v + "' exists, but is not a directory.";
            }
            try {
                effectiveValue = f.getAbsoluteFile().getCanonicalFile();
            } catch (IOException e) {
                return "'" + v + "' can't be resolved to a canonical path.";
            }
            value = new File(v);
        }

        return null;
    }

    @Override
    public File getEffectiveValue() {
        if (effectiveValue == null && defaultValueString != null) {
            String msg = parseValueImpl(defaultValueString);
            if (msg != null) {
                throw new RuntimeException("Error creating GameDirectorySetting default value for '" + name + "': "+msg);
            }
        }
        return effectiveValue;
    }
}

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
 * and can also be relative to the mud root directory.
 * The effective value will be the absolute canonical file path.
 */
public class GameDirectorySetting extends SettingBase<File> {

    public final static String MUDROOT_PREFIX = "${mud.dir}";

    File effectiveValue;
    DirectorySetting mudDirectory;

    public GameDirectorySetting(String name, String description, File defaultValue, DirectorySetting mudDirectory) {
        super(name, description, defaultValue);
        this.mudDirectory = mudDirectory;
        try {
            effectiveValue = defaultValue.getAbsoluteFile().getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Error creating a DirectorySetting instance for default value "+defaultValue, e);
        }
    }

    public GameDirectorySetting(String name, String description, boolean required, DirectorySetting mudDirectory) {
        super(name, description, required);
        this.mudDirectory = mudDirectory;
    }

    @Override
    public String parseValueImpl(String v) {
        if (!StringUtils.isEmpty(v)) {
            if (mudDirectory.getEffectiveValue() == null) {
                return "The mud root directory must be defined first.";
            }
            File f;
            if (v.startsWith(MUDROOT_PREFIX)) {
                f = new File(mudDirectory.getEffectiveValue(), v.substring(MUDROOT_PREFIX.length()+1));
            } else {
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
        return effectiveValue;
    }
}

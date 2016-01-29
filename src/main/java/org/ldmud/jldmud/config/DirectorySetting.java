/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * A setting holding an directory, which must exist.
 * The effective value will be the absolute canonical file path.
 */
class DirectorySetting extends SettingBase<File> {

    File effectiveValue;

    public DirectorySetting(String name, String description, File defaultValue) {
        super(name, description, defaultValue);
        try {
            effectiveValue = defaultValue.getAbsoluteFile().getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Error creating a DirectorySetting instance for default value "+defaultValue, e);
        }
    }

    public DirectorySetting(String name, String description,
            boolean required) {
        super(name, description, required);
    }

    @Override
    public String parseValueImpl(String v) {
        if (! StringUtils.isEmpty(v)) {
            File f = new File(v);
            if (!f.isDirectory()) {
                return "'" + v + "' doesn't exist, or is not a directory.";
            }
            value = f;
            try {
                effectiveValue = f.getAbsoluteFile().getCanonicalFile();
            } catch (IOException e) {
                return "'" + v +"' can't be resolved to a canonical path.";
            }
        }

        return null;
    }

    @Override
    public File getEffectiveValue() {
        return effectiveValue;
    }
}
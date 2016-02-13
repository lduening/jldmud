/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import org.apache.commons.lang.StringUtils;

/**
 * Setting holding a number. The postfixes 'K', 'M' and 'G' (and their lower-case version)
 * are recognized.
 */
public class UnsignedNumberSetting extends SettingBase<Long> {

    private final String postfixes = "KMGkmg";

    public UnsignedNumberSetting(String name, String description, boolean required) {
        super(name, description, required);
    }

    public UnsignedNumberSetting(String name, String description, Long defaultValue) {
        super(name, description, defaultValue);
    }

    /* (non-Javadoc)
     * @see org.ldmud.jldmud.config.SettingBase#parseValueImpl(java.lang.String)
     */
    @Override
    protected String parseValueImpl(String v) {
        if (! StringUtils.isEmpty(v)) {
            long factor = 1L;
            String numbers = v;
            if (postfixes.contains(v.substring(v.length()-1))) {
                numbers = v.substring(0, v.length()-1);
                switch (v.charAt(v.length()-1)) {
                    case 'K':
                    case 'k':
                        factor = 1024L;
                        break;
                    case 'M':
                    case 'm':
                        factor = 1024L * 1024L;
                        break;
                    case 'G':
                    case 'g':
                        factor = 1024L * 1024L * 1024L;
                        break;
                }
            }

            long n = 0L;
            try {
                n = Long.parseUnsignedLong(numbers);
            } catch (NumberFormatException e) {
                return "'"+v+"' is not a recognized number: "+e;
            }

            value = n * factor;
        }
        return null;
    }

}

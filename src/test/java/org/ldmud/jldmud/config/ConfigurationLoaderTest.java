/**
 * Copyright (C) 2013 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link ConfigurationLoader}
 */
public class ConfigurationLoaderTest {

    @Test
    public void testPropertyExistenceValidation() {
        SettingBase<?> requiredProp = new DirectorySetting("mud.dir", "Description", true);
        SettingBase<?> requiredProp2 = new DirectorySetting("mud.dir2", "Description", true);
        SettingBase<?> optionalProp = new DirectorySetting("mud.opt", "Description", false);
        Properties properties;
        List<String> errors;

        // Check for missing properties
        properties = new Properties();
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for required properties with empty values
        properties = new Properties();
        properties.put(requiredProp.name, "");
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for optional properties
        properties = new Properties();
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        properties = new Properties();
        properties.put(optionalProp.name, "");
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        // Check for multiple missing required properties
        properties = new Properties();
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(requiredProp, requiredProp2));
        assertEquals(errors.size(), 2);

        // Check for proper passing of parse errors
        SettingBase<?> parseProp = new SettingBase<Integer>("mud.fail", "description", true) {
            @Override
            public String parseValueImpl(String v) {
                return "failure";
            }
        };
        properties = new Properties();
        properties.put("mud.fail", "foo");
        errors = new ConfigurationLoader().loadProperties(properties, new Properties(), makePropertyList(parseProp));
        assertEquals(errors.size(), 1);
        assertEquals(errors.get(0), "Setting 'mud.fail': failure");
    }

    @Test
    public void testPropertyOverride() {
        DirectorySetting requiredProp = new DirectorySetting("mud.dir", "Description", true);
        Properties properties;
        Properties overrideProperties;
        List<String> errors;

        // Check for existence override
        properties = new Properties();
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = new ConfigurationLoader().loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);

        // Check for value override
        properties = new Properties();
        properties.put(requiredProp.name, "doesn't exist");
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = new ConfigurationLoader().loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);
        assertEquals(requiredProp.value.toString(), ".");
    }

    @Test
    public void testDirectoryProperty() {
        DirectorySetting prop;
        String msg;

        prop = new DirectorySetting("mud.dir", "Description", false);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = new DirectorySetting("mud.dir", "Description", true);
        assertEquals(prop.describe(), "# "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = new DirectorySetting("mud.dir", "Description", new File("foo"));
        assertFalse(prop.required);
        assertNotNull(prop.defaultValue);
        assertEquals(prop.value, prop.defaultValue);
        assertNotNull(prop.effectiveValue);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=foo"+System.lineSeparator());

        prop = new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue(null);
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("");
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("doesn't exist");
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("LICENSE"); // Known existing file
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("."); // Current directory
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.effectiveValue);
    }

    @Test
    public void testGameDirectoryProperty() {
        GameDirectorySetting prop;
        String msg;

        DirectorySetting mudDirUnset = new DirectorySetting("mud.dir", "Description", false);
        DirectorySetting mudDir = new DirectorySetting("mud.dir", "Description", new File("."));

        prop = new GameDirectorySetting("mud.logdir", "Description", true, mudDir);
        assertEquals(prop.describe(), "# "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = new GameDirectorySetting("mud.logdir", "Description", new File("foo"), mudDir);
        assertFalse(prop.required);
        assertNotNull(prop.defaultValue);
        assertEquals(prop.value, prop.defaultValue);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=foo"+System.lineSeparator());

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue(null);
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("");
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("doesn't exist");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("LICENSE"); // Known existing file when a directory is desired
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("."); // Current directory
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("${mud.dir}/target");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.effectiveValue);
        assertFalse(prop.effectiveValue.toString().contains("${mud.dir}"));

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDirUnset);
        msg = prop.parseValue("${mud.dir}/target");
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = new GameDirectorySetting("mud.logdir", "Description", false, mudDir);
        msg = prop.parseValue("${mudder.dir}/target");
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);
    }

    @Test
    public void testUnsignedNumberProperty() {
        UnsignedNumberSetting prop;
        String msg;

        prop = new UnsignedNumberSetting("memory.reserve", "Description", true);
        assertEquals(prop.describe(), "# "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = new UnsignedNumberSetting("memory.reserve", "Description", 1234L);
        assertFalse(prop.required);
        assertNotNull(prop.defaultValue);
        assertEquals(prop.value, prop.defaultValue);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=1234"+System.lineSeparator());

        prop = new UnsignedNumberSetting("memory.reserve", "Description", false);

        msg = prop.parseValue("1234");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L), prop.getEffectiveValue());

        msg = prop.parseValue("1234k");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234K");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234m");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234M");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234g");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L * 1000L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234G");
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.getEffectiveValue());
        assertEquals(Long.valueOf(1234L * 1000L * 1000L * 1000L), prop.getEffectiveValue());

        msg = prop.parseValue("1234X");
        assertNotNull(msg);

        msg = prop.parseValue("-1234");
        assertNotNull(msg);
    }

    /**
     * Convenience method for type-safe creation of a property list.
     */
    private List<SettingBase<?>> makePropertyList(SettingBase<?>... props) {
        return Arrays.asList(props);
    }
}

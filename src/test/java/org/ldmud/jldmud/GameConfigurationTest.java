/**
 * Copyright (C) 2013 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.ldmud.jldmud.GameConfiguration.DirectorySetting;
import org.ldmud.jldmud.GameConfiguration.SettingBase;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for {@link GameConfiguration}
 */
public class GameConfigurationTest {

    @Test
    public void testPropertyRegistration() {
        GameConfiguration config = new GameConfiguration();
        config.allSettings = new ArrayList<>();
        DirectorySetting prop1 = config.new DirectorySetting("foo", "bar", true);
        assertNotNull(config.allSettings);
        assertEquals(config.allSettings.size(), 1);

        DirectorySetting prop2 = config.new DirectorySetting("foo2", "bar", true);
        assertEquals(config.allSettings.size(), 2);

        assertEquals(config.allSettings.get(0), prop1);
        assertEquals(config.allSettings.get(1), prop2);
    }

    @Test
    public void testPropertyExistenceValidation() {
        GameConfiguration config = new GameConfiguration();
        SettingBase<?> requiredProp = config.new DirectorySetting("mud.dir", "Description", true);
        SettingBase<?> requiredProp2 = config.new DirectorySetting("mud.dir2", "Description", true);
        SettingBase<?> optionalProp = config.new DirectorySetting("mud.opt", "Description", false);
        Properties properties;
        List<String> errors;

        // Check for missing properties
        properties = new Properties();
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for required properties with empty values
        properties = new Properties();
        properties.put(requiredProp.name, "");
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for optional properties
        properties = new Properties();
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        properties = new Properties();
        properties.put(optionalProp.name, "");
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        // Check for multiple missing required properties
        properties = new Properties();
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(requiredProp, requiredProp2));
        assertEquals(errors.size(), 2);

        // Check for proper passing of parse errors
        SettingBase<?> parseProp = config.new SettingBase<Integer>("mud.fail", "description", true) {
            @Override
            public String parseValueImpl(String v) {
                return "failure";
            }
        };
        properties = new Properties();
        properties.put("mud.fail", "foo");
        errors = new GameConfiguration().loadProperties(properties, new Properties(), makePropertyList(parseProp));
        assertEquals(errors.size(), 1);
        assertEquals(errors.get(0), "Setting 'mud.fail': failure");
    }

    @Test
    public void testPropertyOverride() {
        GameConfiguration config = new GameConfiguration();
        DirectorySetting requiredProp = config.new DirectorySetting("mud.dir", "Description", true);
        Properties properties;
        Properties overrideProperties;
        List<String> errors;

        // Check for existence override
        properties = new Properties();
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = new GameConfiguration().loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);

        // Check for value override
        properties = new Properties();
        properties.put(requiredProp.name, "doesn't exist");
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = new GameConfiguration().loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);
        assertEquals(requiredProp.value.toString(), ".");
    }

    @Test
    public void testDirectoryProperty() {
        GameConfiguration config = new GameConfiguration();
        DirectorySetting prop;
        String msg;

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = config.new DirectorySetting("mud.dir", "Description", true);
        assertEquals(prop.describe(), "# "+prop.description+System.lineSeparator()+prop.name+"="+System.lineSeparator());

        prop = config.new DirectorySetting("mud.dir", "Description", new File("foo"));
        assertFalse(prop.required);
        assertNotNull(prop.defaultValue);
        assertEquals(prop.value, prop.defaultValue);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=foo"+System.lineSeparator());

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue(null);
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("");
        assertNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("doesn't exist");
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("LICENSE"); // Known existing file
        assertNotNull(msg);
        assertNull(prop.value);
        assertNull(prop.effectiveValue);

        prop = config.new DirectorySetting("mud.dir", "Description", false);
        msg = prop.parseValue("."); // Current directory
        assertNull(msg);
        assertNotNull(prop.value);
        assertNotNull(prop.effectiveValue);
    }

    /**
     * Convenience method for type-safe creation of a property list.
     */
    private List<SettingBase<?>> makePropertyList(SettingBase<?>... props) {
        return Arrays.asList(props);
    }
}

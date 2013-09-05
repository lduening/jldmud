/**
 * Copyright (C) 2013 LDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.ldmud.jldmud.GameConfiguration.DirectoryProperty;
import org.ldmud.jldmud.GameConfiguration.PropertyBase;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for {@link GameConfiguration}
 */
public class GameConfigurationTest {

    @Test
    public void testPropertyRegistration() {
        PropertyBase.allProperties = new ArrayList<>();
        DirectoryProperty prop1 = new DirectoryProperty("foo", "bar", true);
        assertNotNull(PropertyBase.allProperties);
        assertEquals(PropertyBase.allProperties.size(), 1);

        DirectoryProperty prop2 = new DirectoryProperty("foo2", "bar", true);
        assertEquals(PropertyBase.allProperties.size(), 2);

        assertEquals(PropertyBase.allProperties.get(0), prop1);
        assertEquals(PropertyBase.allProperties.get(1), prop2);
    }

    @Test
    public void testPropertyExistenceValidation() {
        PropertyBase<?> requiredProp = new DirectoryProperty("mud.dir", "Description", true);
        PropertyBase<?> requiredProp2 = new DirectoryProperty("mud.dir2", "Description", true);
        PropertyBase<?> optionalProp = new DirectoryProperty("mud.opt", "Description", false);
        Properties properties;
        List<String> errors;

        // Check for missing properties
        properties = new Properties();
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for required properties with empty values
        properties = new Properties();
        properties.put(requiredProp.name, "");
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(requiredProp));
        assertEquals(errors.size(), 1);

        // Check for optional properties
        properties = new Properties();
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        properties = new Properties();
        properties.put(optionalProp.name, "");
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(optionalProp));
        assertEquals(errors.size(), 0);

        // Check for multiple missing required properties
        properties = new Properties();
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(requiredProp, requiredProp2));
        assertEquals(errors.size(), 2);

        // Check for proper passing of parse errors
        PropertyBase<?> parseProp = new PropertyBase<Integer>("mud.fail", "description", true) {
            @Override
            public String parseValue(String v) {
                return "failure";
            }
        };
        properties = new Properties();
        properties.put("mud.fail", "foo");
        errors = GameConfiguration.loadProperties(properties, new Properties(), makePropertyList(parseProp));
        assertEquals(errors.size(), 1);
        assertEquals(errors.get(0), "Property 'mud.fail': failure");
    }

    @Test
    public void testPropertyOverride() {
        DirectoryProperty requiredProp = new DirectoryProperty("mud.dir", "Description", true);
        Properties properties;
        Properties overrideProperties;
        List<String> errors;

        // Check for existence override
        properties = new Properties();
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = GameConfiguration.loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);

        // Check for value override
        properties = new Properties();
        properties.put(requiredProp.name, "doesn't exist");
        overrideProperties = new Properties();
        overrideProperties.put(requiredProp.name, ".");
        errors = GameConfiguration.loadProperties(properties, overrideProperties, makePropertyList(requiredProp));
        assertEquals(errors.size(), 0);
        assertEquals(requiredProp.value.toString(), ".");
    }

    @Test
    public void testDirectoryProperty() {
        DirectoryProperty prop;
        String msg;

        prop = new DirectoryProperty("mud.dir", "Description", false);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=");

        prop = new DirectoryProperty("mud.dir", "Description", true);
        assertEquals(prop.describe(), "# "+prop.description+System.lineSeparator()+prop.name+"=");

        prop = new DirectoryProperty("mud.dir", "Description", new File("foo"));
        assertFalse(prop.required);
        assertNotNull(prop.defaultValue);
        assertEquals(prop.value, prop.defaultValue);
        assertEquals(prop.describe(), "# Optional: "+prop.description+System.lineSeparator()+prop.name+"=foo");

        prop = new DirectoryProperty("mud.dir", "Description", false);

        msg = prop.parseValue(null);
        assertNull(msg);
        assertNull(prop.value);

        msg = prop.parseValue("");
        assertNull(msg);
        assertNull(prop.value);

        msg = prop.parseValue("doesn't exist");
        assertNotNull(msg);
        assertNull(prop.value);

        msg = prop.parseValue("LICENSE"); // Known existing file
        assertNotNull(msg);
        assertNull(prop.value);

        msg = prop.parseValue("."); // Current directory
        assertNull(msg);
        assertNotNull(prop.value);
    }

    /**
     * Convenience method for type-safe creation of a property list.
     */
    private List<PropertyBase<?>> makePropertyList(PropertyBase<?>... props) {
        return Arrays.asList(props);
    }
}

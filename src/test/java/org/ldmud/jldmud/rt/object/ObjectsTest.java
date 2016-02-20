/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link Objects}.
 */
public class ObjectsTest {

    @Test
    public void testEmptyObjects() {
        Objects objects = new Objects();

        assertNull(objects.find(10L));
        assertNull(objects.find("foo"));
        assertEquals(0, objects.getDestroyedObjects().size());
    }

    @Test
    public void testObjectLifeCycle() {
        final String NAME = "foo";
        Objects objects = new Objects();

        MudObject obj = objects.createObject(NAME);
        assertNotEquals(Objects.INVALID_ID, obj.getId());
        assertEquals(obj, objects.find(obj.getId()));
        assertEquals(obj, objects.find(obj.getName()));
        assertEquals(0, objects.getDestroyedObjects().size());

        obj.destroy();
        assertNull(objects.find(obj.getId()));
        assertNull(objects.find(obj.getName()));
        assertEquals(1, objects.getDestroyedObjects().size());

        objects.removeDestroyedObjects();
        assertEquals(0, objects.getDestroyedObjects().size());
    }
}

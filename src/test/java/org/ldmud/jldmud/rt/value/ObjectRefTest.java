/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.value;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.ldmud.jldmud.rt.object.MudObject;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ObjectRef}.
 */
public class ObjectRefTest {

    @Test
    public void testLifeCycle() {
        ObjectRef ref;

        ref = new ObjectRef(null);
        assertNull(ref.get());

        MudObject obj = mock(MudObject.class);
        ref = new ObjectRef(obj);

        when(obj.isDestroyed()).thenReturn(false);
        assertEquals(obj, ref.get());

        when(obj.isDestroyed()).thenReturn(true);
        assertNull(ref.get());
        when(obj.isDestroyed()).thenThrow(new IllegalStateException("The obj reference should have been dropped."));
        assertNull(ref.get());
    }
}

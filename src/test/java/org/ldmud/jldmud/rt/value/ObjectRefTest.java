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
import org.ldmud.jldmud.rt.object.MudObject.Ref;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ObjectRef}.
 */
public class ObjectRefTest {

    @Test
    public void testLifeCycle() {
        ObjectRef ref;

        ref = new ObjectRef((MudObject)null);
        assertNull(ref.get());

        ref = new ObjectRef((Ref)null);
        assertNull(ref.get());

        MudObject obj = mock(MudObject.class);
        when(obj.ref()).thenReturn(new MudObject.Ref(obj));
        ref = new ObjectRef(obj);

        when(obj.isDestroyed()).thenReturn(false);
        assertEquals(obj, ref.get());

        when(obj.isDestroyed()).thenReturn(true);
        assertNull(ref.get());
        when(obj.isDestroyed()).thenThrow(new IllegalStateException("The obj reference should have been dropped."));
        assertNull(ref.get());

        obj = mock(MudObject.class);
        MudObject.Ref objRef = new MudObject.Ref(obj);
        ref = new ObjectRef(objRef);

        when(obj.isDestroyed()).thenReturn(false);
        assertEquals(obj, ref.get());

        when(obj.isDestroyed()).thenReturn(true);
        assertNull(ref.get());
        when(obj.isDestroyed()).thenThrow(new IllegalStateException("The obj reference should have been dropped."));
        assertNull(ref.get());
    }
}

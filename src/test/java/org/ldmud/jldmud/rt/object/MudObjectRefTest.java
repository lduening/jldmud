/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.ldmud.jldmud.rt.object.MudObject;
import org.ldmud.jldmud.rt.object.MudObject.Ref;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link MudObject.Ref}.
 */
public class MudObjectRefTest {

    @Test
    public void testLifeCycle() {
        Ref ref;

        ref = new Ref((MudObject)null);
        assertNull(ref.get());

        MudObject obj = mock(MudObject.class);
        when(obj.ref()).thenReturn(new MudObject.Ref(obj));
        ref = new Ref(obj);

        when(obj.isDestroyed()).thenReturn(false);
        assertEquals(obj, ref.get());

        when(obj.isDestroyed()).thenReturn(true);
        assertNull(ref.get());
        when(obj.isDestroyed()).thenThrow(new IllegalStateException("The obj reference should have been dropped."));
        assertNull(ref.get());
    }
}

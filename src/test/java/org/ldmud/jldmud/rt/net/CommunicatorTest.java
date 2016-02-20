/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.net;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;

import org.ldmud.jldmud.rt.object.MudObject;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Commmunicator}.
 */
public class CommunicatorTest {

    @Test
    public void testInteractiveClassification() {
        Communicator communicator = new Communicator();

        Interactive interactive = mock(Interactive.class);

        when(interactive.getState()).thenReturn(Interactive.State.NEW);
        assertTrue(communicator.isInteractivePending(interactive));

        when(interactive.getState()).thenReturn(Interactive.State.CONNECTION_LOST);
        assertTrue(communicator.isInteractivePending(interactive));

        when(interactive.getState()).thenReturn(Interactive.State.CLOSED);
        assertFalse(communicator.isInteractivePending(interactive));

        when(interactive.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive.isDataPending()).thenReturn(false);
        assertFalse(communicator.isInteractivePending(interactive));

        when(interactive.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive.isDataPending()).thenReturn(true);
        assertTrue(communicator.isInteractivePending(interactive));
    }

    @Test
    public void testInteractiveLifecycle() {
        Communicator communicator = new Communicator();

        MudObject obj = mock(MudObject.class);
        when(obj.ref()).thenReturn(new MudObject.Ref(obj));

        Interactive interactive1 = mock(Interactive.class);
        Interactive interactive2 = mock(Interactive.class);
        when(interactive1.getName()).thenReturn("Interactive #1");
        when(interactive2.getName()).thenReturn("Interactive #2");

        communicator.add(interactive1);
        communicator.add(interactive2);

        // No data pending
        when(interactive1.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive1.isDataPending()).thenReturn(false);
        when(interactive2.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive2.isDataPending()).thenReturn(false);

        assertFalse(communicator.areInteractivesPending());
        assertNull(communicator.nextPendingInteractive());

        // Interactive 2 has data pending
        when(interactive1.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive1.isDataPending()).thenReturn(false);
        when(interactive2.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive2.isDataPending()).thenReturn(true);

        assertTrue(communicator.areInteractivesPending());
        assertEquals(communicator.nextPendingInteractive(), interactive2);
        when(interactive2.isDataPending()).thenReturn(false);
        assertNull(communicator.nextPendingInteractive());

        // Interactive 2 closed
        when(interactive1.getState()).thenReturn(Interactive.State.ACTIVE);
        when(interactive1.isDataPending()).thenReturn(false);
        when(interactive2.getState()).thenReturn(Interactive.State.CLOSED);

        assertFalse(communicator.areInteractivesPending());
        assertNull(communicator.nextPendingInteractive()); // Removes interactive2
        assertFalse(communicator.getAllInteractives().contains(interactive2));
    }
}

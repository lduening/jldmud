/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.net;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link Commmunicator}.
 */
public class CommunicatorTest {

    @Test
    public void testInteractiveLifecycle() {
        Communicator communicator = new Communicator();

        Interactive interactive = new Interactive(communicator);
        communicator.add(interactive);

        assertFalse(communicator.isInteractivesPending());
        assertNull(communicator.nextPendingInteractive());
        assertFalse(interactive.isQueuedForProcessing());

        communicator.addPendingInteractive(interactive);

        assertTrue(interactive.isQueuedForProcessing());

        communicator.addPendingInteractive(interactive);

        assertTrue(communicator.isInteractivesPending());

        Interactive i = communicator.nextPendingInteractive();

        assertEquals(i, interactive);
        assertFalse(interactive.isQueuedForProcessing());

        assertFalse(communicator.isInteractivesPending());
        assertNull(communicator.nextPendingInteractive());

        communicator.addPendingInteractive(interactive);

        interactive.remove();

        assertFalse(interactive.isQueuedForProcessing());
        assertFalse(communicator.isInteractivesPending());
        assertNull(communicator.nextPendingInteractive());
    }
}

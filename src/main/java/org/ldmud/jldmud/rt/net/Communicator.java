/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

/**
 * This class handles the telnet network I/O, and manages the {@code Interactive} instances.
 */
@Singleton
public class Communicator {
    private Logger log = LogManager.getLogger(this.getClass());

    // All known {@link Interactive} instances, kept in a ring-buffer for fair processing.
    // TODO: Maybe use two queues (processed and unprocessed), and switch them around double buffer style
    private Queue<Interactive> allInteractives = new LinkedList<>();

    /**
     * Constructor
     */
    Communicator() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @return {@code true} if there is an {@link Interactive} with data or events to process.
     */
    public synchronized boolean areInteractivesPending() {
        for (Iterator<Interactive> iter = allInteractives.iterator(); iter.hasNext(); ) {
            Interactive interactive = iter.next();
            if (isInteractivePending(interactive)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Find the next {@link Interactive} with data or events pending, while also removing
     * Interactives in state 'CLOSED'.
     *
     * @return The next {@link Interactive} pending for processing, of {@code null} if there is none.
     */
    public synchronized Interactive nextPendingInteractive() {
        Interactive firstNotProcessed = null; // To prevent an endless loop
        Interactive rc = null;
        while (rc == null && allInteractives.peek() != null && allInteractives.peek() != firstNotProcessed) {
            Interactive i = allInteractives.poll();
            if (i.getState() == Interactive.State.CLOSED) {
                log.debug("Removing interactive {}", i);
            } else if (isInteractivePending(i)) {
                rc = i;
            } else {
                allInteractives.add(i);
                if (firstNotProcessed == null) {
                    firstNotProcessed = i;
                }
            }
        }

        if (rc != null) {
            log.debug("Next interactive to process: {}", rc);
        } else {
            log.debug("No interactive pending for processing.");
        }

        return rc;
    }

    /**
     * Test if an {@code Interactive} is in need of processing data or events.
     *
     * @param interactive The {@code Interactive} to test.
     * @return {@code true} if the interactive has something to process.
     */
    boolean isInteractivePending(Interactive interactive) {
        if (interactive.getState() == Interactive.State.ACTIVE) {
            return interactive.isDataPending();
        }
        if (interactive.getState() != Interactive.State.CLOSED) {
            return true;
        }

        return false;
    }

    /**
     * @param interactive A new {@link Interactive} (with active connection) to add.
     */
    public synchronized void add (Interactive interactive) {
        allInteractives.add(interactive);
    }

    /**
     * Close the connection on an {@link Interactive} instance and remove it from the internal lists.
     *
     * @param interactiveThe {@link Interactive} to remove.
     */
    public synchronized void remove(Interactive interactive) {
        log.info("Removing interactive {}", interactive);
        // TODO: Close the connection
        interactive.setState(Interactive.State.CLOSED);
        // It will be removed from the allInteractives list on the next round-robin.
    }

    /**
     * Shutdown all connections still open. This is called when the game is being shutdown,
     * and no new events will be processed.
     */
    public synchronized void shutdown () {
        log.info("Shutting down all remaining connections");
        for (Interactive i : allInteractives) {
            // TODO: Close the connection
            i.setState(Interactive.State.CLOSED);
        }
        allInteractives.clear();
    }

    /**
     * @return The queue of all {@link Interactive} instances.
     */
    Queue<Interactive> getAllInteractives() {
        return allInteractives;
    }


}

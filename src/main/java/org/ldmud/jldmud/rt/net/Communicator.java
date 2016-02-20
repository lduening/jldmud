/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.net;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

/**
 * This class handles the telnet network I/O, and manages the {@code Interactive} instances.
 */
@Singleton
public class Communicator {
    private Logger log = LogManager.getLogger(this.getClass());

    // The set of all active {@link Interactive} instances.
    private Set<Interactive> allInteractives = new HashSet<>();

    // A queue of {@link Interactive} instances with pending events.
    private Queue<Interactive> pendingInteractives = new LinkedList<>();

    /**
     * Constructor
     */
    public Communicator() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @return {@code true} if there is an {@link Interactive} pending for processing.
     */
    public synchronized boolean isInteractivesPending() {
        return pendingInteractives.peek() != null;
    }

    /**
     * @return The next {@link Interactive} pending for processing, of {@code null} if there is none.
     */
    public synchronized Interactive nextPendingInteractive() {
        Interactive rc = pendingInteractives.poll();
        if (rc != null) {
            log.debug("Next interactive to process: #{} '{}'", rc.getId(), rc.getObjLogName());
            rc.setQueuedForProcessing(false);
        } else {
            log.debug("No interactive pending for processing.");
        }
        return rc;
    }

    /**
     * Add an {@link Interactive} to the list of those pending for processing.
     * Repeated calls for the same instance have no effect.
     *
     * @param interactive The {@link Interactive} to queue for processing.
     */
    public synchronized void addPendingInteractive(Interactive interactive) {
        if (! interactive.isQueuedForProcessing()) {
            interactive.setQueuedForProcessing(true);
            pendingInteractives.add(interactive);
        }
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
        log.info("Removing interactive #{} '{}'", interactive.getId(), interactive.getObjLogName());
        // TODO: Close the connection
        allInteractives.remove(interactive);
        pendingInteractives.remove(interactive);
        interactive.setQueuedForProcessing(false);
    }

    /**
     * Shutdown all connections still open. This is called when the game is being shutdown,
     * and no new events will be processed.
     */
    public synchronized void shutdown () {
        log.info("Shutting down all remaining connections");
        for (Interactive i : allInteractives) {
            // TODO: Close the connection
        }
        allInteractives.clear();
        pendingInteractives.clear();
    }
}

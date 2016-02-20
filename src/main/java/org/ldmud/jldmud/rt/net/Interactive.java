/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.net;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.rt.object.MudObject;

/**
 * Instances of the Interactive class provide the connection between
 * and interactive game object, and its telnet connection.
 */
public class Interactive {
    private final Logger log = LogManager.getLogger(this.getClass());

    // Incrementing ID, assigned to new instances.
    private static AtomicLong currentInteractiveId = new AtomicLong();

    // The {@link Communicator} handling this instance.
    private Communicator communicator;

    // The instance id, uniquely identifying the instance.
    private long id;

    // The game object this instance is associated with. If {@code null},
    // the connection had been newly accepted, and this instance needs
    // to be associated with a game object next.
    private MudObject mudObject;

    // For logging: a string identifying the object this instance is associated with.
    private String objLogName;

    // {@code true}: The instance is queued for processing by the communications module.
    private boolean queuedForProcessing;

    /**
     * Construct this instance.
     *
     * @param communicator The {@link Communicator} to handle this instance.
     */
    public Interactive(Communicator communicator) {
        super();
        id = currentInteractiveId.incrementAndGet();
        objLogName = "-";
        this.communicator = communicator;
    }

    /**
     * The game object drops this network connection - close the connection and release
     * resources.
     */
    public void remove() {
        communicator.remove(this);
        mudObject = null;
        objLogName = "-";

    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interactive other = (Interactive) obj;
        if (id != other.id)
            return false;
        return true;
    }

    /**
     * @return the id of this instance.
     */
    public long getId() {
        return id;
    }

    /**
     * @return The associated game object.
     */
    public MudObject getMudObject() {
        return mudObject;
    }

    /**
     * @param obj The associated game object.
     */
    public void setMudObject(MudObject obj) {
        this.mudObject = obj;
        if (obj == null) {
            objLogName = "-";
        } else {
            objLogName = obj.getName();
        }
    }

    /**
     * @return the associated object name used for logging.
     */
    public String getObjLogName() {
        return objLogName;
    }

    /**
     * @return {@code true} if this instance is queued for processing.
     */
    public boolean isQueuedForProcessing() {
        return queuedForProcessing;
    }

    /**
     * @param queuedForProcessing The new flag value.
     */
    public void setQueuedForProcessing(boolean queuedForProcessing) {
        this.queuedForProcessing = queuedForProcessing;
    }
}

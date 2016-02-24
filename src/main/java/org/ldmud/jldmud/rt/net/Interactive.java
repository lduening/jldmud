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
public class Interactive /* implements Shell */ {
    private final Logger log = LogManager.getLogger(this.getClass());

    // Incrementing ID, assigned to new instances.
    private static AtomicLong currentInteractiveId = new AtomicLong();

    // The {@link Communicator} handling this instance.
    private Communicator communicator;

    // The instance id, uniquely identifying the instance, used to
    // provide a hashcode.
    private long id;

    // The game object this instance is associated with. If {@code null},
    // the connection had been newly accepted, and this instance needs
    // to be associated with a game object next.
    private MudObject.Ref mudObject;

    /**
     * The state of the interactive.
     */
    enum State {
        NEW,             // A new interactive, not yet associated with a mud object
        ACTIVE,          // Active interactive
        CONNECTION_LOST, // Active interactive which lost connection
        CLOSED           // Inactive interactive which hasn't been purged yet.
    };
    private State state;

    // A pretty name for this instance
    private String name;

    /**
     * Construct this instance.
     *
     * @param communicator The {@link Communicator} to handle this instance.
     */
    public Interactive(Communicator communicator) {
        super();
        id = currentInteractiveId.incrementAndGet();
        this.communicator = communicator;
        calculateName();
    }

    /**
     * The game object drops this network connection - close the connection and release
     * resources, and disassociate this instance from its owning game object (if any).
     */
    public void remove() {
        log.debug("Destroyed interactive #{} '{}'", this.id, this.name);
        communicator.remove(this);
        if (MudObject.getObject(mudObject) != null) {
            MudObject.getObject(mudObject).setInteractive(null);
        }
        mudObject = null;
        calculateName();
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name+" [state=" + state + "]";
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
        return mudObject.get();
    }

    /**
     * @param obj The associated game object.
     */
    public void setMudObject(MudObject.Ref obj) {
        this.mudObject = obj;
        calculateName();
    }


    /**
     * @return the state of the interactive
     */
    public State getState() {
        return state;
    }

    /**
     * @param state The new state of the interactive
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Calculate and set the name of this instance.
     */
    private void calculateName() {
        if (MudObject.isAlive(mudObject)) {
            name = "Interactive #"+id+" ("+mudObject.get().getName()+")";
        } else {
            name = "Interactive #"+id+" (-)";
        }
    }
    /**
     * @return The name of this instance.
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@code true} if there is data pending for processing.
     */
    public boolean isDataPending() {
        return false;
    }
}

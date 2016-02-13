/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import java.lang.ref.WeakReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base of every mud object.
 *
 * Mud objects are uniquely identified by their numeric id.
 *
 * To simulate the destruction of a MudObject, it is first set to be 'logically' destroyed so that
 * in can no longer be accessed, but all actual cleanup work is delayed until the end of the game cycle.
 *
 * TODO: If we allow objects to be persisted to disk and temporarily removed
 * from memory, we also need to keep the object's id in the ObjectRef, so that the object
 * can be loaded again. Alternatively, this little base object always remains in memory, and the payload
 * is held in separate Java objects (which would give us better modularization).
 */
public class MudObject {
    private Logger log;

    // Reference to the
    // The ID number, uniquely identifying the object.
    private long id;

    // The name by which this object can be found.
    private String name;

    // If {@code true}, the object was logically destructed, but not yet removed from the game.
    private boolean destroyed;

    /**
     * @param id The id of this object
     * @param name The name of this object.
     */
    public MudObject(long id, String name) {
        super();
        this.id = id;
        this.name = name;
        this.log = LogManager.getLogger(this.getClass()+"-"+id);
        this.log.debug("Created object #{} '{}'", this.id, this.name);
    }

    /**
     * Transform the object to be logically destroyed.
     */
    public void destroy() {
        log.debug("Destroyed object #{} '{}'", this.id, this.name);
        destroyed = true;
        // TODO: Additional cleanup
    }

    /**
     * @return A {@link #Ref} to this object.
     */
    public MudObject.Ref ref() {
        return new MudObject.Ref(this);
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
        MudObject other = (MudObject) obj;
        if (id != other.id)
            return false;
        return true;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return The name of the object
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@code true} if the object is logically destroyed, but not yet deallocated.
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * This Reference to a MudObject is the preferred way to keep references around,
     * as they automatically handle deallocated and destroyed MudObjects.
     */
    public static class Ref {

        // The MudObject reference. If {@code null}, a previous call to get() discovered
        // that the object no longer exists, and nulled itself out.
        private WeakReference<MudObject> objectRef;

        /**
         * Object constructor
         */
        public Ref(MudObject obj) {
            if (obj != null) {
                objectRef = new WeakReference<>(obj);
            }
        }

        /**
         * @return The MudObject referenced, or {@code null} if the object no longer exists.
         */
        public MudObject get() {
            MudObject obj = null;
            if (objectRef != null) {
                obj = objectRef.get();
                if (obj == null) {
                    objectRef = null;
                } else if (obj.isDestroyed()) {
                    objectRef = null;
                    obj = null;
                }
            }

            return obj;
        }
    }
}

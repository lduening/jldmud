/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.rt.net.Interactive;

/**
 * The base of every mud object.
 *
 * Mud objects are uniquely identified by their numeric id.<p>
 *
 * Unlike Java, mud objects can exist even without anything referencing them. Therefore, in order to
 * get rid of them, they can be 'destroyed'. This is simulated by logically destroying them (setting
 * a flag) so that all accessing entities can see that the mud object no longer exists. In addition, the
 * (almost) only hard references to MudObject instances are from the {@link MudObjects} look-up tables;
 * all other references are through {@link MudObject.Ref}, which wraps the MudObject into a {@link WeakReference},
 * and also transparently honors the 'destroyed' flag. This way, a destroyed MudObject can be GCed
 * once the hard references from {@link MudObjects} are gone, without having to hunt down all the other
 * references; the downside is that holders of {@link MudObject.Ref} will have to lazily clean up
 * their data structures as they discovery the deceased objects.<p>
 *
 * A difficulty however is that a MudObject might be destroyed as part of its own program running, and
 * in that case its variables and program code still needs to be accessible. For this reason, the
 * destruction process actually involves two steps: in the first, the MudObject is flagged as 'destroyed'
 * and removed from the lookup tables, but added to a dedicated list of 'newly destructed objects'.
 * Once the current program execution ends, the MudObject is removed from that list and all its
 * remaining resources are being released.
 *
 * TODO: If we allow objects to be persisted to disk and temporarily removed
 * from memory, we also need to keep the object's id in the ObjectRef, so that the object
 * can be loaded again. Alternatively, this little base object always remains in memory, and the payload
 * is held in separate Java objects (which would give us better modularization).
 */
public class MudObject {
    private Logger log;

    // Incrementing ID, assigned to new instances.
    private static AtomicLong currentInteractiveId = new AtomicLong();

    // The ID number, uniquely identifying the object.
    private long id;

    // The name by which this object can be found.
    // TODO: LDMud distinguishes load_name and current name, to enable 'virtual' objects
    private String name;

    // The associated network connection
    private Interactive interactive;

    // If {@code true}, the object was logically destructed, but not yet removed from the game.
    private boolean destroyed;

    // If {@code true}, the object was at some point associated with a network connection.
    private boolean onceInteractive;

    // Modules used by this class
    private MudObjects objects;

    /**
     * @param name The name of this object.
     * @param objecst The {@link MudObjects} instance holding this instance.
     */
    public MudObject(String name, MudObjects objects) {
        super();
        this.objects = objects;
        this.id = currentInteractiveId.incrementAndGet();
        this.name = name;
        this.log = LogManager.getLogger(this.getClass()+"-"+id);
        this.log.debug("Created object #{} '{}'", this.id, this.name);
    }

    /**
     * Transform the object to be logically destroyed.
     */
    public void destroy() {
        log.debug("Destroyed object #{} '{}'", this.id, this.name);
        if (interactive != null) {
            interactive.remove();
            interactive = null;
        }
        // TODO: Additional cleanup
        objects.destroyObject(this);
        destroyed = true;
    }

    /**
     * Outside of an execution, fully remove a destroyed object from the system.<p>
     * This method is called from {@link MudObjects}, so those links are being taken
     * care of.
     */
    public void remove() {
        Validate.isTrue(destroyed, "remove() called on a live object");
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
     * @return The {@link Interactive} instance for this object's network connection.
     */
    public Interactive getInteractive() {
        return interactive;
    }

    /**
     * @param interactive The {@link Interactive} instance for this object's network connection.
     */
    public void setInteractive(Interactive interactive) {
        this.interactive = interactive;
        if (interactive != null) {
            onceInteractive = true;
        }
    }

    /**
     * @return {@code true} if the object is logically destroyed, but not yet deallocated.
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * @return {@code true} if the object was or is associated with a network connection.
     */
    public boolean isOnceInteractive() {
        return onceInteractive;
    }


    /**
     * @return {@code true} if the object currently is associated with a network connection.
     */
    public boolean isInteractive() {
        return onceInteractive;
    }

    /**
     * @param onceInteractive the onceInteractive to set
     */
    public void setOnceInteractive(boolean onceInteractive) {
        this.onceInteractive = onceInteractive;
    }

    /**
     * Helper method: Return a reference to a live object.
     *
     * @param obj The object reference to evaluate.
     * @return {@code obj} if it references a live object, or {@code null} otherwise.
     */
    public static MudObject getObject(MudObject obj) {
        return obj != null && !obj.isDestroyed() ? obj : null;
    }

    /**
     * Helper method: Return a reference to a live object.
     *
     * @param obj The object reference to evaluate.
     * @return {@code obj.get()} if it references a live object, or {@code null} otherwise.
     */
    public static MudObject getObject(MudObject.Ref obj) {
        return obj != null && obj.get() != null ? obj.get() : null;
    }

    /**
     * Helper method: Test if a reference points to a live object.
     *
     * @param obj The object reference to evaluate.
     * @return {@code true} if it references a live object.
     */
    public static boolean isAlive(MudObject obj) {
        return obj != null && !obj.isDestroyed();
    }

    /**
     * Helper method: Test if a reference points to a live object.
     *
     * @param obj The object reference to evaluate.
     * @return {@code true} if it references a live object.
     */
    public static boolean isAlive(MudObject.Ref obj) {
        return obj != null && obj.get() != null;
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

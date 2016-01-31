/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.value;

import java.lang.ref.WeakReference;

import org.ldmud.jldmud.rt.object.MudObject;

/**
 * A reference to a {@link MudObject} to be held by other MudObjects.<p>
 *
 * Since MudObjects can be destroyed while other objects are still holding
 * references, the reference is implement with a weak link, to allow the
 * destroyed MudObject to be garbage-collected.
 */
public class ObjectRef implements Value<MudObject> {

    // The MudObject reference. If {@code null}, a previous call to get() discovered
    // that the object no longer exists, and nulled itself out.
    private WeakReference<MudObject> objectRef;

    /**
     * Object constructor
     */
    public ObjectRef(MudObject obj) {
        objectRef = new WeakReference<>(obj);
    }

    /**
     * @return The MudObject referenced, or {@code null} if the object no longer exists.
     */
    @Override
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

/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.value;

import org.ldmud.jldmud.rt.object.MudObject;

/**
 * A reference to a {@link MudObject} to be held by other MudObjects.<p>
 */
public class ObjectRef implements Value<MudObject> {

    // The MudObject reference. If {@code null}, a previous call to get() discovered
    // that the object no longer exists, and nulled itself out.
    private MudObject.Ref objectRef;

    /**
     * Constructor
     *
     * @param obj The MudObject to hold a reference to.
     */
    public ObjectRef(MudObject obj) {
        if (obj != null) {
            objectRef = obj.ref();
        }
    }

    /**
     * Constructor
     *
     * @param objRef The MudObject.Ref to hold.
     */
    public ObjectRef(MudObject.Ref objRef) {
        objectRef = objRef;
    }

    /**
     * Copy Constructor
     */
    @Override
    public ObjectRef copy() {
        return new ObjectRef(this.objectRef);
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
            }
        }

        return obj;
    }
}

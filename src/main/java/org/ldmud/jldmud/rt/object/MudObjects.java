/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Singleton class to create empty {@link MudObject}, and track them by id and name(s).
 *
 * TOOD: Maybe introduce a static 'Destroyed' object so that we don't use null references?
 */
@Singleton
public class MudObjects {
    private Logger log = LogManager.getLogger(this.getClass());

    /**
     * ID value to be used for by-id mud object references no longer referencing actual objects.
     */
    public static final long INVALID_ID = 0L;

    // Tables of all active objects.
    private Map<Long, MudObject> objectById = new HashMap<>();
    private Map<String, MudObject> objectByName = new HashMap<>();

    // List of newly destructed objects which still need to be cleaned up.
    private List<MudObject> destroyedObjects = new LinkedList<>();

    /**
     * Default constructor
     */
    @Inject
    MudObjects() {
        super();
    }

    /**
     * Create a new object with a given name.
     *
     * @param name The desired name of the object.
     * @return The initialized object.
     */
    public MudObject createObject (String name) {
        Validate.isTrue(!objectByName.containsKey(name), "Desired object name already exists: ", name);
        MudObject obj = new MudObject(name, this);
        objectById.put(obj.getId(), obj);
        objectByName.put(obj.getName(), obj);

        return obj;
    }

    /**
     * Removed an object from the lookup tables and schedule it for final removal.
     *
     * @param obj Object being destroyed.
     */
    public void destroyObject(MudObject obj) {
        synchronized (obj) {
            if (!obj.isDestroyed()) {
                destroyedObjects.add(obj);
                objectById.remove(obj.getId());
                objectByName.remove(obj.getName());
            }
        }
    }

    /**
     * Remove all previously destroyed objects.
     */
    public void removeDestroyedObjects() {
        for (MudObject obj : destroyedObjects) {
            obj.remove();
        }
        destroyedObjects.clear();
    }

    /**
     * Find an object by its id.
     *
     * @param id The object's id.
     * @return The object, or {@code null} if not found.
     */
    public MudObject find(long id) {
        return objectById.get(id);
    }

    /**
     * Find an object by its name.
     *
     * @param name The object's name.
     * @return The object, or {@code null} if not found.
     */
    public MudObject find(String name) {
        return objectByName.get(name);
    }

    /**
     * @return The ID -> Object map.
     */
    Map<Long, MudObject> getObjectById() {
        return objectById;
    }

    /**
     * @return The Name -> Object map.
     */
    Map<String, MudObject> getObjectByName() {
        return objectByName;
    }

    /**
     * @return The list of destroyed objects.
     */
    List<MudObject> getDestroyedObjects() {
        return destroyedObjects;
    }
}

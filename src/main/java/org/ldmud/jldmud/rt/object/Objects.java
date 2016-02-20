/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.object;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

/**
 * Singleton bean handling all the objects in the game.
 *
 * TOOD: Maybe introduce a static 'Destroyed' object so that we don't use null references?
 */
@Singleton
public class Objects {
    private Logger log = LogManager.getLogger(this.getClass());

    /**
     * ID value to be used for by-id mud object references no longer referencing actual objects.
     */
    public static final long INVALID_ID = 0L;

    // Tables of all active objects.
    private Map<Long, MudObject> objectById = new HashMap<>();
    private Map<String, MudObject> objectByName = new HashMap<>();

    // List of objects logically destroyed, but not yet processed for deletion.
    private Queue<MudObject> destroyedObjects = new LinkedList<>();

    /**
     * Default constructor
     */
    Objects() {
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
        MudObject obj = new MudObject(name);
        objectById.put(obj.getId(), obj);
        objectByName.put(obj.getName(), obj);

        return obj;
    }

    /**
     * Destroy an object logically, and schedule it for final processing.
     *
     * @param obj Object to be destroyed.
     */
    public void destroyObject(MudObject obj) {
        synchronized (obj) {
            if (!obj.isDestroyed()) {
                obj.destroy();
                objectById.remove(obj.getId());
                objectByName.remove(obj.getName());
                destroyedObjects.add(obj);
            }
        }
    }

    /**
     * Return the next object queued for destruction processing, if there is one.
     *
     * @return The next object for destruction processing, or {@code null}.
     */
    public MudObject getNextDestroyedObject() {
        return destroyedObjects.poll();
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
     * @return The list of logically destroyed objects queued for processing.
     */
    Queue<MudObject> getDestroyedObjects() {
        return destroyedObjects;
    }
}

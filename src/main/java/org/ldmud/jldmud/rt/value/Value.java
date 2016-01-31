/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt.value;

/**
 * Definition of the basic operation of a mud value.
 */
public interface Value<T> {

    /**
     * @return the base value held.
     */
    public T get();
}

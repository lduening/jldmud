/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt;

import java.lang.ref.SoftReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.config.Configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class allocates a memory buffer of configurable size and holds a soft reference to it.
 * If this reference is cleared by the GC, the Mud is running out of memory and shut perform
 * a graceful shutdown.<p/>
 *
 * The size of the buffer is configurable; a size of '0' means that no buffer is being allocated.
 */
@Singleton
public class MemoryReserve {
    private Logger log = LogManager.getLogger(this.getClass());

    private final Configuration config;

    /**
     * The memory buffer. If {@code null}, no memory was configured to be reserved.
     */
    private SoftReference<byte[]> buffer;

    /**
     * Constructor
     *
     * @param config The {@link Configuration} instance.
     */
    @Inject
    public MemoryReserve(Configuration config) {
        super();
        this.config = config;
    }

    /**
     * Reserve the memory.
     */
    public void reserve() {
        if (config.getMemoryReserve() != 0L) {
            byte[] buf = new byte[config.getMemoryReserve().intValue() * 1000000];
            buffer = new SoftReference<>(buf);
            log.info("Reserved {} MB of memory.", config.getMemoryReserve());
        } else {
            log.info("No memory to be reserved.");
        }
    }

    /**
     * @return {@code true} if the reserve memory is still available, or if memory was never reserved to begin with.
     */
    public boolean isAvailable() {
        boolean rc = buffer == null || buffer.get() != null;
        if (!rc) {
            log.info("Out-of-memory sitation detected.");
        }
        return rc;
    }

    /**
     * Remove the memory reserve (if there is one). All future calls to {@link MemoryReserve#isAvailable} will
     * now return {@code true} until {@link MemoryReserve#reserve} is called again.<p/>
     *
     * This call is meant to be used when the driver begins a graceful shutdown, so that the shutdown is not
     * hampered by additional OOM alerts.
     */
    public void reset() {
        log.info("Memory reserve reset.");
        buffer = null;
    }
}

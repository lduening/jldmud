/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.rt.object.MudObject;
import org.ldmud.jldmud.rt.object.Objects;

import com.google.inject.Inject;

/**
 * The main Game Loop
 */
public class GameLoop {
    private Logger log = LogManager.getLogger(this.getClass());

    // External modules.
    private MemoryReserve memoryReserve;
    private Objects objects;

    // Classes to signal the main thread.
    private Lock lock = new ReentrantLock();
    private Condition signalCondition = lock.newCondition();

    // {@code True}: the main thread has been signalled.
    private volatile boolean signalled = false;

    // {@code True}: the timer thread signalled the main thread.
    private volatile boolean oneSecondTimerSignal = false;

    private Thread oneSecondTimerThread = null;

    /**
     * Constructor
     *
     * @param memoryReserve The {@link MemoryReserve} instant;
     * @param objects The {@link Objects} management class.
     */
    @Inject
    public GameLoop(MemoryReserve memoryReserve, Objects objects) {
        super();
        this.memoryReserve = memoryReserve;
        this.objects = objects;

        oneSecondTimerThread = new Thread(new OneSecondTimerThread());
        oneSecondTimerThread.setName("OneSecondTimer");
    }

    /**
     * Send a signal to the main thread that something happened
     * which requires its attention.
     */
    public void signalMainThread() {
        log.debug("Signalling main thread: current signal: {}", signalled);
        lock.lock();
        try {
            signalled = true;
            signalCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Method for the main thread to be signalled. The 'signalled' flag will be reset
     * upon return.
     *
     * @throws InterruptedException
     */
    public void waitForSignal() throws InterruptedException {
        log.debug("Main thread waiting for signal: current signal: {}", signalled);
        lock.lock();
        try {
            while (!signalled) {
                signalCondition.await();
            }
            signalled = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute the main game loop.
     */
    public void run() {
        log.info("Main loop start");

        oneSecondTimerThread.start();

        try {
            while (true) {
                waitForSignal();
                long loopStartTime = System.currentTimeMillis();

                log.debug("Executing loop");

                if (! memoryReserve.isAvailable() ) {
                    log.warn("Memory reserve has been freed - initiating shutdown");
                    memoryReserve.reset();
                    // TODO: Initiate graceful shutdown
                }

                // TODO: Cleanup stuff, e.g. replace existing programs
                // TODO: Check soft malloc limit?

                // Execute the command loop at least once, to make sure that timers are executed.
                boolean hadPlayerCommand = true;
                while (hadPlayerCommand) {
                    hadPlayerCommand = false;
                    // TODO: Execute player command if pending, setting the flag to true in that case

                    if (oneSecondTimerSignal) {
                       log.debug("Executing periodic tasks");
                       oneSecondTimerSignal = false;

                       // TODO: Heartbeat
                       // TODO: Call-out
                       // TODO: Swap, Reset, Cleanup

                       // Remove destroyed objects
                       // TODO: Move this into a 'Simulation' class or something
                       for (MudObject obj = objects.getNextDestroyedObject(); obj != null; obj = objects.getNextDestroyedObject()) {
                           // TODO: Any additional cleanup if required
                       }
                    }
                }
                log.debug("Loop executed in {} ms", System.currentTimeMillis() - loopStartTime);
            }

            // TODO: General shutdown handling here?

        } catch (InterruptedException e) {
            log.info("Main loop was interrupted: {}", e.toString());
        }
        log.info("Main loop end");
    }

    /**
     * This thread sends a signal to the main thread every second.
     */
    public class OneSecondTimerThread implements Runnable {

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            long lastRun = System.currentTimeMillis();

            try {
                while (true) {
                    long waitTime = 1000L - (System.currentTimeMillis() - lastRun);
                    Thread.sleep(waitTime);
                    lastRun = System.currentTimeMillis();
                    log.debug("Tick");
                    oneSecondTimerSignal = true;
                    signalMainThread();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}

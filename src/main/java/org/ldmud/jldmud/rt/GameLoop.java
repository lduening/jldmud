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
import org.ldmud.jldmud.rt.net.Communicator;
import org.ldmud.jldmud.rt.net.Interactive;
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
    private Communicator communicator;

    // Classes to signal the main thread.
    private Lock lock = new ReentrantLock();
    private Condition signalCondition = lock.newCondition();

    // {@code True}: the main thread has been signalled.
    private volatile boolean signalled = false;

    // TODO: Put stateflags like this into a dedicated GameState class?
    // {@code True}: the timer thread signalled the main thread.
    private volatile boolean oneSecondTimerSignal = false;

    // {@code True}: the games is being shut down
    private volatile boolean gameIsBeingShutdown = false;

    // The thread pinging the game loop once every second, and the runnable class.
    private Thread oneSecondTimerThread = null;
    private OneSecondTimerThread oneSecondTimerThreadInstance = null;

    /**
     * Constructor
     *
     * @param memoryReserve The {@link MemoryReserve} instant;
     * @param objects The {@link Objects} management class.
     * @param communicator The {@link Communicator} network management class.
     */
    @Inject
    public GameLoop(MemoryReserve memoryReserve, Objects objects, Communicator communicator) {
        super();
        this.memoryReserve = memoryReserve;
        this.objects = objects;
        this.communicator = communicator;

        oneSecondTimerThreadInstance = new OneSecondTimerThread();
        oneSecondTimerThread = new Thread(oneSecondTimerThreadInstance);
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
        log.trace("Main thread waiting for signal: current signal: {}", signalled);
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
            while (!gameIsBeingShutdown) {
                if (!communicator.isInteractivesPending()) {
                    log.debug("No command pending - waiting for signal");
                    waitForSignal();
                }

                if (gameIsBeingShutdown) {
                    break;
                }

                log.debug("Executing loop");
                long loopStartTime = System.currentTimeMillis();

                if (! memoryReserve.isAvailable() ) {
                    log.warn("Memory reserve has been freed - initiating shutdown");
                    memoryReserve.reset();
                    // TODO: Initiate graceful shutdown, but continue to run
                }

                // TODO: Cleanup stuff, e.g. replace existing programs
                // TODO: Check soft malloc limit?

                // Handle the next pending interactive instance.
                Interactive interactive = communicator.nextPendingInteractive();
                if (interactive != null) {
                    if (interactive.getMudObject() == null) {
                        // TODO: New connection
                    } else {
                        // TODO: Execute command
                    }
                }

                if (oneSecondTimerSignal) {
                    log.debug("Executing periodic tasks");
                    oneSecondTimerSignal = false;

                    // TODO: Heartbeat
                    // TODO: Call-out
                    // TODO: Swap, Reset, Cleanup

                    // Remove destroyed objects
                    // TODO: Move this into a 'Simulation' class or into 'Objects'
                    for (MudObject obj = objects.getNextDestroyedObject(); obj != null; obj = objects.getNextDestroyedObject()) {
                        obj.cleanupDestroyedObject();
                    }
                }

                log.debug("Loop executed in {} ms", System.currentTimeMillis() - loopStartTime);
            }


        } catch (InterruptedException e) {
            log.info("Main loop was interrupted: {}", e.toString());
        }

        log.info("Game is being shut down");
        oneSecondTimerThreadInstance.setStopTimer(true);
        // TODO: General shutdown handling here?
        communicator.shutdown();

        log.info("Main loop end");
    }

    /**
     * This thread sends a signal to the main thread every second.
     */
    public class OneSecondTimerThread implements Runnable {
        private volatile boolean stopTimer = false;

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            long lastRun = System.currentTimeMillis();

            try {
                while (!stopTimer) {
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

        /**
         * @param stopTimer the stopTimer to set
         */
        public void setStopTimer(boolean stopTimer) {
            this.stopTimer = stopTimer;
        }
    }
}

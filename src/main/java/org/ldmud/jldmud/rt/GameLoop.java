/**
 * Copyright (C) 2016 jLDMud Developers.
 * This file is free software under the MIT License - see the file LICENSE for details.
 */
package org.ldmud.jldmud.rt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldmud.jldmud.rt.net.Communicator;
import org.ldmud.jldmud.rt.net.Interactive;
import org.ldmud.jldmud.rt.object.MudObjects;

import com.google.inject.Inject;

/**
 * The main Game Loop
 */
public class GameLoop {
    private Logger log = LogManager.getLogger(this.getClass());

    // External modules.
    private MemoryReserve memoryReserve;
    private MudObjects objects;
    private Communicator communicator;
    private GameStateSignals gameStateSignals;

    // The thread pinging the game loop once every second, and the runnable class.
    private Thread oneSecondTimerThread = null;
    private OneSecondTimerThread oneSecondTimerThreadInstance = null;

    /**
     * Constructor
     *
     * @param memoryReserve The {@link MemoryReserve} instance.
     * @param objects The {@link MudObjects} management class.
     * @param communicator The {@link Communicator} network management class.
     */
    @Inject
    GameLoop(MemoryReserve memoryReserve, MudObjects objects, Communicator communicator, GameStateSignals gameStateSignals) {
        super();
        this.memoryReserve = memoryReserve;
        this.objects = objects;
        this.communicator = communicator;
        this.gameStateSignals = gameStateSignals;

        oneSecondTimerThreadInstance = new OneSecondTimerThread();
        oneSecondTimerThread = new Thread(oneSecondTimerThreadInstance);
        oneSecondTimerThread.setName("OneSecondTimer");
    }

    /**
     * Execute the main game loop.
     */
    public void run() {
        log.info("Main loop start");

        oneSecondTimerThread.start();

        try {
            while (!gameStateSignals.isGameIsBeingShutdown()) {
                if (!communicator.areInteractivesPending()) {
                    log.debug("No command pending - waiting for signal");
                    gameStateSignals.waitForSignal();
                }

                if (gameStateSignals.isGameIsBeingShutdown()) {
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

                // Periodic tasks
                if (gameStateSignals.isOneSecondTimerSignal()) {
                    log.debug("Executing periodic tasks");
                    gameStateSignals.setOneSecondTimerSignal(false);

                    // TODO: Heartbeat
                    // TODO: Call-out
                    // TODO: Swap, Reset, Cleanup
                }

                // Truly drop all previously destroyed objects.
                objects.removeDestroyedObjects();

                log.debug("Loop executed in {} ms", System.currentTimeMillis() - loopStartTime);
            }


        } catch (InterruptedException e) {
            log.info("Main loop was interrupted: {}", e.toString());
            Thread.interrupted();
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
    private class OneSecondTimerThread implements Runnable {
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
                    if (waitTime > 0) {
                        Thread.sleep(waitTime);
                    }
                    lastRun = System.currentTimeMillis();
                    log.debug("Tick");
                    gameStateSignals.setOneSecondTimerSignal(true);
                    gameStateSignals.signalMainThread();
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

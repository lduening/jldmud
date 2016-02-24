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

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class holds various signal flags of global interest,
 * as well as the methods necessary to signal the main game loop
 * that something interesting has happened.
 */
@Singleton
public class GameStateSignals {
    private Logger log = LogManager.getLogger(this.getClass());

    // Classes to signal the main thread.
    private Lock lock = new ReentrantLock();
    private Condition signalCondition = lock.newCondition();

    /**
     * {@code True}: the main thread has been signalled.
     */
    private volatile boolean signalled = false;

    /**
     * {@code True}: the timer thread signalled the main thread.
     */
    private volatile boolean oneSecondTimerSignal = false;

    /**
     * {@code True}: the games is being shut down
     */
    private volatile boolean gameIsBeingShutdown = false;

    /**
     * Constructor
     */
    @Inject
    GameStateSignals() {
        super();
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
     * Method for the main thread to wait for a signal. The 'signalled' flag will be reset
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
     * @return The oneSecondTimerSignal value
     */
    public boolean isOneSecondTimerSignal() {
        return oneSecondTimerSignal;
    }

    /**
     * @param oneSecondTimerSignal the oneSecondTimerSignal value to set
     */
    public void setOneSecondTimerSignal(boolean oneSecondTimerSignal) {
        this.oneSecondTimerSignal = oneSecondTimerSignal;
    }

    /**
     * @return The gameIsBeingShutdown value
     */
    public boolean isGameIsBeingShutdown() {
        return gameIsBeingShutdown;
    }

    /**
     * @param gameIsBeingShutdown The gameIsBeingShutdown value to set
     */
    public void setGameIsBeingShutdown(boolean gameIsBeingShutdown) {
        this.gameIsBeingShutdown = gameIsBeingShutdown;
    }
}

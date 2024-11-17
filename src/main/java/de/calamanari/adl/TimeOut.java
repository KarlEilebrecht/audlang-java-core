//@formatter:off
/*
 * TimeOut
 * Copyright 2024 Karl Eilebrecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"):
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//@formatter:on

package de.calamanari.adl;

/**
 * Optimization of expression trees may suffer from combinatoric explosion and can <i>run away</i> in terms of time and memory.<br/>
 * Due to efficient storage in memory, space is not so problematic as time. Solely based on the number of arguments and values it is hard to tell if and when
 * that will happen because some impressive expressions quickly collapse when checking for contradictions and implications, other examples don't look scary at
 * first glance but literally <i>explode</i>.
 * <p/>
 * Thus, I decided to introduce the {@link TimeOut}, which has a check-method that should be called often in the processing code.<br/>
 * From time to time it checks the system clock if a pre-configured time has already been elapsed. If so, a {@link TimeOutException} will be thrown, usually
 * causing the parent process to return an error.
 * <p/>
 * This method is not optimized for multi-threading, but it is safe. The negative impact might be that we check for timeouts at a lower frequency than expected
 * leading to later timeout detection.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class TimeOut {

    /**
     * Default duration: {@value} ms
     */
    public static final long DEFAULT_DURATION = 5_000;

    /**
     * Default clock check frequency: every {@value} calls to {@link #assertHaveTime()}
     */
    public static final int DEFAULT_CHECK_FREQUENCY = 10_000;

    /**
     * Clock time when this instance will timeout
     */
    private final long targetTimeMillis;

    /**
     * Configured duration in milliseconds
     */
    private final long durationMillis;

    /**
     * Number of calls to {@link #assertHaveTime()} before we actually check the clock
     */
    private final int checkFrequency;

    /**
     * Identifier/name of the observed task
     */
    private final String task;

    /**
     * counts the calls to {@link #assertHaveTime()}
     */
    private int checkCount = 0;

    /**
     * holds the expiration state
     */
    private boolean expired = false;

    /**
     * Creates a default timeout
     * 
     * @param task
     * @return
     */
    public static final TimeOut createDefaultTimeOut(String task) {
        return new TimeOut(task, DEFAULT_DURATION, DEFAULT_CHECK_FREQUENCY);
    }

    /**
     * Creates a new timeout with the given duration that starts immediately. The configurable <code>checkFrequency</code> is the number calls to
     * {@link #assertHaveTime()} before the next call to <code>System.currentTimeMillis()</code> (performance optimization).
     * 
     * @param task name of the task which might run into a timeout
     * @param durationMillis milliseconds (lifetime)
     * @param checkFrequency number of calls, 0 or any negative value disables the optimization
     */
    public TimeOut(String task, long durationMillis, int checkFrequency) {
        this.task = task;
        this.durationMillis = durationMillis > 0 ? durationMillis : 0;
        this.targetTimeMillis = System.currentTimeMillis() + this.durationMillis;
        this.checkFrequency = checkFrequency > 0 ? checkFrequency : -1;
    }

    /**
     * Creates a new timeout with the given duration that starts immediately, with {@link #DEFAULT_CHECK_FREQUENCY}.
     * 
     * @param task name of the task which might run into a timeout
     * @param durationMillis milliseconds (lifetime)
     */
    public TimeOut(String task, long durationMillis) {
        this(task, durationMillis, DEFAULT_CHECK_FREQUENCY);
    }

    /**
     * Method to be called from anywhere in the code, potentially at high frequency
     * 
     * @throws TimeOutException
     */
    public void assertHaveTime() throws TimeOutException {
        if (expired || checkCount > checkFrequency) {
            if (isExpired()) {
                long seconds = (long) (durationMillis / 1000.0);
                throw new TimeOutException(String.format("%s timed out after %s seconds.", task, seconds));
            }
            checkCount = 0;
        }
        else {
            checkCount++;
        }
    }

    /**
     * @return checks if the given time is expired (checks the system clock)
     */
    public boolean isExpired() {
        expired = expired || System.currentTimeMillis() >= targetTimeMillis;
        return expired;
    }

}

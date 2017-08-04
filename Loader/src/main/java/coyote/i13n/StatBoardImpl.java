/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.i13n;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import coyote.commons.DateUtil;
import coyote.commons.Version;


/**
 * This is the default implementation of a statistics board.
 *
 * <p>Some find it useful to make this globally accessible so that many
 * different components can use this in a coordinated manner.
 */
public class StatBoardImpl implements StatBoard {

  private static InetAddress localAddress = null;
  private static String cachedLocalHostName = null;

  /** Re-usable null timer to save object creation and GC'n */
  private static final Timer NULL_TIMER = new NullTimer(null);

  /** Re-usable null ARM transaction to save object creation and GC'n */
  private static final ArmTransaction NULL_ARM = new NullArm(null, null, null);

  /** Re-usable null gauge to save object creation and GC'n */
  private static final Gauge NULL_GAUGE = new NullGauge(null);

  private String BOARDID = UUID.randomUUID().toString().toLowerCase();

  /** The time this statboard was create/started. */
  private long startedTimestamp = 0;

  /** Timing is disabled by default */
  private volatile boolean timingEnabled = false;

  /** Application Response Measurement is disabled by default */
  private volatile boolean armEnabled = false;

  /** Gauges are disabled by default */
  private volatile boolean gaugesEnabled = false;

  /** Map of master timers by their name */
  private final HashMap<String, TimingMaster> masterTimers = new HashMap<String, TimingMaster>();

  /** Map of counters by their name */
  private final HashMap<String, Counter> counters = new HashMap<String, Counter>();

  /** Map of ARM masters by their name */
  private final HashMap<String, ArmMaster> armMasters = new HashMap<String, ArmMaster>();

  /** Map of states by their name */
  private final HashMap<String, State> states = new HashMap<String, State>();

  /** Map of component versions by their name */
  private final HashMap<String, Version> versions = new HashMap<String, Version>();

  /** Map of gauges by their name */
  private final HashMap<String, Gauge> gauges = new HashMap<String, Gauge>();




  public StatBoardImpl() {
    startedTimestamp = System.currentTimeMillis();
  }




  /**
   * Decrease the value with the given name by the given amount.
   *
   * <p>This method retrieves the counter with the given name or creates one by
   * that name if it does not yet exist. The retrieved counter is then
   * decreased by the given amount.
   *
   * @param name The name of the counter to decrease.
   *
   * @return The final value of the counter after the operation.
   */
  @Override
  public long decrease(final String name, final long value) {
    return getCounter(name).decrease(value);
  }




  /**
   * Decrement the value with the given name.
   *
   * <p>This method retrieves the counter with the given name or creates one by
   * that name if it does not yet exist. The retrieved counter is then
   * decreased by one (1).
   *
   * @param name The name of the counter to decrement.
   *
   * @return The final value of the counter after the operation.
   */
  @Override
  public long decrement(final String name) {
    return getCounter(name).decrement();
  }




  /**
   * Deactivate a particular class of Application Response Measurement calls
   * from this point on.
   */
  @Override
  public void disableArmClass(final String name) {
    synchronized (armMasters) {
      // get an existing master ARM or create a new one
      ArmMaster master = armMasters.get(name);
      if (master == null) {
        master = new ArmMaster(name);
        armMasters.put(name, master);
      }
      master.setEnabled(false);
    }
  }




  /**
   * Disable the timer with the given name.
   *
   * <p>Disabling a timer will cause all new timers with the given name to
   * skip processing reducing the amount of processing performed by the
   * timers without losing the existing data in the timer. Any existing
   * timers will continue to accumulate data.
   *
   * <p>If a timer is disabled that has not already been created, a disabled
   * timer will be created in memory that can be enabled at a later time.
   *
   * @param name The name of the timer to disable.
   */
  @Override
  public void disableTimer(final String name) {
    synchronized (masterTimers) {
      // get an existing master timer or create a new one
      TimingMaster master = masterTimers.get(name);
      if (master == null) {
        master = new TimingMaster(name);
        masterTimers.put(name, master);
      }
      master.setEnabled(false);
    }
  }




  /**
   * Activate all Application Response Measurement calls from this point on.
   */
  @Override
  public void enableArm(final boolean flag) {
    synchronized (armMasters) {
      armEnabled = flag;
    }
  }




  /**
   * Activate a particular class of Application Response Measurement calls from
   * this point on.
   */
  @Override
  public void enableArmClass(final String name) {
    synchronized (armMasters) {
      // get an existing master ARM or create a new one
      ArmMaster master = armMasters.get(name);
      if (master == null) {
        master = new ArmMaster(name);
        armMasters.put(name, master);
      }
      master.setEnabled(true);
    }
  }




  /**
   * Activate all gauges calls from this point on.
   */
  @Override
  public void enableGauges(final boolean flag) {
    synchronized (gauges) {
      gaugesEnabled = flag;
    }
  }




  /**
   * Enable the timer with the given name.
   *
   * <p>If a timer is enabled that has not already been created, a new
   * timer will be created in memory.
   *
   * @param name The name of the timer to enable.
   */
  @Override
  public void enableTimer(final String name) {
    synchronized (masterTimers) {
      // get an existing master timer or create a new one
      TimingMaster master = masterTimers.get(name);
      if (master == null) {
        master = new TimingMaster(name);
        masterTimers.put(name, master);
      }
      master.setEnabled(true);
    }
  }




  /**
   * Enable fully-functional timers from this point forward.
   *
   * <p>When timing is enabled, functional timers are returned and their
   * metrics are collected for later reporting. when timing is disabled, null
   * timers are be returned each time a timer is requested. This keeps all code
   * operational regardless of the runtime status of timing.
   */
  @Override
  public void enableTiming(final boolean flag) {
    synchronized (masterTimers) {
      timingEnabled = flag;
    }
  }




  /**
   * Get an iterator over all the ARM Masters in the statboard.
   */
  @Override
  public Iterator<ArmMaster> getArmIterator() {
    final ArrayList<ArmMaster> list = new ArrayList<ArmMaster>();
    synchronized (armMasters) {
      for (final Iterator<ArmMaster> it = armMasters.values().iterator(); it.hasNext(); list.add(it.next())) {
        ;
      }
    }
    return list.iterator();
  }




  /**
   * Return the counter with the given name.
   *
   * <p>If the counter does not exist, one will be created and added to the
   * static list of counters for later retrieval.
   *
   * @param name The name of the counter to return.
   *
   * @return The counter with the given name.
   */
  @Override
  public Counter getCounter(final String name) {
    Counter counter = null;
    synchronized (counters) {
      counter = counters.get(name);
      if (counter == null) {
        counter = new Counter(name);
        counters.put(name, counter);
      }
    }
    return counter;
  }




  /**
   * @return The number of counters in the statboard at the present time.
   */
  @Override
  public int getCounterCount() {
    return counters.size();
  }




  /**
   * Access an iterator over the counters.
   *
   * <p>NOTE: this iterator is detached from the counters in that the remove()
   * call on the iterator will only affect the returned iterator and not the
   * counter collection in the statboard. If you wish to remove a counter, you
   * MUST call removeCounter(Counter) with the reference returned from this
   * iterator as well.
   *
   * @return a detached iterator over the counters.
   */
  @Override
  public Iterator<Counter> getCounterIterator() {
    final ArrayList<Counter> list = new ArrayList<Counter>();
    for (final Iterator<Counter> it = counters.values().iterator(); it.hasNext(); list.add(it.next())) {
      ;
    }
    return list.iterator();
  }




  /**
   * Return the reference to the named gauge.
   *
   * <p>This will always return an object; it may be a stub, or a working
   * implementation depending upon the state of the statboard at the time. If
   * gauges are enabled, then a working gauge is returned, otherwise a null
   * gauge is returned.
   *
   * <p>Because the state of gauge operation can change over the operation of
   * the statboard, it is not advisable to hold on to the reference between calls
   * to the gauge. Always get the appropriate reference to the gauge
   *
   * @param name the name of the gauge to return.
   *
   * @return Either the
   *
   * @throws IllegalArgumentException if the name of the gauge is null
   */
  @Override
  public Gauge getGauge(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Gauge name is null");
    }

    Gauge retval = null;
    if (gaugesEnabled) {
      synchronized (gauges) {
        retval = gauges.get(name);
        if (retval == null) {
          retval = new GaugeBase(name);
          gauges.put(name, retval);
        }
      }
    } else {
      // just return the do-nothing gauge
      retval = NULL_GAUGE;
    }

    return retval;
  }




  /**
   * @return The number of gauges in the statboard at the present time.
   */
  @Override
  public int getGaugeCount() {
    return gauges.size();
  }




  /**
   * Get an iterator over all the gauges in the statboard.
   */
  @Override
  public Iterator<Gauge> getGaugeIterator() {
    final ArrayList<Gauge> list = new ArrayList<Gauge>();
    synchronized (gauges) {
      for (final Iterator<Gauge> it = gauges.values().iterator(); it.hasNext(); list.add(it.next())) {
        ;
      }
    }
    return list.iterator();
  }




  /**
   * Return the identifier the card is using to differentiate itself from other
   * cards on this host and the system overall.
   *
   * @return The identifier for this statboard.
   */
  @Override
  public String getId() {
    return BOARDID;
  }




  /**
   * @return The epoch time in milliseconds this fixture was started.
   */
  @Override
  public long getStartedTime() {
    return startedTimestamp;
  }




  /**
   * Return the state with the given name.
   *
   * <p>If the state does not exist, one will be created and added to the
   * static list of states for later retrieval.
   *
   * @param name The name of the state to return.
   *
   * @return The state with the given name.
   */
  @Override
  public State getState(final String name) {
    State state = null;
    synchronized (states) {
      state = states.get(name);
      if (state == null) {
        state = new State(name);
        states.put(name, state);
      }
    }
    return state;
  }




  /**
   * @return The number of states in the statboard at the present time.
   */
  @Override
  public int getStateCount() {
    return states.size();
  }




  /**
   * Access an iterator over the states.
   *
   * <p>NOTE: this iterator is detached from the states in that the remove()
   * call on the iterator will only affect the returned iterator and not the
   * state collection in the statboard. If you wish to remove a state, you MUST
   * call removeState(Counter) with the reference returned from this iterator
   * as well.
   *
   * @return a detached iterator over the states.
   */
  @Override
  public Iterator<State> getStateIterator() {
    final ArrayList<State> list = new ArrayList<State>();
    for (final Iterator<State> it = states.values().iterator(); it.hasNext(); list.add(it.next())) {
      ;
    }
    return list.iterator();
  }




  /**
   * Get an iterator over all the Master Timers in the statboard.
   */
  @Override
  public Iterator<TimingMaster> getTimerIterator() {
    final ArrayList<TimingMaster> list = new ArrayList<TimingMaster>();
    synchronized (masterTimers) {
      for (final Iterator<TimingMaster> it = masterTimers.values().iterator(); it.hasNext(); list.add(it.next())) {
        ;
      }
    }
    return list.iterator();
  }




  /**
   * Get the master timer with the given name.
   *
   * @param name The name of the master timer to retrieve.
   *
   * @return The master timer with the given name or null if that timer
   *         does not exist.
   */
  @Override
  public TimingMaster getTimerMaster(final String name) {
    synchronized (masterTimers) {
      return masterTimers.get(name);
    }
  }




  /**
   * Return how long the fixture has been active in a format using only the
   * significant time measurements.
   *
   * <p>Significant measurements means if the number of seconds extend past 24
   * hours, then only report the days and hours skipping the minutes and
   * seconds. Examples include <tt>4m 23s</tt> or <tt>22d 4h</tt>. The format
   * is designed to make reporting fixture up-time more polished.
   *
   * @return the time the fixture has been active in a reportable format.
   */
  @Override
  public String getUptimeString() {
    return DateUtil.formatSignificantElapsedTime((System.currentTimeMillis() - startedTimestamp) / 1000);
  }




  /**
   * Increase the value with the given name by the given amount.
   *
   * <p>This method retrieves the counter with the given name or creates one by
   * that name if it does not yet exist. The retrieved counter is then
   * increased by the given amount.
   *
   * @param name The name of the counter to increase.
   *
   * @return The final value of the counter after the operation.
   */
  @Override
  public long increase(final String name, final long value) {
    return getCounter(name).increase(value);
  }




  /**
   * Increment the value with the given name.
   *
   * <p>This method retrieves the counter with the given name or creates one by
   * that name if it does not yet exist. The retrieved counter is then
   * increased by one (1).
   *
   * @param name The name of the counter to increment.
   *
   * @return The final value of the counter after the operation.
   */
  @Override
  public long increment(final String name) {
    return getCounter(name).increment();
  }




  /**
   * Remove the counter with the given name.
   *
   * @param name Name of the counter to remove.
   *
   * @return The removed counter.
   */
  @Override
  public Counter removeCounter(final String name) {
    synchronized (counters) {
      return counters.remove(name);
    }
  }




  /**
   * Remove the gauge with the given name.
   *
   * @param name Name of the gauge to remove.
   *
   * @return The removed gauge.
   */
  @Override
  public Gauge removeGauge(final String name) {
    if (name == null) {
      return null;
    }

    synchronized (gauges) {
      return gauges.remove(name);
    }
  }




  /**
   * Remove the state with the given name.
   *
   * @param name Name of the state to remove.
   *
   * @return The removed state.
   */
  @Override
  public State removeState(final String name) {
    if (name == null) {
      return null;
    }

    synchronized (states) {
      return states.remove(name);
    }
  }




  /**
   * Reset the counter with the given name returning a copy of the counter
   * before the reset occurred.
   *
   * <p>The return value will represent a copy of the counter prior to the
   * reset and is useful for applications that desire delta values. These delta
   * values are simply the return values of successive reset calls.
   *
   * <p>If the counter does not exist, it will be created prior to being reset.
   * The return value will reflect an empty counter with the given name.
   *
   * @param name The name of the counter to reset.
   *
   * @return a counter containing the values of the counter prior to the reset.
   */
  @Override
  public Counter resetCounter(final String name) {
    Counter retval = null;
    synchronized (counters) {
      retval = getCounter(name).reset();
    }

    return retval;
  }




  /**
   * Reset and clear-out the named gauge.
   *
   * @param name The name of the gauge to clear out.
   */
  @Override
  public void resetGauge(final String name) {
    if ((name != null) && (name.length() > 0)) {
      getGauge(name).reset();
    }
  }




  /**
   * Removes all timers from the statboard and frees them up for garbage
   * collection.
   */
  @Override
  public void resetTimers() {
    synchronized (masterTimers) {
      masterTimers.clear();
    }
  }




  /**
   * Assign a unique identifier to this statboard.
   *
   * @param id the unique identifier to set
   */
  @Override
  public void setId(final String id) {
    BOARDID = id;
  }




  /**
   * Set the named state to the given value.
   *
   * @param name The name of the state to set.
   *
   * @param value The value to set in the state.
   */
  @Override
  public void setState(final String name, final double value) {
    getState(name).set(value);
  }




  /**
   * Set the named state to the given value.
   *
   * @param name The name of the state to set.
   *
   * @param value The value to set in the state.
   */
  @Override
  public void setState(final String name, final long value) {
    getState(name).set(value);
  }




  /**
   * Set the named state to the given value.
   *
   * @param name The name of the state to set.
   *
   * @param value The value to set in the state.
   */
  @Override
  public void setState(final String name, final String value) {
    if ((name != null) && (name.length() != 0)) {

      if (value == null) {
        removeState(name);
      } else {
        getState(name).set(value);
      }
    }
  }




  /**
   * Start an Application Response Measurement transaction.
   *
   * @param name Grouping name.
   *
   * @return A transaction to collect ARM data.
   */
  @Override
  public ArmTransaction startArm(final String name) {
    return startArm(name, null);
  }




  /**
   * Start an Application Response Measurement transaction using a particular
   * correlation identifier.
   *
   * @param name Grouping name.
   * @param crid correlation identifier
   *
   * @return A transaction to collect ARM data.
   */
  @Override
  public ArmTransaction startArm(final String name, final String crid) {
    ArmTransaction retval = null;
    if (armEnabled) {
      synchronized (armMasters) {
        // get an existing ARM master or create a new one
        ArmMaster master = armMasters.get(name);
        if (master == null) {
          master = new ArmMaster(name);
          armMasters.put(name, master);
        }

        // have the master ARM return a transaction instance
        retval = master.createArm(name, crid);

        //start the ARM transaction
        retval.start();
      }
    } else {
      // just return the do-nothing timer
      retval = NULL_ARM;
    }

    return retval;
  }




  /**
   * Start a timer with the given name.
   *
   * <p>Use the returned Timer to stop the interval measurement.
   *
   * @param name The name of the timer instance to start.
   *
   * @return The timer instance that should be stopped when the interval is
   *         completed.
   */
  @Override
  public Timer startTimer(final String name) {
    Timer retval = null;
    if (timingEnabled) {
      synchronized (masterTimers) {
        // get an existing master timer or create a new one
        TimingMaster master = masterTimers.get(name);
        if (master == null) {
          master = new TimingMaster(name);
          masterTimers.put(name, master);
        }

        // have the master timer return a timer instance
        retval = master.createTimer();

        //start the timer instance
        retval.start();
      }
    } else {
      // just return the do-nothing timer
      retval = NULL_TIMER;
    }

    // return the started timer
    return retval;
  }




  /**
   * Update the named gauge with the given value.
   *
   * @param name The name of the gauge to update.
   * @param value The value with which to update the gauge.
   */
  @Override
  public void updateGauge(final String name, final long value) {
    if ((name != null) && (name.length() > 0)) {
      getGauge(name).update(value);
    }
  }




  /**
   * Set the version of the given named component.
   * 
   * @param name the name of the component this version describes
   * @param version the version object
   */
  @Override
  public void setVersion(String name, Version version) {
    synchronized (versions) {
      if (name != null && version != null) {
        versions.put(name, version);
      }
    }
  }




  /**
   * Returns the maximum amount of memory that the virtual machine will attempt 
   * to use.
   * 
   * <p>The heap can grow to this limit but never more than this.</p>
   * 
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The maximum number of bytes the heap can ever allocate.
   */
  @Override
  public long getMaxHeapSize() {
    return Runtime.getRuntime().maxMemory();
  }




  /**
   * Returns the total amount of memory currently in use by the heap. 
   * 
   * <p>It is composed of all currently allocated objects and possibly some 
   * space that was freed by the garbage collector.</p>
   * 
   * <p>Heap size is allocated and release in blocks. This is the current size
   * of the heap allocation.</p>
   * 
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The number of bytes currently allocated by the heap.
   */
  @Override
  public long getCurrentHeapSize() {
    return Runtime.getRuntime().totalMemory();
  }




  /**
   * Available memory is the maximum memory available to the VM less the total 
   * memory currently allocated for the heap.
   * 
   * <p>This is different from free memory in that this returns the difference 
   * between the allocated heap and the maximum Heap size. It does not include 
   * the amount of memory available on the current heap allocation.</p>
   * 
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The number of bytes the heap can grow before MaxMemory is reached. 
   */
  @Override
  public long getAvailableMemory() {
    return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
  }




  /**
   * Free memory is the total amount of memory that can be allocated prior to
   * running out of memory in the VM.
   * 
   * <p>This can be used to approximate the amount of memory available to the 
   * application. This will never be totally accurate because the garbage 
   * collection process could change things at any time.</p>
   *  
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return Available Memory and Free Heap size added together.
   */
  @Override
  public long getFreeMemory() {
    return Runtime.getRuntime().freeMemory() + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory());
  }




  /**
   * Returns an approximation of the total amount of memory currently available 
   * on the heap for newly allocated objects.
   * 
   * <p>This is what is available on the current heap allocation before the 
   * next block of memory is requested to be added to total heap memory. In 
   * other words, this is the number of bytes available before the heap will 
   * attempt to grow.</p>
   * 
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The number of bytes available on the currently allocated heap.
   */
  @Override
  public long getFreeHeapSize() {
    return Runtime.getRuntime().freeMemory();
  }




  /**
   * Return the percentage of the Maximum Memory the currently allocated heap 
   * occupies.
   * 
   * <p>In some cases this will always return 1.0 indicating that all memory 
   * has been allocated to the heap. These cases include runtimes where the Ms 
   * and Mx systems properties are set to the same value. This will not mean 
   * that the runtime is out of memory, just that the heap has been allocated 
   * to fill the entire space. The FreeHeap value will then determine the 
   * amount of free memory and should be the same value as FreeMemory.</p>
   *  
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The percentage of maximum heap memory currently allocated to the 
   *         heap.
   */
  @Override
  public float getHeapPercentage() {
    return (float)(1 - ((float)getAvailableMemory() / (float)getMaxHeapSize())); // percent of max
  }




  /**
   * Return the amount of memory used by the heap.
   * 
   * <p>This shows how much memory is used in the currently allocated heap. The
   * MaxHeapSize thould equale this return value added to the FreeMemory 
   * value.</p>
   * 
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return Total used memory percentage of maximum memory.
   */
  @Override
  public float getUsedMemory() {
    return getMaxHeapSize() - getFreeMemory();
  }




  /**
   * Return the percentage of used memory of the maximum memory.
   * 
   * <p>This is different from the Heap Percentage in that this calculation
   * takes both available memory and free heap to determine how much memory is
   * actually used by the runtime.</p>
   *  
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return Total used memory percentage of maximum memory.
   */
  @Override
  public float getUsedMemoryPercentage() {
    return (float)(1 - ((float)getFreeMemory() / (float)getMaxHeapSize())); // percent of max
  }




  /**
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The percentage of total free memory available both on the heap and 
   *         memory that has not yet been allocated to the heap.
   */
  @Override
  public float getFreeMemoryPercentage() {
    return (float)((float)getFreeMemory() / (float)getMaxHeapSize());
  }




  /**
   * @return the mapping of all the versions of the named components.
   */
  @Override
  public Map<String, String> getVersions() {
    final Map<String, String> retval = new HashMap<String, String>();
    synchronized (versions) {
      for (String key : versions.keySet()) {
        retval.put(key, versions.get(key).toString());
      }
    }
    return retval;
  }




  /**
   * Retrieve the version of the component with the given name.
   * 
   * @param name the name of the component to query
   * 
   * @return the version of that component or null if the named component 
   *         could not be found or the name was null
   */
  @Override
  public Version getVersion(String name) {
    if (name != null) {
      synchronized (versions) {
        return versions.get(name);
      }
    }
    return null;
  }




  @Override
  public String getHostname() {
    return getLocalQualifiedHostName();
  }




  @Override
  public InetAddress getHostIpAddress() {
    return getLocalAddress();
  }




  /**
   * Returns an {@code InetAddress} object encapsulating what is most likely 
   * the machine's LAN IP address.
   * 
   * <p>This method is intended for use as a replacement of JDK method {@code 
   * InetAddress.getLocalHost}, because that method is ambiguous on Linux 
   * systems. Linux systems enumerate the loopback network interface the same 
   * way as regular LAN network interfaces, but the JDK {@code 
   * InetAddress.getLocalHost} method does not specify the algorithm used to 
   * select the address returned under such circumstances, and will often 
   * return the loopback address, which is not valid for network 
   * communication. Details 
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
   * 
   * <p>This method will scan all IP addresses on all network interfaces on 
   * the host machine to determine the IP address most likely to be the 
   * machine's LAN address. If the machine has multiple IP addresses, this 
   * method will prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, 
   * usually IPv4) if the machine has one (and will return the first site-local 
   * address if the machine has more than one), but if the machine does not 
   * hold a site-local address, this method will return simply the first non-
   * loopback address found (IPv4 or IPv6).
   * 
   * <p>Last ditch effort is to try a DNS lookup of the machines hostname. 
   * This can take a little time which it is the last resort and any results 
   * are cached to avoid any future DNS lookups.
   * 
   * <p>If the above methods cannot find a non-loopback address using this 
   * selection algorithm, it will fall back to calling and returning the 
   * result of JDK method {@code InetAddress.getLocalHost}.
   */
  private static InetAddress getLocalAddress() {
    if (localAddress != null) {
      return localAddress;
    }

    try {
      InetAddress candidateAddress = null;
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
        NetworkInterface iface = ifaces.nextElement();
        for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
          InetAddress inetAddr = inetAddrs.nextElement();
          if (!inetAddr.isLoopbackAddress()) {
            if (inetAddr.isSiteLocalAddress()) {
              // Found non-loopback site-local address. Cache and return it
              localAddress = inetAddr;
              return localAddress;
            } else if (candidateAddress == null) {
              candidateAddress = inetAddr;
            }
          }
        }
      }
      if (candidateAddress != null) {
        localAddress = candidateAddress;
        return localAddress;
      }

      // Make sure we get the IP Address by which the rest of the world knows 
      // us or at least, our host's default network interface
      InetAddress inetAddr;
      try {
        // This helps insure that we do not get localhost (127.0.0.1)
        inetAddr = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
        if (!inetAddr.isLoopbackAddress() && inetAddr.isSiteLocalAddress()) {
          localAddress = inetAddr;
          return localAddress;
        }
      } catch (UnknownHostException e) {}

      // Fall back to returning whatever InetAddress.getLocalHost() returns...
      inetAddr = InetAddress.getLocalHost();
      if (inetAddr == null) {
        throw new UnknownHostException();
      }
      localAddress = inetAddr;
      return localAddress;
    } catch (Exception e) {}
    //failure. Well, at least we tried.
    return null;
  }




  /**
   * @return the FQDN of the local host or null if the lookup failed for any
   *         reason.
   */
  public static String getLocalQualifiedHostName() {
    // Use the cached version of the hostname to save DNS lookups
    if (cachedLocalHostName != null) {
      return cachedLocalHostName;
    }

    cachedLocalHostName = getQualifiedHostName(getLocalAddress());

    return cachedLocalHostName;
  }




  /**
   * Use the underlying getCanonicalHostName as used in Java, but return null 
   * if the value is the numerical address (dotted-quad) representation of the 
   * address.
   *
   * @param addr The IP address to lookup.
   *
   * @return The Canonical Host Name; null if the FQDN could not be determined
   *         or if the return value was the dotted-quad representation of the
   *         host address.
   */
  public static String getQualifiedHostName(InetAddress addr) {
    String name = null;

    try {
      name = addr.getCanonicalHostName();

      if (name != null) {
        // Check for a return value of and address instead of a name
        if (Character.isDigit(name.charAt(0))) {
          // Looks like an address, return null;
          return null;
        }

        // normalize the case
        name = name.toLowerCase();
      }
    } catch (Exception ex) {}

    return name;
  }

}

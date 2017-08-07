/*
 * Copyright Stephan D. Cote' 2008 - All rights reserved.
 */
package coyote.i13n;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import coyote.commons.DateUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.FileUtil;
import coyote.commons.GUID;
import coyote.commons.UriUtil;
import coyote.commons.Version;
import coyote.commons.network.IpInterface;
import coyote.dataframe.DataFrame;
import coyote.loader.component.Component;
import coyote.loader.log.Log;


/**
 * The Tabs class defines static resources for tracking the state and operation 
 * of business logic running in the VM and making those measurements available 
 * to the external entities. 
 * 
 * <p>This class runs a single thread that communicates to the outside world by 
 * establishing a UDP socket on the default network interface and communicating 
 * with other Tabs facilities.</p>
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 */
public class Tabs {
  /** Tag used in various class identifying locations */
  public static final String CLASS_TAG = "Tabs";

  /** Current version of the instrumentation package */
  public static final Version VERSION = new Version(3, 0, 0, Version.EXPERIMENTAL);

  private static String FIXTUREID = new GUID().toString().toLowerCase();
  private static String FIXTUREGRP = null;
  private static Tabs fixture;
  private static File workDirectory;
  private static File logDirectory;
  static boolean useudp = true;
  private static volatile boolean checkedClasspathAlready = false;
  private static final String LF = System.getProperty("line.separator");

  /**
   * The name of the system property that contains the path to the home 
   * directory.
   */
  public static final String HOMEDIR_TAG = "tabs.home";

  public static final String DEFAULT_HOME = System.getProperty("user.home") + System.getProperty("file.separator") + Tabs.CLASS_TAG.toLowerCase();

  /** 
   * A map that holds component identifiers for components that desire their 
   * presence be made known to others.
   */
  private static final HashMap componentMap = new HashMap();

  static final EventList eventList = new EventList();
  private static final HashMap masterTimers = new HashMap();
  private static final HashMap armMasters = new HashMap();
  private static final HashMap counters = new HashMap();
  private static final HashMap states = new HashMap();
  private static final HashMap gauges = new HashMap();

  /** For the zipFile method */
  private static final int STREAM_BUFFER_SIZE = 8 * 1024;

  public static final String ID_TAG = "tabs.id";
  public static final String GROUP_TAG = "tabs.group";
  public static final String NOUDP_TAG = "tabs.noudp";

  /** Timing is disabled by default */
  private static boolean timingEnabled = false;

  /** Application Response Measurement is disabled by default */
  private static boolean armEnabled = false;

  /** Gauges are disabled by default */
  private static boolean gaugesEnabled = false;

  /** Re-usable null gauge to save object creation and GC'n */
  private static final Gauge NULL_GAUGE = new NullGauge(null);

  /** Re-usable null ARM transaction to save object creation and GC'n */
  private static final ArmTransaction NULL_ARM = new NullArm(null, null, null);

  /** Re-usable null timer to save object creation and GC'n */
  private static final Timer NULL_TIMER = new NullTimer(null);

  /** The time this fixture started. */
  private static long startedTimestamp = 0;

  private static IpInterface primaryInterface = null;

  private static HashMap shadowClasses = new HashMap();
  private static ArrayList missingLibraries = new ArrayList();




  /**
   * Private constructor to eliminate the creation of instances of this class.
   */
  private Tabs() {
    Tabs.fixture = this;

    Tabs.startedTimestamp = System.currentTimeMillis();

    // Add a shutdown hook into the JVM to help us shut everything down nicely
    try {
      Runtime.getRuntime().addShutdownHook(new Thread("TabsHook") {
        public void run() {
          shutdown("Runtime termination");
        }
      });
    } catch (final java.lang.NoSuchMethodError nsme) {
      // Ignore
    } catch (final Throwable e) {
      // Ignore
    }

  }




  public static synchronized Tabs getInstance() {
    if (Tabs.fixture == null) {
      // create the true singleton
      Tabs.fixture = new Tabs();
    }
    return Tabs.fixture;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -




  /**
   * Compress log files older than 7 days and delete archives older than 30 
   * days.
   * 
   * <p>This can be called any time and will archive and delete files as 
   * appropriate for ALL files in the log directory of the Tabs home directory 
   * for ALL instances of Tabs running on this platform.</p>
   * 
   * <p>A separate thread is created to run this logic so the processing does 
   * not take too long in the callers thread of execution.</p>
   */
  public static void archiveLogs() {
    final Thread archiver = new Thread("TabsLogArchiver") {
      public void run() {
        Calendar cal = GregorianCalendar.getInstance();

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        // Archive files older than 7 days
        cal.add(Calendar.DATE, -7);

        long cutoff = cal.getTimeInMillis();
        long lastAccessed = cutoff;

        String[] children = Tabs.logDirectory.list();

        if (children != null) {
          for (int i = 0; i < children.length; i++) {
            File source = new File(Tabs.logDirectory, children[i]);
            lastAccessed = FileUtil.getFileAge(source);

            if (source.isFile() && (lastAccessed <= cutoff)) {
              if (source.length() == 0) {
                source.delete();
              } else {
                if (!source.getName().toLowerCase().endsWith(".zip")) {
                  try {
                    Tabs.zipFile(source);
                    source.delete();
                  } catch (Exception ex) {
                    Log.debug("Could not archive file: '" + source.getAbsolutePath() + "' reason:" + ex.getMessage() + " - (This file will be archived later)");
                  }
                } // not a zip file
              } // file has any data
            } // file is old
          } // for each path in log directory
        } // there are children in log directory

        // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        // Now go back 23 more days (30 total) and delete all old files and
        // directories based upon when they or their children were last modified.
        cal.add(Calendar.DATE, -23);

        cutoff = cal.getTimeInMillis();

        if (children != null) {
          for (int i = 0; i < children.length; i++) {
            File source = new File(Tabs.logDirectory, children[i]);

            if (source.exists()) {
              lastAccessed = FileUtil.getFileAge(source);

              if ((lastAccessed <= cutoff) && source.canWrite()) {
                source.delete();
              }
            } // file exists
          } // for each child in the metric dir list
        } // if metric dir has children
        Tabs.createEvent(null, null, "Tabs", "Log archiving completed", 0, 0, 0, "INFO");
      }
    };
    archiver.setDaemon(false);
    archiver.start();
  }




  /**
   * Create and send an event to any listening agents.
   * 
   * @param appid The identifier of the application sending the event.
   * @param sysid The identifier of the system sending the event.
   * @param cmpid The component identifier sending the event.
   * @param msg The descriptive message of the event.
   * @param sv The severity of the event.
   * @param maj Major code to classify the event.
   * @param min Minor code to sub-clasify the event.
   * @param cat The category of the event.
   * 
   * @return The sequence number of the event created.
   */
  public static long createEvent(final String appid, final String sysid, final String cmpid, final String msg, final int sv, final int maj, final int min, final String cat) {
    final AppEvent event = Tabs.eventList.createEvent(appid, sysid, cmpid, msg, sv, maj, min, cat);
    // return Tabs.oamManager.sendEvent( event );
    return event.getSequence();
  }




  /**
   * Decrease the value with the given name by the given amount.
   * 
   * <p>This method retrieves the counter with the given name or creates one by 
   * that name if it does not yet exist. The retrieved counter is then 
   * decreased by the given amount.</p> 
   * 
   * @param tag The name of the counter to decrease.
   * 
   * @return The final value of the counter after the operation.
   */
  public static long decrease(final String tag, final long value) {
    return Tabs.getCounter(tag).decrease(value);
  }




  /**
   * Decrement the value with the given name.
   * 
   * <p>This method retrieves the counter with the given name or creates one by 
   * that name if it does not yet exist. The retrieved counter is then 
   * decreased by one (1).</p> 
   * 
   * @param tag The name of the counter to decrement.
   * 
   * @return The final value of the counter after the operation.
   */
  public static long decrement(final String tag) {
    return Tabs.getCounter(tag).decrement();
  }




  /**
   * Deactivate a particular class of Application Response Measurement calls 
   * from this point on.
   */
  public static void disableArmClass(final String name) {
    synchronized (Tabs.armMasters) {
      // get an existing master ARM or create a new one
      ArmMaster master = (ArmMaster)Tabs.armMasters.get(name);
      if (master == null) {
        master = new ArmMaster(name);
        Tabs.armMasters.put(name, master);
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
   * timers will continue to accumulate data.</p>
   * 
   * <p>If a timer is disabled that has not already been created, a disabled 
   * timer will be created in memory that can be enabled at a later time.</p>
   * 
   * @param tag The name of the timer to disable.
   */
  public static void disableTimer(final String tag) {
    synchronized (Tabs.masterTimers) {
      // get an existing master timer or create a new one
      TimingMaster master = (TimingMaster)Tabs.masterTimers.get(tag);
      if (master == null) {
        master = new TimingMaster(tag);
        Tabs.masterTimers.put(tag, master);
      }
      master.setEnabled(false);
    }
  }




  /**
   * Activate all Application Response Measurement calls from this point on.
   */
  public static void enableArm(final boolean flag) {
    synchronized (Tabs.armMasters) {
      Tabs.armEnabled = flag;
    }
  }




  /**
   * Activate a particular class of Application Response Measurement calls from 
   * this point on.
   */
  public static void enableArmClass(final String name) {
    synchronized (Tabs.armMasters) {
      // get an existing master ARM or create a new one
      ArmMaster master = (ArmMaster)Tabs.armMasters.get(name);
      if (master == null) {
        master = new ArmMaster(name);
        Tabs.armMasters.put(name, master);
      }
      master.setEnabled(true);
    }
  }




  /**
   * Activate all gauges calls from this point on.
   */
  public static void enableGauges(final boolean flag) {
    synchronized (Tabs.gauges) {
      Tabs.gaugesEnabled = flag;
    }
  }




  /**
   * Enable the timer with the given name.
   * 
   * <p>If a timer is enabled that has not already been created, a new 
   * timer will be created in memory.</p>
   * 
   * @param tag The name of the timer to enable.
   */
  public static void enableTimer(final String tag) {
    synchronized (Tabs.masterTimers) {
      // get an existing master timer or create a new one
      TimingMaster master = (TimingMaster)Tabs.masterTimers.get(tag);
      if (master == null) {
        master = new TimingMaster(tag);
        Tabs.masterTimers.put(tag, master);
      }
      master.setEnabled(true);
    }
  }




  /**
   * Enable fully-functional timers from this point forward.
   * 
   * <p>When timing is enabled, functional timers are returned and their 
   * metrics are collected for later reporting. when timing is disabled, null 
   * timers are be retured each time a timer is requested. This keeps all code 
   * operational regardless of the runtime status of timing.</p>
   */
  public static void enableTiming(final boolean flag) {
    synchronized (Tabs.masterTimers) {
      Tabs.timingEnabled = flag;
    }
  }




  /**
   * Get an iterator over all the ARM Masters in the fixture.
   */
  public static Iterator getArmIterator() {
    final ArrayList list = new ArrayList();
    synchronized (Tabs.armMasters) {
      for (final Iterator it = Tabs.armMasters.values().iterator(); it.hasNext(); list.add((ArmMaster)it.next())) {
        ;
      }
    }
    return list.iterator();
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
  public static long getAvailableMemory() {
    return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
  }




  /**
   * @return An array of component identifiers that are currently registered 
   *         with this fixture.
   */
  public static String[] getComponentIds() {
    synchronized (Tabs.componentMap) {
      // get the size of the map
      int x = Tabs.componentMap.size();

      // create a return value the size of the map then decrement the size so 
      // we can start populating the return value starting from the last 
      // position in the return value array
      final String[] retval = new String[x--];

      // enumerate through the keys, placing them in the return value starting 
      // from the end of the array and working to the front
      for (final Iterator it = Tabs.componentMap.keySet().iterator(); it.hasNext(); retval[x--] = (String)it.next()) {
        ;
      }

      // Return the populated array of component identifiers registered with us.
      return retval;
    }
  }




  public static Iterator getComponentIterator() {
    final ArrayList cmpnts = new ArrayList();
    synchronized (Tabs.componentMap) {
      for (final Iterator it = Tabs.componentMap.values().iterator(); it.hasNext();) {
        final Component cmpnt = (Component)it.next();
        if (cmpnt != null) {
          cmpnts.add(cmpnt);
        } // if
      } // for
    } // sync
    return cmpnts.iterator();
  }




  /**
   * Return the counter with the given name.
   * 
   * <p>If the counter does not exist, one will be created and added to the 
   * static list of counters for later retrieval.</p>
   * 
   * @param name The name of the counter to return.
   * 
   * @return The counter with the given name.
   */
  public static Counter getCounter(final String name) {
    Counter counter = null;
    synchronized (Tabs.counters) {
      counter = (Counter)Tabs.counters.get(name);
      if (counter == null) {
        counter = new Counter(name);
        Tabs.counters.put(name, counter);
      }
    }
    return counter;
  }




  /**
   * @return The number of counters in the fixture at the present time.
   */
  public static int getCounterCount() {
    return Tabs.counters.size();
  }




  /**
   * Access an interator over the counters.
   * 
   * <p>NOTE: this iterator is detached from the counters in that the remove() 
   * call on the iterator will only affect the returned iterator and not the 
   * counter collection in the fixture. If you wish to remove a counter, you 
   * MUST call removeCounter(Counter) with the reference returned from this 
   * iterator as well.</p>
   * 
   * @return a detached iterator over the counters.
   */
  public static Iterator getCounterIterator() {
    final ArrayList list = new ArrayList();
    for (final Iterator it = Tabs.counters.values().iterator(); it.hasNext(); list.add((Counter)it.next())) {
      ;
    }
    return list.iterator();
  }




  /**
   * @return All counters as attributes of a packet.
   */
  public static DataFrame getCounterMessage() {
    final DataFrame retval = new DataFrame();
    //retval.setType( "Counters" );

    synchronized (Tabs.counters) {
      for (final Iterator it = Tabs.counters.entrySet().iterator(); it.hasNext();) {
        final Counter cntr = (Counter)((Map.Entry)it.next()).getValue();
        try {
          retval.put(cntr.getName(), new Long(cntr.getValue()));
        } catch (final Exception ignore) {}
      }
    }
    return retval;
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
  public static long getCurrentHeapSize() {
    return Runtime.getRuntime().totalMemory();
  }




  /**
   * @return Just the domain portion of this hosts name.
   */
  public static String getDomain() {
    return Tabs.primaryInterface.getDomain();
  }




  public static int getEventListSize() {
    return Tabs.eventList.getSize();
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
  public static long getFreeHeapSize() {
    return Runtime.getRuntime().freeMemory();
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
  public static long getFreeMemory() {
    return Runtime.getRuntime().freeMemory() + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory());
  }




  /**
   * <p>Keep in mind this and all memory values are approximate as the garbage 
   * collector can be running at the same time. Also, other threads may be 
   * allocating object on the heap, changing the values between calls.</p>
   *  
   * @return The percentage of total free memory available both on the heap and 
   *         memory that has not yet been allocated to the heap.
   */
  public static float getFreeMemoryPercentage() {
    return (float)((float)Tabs.getFreeMemory() / (float)Tabs.getMaxHeapSize());
  }




  /**
   * Return the reference to the named gauge.
   * 
   * <p>This will always return an object; it may be a stub, or a working 
   * implementation depending upon the state of the fixture at the time. If 
   * gauges are enabled, then a working gauge is returned, otherwise a null 
   * gauge is returned.</p>
   * 
   * <p>Because the state of gauge operation can change over the operation of 
   * the fixture, it is not advisable to hold on to the reference between calls 
   * to the gauge. Always get the appropriate reference to the gauge
   * 
   * @param name the name of the gauge to return.
   * 
   * @return Either the 
   * 
   * @throws IllegalArgumentException if the name of the gauge is null
   */
  public static Gauge getGauge(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Gauge name is null");
    }

    Gauge retval = null;
    if (Tabs.gaugesEnabled) {
      synchronized (Tabs.gauges) {
        retval = (Gauge)Tabs.gauges.get(name);
        if (retval == null) {
          retval = new GaugeBase(name);
          Tabs.gauges.put(name, (Gauge)retval);
        }
      }
    } else {
      // just return the do-nothing gauge
      retval = Tabs.NULL_GAUGE;
    }

    return retval;
  }




  /**
   * Get an iterator over all the gauges in the fixture.
   */
  public static Iterator getGaugeIterator() {
    final ArrayList list = new ArrayList();
    synchronized (Tabs.gauges) {
      for (final Iterator it = Tabs.gauges.values().iterator(); it.hasNext(); list.add((Gauge)it.next())) {
        ;
      }
    }
    return list.iterator();
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
  public static float getHeapPercentage() {
    return (float)(1 - ((float)Tabs.getAvailableMemory() / (float)Tabs.getMaxHeapSize())); // percent of max
  }




  /**
   * @return The default IP address of this host.
   */
  public static InetAddress getHostIpAddress() {
    return Tabs.primaryInterface.getAddress().toInetAddress();
  }




  /**
   * @return The default DNS name of this host.
   */
  public static String getHostname() {
    return Tabs.primaryInterface.getDnsName();
  }




  /**
   * Return the identifier the fixture is using to differentiate itself from 
   * other fixtures on this host.
   * 
   * @return The identifier for this fixture.
   */
  public static String getId() {
    return Tabs.FIXTUREID;
  }




  /**
   * Return a string describing the source location of a given exception.
   * 
   * @param t The exception on which to report.
   * 
   * @return The class and line number throwing the exception.
   */
  public static String getLocation(final Throwable t) {
    final StringBuffer buffer = new StringBuffer();

    final StackTraceElement[] stack = t.getStackTrace();
    StackTraceElement elem = stack[(stack.length - 1)];

    buffer.append(elem.getClassName());
    buffer.append(".");
    buffer.append(elem.getMethodName());
    buffer.append("(");

    if (elem.getLineNumber() < 0) {
      buffer.append("Native Method");
    } else {
      buffer.append(elem.getFileName());
      buffer.append(":");
      buffer.append(elem.getLineNumber());
    }
    buffer.append(")");

    if (stack.length > 1) {
      buffer.append(" - root cause: ");

      elem = stack[0];

      // -- yes, duplicate code, but inline is still faster --
      buffer.append(elem.getClassName());
      buffer.append(".");
      buffer.append(elem.getMethodName());
      buffer.append("(");

      if (elem.getLineNumber() < 0) {
        buffer.append("Native Method");
      } else {
        buffer.append(elem.getFileName());
        buffer.append(":");
        buffer.append(elem.getLineNumber());
      }

      buffer.append(")");
    }
    return buffer.toString();
  }




  /**
   * Access the contents of the current Tabs log file.
   * 
   * <p>This will return the entire log file as a string and care should be 
   * taken to ensure the runtime does not run out of memory by trying to read 
   * in a very large log file.</p>
   *  
   * @return The entire log file as a string.
   */
  public static String getLogContents() {
    // TODO - perform file checks. What if the file is memory based?
    return FileUtil.fileToString(UriUtil.getFile(Tabs.getLogTarget()));
  }




  /**
   * Access the last lines in the current log file.
   * 
   * <p>Will return the last portion of the log file in a manner similar to the 
   * UNIX tail command.</p>
   * 
   * @param lines The number of lines to return. If the number is less than 
   *        one, then the default of 20 is used.
   * 
   * @return The tail portion of the current Tabs log file.
   */
  public static String getLogContents(final int lines) {
    // TODO - perform file checks. What if the file is memory based?
    final LinkedList entries = new LinkedList();
    int limit = lines;

    if (lines < 1) {
      limit = 20;
    }

    final File file = UriUtil.getFile(Tabs.getLogTarget());

    if (file.exists() && file.canRead()) {
      FileInputStream fin = null;
      String logLine;

      try {
        fin = new FileInputStream(file);
        final BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
        while ((logLine = myInput.readLine()) != null) {
          entries.add(logLine);
          if (entries.size() > limit) {
            entries.removeFirst();
          }
        }
      } catch (final Exception e) {}
      finally {
        try {
          if (fin != null) {
            fin.close();
          }
        } catch (final Exception ignore) {}
      }
    }

    final StringBuffer buffer = new StringBuffer();

    while (entries.size() > 0) {
      buffer.append((String)entries.removeFirst());
      if (entries.size() > 0) {
        buffer.append(Tabs.LF);
      }
    }

    return buffer.toString();
  }




  /**
   * @return The URI to the current log file.
   */
  public static URI getLogTarget() {
    return null;// ( (CyclingFileAppender)Tabs.logger ).getTarget();
  }




  /**
   * @return Returns the maximum number of events to keep in the MIB.
   */
  static int getMaxEvents() {
    return EventList.getMaxEvents();
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
  public static long getMaxHeapSize() {
    return Runtime.getRuntime().maxMemory();
  }




  /**
   * @return The netmask for this hosts primary interface or 255.255.255.255 if
   *         the netmask could not be otherwise determined.
   */
  public static String getNetmask() {
    return Tabs.primaryInterface.getNetmask().toString();
  }




  /**
   * @return  the primary IP interface on this host.
   */
  public static IpInterface getPrimaryInterface() {
    return Tabs.primaryInterface;
  }




  /**
   * @return The hostname without the domain portion
   */
  public static String getRelativeHostname() {
    return Tabs.primaryInterface.getRelativeHostname();
  }




  /**
   * @return The epoch time in milliseconds this fixture was started.
   */
  public static long getStartedTime() {
    return Tabs.startedTimestamp;
  }




  /**
   * Return the state with the given name.
   * 
   * <p>If the state does not exist, one will be created and added to the 
   * static list of states for later retrieval.</p>
   * 
   * @param name The name of the state to return.
   * 
   * @return The state with the given name.
   */
  public static State getState(final String name) {
    State state = null;
    synchronized (Tabs.states) {
      state = (State)Tabs.states.get(name);
      if (state == null) {
        state = new State(name);
        Tabs.states.put(name, state);
      }
    }
    return state;
  }




  /**
   * @return The number of states in the fixture at the present time.
   */
  public static int getStateCount() {
    return Tabs.states.size();
  }




  /**
   * Access an interator over the states.
   * 
   * <p>NOTE: this iterator is detached from the states in that the remove() 
   * call on the iterator will only affect the returned iterator and not the 
   * state collection in the fixture. If you wish to remove a state, you MUST 
   * call removeState(Counter) with the reference returned from this iterator 
   * as well.</p>
   * 
   * @return a detached iterator over the states.
   */
  public static Iterator getStateIterator() {
    final ArrayList list = new ArrayList();
    for (final Iterator it = Tabs.states.values().iterator(); it.hasNext(); list.add((State)it.next())) {
      ;
    }
    return list.iterator();
  }




  /**
   * @return All states as attributes of a packet.
   */
  public static DataFrame getStateMessage() {
    final DataFrame retval = new DataFrame();
    //retval.setType( "States" );

    synchronized (Tabs.states) {
      for (final Iterator it = Tabs.states.entrySet().iterator(); it.hasNext();) {
        final State stayt = (State)((Map.Entry)it.next()).getValue();
        try {
          retval.put(stayt.getName(), stayt.getValue());
        } catch (final Exception ignore) {}
      }
    }

    return retval;
  }




  /**
   * @return The subnet address for this hosts primary interface in CIDR format.
   */
  public static String getSubnet() {
    return Tabs.primaryInterface.getSubnet().toString();
  }




  /**
   * Get an iterator over all the Master Timers in the fixture.
   */
  public static Iterator getTimerIterator() {
    final ArrayList list = new ArrayList();
    synchronized (Tabs.masterTimers) {
      for (final Iterator it = Tabs.masterTimers.values().iterator(); it.hasNext(); list.add((TimingMaster)it.next())) {
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
  public static TimingMaster getTimerMaster(final String name) {
    synchronized (Tabs.masterTimers) {
      return (TimingMaster)Tabs.masterTimers.get(name);
    }
  }




  public static DataFrame getTimerDataFrame() {
    final DataFrame retval = new DataFrame();
    //retval.setType( "Timers" );

    synchronized (Tabs.masterTimers) {
      for (final Iterator it = Tabs.masterTimers.entrySet().iterator(); it.hasNext();) {
        final TimingMaster timer = (TimingMaster)((Map.Entry)it.next()).getValue();
        //TODO: retval.add( timer.toMessage() );
      }
    }
    return retval;
  }




  /**
   * Return how long the fixture has been active in a format using only the 
   * significant time measurements.
   * 
   * <p>Significant measurements means if the number of seconds extend past 24 
   * hours, then only report the days and hours skipping the minutes and 
   * seconds. Examples include <tt>4m 23s</tt> or <tt>22d 4h</tt>. The format 
   * is designed to make reporting fixture uptime more polished.</p>
   * 
   * @return the time the fixture has been active in a reportable format.
   */
  public static String getUptimeString() {
    return DateUtil.formatSignificantElapsedTime((System.currentTimeMillis() - Tabs.startedTimestamp) / 1000);
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
  public static float getUsedMemory() {
    return Tabs.getMaxHeapSize() - Tabs.getFreeMemory();
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
  public static float getUsedMemoryPercentage() {
    return (float)(1 - ((float)Tabs.getFreeMemory() / (float)Tabs.getMaxHeapSize())); // percent of max
  }




  /**
   * @return  Returns the workDirectory.
   * @uml.property  name="workDirectory"
   */
  static File getWorkDirectory() {
    return Tabs.workDirectory;
  }




  /**
   * Increase the value with the given name by the given amount.
   * 
   * <p>This method retrieves the counter with the given name or creates one by 
   * that name if it does not yet exist. The retrieved counter is then 
   * increased by the given amount.</p> 
   * 
   * @param tag The name of the counter to increase.
   * 
   * @return The final value of the counter after the operation.
   */
  public static long increase(final String tag, final long value) {
    return Tabs.getCounter(tag).increase(value);
  }




  /**
   * Increment the value with the given name.
   * 
   * <p>This method retrieves the counter with the given name or creates one by 
   * that name if it does not yet exist. The retrieved counter is then 
   * increased by one (1).</p> 
   * 
   * @param tag The name of the counter to increment.
   * 
   * @return The final value of the counter after the operation.
   */
  public static long increment(final String tag) {
    return Tabs.getCounter(tag).increment();
  }




  /**
   * Process the given exception in a uniform manner.
   * 
   * <p>This method will log the exception, generate an event for the exception 
   * and send the exception to the event queue for agents to pick-up.</p>
   * 
   * @param t The excpetion to process
   * 
   * @return The identifier of the event created. This can later be used to 
   *         retrieve the event from the event list.  
   */
  public static long process(final Throwable t) {
    return Tabs.process(t, null, null, null, null);
  }




  /**
   * Process the given exception in a uniform manner.
   * 
   * <p>This method will log the exception, generate an event for the exception 
   * and send the exception to the event queue for agents to pick-up.</p>
   * 
   * @param t The excpetion to process
   * @param info more information about the exception
   * 
   * @return The identifier of the event created. This can later be used to 
   *         retrieve the event from the event list.  
   */
  public static long process(final Throwable t, final String info) {
    return Tabs.process(t, info, null, null, null);
  }




  /**
   * Process the given exception in a uniform manner.
   * 
   * <p>This method will log the exception, generate an event for the exception 
   * and send the exception to the event queue for agents to pick-up.</p>
   * 
   * @param t The excpetion to process
   * @param info more information about the exception
   * @param appId A correlating identifier representing the application.
   * @param sysId A correlating identifier representing a subsystem in the application.
   * @param cmpId A correlating identifier representing a component within a subsystem.
   * 
   * @return The identifier of the event created. This can later be used to 
   *         retrieve the event from the event list.  
   */
  public static long process(final Throwable t, final String info, final String appId, final String sysId, final String cmpId) {
    // create an event from the exception
    final AppEvent event = Tabs.eventList.createEvent(appId, sysId, cmpId, info, AppEvent.WARNING, 0, 0, "EXCEPTION");

    final StringBuffer b = new StringBuffer();
    if (info != null) {
      b.append(info);
      b.append(Tabs.LF);
    }

    // Some helpful information in tracking down the source of the exception
    b.append("EXCEPTION: ");
    b.append(t.getClass().getName());

    b.append(Tabs.LF);
    b.append("Message: ");
    b.append(t.getMessage());

    b.append(Tabs.LF);
    b.append("Location: ");
    b.append(Tabs.getLocation(t));

    b.append(Tabs.LF);
    b.append("EventID: ");
    b.append(event.getSequence());

    b.append(Tabs.LF);
    b.append("RuntimeID: ");
    b.append(Tabs.getId());

    b.append(Tabs.LF);
    b.append("Host: ");
    b.append(Tabs.getHostname());
    b.append("(");
    b.append(Tabs.getHostIpAddress().getHostAddress());
    b.append(")");

    b.append(Tabs.LF);
    b.append("Runtime: Java ");
    b.append(System.getProperty("java.version"));
    b.append("(");
    b.append(System.getProperty("java.vendor"));
    b.append(")");

    b.append(Tabs.LF);
    b.append("Platform: ");
    b.append(System.getProperty("os.arch"));
    b.append(" OS: ");
    b.append(System.getProperty("os.name"));
    b.append("(");
    b.append(System.getProperty("os.version"));
    b.append(")");

    b.append(Tabs.LF);
    b.append("Heap Memory: ");
    b.append(Tabs.getMaxHeapSize());
    b.append(" bytes max, ");
    b.append(Tabs.getCurrentHeapSize());
    b.append(" bytes (");
    b.append(Tabs.getHeapPercentage() * 100);
    b.append("%) allocated");
    b.append(Tabs.LF);
    b.append("Free Memory: ");
    b.append(Tabs.getFreeMemory());
    b.append(" bytes (");
    b.append(Tabs.getFreeMemoryPercentage() * 100);
    b.append("%)");

    b.append(Tabs.LF);
    b.append("Thread: name='");
    b.append(Thread.currentThread().getName());
    b.append("\' type=");
    if (Thread.currentThread().isDaemon()) {
      b.append("daemon, group='");
    } else {
      b.append("user, group='");
    }

    b.append(Thread.currentThread().getThreadGroup().getName());
    b.append("'");
    b.append(Tabs.LF);
    b.append("Thread Call Stack:");
    b.append(Tabs.LF);
    b.append(ExceptionUtil.stackTrace(t));

    // set the exception details in the event
    event.setMessage(b.toString());

    // Exceptions should be logged
    Log.warn(b.toString());

    // final long retval = Tabs.oamManager.sendEvent( event );
    final long retval = event.getSequence();

    return retval;
  }




  /**
   * Add the given component / identifier to the fixture.
   * 
   * <p>If the argument is a string, then the argument will be used to only 
   * record the identifier of the component. If the argument implements 
   * <tt>ManageableComponent</tt>, then the component identifier will be 
   * extracted from the component and a reference to the passed component will 
   * be stored for later reporting operations.</p>
   * 
   * <p>This is helpful when it is desired to know what components are residing 
   * with this fixture. Often other components are required to report if a 
   * particular set of components are loaded, and this facility gives those 
   * components a place to register.</p>
   * 
   * @see #removeComponent(String)
   * 
   * @param cmpnt The component identifier to add
   */
  public static void registerComponent(final Object cmpnt) {
    String cmpntid = null;
    if (cmpnt != null) {
      if (cmpnt instanceof String) {
        cmpntid = (String)cmpnt;
        if (cmpntid.length() > 0) {
          synchronized (Tabs.componentMap) {
            if (!Tabs.componentMap.containsKey(cmpntid)) {
              Tabs.componentMap.put(cmpntid, null);
            }
          }
        }
      } else if (cmpnt instanceof Component) {
        cmpntid = ((Component)cmpnt).getId();
        if ((cmpntid != null) && (cmpntid.length() > 0)) {
          synchronized (Tabs.componentMap) {
            if (Tabs.componentMap.containsKey(cmpntid)) {
              final Exception ex = new Exception();
              ex.fillInStackTrace();

              Log.warn("Overwriting existing named component '" + cmpntid + "' - Make sure component names are unique! Stacktrace:\r\n" + ExceptionUtil.stackTrace(ex));

            }
            Tabs.componentMap.put(cmpntid, (Component)cmpnt);
          }
        }
      }

      // else, try to reflect into the class for a getId() method

      // If all else fails, then just use the classname
      else {
        cmpntid = cmpnt.getClass().getName();
        synchronized (Tabs.componentMap) {
          Tabs.componentMap.put(cmpntid, null);
        }
      }
    } else {
      Log.error("Can not register a NULL component");
    }

  }




  /**
   * Remove the given component identifier from the fixture.
   * 
   * <p>Components that registered their logical identifiers can use this
   * method to remove their registration when those components terminate or 
   * otherwise wish to record their absence.</p>
   * 
   * @param cmpntid The component identifier to remove
   */
  public static void removeComponent(final String cmpntid) {
    if ((cmpntid != null) && (cmpntid.length() > 0)) {
      synchronized (Tabs.componentMap) {
        Tabs.componentMap.remove(cmpntid);
      }
    }
  }




  /**
   * Remove the counter with the given name.
   * 
   * @param name Name of the counter to remove.
   * 
   * @return The removed counter.
   */
  public static Counter removeCounter(final String name) {
    synchronized (Tabs.counters) {
      return (Counter)Tabs.counters.remove(name);
    }
  }




  /**
   * Remove the state with the given name.
   * 
   * @param name Name of the state to remove.
   * 
   * @return The removed state.
   */
  public static State removeState(final String name) {
    if (name == null) {
      return null;
    }

    synchronized (Tabs.states) {
      return (State)Tabs.states.remove(name);
    }
  }




  /**
   * Reset the counter with the given name returning a copy of the counter 
   * before the reset occurred.
   * 
   * <p>The return value will represent a copy of the counter prior to the 
   * reset and is useful for applications that desire delta values. These delta
   * values are simply the return values of successive reset calls.</p>
   * 
   * <p>If the counter does not exist, it will be created prior to being reset.
   * The return value will reflect an empty counter with the given name.</p>
   *
   * @param name The name of the counter to reset.
   *  
   * @return a counter containing the values of the counter prior to the reset.
   */
  public static Counter resetCounter(final String name) {
    Counter retval = null;
    synchronized (Tabs.counters) {
      retval = Tabs.getCounter(name).reset();
    }

    return retval;
  }




  /**
   * Reset and clear-out the named gauge.
   * 
   * @param name The name of the gauge to clear out.
   */
  public static void resetGauge(final String name) {
    if ((name != null) && (name.length() > 0)) {
      Tabs.getGauge(name).reset();
    }
  }




  /**
   * Removes all timers from the fixture and frees them up for garbage 
   * collection.
   */
  public static void resetTimers() {
    synchronized (Tabs.masterTimers) {
      Tabs.masterTimers.clear();
    }
  }




  /**
   * Set the maximum number of events to keep in the MIB.
   * 
   * @param max The maximum number of events to keep.
   */
  static void setMaxEvents(final int max) {
    EventList.setMaxEvents(max);
  }




  /**
   * Set the named state to the given value.
   * 
   * @param name The name of the state to set.
   * 
   * @param value The value to set in the state.
   */
  public static void setState(final String name, final double value) {
    Tabs.getState(name).set(value);
  }




  /**
   * Set the named state to the given value.
   * 
   * @param name The name of the state to set.
   * 
   * @param value The value to set in the state.
   */
  public static void setState(final String name, final long value) {
    Tabs.getState(name).set(value);
  }




  /**
   * Set the named state to the given value.
   * 
   * @param name The name of the state to set.
   * 
   * @param value The value to set in the state.
   */
  public static void setState(final String name, final String value) {
    if ((name != null) && (name.length() != 0)) {
      if (value == null) {
        Tabs.removeState(name);
      } else {
        Tabs.getState(name).set(value);
      }
    }
  }




  /**
   * Start an Application Response Measurement transaction.
   * 
   * @param tag Grouping tag.
   * 
   * @return A transaction to collect ARM data.
   */
  public static ArmTransaction startArm(final String tag) {
    return Tabs.startArm(tag, null);
  }




  /**
   * Start an Application Response Measurement transaction using a particular
   * correlation identifier.
   * 
   * @param tag Grouping tag.
   * @param crid correlation identifier
   * 
   * @return A transaction to collect ARM data.
   */
  public static ArmTransaction startArm(final String tag, final String crid) {
    ArmTransaction retval = null;
    if (Tabs.armEnabled) {
      synchronized (Tabs.armMasters) {
        // get an existing ARM master or create a new one
        ArmMaster master = (ArmMaster)Tabs.armMasters.get(tag);
        if (master == null) {
          master = new ArmMaster(tag);
          Tabs.armMasters.put(tag, master);
        }

        // have the master ARM return a transaction instance
        retval = master.createArm(tag, crid);

        //start the ARM transaction
        retval.start();
      }
    } else {
      // just return the do-nothing timer
      retval = Tabs.NULL_ARM;
    }

    return retval;
  }




  /**
   * Start a timer with the given name.
   * 
   * <p>Use the returned Timer to stop the interval measurement.</p>
   *  
   * @param tag The name of the timer instance to start.
   * 
   * @return The timer instance that should be stopped when the interval is 
   *         completed.
   */
  public static Timer startTimer(final String tag) {
    Timer retval = null;
    if (Tabs.timingEnabled) {
      synchronized (Tabs.masterTimers) {
        // get an existing master timer or create a new one
        TimingMaster master = (TimingMaster)Tabs.masterTimers.get(tag);
        if (master == null) {
          master = new TimingMaster(tag);
          Tabs.masterTimers.put(tag, master);
        }

        // have the master timer return a timer instance
        retval = master.createTimer();

        //start the timer instance
        retval.start();
      }
    } else {
      // just return the do-nothing timer
      retval = Tabs.NULL_TIMER;
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
  public static void updateGauge(final String name, final long value) {
    if ((name != null) && (name.length() > 0)) {
      Tabs.getGauge(name).update(value);
    }
  }




  /**
   * Zip a single file into a compressed ZIP archive.
   * 
   * <p>This will result in a compressed archive that contain a single file 
   * entry. The name of the archive will be the name of the source file with 
   * the added extension of 'zip'.</p>
   * 
   * @param source The file to archive.
   * 
   * @throws IOException If problems were experienced.
   */
  private static void zipFile(final File source) throws IOException {
    BufferedInputStream origin = null;
    ZipOutputStream out = null;
    try {
      final FileOutputStream dest = new FileOutputStream(source.getAbsolutePath() + ".zip");
      out = new ZipOutputStream(new BufferedOutputStream(dest));

      final byte data[] = new byte[Tabs.STREAM_BUFFER_SIZE];

      final FileInputStream fi = new FileInputStream(source);
      origin = new BufferedInputStream(fi, Tabs.STREAM_BUFFER_SIZE);

      final ZipEntry entry = new ZipEntry(source.getName());
      out.putNextEntry(entry);

      int count;
      while ((count = origin.read(data, 0, Tabs.STREAM_BUFFER_SIZE)) != -1) {
        out.write(data, 0, count);
      }
    } catch (final FileNotFoundException e) {
      throw new IOException(e.getMessage());
    }
    finally {
      try {
        origin.close();
      } catch (final IOException ignore) {}
      try {
        out.close();
      } catch (final IOException ignore) {}
    }
  }




  public void finalize() throws Throwable {
    shutdown("Finalization");
  }




  /**
   * This will initialize the environment for the fixture.
   */
  private void open() {
    // Get the primary interface for this host
    Tabs.primaryInterface = IpInterface.getPrimary();

    // Make sure we have a home directory we can use
    if (System.getProperty(Tabs.HOMEDIR_TAG) == null) {
      System.setProperty(Tabs.HOMEDIR_TAG, Tabs.DEFAULT_HOME);
    } else {
      // Normalize the "." that sometimes is set in the SG_HOME property
      if (System.getProperty(Tabs.HOMEDIR_TAG).trim().equals(".")) {
        System.setProperty(Tabs.HOMEDIR_TAG, Tabs.DEFAULT_HOME);
      } else if (System.getProperty(Tabs.HOMEDIR_TAG).trim().length() == 0) {
        // catch empty home property and just use the home directory
        System.setProperty(Tabs.HOMEDIR_TAG, Tabs.DEFAULT_HOME);
      }
    }

    // Remove all the relations and extra slashes from the home path
    System.setProperty(Tabs.HOMEDIR_TAG, FileUtil.normalizePath(System.getProperty(Tabs.HOMEDIR_TAG)));

    // Check for a configured Fixture Id
    String prop = System.getProperty(Tabs.ID_TAG);
    if (prop != null) {
      Tabs.FIXTUREID = prop;
    }

    // Check for a configured Fixture Group
    prop = System.getProperty(Tabs.GROUP_TAG);
    if (prop != null) {
      Tabs.FIXTUREGRP = prop;
    }
  }




  /**
   * Called by the shutdown hook to make sure everything is closed properly.
   */
  private void shutdown(final String msg) {}
}

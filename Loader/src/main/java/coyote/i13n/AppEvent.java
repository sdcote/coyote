/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.i13n;

/**
 * The AppEvent class models an base class for an occurrence to be noted.
 *
 * <p>AppEvents are used to store significant events in an application. These
 * are often errors which can easily get lost in log files. The Scorecard holds
 * events separately and allow the system to handle them in a manner apart from
 * simply recording them to a log file for later analysis.
 *
 * <p>A basic assumption is made that all applications are made up of systems
 * and those systems are made up of components. Each of these tiers can be
 * identified and the composite business key of application-system-component
 * can be used to identify the source of events in very large, complex
 * environments.
 *
 * <p>It is also assumed that operations uses the concept of codes to represent
 * events in a language independent manner. Events can contain a message for
 * local operations staff, but using a major-minor code key into a resource
 * file enables the use of language specific resources to be used in
 * representing the meaning of the event. Using a major code to categorize the
 * event can speed resolution workflows and automated processing while minor
 * event codes can give more details. It is very common in large data centers
 * to see troubleshooting manuals list resolutions by event codes and this
 * class continues support for this effective approach.
 *
 * <p>Scorecards hold a reference to a buffer of events. When an event is added
 * and the buffer is full, the oldest event is dropped to make room for the new
 * one. This keeps memory under control, but can cause problems during garbage
 * collection if the application is experiencing an event storm.
 */
public class AppEvent {
  /**
   * Critical Severity.<p>Indicates that a service affecting fault condition
   * has occurred which prevents system function, and an immediate corrective
   * response is required.
   */
  public static final int CRITICAL = 5;
  /**
   * Major Severity.<p>Indicates that a service affecting fault condition has
   * occurred which inhibits system functionality, and an urgent corrective
   * response is required.
   */
  public static final int MAJOR = 4;
  /**
   * Minor Severity.<p>Indicates that a non-service affecting fault condition
   * has occurred, and that corrective response should be taken to prevent a
   * more serious fault condition.
   */
  public static final int MINOR = 3;
  /**
   * Warning Severity.<p>Indicates that a non-service affecting, non-fault
   * condition exists, and that corrective response should be taken to prevent
   * a fault condition.
   */
  public static final int WARNING = 2;
  /**
   * Normal Severity.<p>Indicates an informational event, not a problem of
   * any sort.
   */
  public static final int NORMAL = 1;
  /**
   * Indeterminate Severity.<p>Indicates that the severity level cannot be
   * determined.
   */
  public static final int INDETERMINATE = 0;
  public static final String[] severities = { "unknown", "normal", "warning", "minor", "major", "critical" };




  public static String getSeverityString( final int sev ) {
    if ( ( sev > -1 ) && ( sev < AppEvent.severities.length ) ) {
      return AppEvent.severities[sev];
    } else {
      return AppEvent.severities[0];
    }
  }

  private String _msg = null;

  private long _time = 0;
  private int _maj = 0;

  private int _min = 0;
  private String _cls = null;
  private final String _appId;
  private final String _sysId;
  private final String _cmpId;
  private long _seq = -1;

  /** The severity of the event */
  private int _sev = 0;

  /** Time when the event was cleared. */
  private long _clrd = 0;

  /** Reason why the alert was cleared */
  private String _rsn = null;

  /** The list to which this event belongs */
  private EventList _lst = null;




  /**
   * Constructor used by parsers when constructing an event foreign to this
   * runtime.
   *
   * <p>There is no reference to a list and the sequence number is NOT
   * automatically assigned.
   *
   * @param seq the sequence of this event in the scorecard.
   * @param appid
   * @param sysid
   * @param cmpid
   * @param msg
   * @param sv
   * @param maj
   * @param min
   * @param cls
   */
  public AppEvent( final long seq, final String appid, final String sysid, final String cmpid, final String msg, final int sv, final int maj, final int min, final String cls ) {
    _time = System.currentTimeMillis();
    _appId = appid;
    _sysId = sysid;
    _cmpId = cmpid;
    _maj = maj;
    _min = min;
    _cls = cls;
    _msg = msg;
    _seq = seq;
    _sev = sv;
    _lst = null;
  }




  /**
   * This constructs an event with all the fields populated.
   *
   * <p>It is expected that the scorecard will generate this event when asked and populate some of the attributes of this class for the caller.
   *
   * @param seq the sequence of this event in the scorecard.
   * @param appid Application identifier (e.g. Circuit Provisioning)
   * @param sysid System identifier (e.g. Reporting)
   * @param cmpid Component Identifier (e.g. ABB Recloser Driver)
   * @param msg A description of the event (e.g. 'Could not connect to field device')
   * @param sv The severity of the event (e.g. 5 for 'critical')
   * @param maj Major code describing the event. This is like a classification or general category of the event.
   * @param min Minor code of the event. This provides fine-grained identification of the event when combined with the major code.
   * @param cls The classification of the event (e.g. 'Network Error')
   * @param list The event list to which this event belongs.
   */
  AppEvent( final long seq, final String appid, final String sysid, final String cmpid, final String msg, final int sv, final int maj, final int min, final String cls, final EventList list ) {
    _time = System.currentTimeMillis();
    _appId = appid;
    _sysId = sysid;
    _cmpId = cmpid;
    _maj = maj;
    _min = min;
    _cls = cls;
    _msg = msg;
    _seq = seq;
    _sev = sv;
    _lst = list;

  }




  /**
   * Clear the event so it is no longer pending.
   */
  public void clear() {
    _clrd = System.currentTimeMillis();
    if ( _lst != null ) {
      _lst.remove( this );
    }
  }




  /**
   * Clear the event for the given reason.
   *
   * @param reason Reason why the event was cleared.
   */
  public void clear( final String reason ) {
    clear();
    _rsn = reason;
  }




  /**
   * @return  Returns the application ID.
   */
  public String get_appId() {
    return _appId;
  }




  /**
   * @return  Returns the component ID.
   */
  public String get_cmpId() {
    return _cmpId;
  }




  /**
   * @return  Returns the system ID.
   */
  public String get_sysId() {
    return _sysId;
  }




  /**
   * @return Returns the Classification of this event.
   */
  public String getClassification() {
    return _cls;
  }




  /**
   * @return Returns the major event code.
   */
  public int getMajorCode() {
    return _maj;
  }




  /**
   * @return Returns the message.
   */
  public String getMessage() {
    return _msg;
  }




  /**
   * @return Returns the minor event code.
   */
  public int getMinorCode() {
    return _min;
  }




  /**
   * @return The reason the event was cleared or null if the event has not been
   *         cleared or no reason was given.
   */
  public String getReasonCleared() {
    return _rsn;
  }




  /**
   * @return Returns the sequence.
   */
  public long getSequence() {
    return _seq;
  }




  /**
   * @return Returns the severity of the event.
   */
  public int getSeverity() {
    return _sev;
  }




  public String getSeverityString() {
    return AppEvent.getSeverityString( _sev );

  }




  /**
   * @return The epoch time in milliseconds.
   */
  public long getTime() {
    return _time;
  }




  /**
   * @return The epoch time in milliseconds when the event was cleared or 0 if
   *         the event has not been cleared.
   */
  public long getTimeCleared() {
    return _clrd;
  }




  /**
   * @param msg The message text to set.
   */
  public void setMessage( final String msg ) {
    _msg = msg;
  }




  /**
   * @param sev The severity to set.
   */
  public void setSeverity( final int sev ) {
    _sev = sev;
  }




  /**
   * @param millis The current epoch time in milliseconds to set.
   */
  public void setTime( final long millis ) {
    _time = millis;
  }




  @Override
  public String toString() {
    return new String( "Event[" + _cls + "|" + _appId + "|" + _sysId + "|" + _cmpId + "]:" + _seq + ":" + _sev + ":" + ":" + _maj + "." + _min + ":" + _time + ":" + _msg );
  }
}

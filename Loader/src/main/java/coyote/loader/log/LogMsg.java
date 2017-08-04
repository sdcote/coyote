/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Retrieves messages from a message {@link ResourceBundle} and formats them as 
 * appropriate using {@link java.text.MessageFormat}. Instances of this object 
 * are mostly read-only - the only thing you can change is the locale. Changing 
 * the locale will attempt to convert the message to that new locale's format.
 *
 * <p><code>LogMsg</code> objects are serializable and will be re-translated 
 * once it is deserialized. This means that a message could be localized in the 
 * English language, serialized and sent over the wire to another JVM with a 
 * German locale and when the deserialized message is retrieved again, it will 
 * be in German. This feature assumes a resource bundle with the same base 
 * bundle name exists in the JVM that deserialized the <code>LogMsg</code>. If 
 * it does not, then the original message will be used (in the previous 
 * example, it would mean the English message would be retrieved in the JVM, 
 * even though its locale is German).</p>
 *
 * <p>You always specify the base bundle name first, followed by the locale, 
 * the bundle key and the variable list of arguments that go with the keyed 
 * message (all in that order). Bundle name and locale are both optional. When 
 * you need to specify a <code>Throwable</code> with your localized message, it 
 * is specified before those parameters. Again, this is consistent both in this 
 * class and the localized exception classes.</p>
 */
public class LogMsg implements Serializable {
  /**
   * The purpose of this class is to offer a strongly typed object (more 
   * strongly than String) so we can pass bundle base names to our args methods 
   * and not have this be confused with a key or arg parameter.
   */
  public static final class BundleBaseName implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bName;




    /**
     * Creates a new {@link BundleBaseName} object.
     *
     * @param bundleBaseName the bundle base name string
     */
    public BundleBaseName(final String bundleBaseName) {
      bName = bundleBaseName;
    }




    /**
     * Returns the bundle base name.
     *
     * @return the bundle base name.
     */
    public String getBundleBaseName() {
      return bName;
    }




    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return getBundleBaseName();
    }
  }

  /**
   * Serializable UID
   */
  private static final long serialVersionUID = 139484731487L;

  /**
   * Default resource bundle base name. This is not final - you can change this 
   * via {@link LogMsg#setBundleBaseNameDefault(BundleBaseName)}. Because it 
   * is static, this value is not serialized when the object is serialized. 
   * This is probably OK; we can assume if we move over to another VM, we 
   * probably will want to go back to the original bundle name default.
   */
  private static BundleBaseName bundleBasenameDefault = new BundleBaseName("LogMsg");




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns.</p>
   *
   * @param basename the base name of the resource bundle
   * @param locale the locale to determine what bundle to use
   * @param key the resource bundle key name
   * @param args arguments to help fill in the resource bundle message
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final BundleBaseName basename, final Locale locale, final String key, final Object[] args) {
    final LogMsg msg = new LogMsg(basename, locale);
    msg.getMsg(key, args);
    return msg;
  }




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns.</p>
   *
   * @param basename the base name of the resource bundle
   * @param key the resource bundle key name
   * @param args arguments to help fill in the resource bundle message
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final BundleBaseName basename, final String key, final Object... args) {
    final LogMsg msg = new LogMsg(basename);
    msg.getMsg(key, args);
    return msg;
  }




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns. A default basename is used along 
   * with the given locale.</p>
   *
   * @param locale the locale to determine what bundle to use
   * @param key the resource bundle key name
   * @param args arguments to help fill in the resource bundle message
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final Locale locale, final String key, final Object... args) {
    final LogMsg msg = new LogMsg(locale);
    msg.getMsg(key, args);
    return msg;
  }




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns. A default basename and the 
   * default locale is used to determine what resource bundle to use.</p>
   *
   * @param key the resource bundle key name
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final String key) {
    final LogMsg msg = new LogMsg();
    msg.getMsg(key, new Object[]{});
    return msg;
  }




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns. A default basename and the 
   * default locale is used to determine what resource bundle to use.</p>
   *
   * @param key the resource bundle key name
   * @param args arguments to help fill in the resource bundle message
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final String key, final Object... args) {
    final LogMsg msg = new LogMsg();
    msg.getMsg(key, args);
    return msg;
  }




  /**
   * Creates a {@link LogMsg message} object and automatically looks up the 
   * given resource bundle message. 
   * 
   * <p>The caller need only call {@link LogMsg#toString()} to get the resource 
   * bundle message after this method returns. A default basename and the 
   * default locale is used to determine what resource bundle to use.</p>
   *
   * @param key the resource bundle key name
   * @param arg argument to help fill in the resource bundle message
   *
   * @return if the message was logged, a non-<code>null</code> LogMsg object 
   *         is returned
   */
  public static LogMsg createMsg(final String key, final Object arg) {
    return createMsg(key, new Object[]{arg});
  }




  /**
   * Deserializes the given serialization data and returns the object.
   *
   * @param serializedData the serialized data as a byte array
   *
   * @return the deserialized object
   *
   * @throws Exception if failed to deserialize the object
   */
  protected static Object deserialize(final byte[] serializedData) throws Exception {
    final ByteArrayInputStream byteStream = new ByteArrayInputStream(serializedData);
    ObjectInputStream ois;
    Object retObject;

    ois = new ObjectInputStream(byteStream);
    retObject = ois.readObject();
    ois.close();

    return retObject;
  }




  /**
   * Returns the default bundle base name that all instances of this class will 
   * use when no basename is provided.
   *
   * @return the bundle base name default (e.g. "my.messages")
   */
  public static BundleBaseName getBundleBaseNameDefault() {
    return LogMsg.bundleBasenameDefault;
  }




  /**
   * Given a serializable object, this will return the object's serialized byte 
   * array representation.
   *
   * @param object the object to serialize
   *
   * @return the serialized bytes
   *
   * @throws Exception if failed to serialize the object
   */
  protected static byte[] serialize(final Serializable object) throws Exception {
    final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream oos;

    oos = new ObjectOutputStream(byteStream);
    oos.writeObject(object);
    oos.close();

    return byteStream.toByteArray();
  }




  /**
   * Sets the default bundle base name that all instances of this class will 
   * use when no basename is provided.
   *
   * @param newDefault the new bundle base name default (e.g. "my.messages")
   */
  public static void setBundleBaseNameDefault(final BundleBaseName newDefault) {
    LogMsg.bundleBasenameDefault = newDefault;
  }

  /**
   * The resource bundle's base name, used in conjunction with the locale to 
   * determine which actual resource bundle to find the message in.
   */
  private BundleBaseName bundleBaseName;

  /**
   * The locale used to determine which actual resource bundle to find the 
   * message in.
   */
  private Locale locale;

  /**
   * The bundle message that was found last.
   */
  private String lastMessage;

  /**
   * The last resource bundle key that was used to get the last message.
   */
  private String lastKey;

  /**
   * The last set of variable arguments that was used to get the last message. 
   * 
   * <p>This may be <code>null</code> if this instance never retreived a 
   * message or this instance was serialized and one or more of the args 
   * objects was not serializable.<p>
   */
  private Object[] lastArgs;

  /**
   * Localized resource bundle used to look up messages
   */
  private transient ResourceBundle bundle;

  /**
   * A flag used to indicate that the last call to 
   * {@link #getMsg(String, Object[])} failed to obtain the message 
   * successfully from the resource bundle. Its an internal flag that is 
   * allowed to be transient, no need to serialize it.
   */
  private transient boolean getFailed;




  /**
   * Initializes the message repository using the default, localized resource 
   * bundle.
   */
  public LogMsg() {
    this(LogMsg.bundleBasenameDefault, Locale.getDefault());
  }




  /**
   * Initializes the message repository using the default locale and given 
   * resource bundle base name.
   *
   * @param basename resource bundle to use
   *
   * @see LogMsg#LogMsg(BundleBaseName, Locale)
   */
  public LogMsg(final BundleBaseName basename) {
    this(basename, Locale.getDefault());
  }




  /**
   * Initializes the message repository with the appropriate resource bundle.
   *
   * @param basename resource bundle to use, if <code>null</code> uses the
   * {@link #getBundleBaseNameDefault() default}.
   * @param locale locale used to determine proper resource bundle to use, if 
   * <code>null</code> uses the default locale of the JVM.
   */
  public LogMsg(BundleBaseName basename, Locale locale) {
    if (basename == null) {
      basename = LogMsg.bundleBasenameDefault;
    }

    if (locale == null) {
      locale = Locale.getDefault();
    }

    getFailed = false;
    bundleBaseName = basename;
    this.locale = locale;
    bundle = null;
    lastMessage = null;
    lastKey = null;
    lastArgs = null;
  }




  /**
   * Initializes the message repository using the default resource bundle and 
   * given locale.
   *
   * @param locale locale used to determine proper resource bundle to use
   *
   * @see LogMsg#LogMsg(BundleBaseName, Locale)
   */
  public LogMsg(final Locale locale) {
    this(LogMsg.bundleBasenameDefault, locale);
  }




  /**
   * Returns the base name of the resource bundles to be used when looking for 
   * messages.
   *
   * @return the resource bundles' base name
   */
  public BundleBaseName getBundleBaseName() {
    return bundleBaseName;
  }




  /**
   * Returns the last message that this instance read from a resource bundle.
   *
   * <p>This object can change the messages it finds by simply 
   * {@link #setLocale(Locale) setting the locale}. When you change the local, 
   * the message is retrieved again when this method is called and is stored as 
   * the last message.
   *
   * @return the last retrieved resource bundle message
   */
  public String getLastMessage() {
    // If bundle was not yet set, we've either never retreived a message or
    // we've been serialized to another VM or someone set a new locale. In 
    // either of these cases, we need to get the message again.
    if (bundle == null) {
      // Note that if the last args is null, that means the deserialization of 
      // the args failed. In this case, we don't want to get the message since 
      // we've now lost some of the message data. We will rely on the last 
      // message that hopefully contains all the data, albeit in a different 
      // locale (but at least the data isn't lost).
      if (lastArgs != null) {
        final String lastMessageBackup = lastMessage;

        getMsg(lastKey, lastArgs);

        if (getFailed) {
          lastMessage = lastMessageBackup;
        }
      }
    }

    return lastMessage;
  }




  /**
   * Sets a new locale. This allows this class to change the message it returns 
   * after this instance has already been constructed.
   *
   * @return the current locale used by this class to determine which bundle to 
   * find the message in
   */
  public Locale getLocale() {
    return this.locale;
  }




  /**
   * Returns the message string identified with the given key. 
   * 
   * <p>The additional arguments replace any placeholder found in the message. 
   * This sets the {@link #getLastMessage()} when it returns.</p>
   *
   * @param key identifies the message to be retrieved
   * @param args arguments to replace placeholders in message
   *
   * @return localized and formatted message
   *
   * @see java.text.MessageFormat
   */
  public String getMsg(final String key, final Object[] args) {
    String retval = null;

    // See if we can find the bundle that has our new locale's messages. If we 
    // can't, this usually means this object was serialized and sent to another 
    // VM that doesn't have the resource bundles.
    // Any exception in here will fall back to using the key and args as the 
    // last message unless the resource message was found (but failed to be 
    // formatted) - in that case, the resource message will be used.
    // In either case, the args will be returned in the standard Java 
    // List.toString format.
    try {
      if (bundle == null) {
        bundle = getResourceBundle();
      }

      retval = bundle.getString(key);

      if (args.length > 0) {
        final MessageFormat mf = new MessageFormat(retval, locale);
        retval = mf.format(args);
      }

      // remember these in case we are asked to get the message again in a 
      // different locale or we need to reconstruct the message after 
      // serialization
      lastKey = key;
      lastArgs = args;

      // everything is OK
      getFailed = false;
    } catch (final Exception e) {
      getFailed = true;

      if (retval == null) {
        retval = key;
      }

      //final Formatter formatter = new Formatter();
      //formatter.format( "Missing resource message key=[%s] args=%s", args );

      retval = "Missing resource message key=[" + key + "]";
    }

    lastMessage = retval;

    return retval;
  }




  /**
   * Gets the resource bundle to use, based on the current values of 
   * {@link #getBundleBaseName() the base name} and the 
   * {@link #getLocale() locale}.
   *
   * @return the resource bundle to be used by this object when looking up 
   *         messages
   */
  protected ResourceBundle getResourceBundle() {
    final Locale locale = getLocale();
    final BundleBaseName basename = getBundleBaseName();
    final ResourceBundle bundle = ResourceBundle.getBundle(basename.getBundleBaseName(), locale);

    return bundle;
  }




  /**
   * <code>ResourceBundle</code> is not serializable so this deserializes the 
   * base bundle name and the locale with the hopes that this will be enough to 
   * look up the message. 
   * 
   * <p>This assumes the new place where this object is being deserialized has 
   * the resource bundle available. If it does not, the original message will 
   * be reused.</p>
   *
   * @param in where to read the serialized stream
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    // set our transient fields
    bundle = null;
    getFailed = false;

    // now read in our serialized object
    bundleBaseName = (BundleBaseName)in.readObject();
    locale = (Locale)in.readObject();
    lastMessage = (String)in.readObject();
    lastKey = (String)in.readObject();
    lastArgs = null;

    final int argsLength = in.readInt();

    if (argsLength != -1) {
      final byte[] args = new byte[argsLength];

      try {
        in.readFully(args);
        lastArgs = (Object[])LogMsg.deserialize(args);
      } catch (final Exception e) {
        lastArgs = null;
      }
    }
  }




  /**
   * Sets a new locale. 
   *
   * <p>This allows this class to change the message it returns after this 
   * instance has already been constructed. The side effect of calling this 
   * method is that the next time {@link #getLastMessage()} is called, it may 
   * return a different string if the locale was set to a different locale than 
   * what it was before.</p>
   *
   * @param locale the new locale to set, if <code>null</code>, the default 
   * locale will be set
   */
  public void setLocale(Locale locale) {
    if (locale == null) {
      locale = Locale.getDefault();
    }

    if (!locale.equals(getLocale())) {
      this.locale = locale;

      // since the locale has changed the current bundle is no longer valid
      bundle = null;
    }
  }




  /**
   * Same as {@link #getLastMessage()}.
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getLastMessage();
  }

  //

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  // 

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -




  // 

  /**
   * <code>ResourceBundle</code> is not serializable so this serializes the 
   * base bundle name and the locale with the hopes that this will be enough to 
   * look up the message again when this instance is deserialized. 
   * 
   * <p>This assumes the new place where this object was deserialized has the 
   * resource bundle available. If it does not, the original message will be 
   * reused.</p>
   *
   * @param out where to write the serialized stream
   *
   * @throws IOException
   */
  private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(bundleBaseName);
    out.writeObject(locale);
    out.writeObject(lastMessage);
    out.writeObject(lastKey);

    byte[] args;
    int argsLength;

    try {
      // Nothing we can do about it if one or more args aren't serializable.
      // We'll just have to rely on the last message when we get to the other 
      // side. We do our own serialization here because the writeObject docs 
      // says that if a write fails, the whole output stream is corrupted and 
      // in an indeterminate state. Since it is completely valid that some args 
      // may not be serializable, we have to take into account that we may not 
      // be able to serialize the args.
      args = LogMsg.serialize(lastArgs);
      argsLength = args.length;
    } catch (final Exception e) {
      args = null;
      argsLength = -1;
    }

    out.writeInt(argsLength);
    if (argsLength != -1) {
      out.write(args);
    }
  }

}
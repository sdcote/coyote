package coyote.commons.network.mqtt;

import java.io.UnsupportedEncodingException;

import coyote.commons.StringUtil;
import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.protocol.PublishMessage;


/**
 * Represents a topic destination, used for publish/subscribe messaging.
 * 
 * <p>The "topic filter" string used when subscribing may contain special 
 * characters, which allow you to subscribe to multiple topics at once.</p>
 * 
 * <p>The topic level separator is used to introduce structure into the topic, 
 * and can therefore be specified within the topic for that purpose. The 
 * multi-level wildcard and single-level wildcard can be used for 
 * subscriptions, but they cannot be used within a topic by the publisher of a 
 * message.<dl>
 *  <dt>Topic level separator</dt>
 *  <dd>The forward slash (/) is used to separate each level within
 *  a topic tree and provide a hierarchical structure to the topic space. The
 *  use of the topic level separator is significant when the two wildcard 
 *  characters are encountered in topics specified by subscribers.</dd>
 *
 *  <dt>Multi-level wildcard</dt>
 *  <dd><p>The number sign (#) is a wildcard character that matches any number 
 *  of levels within a topic. For example, if you subscribe to 
 *  finance/stock/ibm/#, you receive messages on these topics:<pre>
 *  finance/stock/ibm<br>
 *  finance/stock/ibm/closingprice<br>
 *  finance/stock/ibm/currentprice</pre>
 *  
 *  The multi-level wildcard can represent zero or more levels. Therefore, 
 *  <em>finance/#</em> can also match the singular <em>finance</em>, where 
 *  <em>#</em> represents zero levels. The topic level separator is meaningless 
 *  in this context, because there are no levels to separate.
 *
 *  <p>The <span>multi-level</span> wildcard can be specified only on its own 
 *  or next to the topic level separator character. Therefore, <em>#</em> and 
 *  <em>finance/#</em> are both valid, but <em>finance#</em> is not valid. 
 *  <span>The multi-level wildcard must be the last character used within the 
 *  topic tree. For example, <em>finance/#</em> is valid but 
 *  <em>finance/#/closingprice</em> is not valid.</span></p></dd>
 *
 *  <dt>Single-level wildcard</dt>
 *  <dd><p>The plus sign (+) is a wildcard character that matches only one 
 *  topic level. For example, <em>finance/stock/+</em> matches 
 *  <em>finance/stock/ibm</em> and <em>finance/stock/xyz</em>, but not 
 *  <em>finance/stock/ibm/closingprice</em>. Also, because the single-level
 *  wildcard matches only a single level, <em>finance/+</em> does not match 
 *  <em>finance</em>.</p>
 *
 *  <p>Use the single-level wildcard at any level in the topic tree, and in 
 *  conjunction with the multilevel wildcard. Specify the single-level wildcard 
 *  next to the topic level separator, except when it is specified on its own. 
 *  Therefore, <em>+</em> and <em>finance/+</em> are both valid, but 
 *  <em>finance+</em> is not valid. The single-level wildcard can be used at 
 *  the end of the topic tree or within the topic tree. For example, 
 *  <em>finance/+</em> and <em>finance/+/ibm</em> are both valid.</p>
 *  </dd></dl>
 */
public class Topic {

  private final Connection connection;

  private final String name;

  /**
   * The forward slash (/) is used to separate each level within a topic tree
   * and provide a hierarchical structure to the topic space. The use of the
   * topic level separator is significant when the two wildcard characters are
   * encountered in topics specified by subscribers.
   */
  public static final String TOPIC_LEVEL_SEPARATOR = "/";

  /**
   * Multi-level wildcard The number sign (#) is a wildcard character that
   * matches any number of levels within a topic.
   */
  public static final String MULTI_LEVEL_WILDCARD = "#";

  /**
   * Single-level wildcard The plus sign (+) is a wildcard character that
   * matches only one topic level.
   */
  public static final String SINGLE_LEVEL_WILDCARD = "+";

  /**
   * Multi-level wildcard pattern(/#)
   */
  public static final String MULTI_LEVEL_WILDCARD_PATTERN = TOPIC_LEVEL_SEPARATOR + MULTI_LEVEL_WILDCARD;

  /**
   * Topic wildcards (#+)
   */
  public static final String TOPIC_WILDCARDS = MULTI_LEVEL_WILDCARD + SINGLE_LEVEL_WILDCARD;

  // topic name and topic filter length range defined in the spec
  private static final int MIN_TOPIC_LEN = 1;
  private static final int MAX_TOPIC_LEN = 65535;
  private static final char NUL = '\u0000';

  public static final boolean ALLOW_WILDCARDS = true;
  public static final boolean NO_WILDCARDS_ALLOWED = false;




  public Topic( final String name, final Connection conn ) {
    connection = conn;
    this.name = name;
  }




  /**
   * Check the supplied topic name and filter match
   * 
   * @param topicFilter topic filter: wildcards allowed
   * @param topicName topic name: wildcards not allowed
   * 
   * @throws IllegalArgumentException if the topic name or filter is invalid
   */
  public static boolean isMatched( final String topicFilter, final String topicName ) throws IllegalStateException, IllegalArgumentException {
    int curn = 0, curf = 0;
    final int curn_end = topicName.length();
    final int curf_end = topicFilter.length();

    Topic.validate( topicFilter, ALLOW_WILDCARDS );
    Topic.validate( topicName, NO_WILDCARDS_ALLOWED );

    if ( topicFilter.equals( topicName ) ) {
      return true;
    }

    while ( ( curf < curf_end ) && ( curn < curn_end ) ) {
      if ( ( topicName.charAt( curn ) == '/' ) && ( topicFilter.charAt( curf ) != '/' ) ) {
        break;
      }
      if ( ( topicFilter.charAt( curf ) != '+' ) && ( topicFilter.charAt( curf ) != '#' ) && ( topicFilter.charAt( curf ) != topicName.charAt( curn ) ) ) {
        break;
      }
      if ( topicFilter.charAt( curf ) == '+' ) { // skip until we meet the next separator, or end of string
        int nextpos = curn + 1;
        while ( ( nextpos < curn_end ) && ( topicName.charAt( nextpos ) != '/' ) ) {
          nextpos = ++curn + 1;
        }
      } else if ( topicFilter.charAt( curf ) == '#' ) {
        curn = curn_end - 1; // skip until end of string
      }
      curf++;
      curn++;
    };

    return ( curn == curn_end ) && ( curf == curf_end );
  }




  /**
   * Validate the topic name or topic filter
   * 
   * @param topicString topic name or filter
   * @param wildcardAllowed true if validate topic filter, false otherwise
   * 
   * @throws IllegalArgumentException if the topic is invalid
   */
  public static void validate( final String topicString, final boolean wildcardAllowed ) throws IllegalStateException, IllegalArgumentException {
    int topicLen = 0;
    try {
      topicLen = topicString.getBytes( "UTF-8" ).length;
    } catch ( final UnsupportedEncodingException e ) {
      throw new IllegalStateException( e );
    }

    if ( ( topicLen < MIN_TOPIC_LEN ) || ( topicLen > MAX_TOPIC_LEN ) ) {
      throw new IllegalArgumentException( String.format( "Invalid topic length, should be in range[%d, %d]!", new Object[] { new Integer( MIN_TOPIC_LEN ), new Integer( MAX_TOPIC_LEN ) } ) );
    }

    if ( wildcardAllowed ) {
      // Only # or +
      if ( StringUtil.equalsAny( topicString, new String[] { MULTI_LEVEL_WILDCARD, SINGLE_LEVEL_WILDCARD } ) ) {
        return;
      }

      if ( ( StringUtil.countMatches( topicString, MULTI_LEVEL_WILDCARD ) > 1 ) || ( topicString.contains( MULTI_LEVEL_WILDCARD ) && !topicString.endsWith( MULTI_LEVEL_WILDCARD_PATTERN ) ) ) {
        throw new IllegalArgumentException( "Invalid usage of multi-level wildcard in topic string: " + topicString );
      }

      validateSingleLevelWildcard( topicString );

    } else {
      if ( StringUtil.containsAny( topicString, TOPIC_WILDCARDS ) ) {
        throw new IllegalArgumentException( "The topic name MUST NOT contain any wildcard characters (#+)" );
      }
    }
    return;

  }




  private static void validateSingleLevelWildcard( final String topicString ) {
    final char singleLevelWildcardChar = SINGLE_LEVEL_WILDCARD.charAt( 0 );
    final char topicLevelSeparatorChar = TOPIC_LEVEL_SEPARATOR.charAt( 0 );

    final char[] chars = topicString.toCharArray();
    final int length = chars.length;
    char prev = NUL, next = NUL;
    for ( int i = 0; i < length; i++ ) {
      prev = ( ( i - 1 ) >= 0 ) ? chars[i - 1] : NUL;
      next = ( ( i + 1 ) < length ) ? chars[i + 1] : NUL;

      if ( chars[i] == singleLevelWildcardChar ) {
        // prev and next can be only '/' or none
        if ( ( ( prev != topicLevelSeparatorChar ) && ( prev != NUL ) ) || ( ( next != topicLevelSeparatorChar ) && ( next != NUL ) ) ) {
          throw new IllegalArgumentException( String.format( "Invalid usage of single-level wildcard in topic string '%s'!", new Object[] { topicString } ) );
        }
      }
    }
  }




  /**
   * Create a PUBLISH packet from the specified message.
   */
  private PublishMessage createPublish( final MqttMessage message ) {
    return new PublishMessage( getName(), message );
  }




  /**
   * Returns the name of the queue or topic.
   * 
   * @return the name of this destination.
   */
  public String getName() {
    return name;
  }




  /**
   * Publishes a message on the topic.
   * 
   * <p>This is a convenience method, which will create a {@link MqttMessage} 
   * object with a byte array payload and the specified QoS, and then publish 
   * it.  All other values in the message will be set to the defaults.</p> 

   * @param payload the byte array to use as the payload
   * @param qos the Quality of Service.  Valid values are 0, 1 or 2.
   * @param retained whether or not this message should be retained by the server.
   * 
   * @throws IllegalArgumentException if value of QoS is not 0, 1 or 2.
   * 
   * @see #publish(MqttMessage)
   * @see MqttMessage#setQos(int)
   * @see MqttMessage#setRetained(boolean)
   */
  public MqttDeliveryTokenImpl publish( final byte[] payload, final int qos, final boolean retained ) throws MqttException, CacheException {
    final MqttMessage message = new MqttMessage( payload );
    message.setQos( qos );
    message.setRetained( retained );
    return this.publish( message );
  }




  /**
   * Publishes the specified message to this topic, but does not wait for 
   * delivery of the message to complete. 
   * 
   * <p>The returned {@link MqttDeliveryTokenImpl token} can be used to track the 
   * delivery status of the message.  Once this method has returned cleanly, 
   * the message has been accepted for publication by the client. Message 
   * delivery will be completed in the background when a connection is 
   * available.</p>
   * 
   * @param message the message to publish
   * 
   * @return an MqttDeliveryToken for tracking the delivery of the message
   */
  public MqttDeliveryTokenImpl publish( final MqttMessage message ) throws MqttException, CacheException {
    final MqttDeliveryTokenImpl token = new MqttDeliveryTokenImpl();
    token.setMessage( message );
    connection.sendNoWait( createPublish( message ), token );
    token.waitUntilSent();
    return token;
  }




  /**
   * @return a string representation of this topic.
   */
  @Override
  public String toString() {
    return getName();
  }

}

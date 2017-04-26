package coyote.commons.network.mqtt;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.network.mqtt.Topic;
import coyote.commons.network.mqtt.utilities.Utility;


/**
 * Tests MQTT topic wildcards
 */
public class MqttTopicTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}




  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  @Test
  public void testValidTopicFilterWildcards() throws Exception {
    String[] topics = new String[] { "+", "+/+", "+/foo", "+/tennis/#", "foo/+", "foo/+/bar", "/+", "/+/sport/+/player1", "#", "/#", "sport/#", "sport/tennis/#" };

    for ( String topic : topics ) {
      Topic.validate( topic, true );
    }
  }




  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTopicFilterWildcards1() throws Exception {
    Topic.validate( "sport/tennis#", true );
  }




  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTopicFilterWildcards2() throws Exception {
    Topic.validate( "sport/tennis/#/ranking", true );
  }




  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTopicFilterWildcards3() throws Exception {
    Topic.validate( "sport+", true );
  }




  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTopicFilterWildcards4() throws Exception {
    Topic.validate( "sport/+aa", true );
  }




  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTopicFilterWildcards5() throws Exception {
    Topic.validate( "sport/#/ball/+/aa", true );
  }

}

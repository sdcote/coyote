package coyote.loader.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//import static org.junit.Assert.*;
import org.junit.Test;

import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.marshal.JSONMarshaler;


public class ConfigTest {

  @Test
  public void test() {
    Config config = new Config();
    config.setName( "Loader" );
    config.setClassName( coyote.loader.DefaultLoader.class.getName() );
    config.setId( GUID.randomGUID().toString() );
    config.setName( "Bob" );

    // The configuration section of all the components this loader is to load
    Config componentCfg = new Config();

    // Create a configuration for component 1
    Config cfg = new Config();
    cfg.setName( "Component1" );
    cfg.setClassName( coyote.loader.SimpleComponent.class.getName() );
    cfg.setId( "F56A" );
    componentCfg.add( cfg );

    cfg = new Config();
    cfg.setName( "Component2" );
    cfg.setClassName( coyote.loader.SimpleComponent.class.getName() );
    cfg.setId( "DB46" );
    componentCfg.add( cfg );

    cfg = new Config();
    cfg.setName( "Component3" );
    cfg.setClassName( coyote.loader.SimpleComponent.class.getName() );
    cfg.setId( "8A0A" );
    componentCfg.add( cfg );

    // Add the component configuration to the main Loader config
    config.add( "Component", componentCfg );

    // This is a standard configuration for a loader
    System.out.println( JSONMarshaler.toFormattedString( config ) );

  }




  @Test
  public void testGetInt() {

    Config cfg = new Config();
    cfg.set( "port", "123" );

    try {
      int value = cfg.getInt( "PORT" );
      assertTrue( value == 123 );
    } catch ( NumberFormatException e ) {
      fail( "Could not retrieve as an integer" );
    }

    cfg.set( "fail", null );
    try {
      int value = cfg.getInt( "FAIL" );
      fail( "Should have thrown an exception" );

      value = cfg.getInt( "NotThere" );
      fail( "Should have thrown an exception - null value" );
    } catch ( NumberFormatException e ) {}

    try {
      int value = cfg.getInt( "NotThere" );
      fail( "Should have thrown an exception - not found" );
    } catch ( NumberFormatException e ) {}

  }




  @Test
  public void readFromNetwork() throws URISyntaxException, IOException, ConfigurationException {
    URI cfgUri = new URI( "http://coyote.systems/device/ou812" );
    Config config = Config.read( cfgUri );
    assertNotNull( config );
    if ( StringUtil.isBlank( config.getName() ) ) {
      String basename = UriUtil.getBase( cfgUri );
      assertNotNull( basename );
      config.setName( basename );
      assertNotNull( config.getName() );
      assertEquals( basename, config.getName() );
    }
    //System.out.println( JSONMarshaler.toFormattedString( config ) );
  }




  @Test
  public void testGetString() {

    Config cfg = new Config();
    cfg.set( "port", "123" );

    // case insensitive by default
    assertNotNull( cfg.getString( "Port" ) );

    // case sensitive (ignoreCase=false)
    assertNull( cfg.getString( "Port", false ) );

    // case insensitive (ignoreCase=true)
    assertNotNull( cfg.getString( "Port", true ) );
  }




  @Test
  public void copyTest() {
    Config cfg = new Config();
    cfg.set( "port", "123" );
    assertNotNull( cfg.getString( "port" ) );
    assertEquals( cfg.getString( "port" ), "123" );

    Config copy = cfg.copy();
    assertNotNull( copy.getString( "port" ) );
    assertEquals( "123", copy.getString( "port" ) );
    copy.put( "port", "456" );
    
    assertEquals( "456",copy.getString( "port" ) );
    assertEquals( "123", cfg.getString( "port" ) );

  }

}

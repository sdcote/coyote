/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.marshal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import coyote.commons.ByteUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DecodeException;
import coyote.dataframe.marshal.xml.XmlFrameParser;


/**
 * 
 */
public class Stepper {

  /** Field ISO8859_1 */
  public static String ISO8859_1;
  static {
    final String iso = System.getProperty( "ISO_8859_1" );
    if ( iso != null ) {
      Stepper.ISO8859_1 = iso;
    } else {
      try {
        new String( new byte[] { (byte)20 }, "ISO-8859-1" );

        Stepper.ISO8859_1 = "ISO-8859-1";
      } catch ( final java.io.UnsupportedEncodingException e ) {
        Stepper.ISO8859_1 = "ISO8859_1";
      }
    }
  }




  public static void main( String[] args ) {
    //json();
    //xml();
    // xml2();
    readDump();
  }




  private static void readDump() {
    byte[] data = Stepper.read( new File( "dump.dat" ) );
    try {
      DataFrame frame = new DataFrame( data );
    } catch ( DecodeException e ) {
      System.out.println( "Previous:" + e.getPreviousPosition() + " Current:" + e.getPosition() );
      System.out.println(ByteUtil.dump( data ));
    }

  }




  private static void xml2() {
    String rawBody = Stepper.fileToString( new File( "Soap.xml" ) );
    System.out.println( rawBody );
    XmlFrameParser parser = new XmlFrameParser( rawBody );
    try {
      DataFrame frame = parser.parse();
    } catch ( DecodeException e ) {
      Stepper.write( new File( "dump.dat" ), e.getBytes() );
      e.printStackTrace();
    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }




  /**
   * Opens a file, reads it and returns the data as a string and closes the
   * file.
   *
   * @param file - file to open
   *
   * @return String representing the file data
   */
  public static String fileToString( final File file ) {
    try {
      final byte[] data = Stepper.read( file );

      if ( data != null ) {
        // Attempt to return the string
        try {
          return new String( data, Stepper.ISO8859_1 );
        } catch ( final UnsupportedEncodingException uee ) {
          // Send it back in default encoding
          return new String( data );
        }
      }
    } catch ( final Exception ex ) {}

    return null;
  }




  /**
   * Read the entire file into memory as an array of bytes.
   *
   * @param file The file to read
   *
   * @return A byte array that contains the contents of the file.
   */
  public static byte[] read( final File file ) {
    if ( file.exists() && file.canRead() ) {
      DataInputStream dis = null;
      final byte[] bytes = new byte[new Long( file.length() ).intValue()];
      try {
        dis = new DataInputStream( new FileInputStream( file ) );
        dis.readFully( bytes );
        return bytes;
      } catch ( final Exception ignore ) {}
      finally {
        try {
          if ( dis != null ) {
            dis.close();
          }
        } catch ( final Exception ignore ) {}
      }
    }

    return null;
  }




  /**
   * Write the given data to the given file object creating it and it's parent
   * directories as necessary.
   *
   * @param file The file reference to which the data will be written.
   * @param data The data to write to the file.
   */
  public static void write( final File file, final byte[] data ) {
    if ( !file.exists() || ( file.exists() && file.canWrite() ) ) {
      DataOutputStream dos = null;
      try {
        if ( file.getParent() != null ) {
          file.getParentFile().mkdirs();
        }
        if ( data.length > 0 ) {
          dos = new DataOutputStream( new FileOutputStream( file ) );
          dos.write( data );
          dos.flush();
        }
      } catch ( final Exception e ) {}
      finally {
        try {
          if ( dos != null )
            dos.close();
        } catch ( final Exception e ) {}
        finally {}
      }
    }
  }




  static void xml() {

    DataFrame frame = new DataFrame();
    frame.put( "test", "This is a test" );
    frame.put( "another", "This is another" );

    DataFrame nested = new DataFrame();
    nested.put( "inner", "This is some inner data" );
    frame.put( "nested", nested );

    frame.put( "more", "Some more data" );

    DataFrame frame1 = new DataFrame();
    frame1.put( "number", 123 );
    frame1.put( "bool", true );

    DataFrame frame2 = new DataFrame();
    frame2.put( "long", 456l );
    frame2.put( "double", 5.3D );

    DataFrame frame3 = new DataFrame();
    frame3.put( "date", new Date() );

    frame2.put( "Frame3", frame3 );
    frame1.put( "Frame2", frame2 );
    frame.put( "Frame1", frame1 );

    frame.put( "LAST", "The End" );

    // All valid XML has one root, put our data in that root
    DataFrame root = new DataFrame();
    root.put( "root", frame );

    //String xml = XMLMarshaler.marshal( frame );
    String xml = XMLMarshaler.toFormattedString( root );
    System.out.println( xml );

    System.out.println( XMLMarshaler.marshal( frame2 ) );

    System.out.println();
    xml = XMLMarshaler.toTypedString( root );
    System.out.println( xml );

    System.out.println();
    xml = XMLMarshaler.toFormattedTypedString( root );
    System.out.println( xml );
  }




  static void json() {
    //String json = "[{\"message_stats\":{\"deliver_get\":2,\"deliver_get_details\":{\"rate\":0.0},\"get_no_ack\":2,\"get_no_ack_details\":{\"rate\":0.0},\"publish\":2,\"publish_details\":{\"rate\":0.0}},\"messages\":0,\"messages_details\":{\"rate\":0.0},\"messages_ready\":0,\"messages_ready_details\":{\"rate\":0.0},\"messages_unacknowledged\":0,\"messages_unacknowledged_details\":{\"rate\":0.0},\"name\":\"/\",\"tracing\":false}]";
    //String json = "[{\"tracing\":false}]";

    String json = "{\"Reader\":{  \"preload\" : true, \"header\" : true } }";

    System.out.println( json );
    List<DataFrame> results = JSONMarshaler.marshal( json );
    DataFrame frame = results.get( 0 );
    System.out.println( "Minimal ----------------------------" );
    String nativetxt = frame.toString();
    String minimal = JSONMarshaler.marshal( frame );
    System.out.println( minimal );

    System.out.println( "Indented ----------------------------" );
    String indented = JSONMarshaler.toFormattedString( frame );
    System.out.println( indented );

    json = JSONMarshaler.marshal( frame );
    System.out.println( json );

  }
}

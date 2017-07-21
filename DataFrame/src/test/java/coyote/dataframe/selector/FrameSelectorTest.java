/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.selector;

import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;


/**
 * 
 */
public class FrameSelectorTest {

  static String json;




  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ClassLoader classLoader = FrameSelectorTest.class.getClassLoader();
    File file = new File( classLoader.getResource( "nvdcve.json" ).getFile() );
    byte[] bytes = new byte[new Long( file.length() ).intValue()];
    try (DataInputStream dis = new DataInputStream( new FileInputStream( file ) )) {
      dis.readFully( bytes );
    } catch ( final Exception ignore ) {}
    json = new String( bytes );
  }




  @Test
  public void readArrayData() {
    List<DataFrame> frames = JSONMarshaler.marshal( json );
    assertTrue( frames.size() == 1 );
    DataFrame frame = frames.get( 0 );

    FrameSelector selector = new FrameSelector( "CVE_Items.*.cve" );
    List<DataFrame> results = selector.select( frame );
    
    assertTrue( results.size()==4);
  }

}

/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


/**
 * 
 */
public abstract class FileWriter implements Writer {
  protected PrintWriter writer = null;
  protected String filename = null;
  protected boolean printHeader = true;




  /**
   * @see coyote.testdata.format.Writer#close()
   */
  @Override
  public void close() {
    try {
      writer.close();
    } catch ( final Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }




  /**
   * @see coyote.testdata.format.Writer#open()
   */
  @Override
  public void open() {
    try {
      File target = new File( filename );
      if ( target.exists() )
        target.delete();
    } catch ( Exception e1 ) {
      e1.printStackTrace();
    }

    try {
      writer = new PrintWriter( filename, "UTF-8" );
    } catch ( final FileNotFoundException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch ( final UnsupportedEncodingException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    printHeader = true;
  }

}

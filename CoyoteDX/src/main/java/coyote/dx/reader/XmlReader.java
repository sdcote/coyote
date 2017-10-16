/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.util.List;

import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;


/**
 * 
 */
public class XmlReader extends MarshalingFrameReader implements FrameReader, ConfigurableComponent {

  /**
   * @see coyote.dx.reader.MarshalingFrameReader#getFrames(java.lang.String)
   */
  @Override
  protected List<DataFrame> getFrames(String data) {
    return XMLMarshaler.marshal(data);
  }

}

/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.network.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Output stream that will automatically send every write to the wrapped
 * OutputStream according to chunked transfer:
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.6.1
 */
class ChunkedOutputStream extends FilterOutputStream {

  public ChunkedOutputStream(final OutputStream out) {
    super(out);
  }




  public void finish() throws IOException {
    out.write("0\r\n\r\n".getBytes());
  }




  @Override
  public void write(final byte[] b) throws IOException {
    write(b, 0, b.length);
  }




  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if (len != 0) {
      out.write(String.format("%x\r\n", len).getBytes());
      out.write(b, off, len);
      out.write("\r\n".getBytes());
    }
  }




  @Override
  public void write(final int b) throws IOException {
    final byte[] data = {(byte)b};
    write(data, 0, 1);
  }

}
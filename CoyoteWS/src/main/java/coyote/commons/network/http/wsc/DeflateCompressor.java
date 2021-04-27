package coyote.commons.network.http.wsc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


/**
 * DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>)
 * compressor implementation.
 */
class DeflateCompressor {

  /**
   * Compress the given input using the Deflate algorithm.
   * 
   * @param input data to be deflated
   * 
   * @return deflated (compressed) data
   * 
   * @throws IOException if there were problems deflating data
   */
  public static byte[] compress(final byte[] input) throws IOException {
    // Destination where compressed data will be stored.
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // Create a compressor.
    final Deflater deflater = createDeflater();
    final DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);

    // Compress the data.
    dos.write(input, 0, input.length);
    dos.close();

    // Release the resources held by the compressor.
    deflater.end();

    // Retrieve the compressed data.
    return baos.toByteArray();
  }




  /**
   * Create a deflater which only returns deflate blocks and no ZLIB headers 
   * or checksum fields.
   * 
   * @return the deflater configured to create raw deflate blocks.
   */
  private static Deflater createDeflater() {
    return new Deflater(Deflater.DEFAULT_COMPRESSION, true);
  }

}

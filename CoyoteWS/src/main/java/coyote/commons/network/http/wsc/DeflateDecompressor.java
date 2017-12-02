package coyote.commons.network.http.wsc;

/**
 * DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>)
 * decompressor implementation.
 */
class DeflateDecompressor {

  /**
   * 
   * @param input the incoming data array
   * @param output destination of the decompressed data
   * 
   * @throws FormatException if the data is encoded improperly
   */
  public static void decompress(final ByteArray input, final ByteArray output) throws FormatException {
    decompress(input, 0, output);
  }




  /**
   * 
   * @param input the incoming data array
   * @param index where in the input to start processing (0 = the beginning)
   * @param output destination of the decompressed data
   * 
   * @throws FormatException if the data is encoded improperly
   */
  private static void decompress(final ByteArray input, final int index, final ByteArray output) throws FormatException {
    // The data is compressed on a bit basis, so use a bit index.
    final int[] bitIndex = new int[1];
    bitIndex[0] = index * 8;

    // Process all blocks one by one until the end.
    // inflateBlock() returns false if no more block exists.
    while (inflateBlock(input, bitIndex, output)) {}
  }




  /**
   * Duplicate a portion of the output buffer and append it to the buffer.
   * 
   * @param length how much data to copy
   * @param distance distance from the end of the data to start copying
   * @param output The source and destination of the data
   */
  private static void duplicate(final int length, final int distance, final ByteArray output) {
    // Get the number of bytes written so far.
    final int sourceLength = output.length();

    // An array to finally append to the output.
    final byte[] target = new byte[length];

    // The position from which to start copying data.
    final int initialPosition = sourceLength - distance;
    int sourceIndex = initialPosition;

    for (int targetIndex = 0; targetIndex < length; ++targetIndex, ++sourceIndex) {
      if (sourceLength <= sourceIndex) {
        // Reached the end of the current output buffer.
        // The specification says as follows in 3.2.3.
        //
        //   Note also that the referenced string may
        //   overlap the current position; for example,
        //   if the last 2 bytes decoded have values X
        //   and Y, a string reference with <length=5,
        //   distance=2> adds X,Y,X,Y,X to the output
        //   stream.

        // repeat.
        sourceIndex = initialPosition;
      }

      target[targetIndex] = output.get(sourceIndex);
    }

    // Append the duplicated bytes to the output.
    output.put(target);
  }




  /**
   * Inflate the next block.
   * 
   * <p>According to RFC 1951, each block has a block header which consists of 
   * 3 bits. The first bit indicates whether the block is the last one or not 
   * and the next two bits indicate the compression type of the block. 
   * Compression types are as follows:<ul>
   * <li>00: No compression.
   * <li>01: Compressed with fixed Huffman codes
   * <li>10: Compressed with dynamic Huffman codes
   * <li>11: Reserved (error)</ul>
   * 
   * @param input the incoming data array
   * @param bitIndex index into the data
   * @param output destination of the processed block
   * 
   * @return true if there are more blocks, false if this block is the last one.
   * 
   * @throws FormatException if the data is encoded improperly
   */
  private static boolean inflateBlock(final ByteArray input, final int[] bitIndex, final ByteArray output) throws FormatException {
    boolean last = input.readBit(bitIndex);
    final int type = input.readBits(bitIndex, 2);

    switch (type) {
      case 0:
        // No compression
        inflatePlainBlock(input, bitIndex, output);
        break;

      case 1:
        // Compressed with fixed Huffman codes
        inflateFixedBlock(input, bitIndex, output);
        break;

      case 2:
        // Compressed with dynamic Huffman codes
        inflateDynamicBlock(input, bitIndex, output);
        break;

      default:
        // Bad compression type at the bit index.
        final String message = String.format("[%s] Bad compression type '11' at the bit index '%d'.", DeflateDecompressor.class.getSimpleName(), bitIndex[0]);

        throw new FormatException(message);
    }

    // If no more data are available.
    if (input.length() <= (bitIndex[0] / 8)) {
      // Last even if BFINAL bit is false.
      last = true;
    }

    // Return true if this block is not the last one.
    return !last;
  }




  /**
   * Inflate compressed blocks (length and distance codes).
   * 
   * @param input the incoming data array
   * @param bitIndex index into the data
   * @param output destination of the processed block
   * @param literalLengthHuffman iteral/length symbols
   * @param distanceHuffman distance from the input symbols
   * 
   * @throws FormatException if the data is encoded improperly
   */
  private static void inflateData(final ByteArray input, final int[] bitIndex, final ByteArray output, final Huffman literalLengthHuffman, final Huffman distanceHuffman) throws FormatException {
    while (true) {
      // Read a literal/length symbol from the input.
      final int literalLength = literalLengthHuffman.readSym(input, bitIndex);

      // Symbol value '256' indicates the end.
      if (literalLength == 256) {
        // End of this data.
        break;
      }

      // Symbol values from 0 to 255 represent literal values.
      if (0 <= literalLength && literalLength <= 255) {
        // Output as is.
        output.put(literalLength);
        continue;
      }

      // Symbol values from 257 to 285 represent <length,distance> pairs.
      // Depending on symbol values, some extra bits in the input may be
      // consumed to compute the length.
      final int length = DeflateUtil.readLength(input, bitIndex, literalLength);

      // Read the distance from the input.
      final int distance = DeflateUtil.readDistance(input, bitIndex, distanceHuffman);

      // Extract some data from the output buffer and copy them.
      duplicate(length, distance, output);
    }
  }




  /**
   * Inflate dynamic Huffman codes (BTYPE=10)
   * 
   * @param input the incoming data array
   * @param bitIndex index into the data
   * @param output destination of the processed block
   * 
   * @throws FormatException if the data is encoded improperly
   */
  private static void inflateDynamicBlock(final ByteArray input, final int[] bitIndex, final ByteArray output) throws FormatException {
    // Read 2 tables. One is a table to convert "code value of literal/length
    // alphabet" into "literal/length symbol". The other is a table to convert
    // "code value of distance alphabet" into "distance symbol".
    final Huffman[] tables = new Huffman[2];
    DeflateUtil.readDynamicTables(input, bitIndex, tables);

    // The actual compressed data of this block. The data are encoded using
    // the literal/length and distance Huffman codes that were parsed above.
    inflateData(input, bitIndex, output, tables[0], tables[1]);
  }




  /**
   * Inflate fixed Huffman codes (BTYPE=01)
   *
   * <p>Inflate the compressed data using the pre-defined conversion tables. 
   * The specification says in 3.2.2 as follows.:blockquote>
   * The only differences between the two compressed cases is how the Huffman 
   * codes for the literal/length and distance alphabets are defined.
   * </blockquote>
   * 
   * <p>The "two compressed cases" in the above sentence are "fixed Huffman 
   * codes" and "dynamic Huffman codes".
   * 
   * @param input the incoming data array
   * @param bitIndex index into the data
   * @param output destination of the processed block
   * 
   * @throws FormatException if the data is encoded improperly
   */
  private static void inflateFixedBlock(final ByteArray input, final int[] bitIndex, final ByteArray output) throws FormatException {
    inflateData(input, bitIndex, output, FixedLiteralLengthHuffman.getInstance(), FixedDistanceHuffman.getInstance());
  }




  /**
   * Inflate non-compressed blocks (BTYPE=00)
   * 
   * @param input the incoming data array
   * @param bitIndex index into the data
   * @param output destination of the processed block
   */
  private static void inflatePlainBlock(final ByteArray input, final int[] bitIndex, final ByteArray output) {
    // Skip any remaining bits in current partially processed byte.
    final int bi = (bitIndex[0] + 7) & ~7;

    // Data copy is performed on a byte basis, so convert the bit index to a 
    // byte index.
    int index = bi / 8;

    // LEN: 2 bytes. The data length.
    final int len = (input.get(index) & 0xFF) + (input.get(index + 1) & 0xFF) * 256;

    // Skip LEN and NLEN.
    index += 4;

    // Copy the data to the output.
    output.put(input, index, len);

    // Make the bitIndex point to the bit next to the end of the copied data.
    bitIndex[0] = (index + len) * 8;
  }

}

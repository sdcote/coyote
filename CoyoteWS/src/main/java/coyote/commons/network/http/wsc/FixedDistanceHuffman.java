package coyote.commons.network.http.wsc;

class FixedDistanceHuffman extends Huffman {
  private static final FixedDistanceHuffman INSTANCE = new FixedDistanceHuffman();




  private static int[] buildCodeLensFromSym() {
    // 3.2.6. Compression with fixed Huffman codes (BTYPE=01)
    //
    // "Distance codes 0-31 are represented by (fixed-length)
    // 5-bit codes", the specification says.

    final int[] codeLengths = new int[32];

    for (int symbol = 0; symbol < 32; ++symbol) {
      codeLengths[symbol] = 5;
    }

    // Let Huffman class generate code values from code lengths.
    // Note that "code lengths are sufficient to generate the
    // actual codes". See 3.2.2. of RFC 1951.
    return codeLengths;
  }




  public static FixedDistanceHuffman getInstance() {
    return INSTANCE;
  }




  private FixedDistanceHuffman() {
    super(buildCodeLensFromSym());
  }
}

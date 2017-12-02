package coyote.commons.network.http.wsc;

import java.nio.ByteBuffer;


/**
 * Expandable byte array with byte-basis and bit-basis operations.
 */
class ByteArray {
  private static final int ADDITIONAL_BUFFER_SIZE = 1024;

  // The buffer.
  private ByteBuffer buffer;

  // The current length.
  private int bufferLength;




  /**
   * Constructor with initial data. The length of the data is used
   * as the initial capacity of the internal buffer.
   *
   * @param data
   *         Initial data.
   */
  public ByteArray(final byte[] data) {
    buffer = ByteBuffer.wrap(data);
    bufferLength = data.length;
  }




  /**
   * Constructor with initial capacity.
   *
   * @param capacity
   *         Initial capacity for the internal buffer.
   */
  public ByteArray(final int capacity) {
    buffer = ByteBuffer.allocate(capacity);
    bufferLength = 0;
  }




  public void clear() {
    buffer.clear();
    buffer.position(0);
    bufferLength = 0;
  }




  public void clearBit(final int bitIndex) {
    setBit(bitIndex, false);
  }




  /**
   * Expand the size of the internal buffer.
   */
  private void expandBuffer(final int newBufferSize) {
    // Allocate a new buffer.
    final ByteBuffer newBuffer = ByteBuffer.allocate(newBufferSize);

    // Copy the content of the current buffer to the new buffer.
    final int oldPosition = buffer.position();
    buffer.position(0);
    newBuffer.put(buffer);
    newBuffer.position(oldPosition);

    // Replace the buffers.
    buffer = newBuffer;
  }




  /**
   * Get a byte at the index.
   */
  public byte get(final int index) throws IndexOutOfBoundsException {
    if (index < 0 || bufferLength <= index) {
      // Bad index.
      throw new IndexOutOfBoundsException(String.format("Bad index: index=%d, length=%d", index, bufferLength));
    }

    return buffer.get(index);
  }




  public boolean getBit(final int bitIndex) {
    final int index = bitIndex / 8;
    final int shift = bitIndex % 8;
    final int value = get(index);

    // Return true if the bit pointed to by bitIndex is set.
    return ((value & (1 << shift)) != 0);
  }




  public int getBits(final int bitIndex, final int nBits) {
    int number = 0;
    int weight = 1;

    // Convert consecutive bits into a number.
    for (int i = 0; i < nBits; ++i, weight *= 2) {
      // getBit() returns true if the bit is set.
      if (getBit(bitIndex + i)) {
        number += weight;
      }
    }

    return number;
  }




  public int getHuffmanBits(final int bitIndex, final int nBits) {
    int number = 0;
    int weight = 1;

    // Convert consecutive bits into a number.
    //
    // Note that 'i' is initialized by 'nBits - 1', not by 1.
    // This is because "3.1.1. Packing into bytes" in RFC 1951
    // says as follows:
    //
    //     Huffman codes are packed starting with the most
    //     significant bit of the code.
    //
    for (int i = nBits - 1; 0 <= i; --i, weight *= 2) {
      // getBit() returns true if the bit is set.
      if (getBit(bitIndex + i)) {
        number += weight;
      }
    }

    return number;
  }




  /**
   * The length of the data.
   */
  public int length() {
    return bufferLength;
  }




  /**
   * Add data at the current position.
   *
   * @param source
   *         Source data.
   */
  public void put(final byte[] source) {
    // If the buffer is small.
    if (buffer.capacity() < (bufferLength + source.length)) {
      expandBuffer(bufferLength + source.length + ADDITIONAL_BUFFER_SIZE);
    }
    buffer.put(source);
    bufferLength += source.length;
  }




  /**
   * Add data at the current position.
   *
   * @param source
   *         Source data.
   *
   * @param index
   *         The index in the source data. Data from the index is copied.
   *
   * @param length
   *         The length of data to copy.
   */
  public void put(final byte[] source, final int index, final int length) {
    // If the buffer is small.
    if (buffer.capacity() < (bufferLength + length)) {
      expandBuffer(bufferLength + length + ADDITIONAL_BUFFER_SIZE);
    }

    buffer.put(source, index, length);
    bufferLength += length;
  }




  /**
   * Add data at the current position.
   *
   * @param source
   *         Source data.
   *
   * @param index
   *         The index in the source data. Data from the index is copied.
   *
   * @param length
   *         The length of data to copy.
   */
  public void put(final ByteArray source, final int index, final int length) {
    put(source.buffer.array(), index, length);
  }




  /**
   * Add a byte at the current position.
   */
  public void put(final int data) {
    // If the buffer is small.
    if (buffer.capacity() < (bufferLength + 1)) {
      expandBuffer(bufferLength + ADDITIONAL_BUFFER_SIZE);
    }

    buffer.put((byte)data);
    ++bufferLength;
  }




  public boolean readBit(final int[] bitIndex) {
    final boolean result = getBit(bitIndex[0]);
    ++bitIndex[0];
    return result;
  }




  public int readBits(final int[] bitIndex, final int nBits) {
    final int number = getBits(bitIndex[0], nBits);
    bitIndex[0] += nBits;
    return number;
  }




  public void setBit(final int bitIndex, final boolean bit) {
    final int index = bitIndex / 8;
    final int shift = bitIndex % 8;
    int value = get(index);
    if (bit) {
      value |= (1 << shift);
    } else {
      value &= ~(1 << shift);
    }
    buffer.put(index, (byte)value);
  }




  public void shrink(final int size) {
    if (buffer.capacity() <= size) {
      return;
    }

    final int endIndex = bufferLength;
    final int beginIndex = bufferLength - size;

    final byte[] bytes = toBytes(beginIndex, endIndex);

    buffer = ByteBuffer.wrap(bytes);
    buffer.position(bytes.length);
    bufferLength = bytes.length;
  }




  /**
   * Convert to a byte array ({@code byte[]}).
   */
  public byte[] toBytes() {
    return toBytes(0);
  }




  public byte[] toBytes(final int beginIndex) {
    return toBytes(beginIndex, length());
  }




  public byte[] toBytes(final int beginIndex, final int endIndex) {
    final int len = endIndex - beginIndex;

    if (len < 0 || beginIndex < 0 || bufferLength < endIndex) {
      throw new IllegalArgumentException(String.format("Bad range: beginIndex=%d, endIndex=%d, length=%d", beginIndex, endIndex, bufferLength));
    }

    final byte[] bytes = new byte[len];

    if (len != 0) {
      System.arraycopy(buffer.array(), beginIndex, bytes, 0, len);
    }

    return bytes;
  }
  
}

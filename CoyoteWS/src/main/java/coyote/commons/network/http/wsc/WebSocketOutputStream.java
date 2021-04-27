package coyote.commons.network.http.wsc;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class WebSocketOutputStream extends FilterOutputStream {
  public WebSocketOutputStream(final OutputStream out) {
    super(out);
  }




  public void write(final String string) throws IOException {
    // Convert the string into a byte array.
    final byte[] bytes = WebSocketUtil.getBytesUTF8(string);
    write(bytes);
  }




  public void write(final WebSocketFrame frame) throws IOException {
    writeFrame0(frame);
    writeFrame1(frame);
    writeFrameExtendedPayloadLength(frame);

    // Generate a random masking key.
    final byte[] maskingKey = WebSocketUtil.getRandomBytes(4);

    // Write the masking key.
    write(maskingKey);

    // Write the payload.
    writeFramePayload(frame, maskingKey);
  }




  private void writeFrame0(final WebSocketFrame frame) throws IOException {
    final int b = (frame.getFin() ? 0x80 : 0x00) | (frame.getRsv1() ? 0x40 : 0x00) | (frame.getRsv2() ? 0x20 : 0x00) | (frame.getRsv3() ? 0x10 : 0x00) | (frame.getOpcode() & 0x0F);

    write(b);
  }




  private void writeFrame1(final WebSocketFrame frame) throws IOException {
    // Frames sent from a client are always masked.
    int b = 0x80;

    final int len = frame.getPayloadLength();

    if (len <= 125) {
      b |= len;
    } else if (len <= 65535) {
      b |= 126;
    } else {
      b |= 127;
    }

    write(b);
  }




  private void writeFrameExtendedPayloadLength(final WebSocketFrame frame) throws IOException {
    final int len = frame.getPayloadLength();

    if (len <= 125) {
      return;
    }

    if (len <= 65535) {
      // 2-byte in network byte order.
      write((len >> 8) & 0xFF);
      write((len) & 0xFF);
      return;
    }

    // In this implementation, the maximum payload length is (2^31 - 1).
    // So, the first 4 bytes are 0.
    write(0);
    write(0);
    write(0);
    write(0);
    write((len >> 24) & 0xFF);
    write((len >> 16) & 0xFF);
    write((len >> 8) & 0xFF);
    write((len) & 0xFF);
  }




  private void writeFramePayload(final WebSocketFrame frame, final byte[] maskingKey) throws IOException {
    final byte[] payload = frame.getPayload();

    if (payload == null) {
      return;
    }

    for (int i = 0; i < payload.length; ++i) {
      // Mask
      final int b = (payload[i] ^ maskingKey[i % 4]) & 0xFF;

      // Write
      write(b);
    }
  }
}

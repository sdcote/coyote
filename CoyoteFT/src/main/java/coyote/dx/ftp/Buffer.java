package coyote.dx.ftp;

public class Buffer {
  final byte[] tmp = new byte[4];
  byte[] buffer;
  int index;
  int s;




  public Buffer( final int size ) {
    buffer = new byte[size];
    index = 0;
    s = 0;
  }




  public Buffer( final byte[] buffer ) {
    this.buffer = buffer;
    index = 0;
    s = 0;
  }




  public Buffer() {
    this( 1024 * 10 * 2 );
  }




  public void putByte( final byte foo ) {
    buffer[index++] = foo;
  }




  public void putByte( final byte[] foo ) {
    putByte( foo, 0, foo.length );
  }




  public void putByte( final byte[] foo, final int begin, final int length ) {
    System.arraycopy( foo, begin, buffer, index, length );
    index += length;
  }




  public void putString( final byte[] foo ) {
    putString( foo, 0, foo.length );
  }




  public void putString( final byte[] foo, final int begin, final int length ) {
    putInt( length );
    putByte( foo, begin, length );
  }




  public void putInt( final int val ) {
    tmp[0] = (byte)( val >>> 24 );
    tmp[1] = (byte)( val >>> 16 );
    tmp[2] = (byte)( val >>> 8 );
    tmp[3] = (byte)( val );
    System.arraycopy( tmp, 0, buffer, index, 4 );
    index += 4;
  }




  public void putLong( final long val ) {
    tmp[0] = (byte)( val >>> 56 );
    tmp[1] = (byte)( val >>> 48 );
    tmp[2] = (byte)( val >>> 40 );
    tmp[3] = (byte)( val >>> 32 );
    System.arraycopy( tmp, 0, buffer, index, 4 );
    tmp[0] = (byte)( val >>> 24 );
    tmp[1] = (byte)( val >>> 16 );
    tmp[2] = (byte)( val >>> 8 );
    tmp[3] = (byte)( val );
    System.arraycopy( tmp, 0, buffer, index + 4, 4 );
    index += 8;
  }




  void skip( final int n ) {
    index += n;
  }




  void putPad( int n ) {
    while ( n > 0 ) {
      buffer[index++] = (byte)0;
      n--;
    }
  }




  public void putMPInt( final byte[] foo ) {
    int i = foo.length;
    if ( ( foo[0] & 0x80 ) != 0 ) {
      i++;
      putInt( i );
      putByte( (byte)0 );
    } else {
      putInt( i );
    }
    putByte( foo );
  }




  public int getLength() {
    return index - s;
  }




  public int getOffSet() {
    return s;
  }




  public void setOffSet( final int s ) {
    this.s = s;
  }




  public long getLong() {
    long foo = getInt() & 0xffffffffL;
    foo = ( ( foo << 32 ) ) | ( getInt() & 0xffffffffL );
    return foo;
  }




  public int getInt() {
    int foo = getShort();
    foo = ( ( foo << 16 ) & 0xffff0000 ) | ( getShort() & 0xffff );
    return foo;
  }




  public long getUInt() {
    long foo = 0L;
    long bar = 0L;
    foo = getByte();
    foo = ( ( foo << 8 ) & 0xff00 ) | ( getByte() & 0xff );
    bar = getByte();
    bar = ( ( bar << 8 ) & 0xff00 ) | ( getByte() & 0xff );
    foo = ( ( foo << 16 ) & 0xffff0000 ) | ( bar & 0xffff );
    return foo;
  }




  int getShort() {
    int foo = getByte();
    foo = ( ( foo << 8 ) & 0xff00 ) | ( getByte() & 0xff );
    return foo;
  }




  public int getByte() {
    return ( buffer[s++] & 0xff );
  }




  public void getByte( final byte[] foo ) {
    getByte( foo, 0, foo.length );
  }




  void getByte( final byte[] foo, final int start, final int len ) {
    System.arraycopy( buffer, s, foo, start, len );
    s += len;
  }




  public int getByte( final int len ) {
    final int foo = s;
    s += len;
    return foo;
  }




  public byte[] getMPInt() {
    int i = getInt(); // uint32
    if ( ( i < 0 ) || // bigger than 0x7fffffff
        ( i > ( 8 * 1024 ) ) ) {
      // TODO: an exception should be thrown.
      i = 8 * 1024; // the session will be broken, but working around
      // OOME.
    }
    final byte[] foo = new byte[i];
    getByte( foo, 0, i );
    return foo;
  }




  public byte[] getMPIntBits() {
    final int bits = getInt();
    final int bytes = ( bits + 7 ) / 8;
    byte[] foo = new byte[bytes];
    getByte( foo, 0, bytes );
    if ( ( foo[0] & 0x80 ) != 0 ) {
      final byte[] bar = new byte[foo.length + 1];
      bar[0] = 0; // ??
      System.arraycopy( foo, 0, bar, 1, foo.length );
      foo = bar;
    }
    return foo;
  }




  public byte[] getString() {
    int i = getInt(); // uint32
    if ( ( i < 0 ) || // bigger than 0x7fffffff
        ( i > ( 256 * 1024 ) ) ) {
      // TODO: an exception should be thrown.
      i = 256 * 1024; // the session will be broken, but working around
      // OOME.
    }
    final byte[] foo = new byte[i];
    getByte( foo, 0, i );
    return foo;
  }




  byte[] getString( final int[] start, final int[] len ) {
    final int i = getInt();
    start[0] = getByte( i );
    len[0] = i;
    return buffer;
  }




  public void reset() {
    index = 0;
    s = 0;
  }




  public void shift() {
    if ( s == 0 )
      return;
    System.arraycopy( buffer, s, buffer, 0, index - s );
    index = index - s;
    s = 0;
  }




  void rewind() {
    s = 0;
  }




  byte getCommand() {
    return buffer[5];
  }




  void checkFreeSize( final int n ) {
    if ( buffer.length < ( index + n ) ) {
      final byte[] tmp = new byte[buffer.length * 2];
      System.arraycopy( buffer, 0, tmp, 0, index );
      buffer = tmp;
    }
  }

}

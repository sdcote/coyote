package coyote.commons.network.http.wsc;

class InsufficientDataException extends WebSocketException {
  private static final long serialVersionUID = -7644717922800198356L;
  private final int requestedByteCount;
  private final int readByteCount;




  public InsufficientDataException(final int requestedByteCount, final int readByteCount) {
    super(WebSocketError.INSUFFICENT_DATA, "The end of the stream has been reached unexpectedly.");
    this.requestedByteCount = requestedByteCount;
    this.readByteCount = readByteCount;
  }




  public int getReadByteCount() {
    return readByteCount;
  }




  public int getRequestedByteCount() {
    return requestedByteCount;
  }
  
}

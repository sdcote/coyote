package coyote.commons.network.http.wsc;

/**
 * Types of threads which are created internally in the implementation.
 */
public enum ThreadType {
  /** A thread which reads WebSocket frames from the server */
  READING_THREAD,

  /** A thread which sends WebSocket frames to the server */
  WRITING_THREAD,

  /** A thread which calls {@link WebSocket#connect()} asynchronously */
  CONNECT_THREAD,

  /** A thread which does finalization of a {@link WebSocket} instance. */
  FINISH_THREAD,
}

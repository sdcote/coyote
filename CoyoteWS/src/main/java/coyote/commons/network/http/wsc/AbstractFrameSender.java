package coyote.commons.network.http.wsc;

import java.util.Timer;
import java.util.TimerTask;

import coyote.loader.log.Log;


/**
 * Base class for sending frames at regular intervals.
 * 
 * @see PingSender
 * @see PongSender
 */
abstract class AbstractFrameSender {
  private final WebSocket webSocket;
  private final String timerName;
  private Timer timer;
  private boolean scheduled;
  private long interval;
  private PayloadGenerator generator;




  public AbstractFrameSender(final WebSocket webSocket, final String timerName, final PayloadGenerator generator) {
    this.webSocket = webSocket;
    this.timerName = timerName;
    this.generator = generator;
  }




  private void doTask() {
    synchronized (this) {
      if (interval == 0 || webSocket.isOpen() == false) {
        scheduled = false;

        // Not schedule a new task.
        return;
      }

      // Create a frame and send it to the server.
      webSocket.sendFrame(createFrame());

      // Schedule a new task.
      scheduled = schedule(timer, new Task(), interval);
    }
  }




  private WebSocketFrame createFrame() {
    // Prepare payload of a frame.
    final byte[] payload = generatePayload();

    // Let the subclass create a frame.
    return createFrame(payload);
  }




  /**
   * Create a frame using the given payload.
   * 
   * @param payload the payload for the frame
   * 
   * @return a frame containing the payload
   */
  protected abstract WebSocketFrame createFrame(byte[] payload);




  /**
   * Have the currently set payload generator generate the payload.
   * 
   * @return the data representing the generated payload
   */
  private byte[] generatePayload() {
    if (generator == null) {
      return null;
    }

    try {
      return generator.generate();
    } catch (final Throwable t) {
      Log.error(generator.getClass().getName() + " could not generate a payload", t);
      return null;
    }
  }




  public long getInterval() {
    synchronized (this) {
      return interval;
    }
  }




  public PayloadGenerator getPayloadGenerator() {
    synchronized (this) {
      return generator;
    }
  }




  public void setInterval(long interval) {
    if (interval < 0) {
      interval = 0;
    }

    synchronized (this) {
      this.interval = interval;
    }

    if (interval == 0) {
      return;
    }

    if (webSocket.isOpen() == false) {
      return;
    }

    synchronized (this) {
      if (timer == null) {
        timer = new Timer(timerName);
      }

      if (scheduled == false) {
        scheduled = schedule(timer, new Task(), interval);
      }
    }
  }




  public void setPayloadGenerator(final PayloadGenerator generator) {
    synchronized (this) {
      this.generator = generator;
    }
  }




  public void start() {
    setInterval(getInterval());
  }




  public void stop() {
    synchronized (this) {
      if (timer == null) {
        return;
      }

      scheduled = false;
      timer.cancel();
    }
  }




  private static boolean schedule(final Timer timer, final Task task, final long interval) {
    try {
      // Schedule the task.
      timer.schedule(task, interval);

      // Successfully scheduled the task.
      return true;
    } catch (final RuntimeException e) {
      // Failed to schedule the task. Probably, the exception is
      // an IllegalStateException which is raised due to one of
      // the following reasons (according to the Javadoc):
      //
      //   (1) if task was already scheduled or cancelled,
      //   (2) timer was cancelled, or
      //   (3) timer thread terminated.
      //
      // Because a new task is created every time this method is
      // called and there is no code to call TimerTask.cancel(),
      // (1) cannot be a reason.
      //
      // In either case of (2) and (3), we don't have to retry to
      // schedule the task, because the timer that is expected to
      // host the task will stop or has stopped anyway.
      return false;
    }
  }

  private final class Task extends TimerTask {
    @Override
    public void run() {
      doTask();
    }
  }
}

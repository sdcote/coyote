package coyote.dx.writer;

import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.FrameWriter;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

/**
 * This writer collects all the metric sample and generates an OpenMetric payload to sent to a Prometheus PushGateway.
 */
public class PushGatewayWriter extends AbstractFrameWriter implements FrameWriter {


  /**
   * @see coyote.dx.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (expression != null) {
      try {
        if (evaluator.evaluateBoolean(expression)) {
          System.out.println(frame);
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage()));
      }
    } else {
      System.out.println(frame);
    }
  }

}

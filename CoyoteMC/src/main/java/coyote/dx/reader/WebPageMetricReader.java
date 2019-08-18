package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;

public class WebPageMetricReader extends AbstractFrameReader implements FrameReader {

  @Override
  public DataFrame read(TransactionContext context) {
    return null;
  }

  @Override
  public boolean eof() {
    return false;
  }

}

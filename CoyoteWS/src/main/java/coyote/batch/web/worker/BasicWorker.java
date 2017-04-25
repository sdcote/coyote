package coyote.batch.web.worker;

import coyote.batch.web.Resource;


public class BasicWorker extends AbstractWorker implements ResourceWorker {

  public BasicWorker( final Resource resource ) {
    super( resource );
  }

}

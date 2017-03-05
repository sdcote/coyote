package coyote.batch.http;

public class TestResponse {
  String location = null;
  int status = 0;
  String data = null;
  private Exception exception=null;
  private volatile boolean complete=false;




  public TestResponse( final String url ) {
    location = url;
  }




  public String getData() {
    return data;
  }




  public String getLocation() {
    return location;
  }




  public int getStatus() {
    return status;
  }




  public void setData( final String data ) {
    this.data = data;
  }




  public void setLocation( final String url ) {
    location = url;
  }




  public void setStatus( final int code ) {
    status = code;
  }




  /**
   * @param ex The exception thrown by the client performing its request.
   */
  public void setException( Exception ex) {
    exception = ex;
  }


  /**
   * @return the exception thrown by the client
   */
  public Exception getException(){
    return exception;
  }


  /**
   * @return true if the exchange was completed sucessfully, false if there was an error.
   */
  public boolean isComplete() {
    return complete;
  }




  /**
   * @param flag The stete of the completion, true means the exchange completed sucessfully.
   */
  public void setComplete( boolean flag ) {
    complete = flag;
  }

}

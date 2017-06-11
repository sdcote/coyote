package coyote.dataframe.marshal;

public class MarshalException extends RuntimeException {

  private static final long serialVersionUID = 651310756998856295L;




  public MarshalException() {}




  public MarshalException( String msg ) {
    super( msg );
  }




  public MarshalException( String msg, Throwable t ) {
    super( msg, t );
  }

}
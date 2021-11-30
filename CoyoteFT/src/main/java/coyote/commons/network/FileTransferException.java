package coyote.commons.network;

public class FileTransferException extends Exception {

  private static final long serialVersionUID = -8021115820787018776L;




  public FileTransferException( final String message ) {
    super( message );
  }




  public FileTransferException( final String message, final Throwable cause ) {
    super( message, cause );
  }
}
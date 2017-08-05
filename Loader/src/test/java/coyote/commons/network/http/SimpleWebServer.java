package coyote.commons.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringTokenizer;

import coyote.commons.network.MimeType;


public class SimpleWebServer extends HTTPD {

  /**
   * Default Index file names.
   */
  public static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {
    private static final long serialVersionUID = -7551230240891475502L;
    {
      add( "index.html" );
      add( "index.htm" );
    }
  };

  private static Map<String, WebServerPlugin> mimeTypeHandlers = new HashMap<String, WebServerPlugin>();

  private final static String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

  private final static int MAX_AGE = 42 * 60 * 60;

  // explicitly relax visibility to package for tests purposes
  final static String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type";

  public final static String ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME = "AccessControlAllowHeader";




  /**
   * Starts as a stand-alone file server and waits for Enter.
   * <p>Parameters as as follows
   * <li>-h or --host</li>
   * <li>-p or --port 8080 is the default</li>
   * <li>-q or --quiet</li>
   * <li>-d or --dir content directory. Use multiple time to specify multiple directories, current working directory is the default</li>
   * <li>--cors</li>
   * <li>-X:[name]=[value] set a named option</li>
   * <li></li>
   */
  public static void main( final String[] args ) {
    // Defaults
    int port = 8080;

    String host = null; // bind to all interfaces by default
    final List<File> rootDirs = new ArrayList<File>();
    boolean quiet = false;
    String cors = null;
    final Map<String, String> options = new HashMap<String, String>();

    // Parse command-line, with short and long versions of the options.
    for ( int i = 0; i < args.length; ++i ) {
      if ( "-h".equalsIgnoreCase( args[i] ) || "--host".equalsIgnoreCase( args[i] ) ) {
        host = args[i + 1];
      } else if ( "-p".equalsIgnoreCase( args[i] ) || "--port".equalsIgnoreCase( args[i] ) ) {
        port = Integer.parseInt( args[i + 1] );
      } else if ( "-q".equalsIgnoreCase( args[i] ) || "--quiet".equalsIgnoreCase( args[i] ) ) {
        quiet = true;
      } else if ( "-d".equalsIgnoreCase( args[i] ) || "--dir".equalsIgnoreCase( args[i] ) ) {
        rootDirs.add( new File( args[i + 1] ).getAbsoluteFile() );
      } else if ( args[i].startsWith( "--cors" ) ) {
        cors = "*";
        final int equalIdx = args[i].indexOf( '=' );
        if ( equalIdx > 0 ) {
          cors = args[i].substring( equalIdx + 1 );
        }
      } else if ( args[i].startsWith( "-X:" ) ) {
        final int dot = args[i].indexOf( '=' );
        if ( dot > 0 ) {
          final String name = args[i].substring( 0, dot );
          final String value = args[i].substring( dot + 1, args[i].length() );
          options.put( name, value );
        }
      }
    }

    if ( rootDirs.isEmpty() ) {
      rootDirs.add( new File( "." ).getAbsoluteFile() );
    }
    options.put( "host", host );
    options.put( "port", "" + port );
    options.put( "quiet", String.valueOf( quiet ) );
    final StringBuilder sb = new StringBuilder();
    for ( final File dir : rootDirs ) {
      if ( sb.length() > 0 ) {
        sb.append( ":" );
      }
      try {
        sb.append( dir.getCanonicalPath() );
      } catch ( final IOException ignored ) {}
    }
    options.put( "home", sb.toString() );
    final ServiceLoader<WebServerPluginInfo> serviceLoader = ServiceLoader.load( WebServerPluginInfo.class );
    for ( final WebServerPluginInfo info : serviceLoader ) {
      final String[] mimeTypes = info.getMimeTypes();
      for ( final String mime : mimeTypes ) {
        final String[] indexFiles = info.getIndexFilesForMimeType( mime );
        if ( !quiet ) {
          System.out.print( "# Found plugin for Mime type: \"" + mime + "\"" );
          if ( indexFiles != null ) {
            System.out.print( " (serving index files: " );
            for ( final String indexFile : indexFiles ) {
              System.out.print( indexFile + " " );
            }
          }
          System.out.println( ")." );
        }
        registerPluginForMimeType( indexFiles, mime, info.getWebServerPlugin( mime ), options );
      }
    }
    ServerRunner.executeInstance( new SimpleWebServer( host, port, rootDirs, quiet, cors ) );
  }




  public static Response newFixedLengthResponse( final IStatus status, final String mimeType, final String message ) {
    final Response response = Response.createFixedLengthResponse( status, mimeType, message );
    response.addHeader( "Accept-Ranges", "bytes" );
    return response;
  }




  protected static void registerPluginForMimeType( final String[] indexFiles, final String mimeType, final WebServerPlugin plugin, final Map<String, String> commandLineOptions ) {
    if ( ( mimeType == null ) || ( plugin == null ) ) {
      return;
    }

    if ( indexFiles != null ) {
      for ( final String filename : indexFiles ) {
        final int dot = filename.lastIndexOf( '.' );
        if ( dot >= 0 ) {
          filename.substring( dot + 1 ).toLowerCase();
        }
      }
      SimpleWebServer.INDEX_FILE_NAMES.addAll( Arrays.asList( indexFiles ) );
    }
    SimpleWebServer.mimeTypeHandlers.put( mimeType, plugin );
    plugin.initialize( commandLineOptions );
  }

  private final boolean quiet;

  private final String cors;

  protected List<File> rootDirs;




  public SimpleWebServer( final String host, final int port, final File wwwroot, final boolean quiet ) {
    this( host, port, Collections.singletonList( wwwroot ), quiet, null );
  }




  public SimpleWebServer( final String host, final int port, final File wwwroot, final boolean quiet, final String cors ) {
    this( host, port, Collections.singletonList( wwwroot ), quiet, cors );
  }




  public SimpleWebServer( final String host, final int port, final List<File> wwwroots, final boolean quiet ) {
    this( host, port, wwwroots, quiet, null );
  }




  public SimpleWebServer( final String host, final int port, final List<File> wwwroots, final boolean quiet, final String cors ) {
    super( host, port );
    this.quiet = quiet;
    this.cors = cors;
    rootDirs = new ArrayList<File>( wwwroots );

    init();
  }




  protected Response addCORSHeaders( final Map<String, String> queryHeaders, final Response resp, final String cors ) {
    resp.addHeader( "Access-Control-Allow-Origin", cors );
    resp.addHeader( "Access-Control-Allow-Headers", calculateAllowHeaders( queryHeaders ) );
    resp.addHeader( "Access-Control-Allow-Credentials", "true" );
    resp.addHeader( "Access-Control-Allow-Methods", ALLOWED_METHODS );
    resp.addHeader( "Access-Control-Max-Age", "" + MAX_AGE );

    return resp;
  }




  private String calculateAllowHeaders( final Map<String, String> queryHeaders ) {
    // here we should use the given asked headers but HTTPD uses a Map whereas 
    // it is possible for requester to send several time the same header let's 
    // just use default values for this version
    return System.getProperty( ACCESS_CONTROL_ALLOW_HEADER_PROPERTY_NAME, DEFAULT_ALLOWED_HEADERS );
  }




  private boolean canServeUri( final String uri, final File homeDir ) {
    boolean canServeUri;
    final File f = new File( homeDir, uri );
    canServeUri = f.exists();
    if ( !canServeUri ) {
      final WebServerPlugin plugin = SimpleWebServer.mimeTypeHandlers.get( getMimeTypeForFile( uri ) );
      if ( plugin != null ) {
        canServeUri = plugin.canServeUri( uri, homeDir );
      }
    }
    return canServeUri;
  }




  private Response defaultRespond( final Map<String, String> headers, final IHTTPSession session, String uri ) {
    // Remove URL arguments
    uri = uri.trim().replace( File.separatorChar, '/' );
    if ( uri.indexOf( '?' ) >= 0 ) {
      uri = uri.substring( 0, uri.indexOf( '?' ) );
    }

    // Prohibit getting out of current directory
    if ( uri.contains( "../" ) ) {
      return getForbiddenResponse( "Won't serve ../ for security reasons." );
    }

    boolean canServeUri = false;
    File homeDir = null;
    for ( int i = 0; !canServeUri && ( i < rootDirs.size() ); i++ ) {
      homeDir = rootDirs.get( i );
      canServeUri = canServeUri( uri, homeDir );
    }
    if ( !canServeUri ) {
      return getNotFoundResponse();
    }

    // Browsers get confused without '/' after the directory, send a redirect.
    final File f = new File( homeDir, uri );
    if ( f.isDirectory() && !uri.endsWith( "/" ) ) {
      uri += "/";
      final Response res = newFixedLengthResponse( Status.REDIRECT, MimeType.HTML.getType(), "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>" );
      res.addHeader( HTTP.HDR_LOCATION, uri );
      return res;
    }

    if ( f.isDirectory() ) {
      // First look for index files (index.html, index.htm, etc) and if
      // none found, list the directory if readable.
      final String indexFile = findIndexFileInDirectory( f );
      if ( indexFile == null ) {
        if ( f.canRead() ) {
          // No index file, list the directory if it is readable
          return newFixedLengthResponse( Status.OK, MimeType.HTML.getType(), listDirectory( uri, f ) );
        } else {
          return getForbiddenResponse( "No directory listing." );
        }
      } else {
        return respond( headers, session, uri + indexFile );
      }
    }
    final String mimeTypeForFile = getMimeTypeForFile( uri );
    final WebServerPlugin plugin = SimpleWebServer.mimeTypeHandlers.get( mimeTypeForFile );
    Response response = null;
    if ( ( plugin != null ) && plugin.canServeUri( uri, homeDir ) ) {
      response = plugin.serveFile( uri, headers, session, f, mimeTypeForFile );
      if ( ( response != null ) && ( response instanceof InternalRewrite ) ) {
        final InternalRewrite rewrite = (InternalRewrite)response;
        return respond( rewrite.getHeaders(), session, rewrite.getUri() );
      }
    } else {
      response = serveFile( uri, headers, f, mimeTypeForFile );
    }
    return response != null ? response : getNotFoundResponse();
  }




  /**
   * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
   * instead of '+'.
   */
  private String encodeUri( final String uri ) {
    String newUri = "";
    final StringTokenizer st = new StringTokenizer( uri, "/ ", true );
    while ( st.hasMoreTokens() ) {
      final String tok = st.nextToken();
      if ( "/".equals( tok ) ) {
        newUri += "/";
      } else if ( " ".equals( tok ) ) {
        newUri += "%20";
      } else {
        try {
          newUri += URLEncoder.encode( tok, "UTF-8" );
        } catch ( final UnsupportedEncodingException ignored ) {}
      }
    }
    return newUri;
  }




  private String findIndexFileInDirectory( final File directory ) {
    for ( final String fileName : SimpleWebServer.INDEX_FILE_NAMES ) {
      final File indexFile = new File( directory, fileName );
      if ( indexFile.isFile() ) {
        return fileName;
      }
    }
    return null;
  }




  protected Response getForbiddenResponse( final String s ) {
    return newFixedLengthResponse( Status.FORBIDDEN, MimeType.TEXT.getType(), "FORBIDDEN: " + s );
  }




  protected Response getInternalErrorResponse( final String s ) {
    return newFixedLengthResponse( Status.INTERNAL_ERROR, MimeType.TEXT.getType(), "INTERNAL ERROR: " + s );
  }




  protected Response getNotFoundResponse() {
    return newFixedLengthResponse( Status.NOT_FOUND, MimeType.TEXT.getType(), "Error 404, file not found." );
  }




  /**
   * Used to initialize and customize the server.
   */
  public void init() {}




  protected String listDirectory( final String uri, final File f ) {
    final String heading = "Directory " + uri;
    final StringBuilder msg = new StringBuilder( "<html><head><title>" + heading + "</title><style><!--\n" + "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n" + "// -->\n" + "</style>" + "</head><body><h1>" + heading + "</h1>" );

    String up = null;
    if ( uri.length() > 1 ) {
      final String u = uri.substring( 0, uri.length() - 1 );
      final int slash = u.lastIndexOf( '/' );
      if ( ( slash >= 0 ) && ( slash < u.length() ) ) {
        up = uri.substring( 0, slash + 1 );
      }
    }

    final List<String> files = Arrays.asList( f.list( new FilenameFilter() {

      @Override
      public boolean accept( final File dir, final String name ) {
        return new File( dir, name ).isFile();
      }
    } ) );
    Collections.sort( files );
    final List<String> directories = Arrays.asList( f.list( new FilenameFilter() {

      @Override
      public boolean accept( final File dir, final String name ) {
        return new File( dir, name ).isDirectory();
      }
    } ) );
    Collections.sort( directories );
    if ( ( up != null ) || ( ( directories.size() + files.size() ) > 0 ) ) {
      msg.append( "<ul>" );
      if ( ( up != null ) || ( directories.size() > 0 ) ) {
        msg.append( "<section class=\"directories\">" );
        if ( up != null ) {
          msg.append( "<li><a rel=\"directory\" href=\"" ).append( up ).append( "\"><span class=\"dirname\">..</span></a></b></li>" );
        }
        for ( final String directory : directories ) {
          final String dir = directory + "/";
          msg.append( "<li><a rel=\"directory\" href=\"" ).append( encodeUri( uri + dir ) ).append( "\"><span class=\"dirname\">" ).append( dir ).append( "</span></a></b></li>" );
        }
        msg.append( "</section>" );
      }
      if ( files.size() > 0 ) {
        msg.append( "<section class=\"files\">" );
        for ( final String file : files ) {
          msg.append( "<li><a href=\"" ).append( encodeUri( uri + file ) ).append( "\"><span class=\"filename\">" ).append( file ).append( "</span></a>" );
          final File curFile = new File( f, file );
          final long len = curFile.length();
          msg.append( "&nbsp;<span class=\"filesize\">(" );
          if ( len < 1024 ) {
            msg.append( len ).append( " bytes" );
          } else if ( len < ( 1024 * 1024 ) ) {
            msg.append( len / 1024 ).append( "." ).append( ( ( len % 1024 ) / 10 ) % 100 ).append( " KB" );
          } else {
            msg.append( len / ( 1024 * 1024 ) ).append( "." ).append( ( ( len % ( 1024 * 1024 ) ) / 10000 ) % 100 ).append( " MB" );
          }
          msg.append( ")</span></li>" );
        }
        msg.append( "</section>" );
      }
      msg.append( "</ul>" );
    }
    msg.append( "</body></html>" );
    return msg.toString();
  }




  private Response newFixedFileResponse( final File file, final String mime ) throws FileNotFoundException {
    Response res;
    res = Response.createFixedLengthResponse( Status.OK, mime, new FileInputStream( file ), (int)file.length() );
    res.addHeader( "Accept-Ranges", "bytes" );
    return res;
  }




  private Response respond( final Map<String, String> headers, final IHTTPSession session, final String uri ) {
    // First let's handle CORS OPTION query
    Response r;
    if ( ( cors != null ) && Method.OPTIONS.equals( session.getMethod() ) ) {
      r = new Response( Status.OK, MimeType.HTML.getType(), null, 0 );
    } else {
      r = defaultRespond( headers, session, uri );
    }

    if ( cors != null ) {
      r = addCORSHeaders( headers, r, cors );
    }
    return r;
  }




  @Override
  public Response serve( final IHTTPSession session ) {
    final Map<String, String> header = session.getRequestHeaders();
    final Map<String, String> parms = session.getParms();
    final String uri = session.getUri();

    if ( !quiet ) {
      System.out.println( session.getMethod() + " '" + uri + "' " );

      Iterator<String> e = header.keySet().iterator();
      while ( e.hasNext() ) {
        final String value = e.next();
        System.out.println( "  HDR: '" + value + "' = '" + header.get( value ) + "'" );
      }
      e = parms.keySet().iterator();
      while ( e.hasNext() ) {
        final String value = e.next();
        System.out.println( "  PRM: '" + value + "' = '" + parms.get( value ) + "'" );
      }
    }

    for ( final File homeDir : rootDirs ) {
      // Make sure we won't die of an exception later
      if ( !homeDir.isDirectory() ) {
        return getInternalErrorResponse( "given path is not a directory (" + homeDir + ")." );
      }
    }
    return respond( Collections.unmodifiableMap( header ), session, uri );
  }




  /**
   * Serves file from homeDir and its' subdirectories (only). Uses only URI,
   * ignores all headers and HTTP parameters.
   */
  Response serveFile( final String uri, final Map<String, String> header, final File file, final String mime ) {
    Response res;
    try {
      // Calculate etag
      final String etag = Integer.toHexString( ( file.getAbsolutePath() + file.lastModified() + "" + file.length() ).hashCode() );

      // Support (simple) skipping:
      long startFrom = 0;
      long endAt = -1;
      String range = header.get( "range" );
      if ( range != null ) {
        if ( range.startsWith( "bytes=" ) ) {
          range = range.substring( "bytes=".length() );
          final int minus = range.indexOf( '-' );
          try {
            if ( minus > 0 ) {
              startFrom = Long.parseLong( range.substring( 0, minus ) );
              endAt = Long.parseLong( range.substring( minus + 1 ) );
            }
          } catch ( final NumberFormatException ignored ) {}
        }
      }

      // get if-range header. If present, it must match etag or else we
      // should ignore the range request
      final String ifRange = header.get( "if-range" );
      final boolean headerIfRangeMissingOrMatching = ( ( ifRange == null ) || etag.equals( ifRange ) );

      final String ifNoneMatch = header.get( "if-none-match" );
      final boolean headerIfNoneMatchPresentAndMatching = ( ifNoneMatch != null ) && ( "*".equals( ifNoneMatch ) || ifNoneMatch.equals( etag ) );

      // Change return code and add Content-Range header when skipping is
      // requested
      final long fileLen = file.length();

      if ( headerIfRangeMissingOrMatching && ( range != null ) && ( startFrom >= 0 ) && ( startFrom < fileLen ) ) {
        // range request that matches current etag
        // and the startFrom of the range is satisfiable
        if ( headerIfNoneMatchPresentAndMatching ) {
          // range request that matches current etag
          // and the startFrom of the range is satisfiable
          // would return range from file
          // respond with not-modified
          res = newFixedLengthResponse( Status.NOT_MODIFIED, mime, "" );
          res.addHeader( "ETag", etag );
        } else {
          if ( endAt < 0 ) {
            endAt = fileLen - 1;
          }
          long newLen = ( endAt - startFrom ) + 1;
          if ( newLen < 0 ) {
            newLen = 0;
          }

          final FileInputStream fis = new FileInputStream( file );
          fis.skip( startFrom );

          res = Response.createFixedLengthResponse( Status.PARTIAL_CONTENT, mime, fis, newLen );
          res.addHeader( "Accept-Ranges", "bytes" );
          res.addHeader( "Content-Length", "" + newLen );
          res.addHeader( "Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen );
          res.addHeader( "ETag", etag );
        }
      } else {

        if ( headerIfRangeMissingOrMatching && ( range != null ) && ( startFrom >= fileLen ) ) {
          // return the size of the file
          // 4xx responses are not trumped by if-none-match
          res = newFixedLengthResponse( Status.RANGE_NOT_SATISFIABLE, MimeType.TEXT.getType(), "" );
          res.addHeader( "Content-Range", "bytes */" + fileLen );
          res.addHeader( "ETag", etag );
        } else if ( ( range == null ) && headerIfNoneMatchPresentAndMatching ) {
          // full-file-fetch request
          // would return entire file
          // respond with not-modified
          res = newFixedLengthResponse( Status.NOT_MODIFIED, mime, "" );
          res.addHeader( "ETag", etag );
        } else if ( !headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching ) {
          // range request that doesn't match current etag
          // would return entire (different) file
          // respond with not-modified

          res = newFixedLengthResponse( Status.NOT_MODIFIED, mime, "" );
          res.addHeader( "ETag", etag );
        } else {
          // supply the file
          res = newFixedFileResponse( file, mime );
          res.addHeader( "Content-Length", "" + fileLen );
          res.addHeader( "ETag", etag );
        }
      }
    } catch ( final IOException ioe ) {
      res = getForbiddenResponse( "Reading file failed." );
    }

    return res;
  }
}

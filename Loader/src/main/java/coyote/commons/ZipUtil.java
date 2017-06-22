/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial API and implementation
 */
package coyote.commons;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Various methods to assist in compressing files.
 */
public class ZipUtil {
  private static final int ZIP_BUFFER_SIZE = 50;
  private static final int STREAM_BUFFER_SIZE = 8 * 1024;
  private static final String PREFIX = "MAP";
  private static final String SUFFIX = "TMP";
  private static final String TMP_FILENAME = PREFIX + "." + SUFFIX;
  private static final String ZIP_SUFFIX = ".zip";




  private ZipUtil() {}




  /**
   * Method addEntries
   *
   * @param zos The ZipOutputStream to send all our ZipEntries through
   * @param topDir The Top-level directory used to calculate the proper 
   *        relative ZipEntry name
   * @param currentFile The current file or directory we are zipping-up this
   *        iteration
   * @param match The file globber we use to indicate the files to include in
   *        the ZIP archive stream, can be null to include everything
   * @param avoid The file globber we use to indicate the files to exclude 
   *        from the ZIP archive stream, can be null to exclude nothing
   * 
   * @throws IOException if problems occurred.
   */
  private static void addEntries( final ZipOutputStream zos, final File topDir, final File currentFile, final Glob match, final Glob avoid ) throws IOException {
    int len;
    ZipEntry ze;
    final byte[] buf = new byte[STREAM_BUFFER_SIZE];

    // If we have a match Glob and it has no matching files, just return
    if ( ( match != null ) && !hasMatchingFiles( currentFile, match ) ) {
      return;
    }

    final String[] files = currentFile.list();

    for ( int i = 0; i < files.length; ++i ) {
      final File f = new File( currentFile, files[i] );

      // If we have a Glob of files to avoid and it matches this file, skip it
      if ( ( avoid != null ) && avoid.isFileMatched( files[i] ) ) {
        continue;
      }

      // If we have a Glob of files to match and the file doesn't match,
      // skip the file
      if ( ( match != null ) && !match.isFileMatched( files[i] ) ) {
        continue;
      }

      if ( f.isDirectory() ) {
        // recurse into this directory
        addEntries( zos, topDir, f, match, avoid );
      } else {
        FileInputStream ins = null;
        try {
          ins = new FileInputStream( f );

          ze = new ZipEntry( getZipName( topDir, f ) );

          ze.setTime( f.lastModified() );
          ze.setMethod( ZipEntry.DEFLATED );
          zos.putNextEntry( ze );

          for ( ; ( len = ins.read( buf ) ) >= 0; ) {
            zos.write( buf, 0, len );
          }
        } catch ( final IOException ignore ) {
          throw ignore;
        }
        finally {
          zos.closeEntry();
          ins.close();
        }
      }
    }
  }




  /**
   * Calculate a relational path name of a file from the given top level
   * directory.
   *
   * <p>Assumes the file is a child of the given top level directory.
   *
   * @param topDir The top directory of the zip archive
   * @param file The file currently being named
   *
   * @return A name that represents the complete path of the file from the top
   *         directory
   *
   * @throws IOException if the CanonicalPath could not be retrieved for either
   *         argument
   */
  private static String getZipName( final File topDir, final File file ) throws IOException {
    final String path = file.getCanonicalPath();
    final String root = topDir.getCanonicalPath();
    String relPath = path.substring( root.length() + 1 ).replace( File.separatorChar, '/' );

    if ( file.isDirectory() ) {
      if ( !relPath.endsWith( "/" ) ) {
        relPath = relPath + "/";
      }
    }

    return relPath;
  }




  private static boolean hasMatchingFiles( final File dir, final Glob glob ) {
    if ( dir.isDirectory() ) {
      final String[] files = dir.list();

      for ( final String file : files ) {
        final File f = new File( dir, file );

        if ( f.isDirectory() ) {
          if ( hasMatchingFiles( f, glob ) ) {
            return true;
          }
        } else {
          if ( glob.isFileMatched( file ) ) {
            return true;
          }
        }
      }
    } else {
      if ( glob.isFileMatched( dir.getName() ) ) {
        return true;
      }
    }

    return false;
  }




  private static void inflaterInputStmToFileOutputStm( final InflaterInputStream stmIn, final FileOutputStream stmOut ) throws IOException {
    byte[] buffer = null;
    final int iBufferSize = STREAM_BUFFER_SIZE;

    boolean bKeepStreaming = true;

    while ( bKeepStreaming ) {
      buffer = new byte[iBufferSize];

      final int iBytes = stmIn.read( buffer );

      if ( iBytes == -1 ) {
        bKeepStreaming = false;
      } else {
        if ( iBytes < iBufferSize ) {
          bKeepStreaming = false;

          final byte[] tmp = new byte[iBytes];

          for ( int i = 0; i < iBytes; i++ ) {
            tmp[i] = buffer[i];
          }

          buffer = tmp;
        }

        stmOut.write( buffer );

        if ( stmIn.available() == 1 ) {
          bKeepStreaming = true;
        }
      }
    }
  }




  /**
   * Simple way to deflate and inflate a file
   *
   * @param args First argument is the file to deflate
   */
  public static void main( final String[] args ) {
    try {
      final File source = new File( args[0] );
      final File target = new File( args[0] + ".zip" );
      System.out.println( "Deflating file" );
      deflateFileToFile( source, target );
      System.out.println( "Inflating file." );
      inflateFileToFile( target, new File( args[0] + ".unzip" ) );
      System.out.println( "Done." );
    } catch ( final Exception ex ) {
      ex.printStackTrace();
    }
  }




  private static byte[] readEntry( final ZipInputStream zis ) throws IOException {
    byte[] retval = null;

    try {
      retval = StreamUtil.readFully( zis );
    }
    finally {
      zis.closeEntry();
    }

    return retval;
  }




  /**
   * Method saveFile
   *
   * @param filename
   * @param name
   * @param target
   *
   * @throws IOException if problems were experienced.
   */
  public static void saveFile( final String filename, final String name, final String target ) throws IOException {
    ZipInputStream zis = null;
    ZipOutputStream zos = null;

    try {
      final File file = new File( filename );

      final File tmpFile = new File( TMP_FILENAME );
      final FileOutputStream fileoutputstream = new FileOutputStream( tmpFile );
      zos = new ZipOutputStream( fileoutputstream );

      if ( file.exists() ) {
        final FileInputStream fileinputstream = new FileInputStream( file );
        zis = new ZipInputStream( fileinputstream );

        do {
          ZipEntry zipentry;

          if ( ( zipentry = zis.getNextEntry() ) == null ) {
            break;
          }

          if ( !zipentry.getName().equals( name ) ) {
            final byte[] data = readEntry( zis );
            writeEntry( zos, zipentry.getName(), data );
          }
        }
        while ( true );

        zis.close();
        zis = null;
        file.delete();
      }

      writeEntry( zos, name, target.getBytes() );
      zos.close();
      zos = null;

      final String s3 = filename.substring( 0, filename.length() - file.getName().length() );
      final File file2 = new File( s3 );

      if ( !file2.exists() ) {
        file2.mkdirs();
      }

      tmpFile.renameTo( file );
    } catch ( final FileNotFoundException filenotfoundexception ) {
      throw new IOException( filenotfoundexception.getMessage() );
    } catch ( final IOException ioexception ) {
      throw new IOException( ioexception.getMessage() );
    }
    finally {
      if ( zis != null ) {
        zis.close();
      }

      if ( zos != null ) {
        zos.close();
      }
    }
  }




  /**
   * Inflates a previously deflated array of bytes.
   *
   * @param ary the deflated array of bytes
   *
   * @return the original (inflated) byte array
   *
   * @throws IOException if problems occurred.
   */
  public static byte[] unzipByteArray( final byte[] ary ) throws IOException {
    byte[] retval = null;

    final Inflater inflater = new Inflater( false );
    inflater.setInput( ary );

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      while ( !inflater.finished() ) {
        final byte[] data = new byte[ZIP_BUFFER_SIZE];
        final int count = inflater.inflate( data );

        if ( count == data.length ) {
          baos.write( data );
        } else {
          baos.write( data, 0, count );
        }
      }

      retval = baos.toByteArray();
    } catch ( final DataFormatException ex ) {
      throw new IOException( "Attempting to unzip data that is not zipped." );
    }
    finally {
      baos.close();
    }

    return retval;
  }




  /**
   * Read a deflated file and inflate it to its original state.
   *
   * <p>This method will NOT read standard Zip archives as it will not handle
   * any Zip entries it may find.
   *
   * <p>This method is a complement to the deflateFileToFile method.
   *
   * @param source
   * @param target
   *
   * @throws IOException if problems were experienced.
   */
  public static void inflateFileToFile( final File source, final File target ) throws IOException {
    final Inflater inflater = new Inflater( false );
    final FileInputStream in = new FileInputStream( source );
    final FileOutputStream out = new FileOutputStream( target );
    final InflaterInputStream inflaterInputStream = new InflaterInputStream( in, inflater );

    try {
      inflaterInputStmToFileOutputStm( inflaterInputStream, out );
    }
    finally {
      out.flush();
      out.close();
      inflaterInputStream.close();
      in.close();
    }
  }




  private static void writeEntry( final ZipOutputStream zos, final String name, final byte[] data ) throws IOException {
    final ZipEntry zipentry = new ZipEntry( name );
    zos.putNextEntry( zipentry );
    zos.write( data, 0, data.length );
    zos.closeEntry();
  }




  /**
   * Take the given file and zip its contents sending the zip data over the
   * given stream.
   *
   * <p>This keeps the zip from having to be built in memory or on disk.
   *
   * @param os outputstream to which compressed data is written
   * @param dir the directory to compress
   *
   * @throws IOException if problems were experienced.
   */
  public static void writeZipStream( final OutputStream os, final File dir ) throws IOException {
    writeZipStream( os, dir, null, null );
  }




  /**
   * Take the given file and zip its contents sending the zip data over the
   * given stream using the given Globs to accept or reject file entries.
   *
   * <p>This keeps the zip from having to be built in memory or on disk.
   *
   * @param os outputstream to which compressed data is written
   * @param file the file or directory to archive
   * @param match The file globber we use to indicate the files to include in
   *        the ZIP archive stream, can be null to include everything
   * @param avoid The file globber we use to indicate the files to exclude 
   *        from the ZIP archive stream, can be null to exclude nothing
   *
   * @throws IOException if problems were experienced.
   */
  public static void writeZipStream( final OutputStream os, final File file, final Glob match, final Glob avoid ) throws IOException {
    final ZipOutputStream zos = new ZipOutputStream( os );

    zos.setLevel( Deflater.BEST_COMPRESSION );
    zos.setMethod( ZipOutputStream.DEFLATED );

    if ( file.isDirectory() ) {
      final ZipEntry ze = new ZipEntry( file.getName() + "/" );
      ze.setTime( file.lastModified() );
      zos.putNextEntry( ze );
      zos.closeEntry();
      addEntries( zos, file, file, match, avoid );
    } else {
      final byte[] buf = new byte[STREAM_BUFFER_SIZE];

      final FileInputStream ins = new FileInputStream( file );

      final ZipEntry ze = new ZipEntry( getZipName( file.getParentFile(), file ) );

      ze.setTime( file.lastModified() );
      ze.setMethod( ZipEntry.DEFLATED );
      zos.putNextEntry( ze );

      for ( int len = 0; ( len = ins.read( buf ) ) >= 0; ) {
        zos.write( buf, 0, len );
      }

      zos.closeEntry();
      ins.close();
    }

    zos.close();
  }




  /**
   * Deflates the file and returns the deflated file.
   * 
   * <p>This is useful for compressing the contents of a single file or data 
   * field. Some use it as an obfuscation tactic, encoding a string to bytes,
   * deflating it and converting to Base64.
   *
   * @param array the array of bytes to be deflated
   *
   * @return a compressed (deflated) array of bytes
   *
   * @throws IOException if problems were experienced.
   */
  public static byte[] deflateByteArray( final byte[] array ) throws IOException {
    byte[] retval = null;
    final Deflater deflater = new Deflater( Deflater.DEFLATED, false );
    deflater.setInput( array );
    deflater.finish();

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      while ( !deflater.finished() ) {
        final byte[] data = new byte[ZIP_BUFFER_SIZE];
        final int count = deflater.deflate( data );

        if ( count == data.length ) {
          baos.write( data );
        } else {
          baos.write( data, 0, count );
        }
      }

      deflater.end();

      retval = baos.toByteArray();
    }
    finally {
      baos.close();
    }

    return retval;
  }




  /**
   * Take the contents of one file and deflate it into another.
   * 
   * <p>If the source file is a directory, all its contents will be included 
   * in a ZIP file format. If the source is a file, the target will NOT be 
   * standard zip, but just a deflated version of the original file with no 
   * Zip entries. It is designed to simply deflate a file to save space and 
   * not archive the file.
   *
   * <p>This method is a complement to the inflateFileToFile method.
   *
   * @param source
   * @param target
   *
   * @throws IOException
   */
  public static void deflateFileToFile( final File source, final File target ) throws IOException {
    final FileOutputStream out = new FileOutputStream( target );

    if ( source.isDirectory() ) {
      writeZipStream( out, source );
    } else {
      final Deflater deflater = new Deflater( Deflater.DEFLATED, false );

      final FileInputStream in = new FileInputStream( source );
      final DeflaterOutputStream stmDeflateOut = new DeflaterOutputStream( out, deflater );

      try {
        StreamUtil.inputStreamToOutputStream( in, stmDeflateOut );
      }
      finally {
        stmDeflateOut.finish();
        stmDeflateOut.flush();
        stmDeflateOut.close();
        out.close();
        in.close();
      }
    }
  }




  /**
   * Create a Zip file from the source file.
   * 
   * <p>The target file will be named after the source file with a ".zip" 
   * extension.
   * 
   * @param source file (or directory) from which to read
   * 
   * @throws IOException if problems occur
   */
  public static void zip( final File source ) throws IOException {
    final File target = new File( source.getAbsolutePath() + ZIP_SUFFIX );
    if ( source.isDirectory() ) {
      writeZipStream( new FileOutputStream( target ), source );
    } else {
      zipFile( source, target );
    }
  }




  /**
   * Create a Zip file from the source file and wite it to the target file.
   * 
   * @param source file (or directory) from which to read
   * @param target file to which the data is to be written
   * 
   * @throws IOException if problems occur
   */
  public static void zip( final File source, final File target ) throws IOException {
    if ( source.isDirectory() ) {
      writeZipStream( new FileOutputStream( target ), source );
    } else {
      zipFile( source, target );
    }
  }




  /**
   * Zip a single file into a compressed ZIP archive.
   *
   * <p>This will result in a compressed archive that contain a single file
   * entry. The name of the archive will be the name of the source file with
   * the added extension of 'zip'.
   *
   * @param source The file to archive.
   *
   * @throws IOException if problems were experienced.
   */
  public static void zipFile( final File source ) throws IOException {
    zipFile( source, new File( source.getAbsolutePath() + ZIP_SUFFIX ) );
  }




  /**
   * Zip a single file into a compressed ZIP archive.
   *
   * <p>This will result in a compressed archive that contain a single file
   * entry. The name of the archive will be the name of the source file with
   * the added extension of 'zip'.
   *
   * @param source The file to archive.
   * @param target the archive file to write
   *
   * @throws IOException if problems were experienced.
   */
  public static void zipFile( final File source, final File target ) throws IOException {
    BufferedInputStream origin = null;
    ZipOutputStream out = null;
    try {
      final FileOutputStream dest = new FileOutputStream( target );
      out = new ZipOutputStream( new BufferedOutputStream( dest ) );

      final byte data[] = new byte[STREAM_BUFFER_SIZE];

      final FileInputStream fi = new FileInputStream( source );
      origin = new BufferedInputStream( fi, STREAM_BUFFER_SIZE );

      final ZipEntry entry = new ZipEntry( source.getName() );
      out.putNextEntry( entry );

      int count;
      while ( ( count = origin.read( data, 0, STREAM_BUFFER_SIZE ) ) != -1 ) {
        out.write( data, 0, count );
      }
    } catch ( final FileNotFoundException e ) {
      throw new IOException( e.getMessage() );
    }
    finally {
      try {
        if ( origin != null ) {
          origin.close();
        }
      } catch ( final IOException ignore ) {}
      try {
        if ( out != null ) {
          out.close();
        }
      } catch ( final IOException ignore ) {}
    }
  }

}
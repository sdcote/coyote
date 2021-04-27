/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import coyote.commons.StreamUtil;
import coyote.commons.UriUtil;


/**
 * Higher-level interface to the native Java ZIP format handling classes.
 * 
 * <p>This allows easy addition and modification of archives without needing to
 * instantiate a local ZipFile. The trade-off is that the entire archive needs
 * to fit in memory, at least temporarily, so this is really suitable only for
 * moderate-sized archives.</p>
 * 
 * <p>Furthermore, it removes a lot of a flexibility of the lower-level 
 * interfaces in exchange for simplicity: reading an entry, dumping the whole 
 * ZIP into a directory and adding the contents of a directory recursively are 
 * all single operations and require no knowledge of the ZIP API.</p>
 * 
 * <p>This class can also be used for generating JAR files, though it will not
 * automatically create the Manifest for you. A previous generation of this 
 * code had a subclass JarArchive that had a hacked set of routines for 
 * building a manifest (pre-Java 2) that I'm leaving out for now until 
 * needed.</p>
 */
public class ZipArchive {
  private static IFileFinder fileFinder = new DefaultFileFinder();




  /**
   * Copies one file to another, byte-by-byte
   *
   * @param srcFile
   * @param destFile
   * 
   * @throws IOException
   */
  private static void copyFile(final File srcFile, final File destFile) throws IOException {
    FileInputStream fin = null;
    FileOutputStream fout = null;

    try {
      fin = new FileInputStream(srcFile);

      final BufferedInputStream bin = new BufferedInputStream(fin);
      fout = new FileOutputStream(destFile);

      final BufferedOutputStream bout = new BufferedOutputStream(fout);
      StreamUtil.copy(bin, bout);
    }
    finally {
      try {
        if (fin != null) {
          fin.close();
        }
      } catch (final IOException e) {}

      try {
        if (fout != null) {
          fout.close();
        }
      } catch (final IOException e) {}
    }
  }




  /**
   * Utility that ensures we have the right path separator
   *
   * @param zipPath
   * 
   * @return .
   */
  private static final String getLocalFileName(final String zipPath) {
    return zipPath.replace('/', File.separatorChar);
  }




  /**
   * Normalizes the name (stripping out the base directory) and changes the path
   * separator.
   *
   * @param baseDir
   * @param srcFile
   * 
   * @return .
   */
  private static final String getZipName(final File baseDir, final File srcFile) {
    String baseAbsPath = baseDir.getAbsolutePath();

    if (!baseAbsPath.endsWith(File.separator)) {
      baseAbsPath += File.separatorChar;
    }

    final String srcAbsPath = srcFile.getAbsolutePath();
    return srcAbsPath.substring(baseAbsPath.length()).replace(File.separatorChar, '/');
  }

  private URL archiveURL;
  private File archiveFile;

  private ZipFile zipFile;

  private Map<String, byte[]> addMap;

  private Map<String, Boolean> removeMap;




  /**
   * Opens an archive from a local file.
   *
   * @param archiveFile
   * @throws IOException
   */
  public ZipArchive(final File archiveFile) throws IOException {
    this.archiveFile = archiveFile;

    if (archiveFile.exists()) {
      zipFile = new ZipFile(archiveFile);
    }
  }




  /**
   * Opens an archive at the given location. 
   * 
   * <p>The cache is lazily-instantiated, so at this point no action is 
   * taken.</p>
   *
   * @param url
   * 
   * @throws IOException
   */
  public ZipArchive(final URL url) throws IOException {
    archiveFile = UriUtil.getFile(url);
    setArchiveURL(url);
    archiveFile = downloadZip(url);

    if (archiveFile.exists()) {
      zipFile = new ZipFile(archiveFile);
    }
  }




  /**
   * Adds a new entry to the in-memory copy of the archive.
   *
   * @param entryName
   * @param data
   * 
   * @throws IOException
   */
  public void addEntry(final String entryName, final byte[] data) throws IOException {
    if (addMap == null) {
      addMap = new HashMap<String, byte[]>();
    }

    addMap.put(entryName, data);

    // if we were slated to remove the entry earlier, we have to dequeue the
    // entry now, else it will not get flushed later
    if ((removeMap != null) && removeMap.containsKey(entryName)) {
      removeMap.remove(entryName);
    }
  }




  /**
   * rawer interface for adding a set of bytes and a stream directly
   *
   * @param zos
   * @param name
   * @param data
   * 
   * @throws IOException
   */
  private void addEntryToZip(final ZipOutputStream zos, final String name, final byte[] data) throws IOException {
    // get the CRC32 value
    final CRC32 crc = new CRC32();
    crc.update(data);

    // create a new ZipEntry and set its CRC
    final ZipEntry ze = new ZipEntry(name);
    ze.setSize(data.length);
    ze.setCrc(crc.getValue());
    // write the data
    zos.putNextEntry(ze);
    zos.write(data, 0, data.length);
  }




  /**
   * Adds all files in the given directory. 
   *
   * @param baseDir
   * 
   * @throws IOException
   * 
   * @see #addFiles(File, FilenameFilter)
   */
  public void addFiles(final File baseDir) throws IOException {
    addFiles(baseDir, new AllFilesFilenameFilter());
  }




  /**
   * Convenience method that adds files to an archive with no prefix directory
   * path.
   *
   * @see #addFiles(File,String,FilenameFilter)
   * @param baseDir
   * @param filter
   * 
   * @throws IOException
   */
  public void addFiles(final File baseDir, final FilenameFilter filter) throws IOException {
    addFiles(baseDir, null, filter);
  }




  /**
   * Recursively adds all of the contents of the given root directory to this
   * archive.
   * 
   * <p>This will be written back to disk at the time of the next 
   * {@link #flush()} call. If non-null, the given archive base path is 
   * pre-pended to all resulting archive file names.</p>
   *
   * @param baseDir
   * @param archiveBasePath
   * @param filter
   * 
   * @throws IOException
   */
  public void addFiles(final File baseDir, final String archiveBasePath, final FilenameFilter filter) throws IOException {
    final List<String> fileList = new ArrayList<String>();

    fileFinder.accumulateFiles(baseDir, fileList, filter);

    for (int ii = 0; ii < fileList.size(); ii++) {
      final File addFile = new File(fileList.get(ii).toString());
      final FileInputStream fis = new FileInputStream(addFile);

      if (archiveBasePath != null) {
        addEntry(archiveBasePath + "/" + getZipName(baseDir, addFile), StreamUtil.loadBytes(fis));
      } else {
        addEntry(getZipName(baseDir, addFile), StreamUtil.loadBytes(fis));
      }
    }
  }




  /**
   * Helper method that creates a temporary local file that we can read using
   * random access IO for faster ZIP access to remote resources
   *
   * @param archiveURL
   * 
   * @return .
   * 
   * @throws IOException
   */
  private File downloadZip(final URL archiveURL) throws IOException {
    // we're going to create a temporary file for this session so we can
    // download the ZIP and access it as a random access file (ZipFile)
    final File tmp = File.createTempFile("cmdjar", null);
    tmp.deleteOnExit();

    final FileOutputStream out = new FileOutputStream(tmp);
    final InputStream in = archiveURL.openStream();
    out.write(StreamUtil.loadBytes(in));
    out.flush();

    return tmp;
  }




  /**
   * Gets the names of all entries in the zip file.
   * 
   * <p>The names returned are in zip internal format--with "/" as path 
   * separator character, not the local filesystem separator.</p>
   *
   * @return . 
   * 
   * @throws IOException
   */
  public Iterator<String> entryNames() throws IOException {
    flush();

    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
    return new Iterator() {
      @Override
      public boolean hasNext() {
        return entries.hasMoreElements();
      }




      @Override
      public String next() {
        final ZipEntry ze = entries.nextElement();
        return ze.getName();
      }




      @Override
      public void remove() {}
    };
  }




  /**
   * Unpacks this archive to the given root directory.
   *
   * @param baseDir
   * @throws IOException
   */
  public void extractTo(final File baseDir) throws IOException {
    extractTo(baseDir, new AllZipEntryFilter());
  }




  /**
   * Unpacks matching entries in this archive to the given root directory.
   *
   * @param baseDir
   * @param filter
   * 
   * @throws IOException
   */
  public void extractTo(final File baseDir, final IZipEntryFilter filter) throws IOException {
    flush();

    final Enumeration<? extends ZipEntry> entries = zipFile.entries();

    while (entries.hasMoreElements()) {
      final ZipEntry ze = entries.nextElement();

      if (filter.accept(ze.getName())) {
        writeEntryTo(baseDir, ze);
      }
    }
  }




  /**
   * Unpacks the named entry into a file located below the given base directory.
   *
   * @param baseDir
   * @param entryName
   * 
   * @throws IOException
   */
  public void extractTo(final File baseDir, final String entryName) throws IOException {
    flush();
    writeEntryTo(baseDir, zipFile.getEntry(entryName));
  }




  /**
   * Writes the archive back to its source (if changed).
   *
   * @throws IOException
   */
  public void flush() throws IOException {
    if ((addMap != null) || (removeMap != null)) {
      // so we're not doing these checks again and again inside tight loops
      if (removeMap == null) {
        removeMap = new HashMap<String, Boolean>();
      }

      if (addMap == null) {
        addMap = new HashMap<String, byte[]>();
      }

      // to flush, we need to set up a temporary file to write the new zip
      // file, copy the existing entries in (minus those on our removeMap or
      // addMap) and then add those stored just in the addMap, and finally
      // overwrite the old ZipFile with our temporary one
      final File tmpFile = File.createTempFile("ZipArchive", null);
      final FileOutputStream fos = new FileOutputStream(tmpFile);
      final BufferedOutputStream bos = new BufferedOutputStream(fos);
      final ZipOutputStream zos = new ZipOutputStream(bos);

      // handle writing the old entries that haven't changed; we don't have to
      // do this for a brand new ZIP
      if (zipFile != null) {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
          final ZipEntry ze = entries.nextElement();

          if (!removeMap.containsKey(ze.getName()) && !addMap.containsKey(ze.getName())) {
            final byte[] data = StreamUtil.loadBytes(zipFile.getInputStream(ze));
            zos.putNextEntry(ze);
            zos.write(data, 0, data.length);
          }
        }
      }

      // handle writing the new entries that are just in memory
      final Iterator<String> addEntries = addMap.keySet().iterator();

      while (addEntries.hasNext()) {
        final String entryName = addEntries.next();
        addEntryToZip(zos, entryName, addMap.get(entryName));
      }

      zos.finish();
      zos.close();
      bos.close();
      fos.close();

      // now we can overwrite the old ZipFile and re-open it with the new data
      // we just added
      if (zipFile != null) {
        zipFile.close();
      }

      if (archiveFile.exists()) {
        if (archiveFile.canWrite()) {
          if (!archiveFile.delete()) {
            //throw new IOException( "unable to delete old ZIP file '" + archiveFile + "'" );
          }
        } else {
          System.out.println("Archive NOT Writable!");
        }
      }

      // if rename fails, we have to try copying the file first before we give
      // up, because renameTo() will not work across different file systems
      if (!tmpFile.renameTo(archiveFile)) {
        try {
          copyFile(tmpFile, archiveFile);
        } catch (final IOException e) {
          throw new IOException("unable to rename temporary ZIP file '" + tmpFile + "' to '" + archiveFile + "'");
        }
      }

      zipFile = new ZipFile(archiveFile);

      // destroy the old add/remove maps to free up memory
      addMap = null;
      removeMap = null;
    }
  }




  /**
   * @return the archiveURL
   */
  public URL getArchiveURL() {
    return archiveURL;
  }




  /**
   * Reads and uncompresses the data contained in the named entry.
   *
   * @param entryName
   * 
   * @return .
   * 
   * @throws IOException
   */
  public byte[] getEntry(final String entryName) throws IOException {
    // first check to see if there's a cache of not-yet-flushed added entries
    // and if the entry's there
    if ((addMap != null) && (addMap.containsKey(entryName))) {
      return addMap.get(entryName);
    } else {
      // ...otherwise go read directly from the local ZipFile
      if ((removeMap != null) && removeMap.containsKey(entryName)) {
        throw new IOException("zip entry not found: " + entryName);
      }

      // if we get here, we have to consult the ZipFile...which may not exists
      // if this is a newly-created file. But there's also a chance we were
      // handed a bum archiveFile, so we should check here--maybe need a
      // OPEN_READ_WRITE, OPEN_CREATE flag set
      if (zipFile == null) {
        throw new IOException("File not found: " + archiveFile);
      }

      final ZipEntry ze = zipFile.getEntry(entryName);

      if (ze == null) {
        throw new IOException("zip entry not found: " + entryName);
      }

      final InputStream in = zipFile.getInputStream(ze);

      try {
        return StreamUtil.loadBytes(in);
      }
      finally {
        if (in != null) {
          try {
            in.close();
          } catch (final IOException e) {}
        }
      }
    }
  }




  /**
   * Removes an entry from the in-memory copy of the archive. Returns a copy of
   * the removed entry.
   *
   * @param entryName
   * 
   * @return . 
   *  
   * @throws IOException
   */
  public byte[] removeEntry(final String entryName) throws IOException {
    final byte[] entryData = getEntry(entryName);

    // if we're removing an entry that was just in cache and never got flushed,
    // we can safely just remove it from memory; otherwise we add it to our
    // "to-remove" list for consultatation on the next flush
    if ((addMap != null) && (addMap.containsKey(entryName))) {
      addMap.remove(entryName);
    } else {
      removeMap.put(entryName, Boolean.TRUE);
    }

    return entryData;
  }




  /**
   * @param archiveURL the archiveURL to set
   */
  public void setArchiveURL(final URL archiveURL) {
    this.archiveURL = archiveURL;
  }




  /**
   * Internal helper method that handles the details of file extraction and
   * creation relative to a base directory.
   *
   * @param baseDir
   * @param ze
   * 
   * @throws IOException
   */
  private void writeEntryTo(final File baseDir, final ZipEntry ze) throws IOException {
    final File out = new File(baseDir, getLocalFileName(ze.getName()));
    out.getParentFile().mkdirs();

    if (!ze.isDirectory()) {
      final FileOutputStream fos = new FileOutputStream(out);
      final BufferedOutputStream bos = new BufferedOutputStream(fos);
      StreamUtil.copy(zipFile.getInputStream(ze), fos);
      bos.close();
    }
  }
}
/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.net.URI;

import coyote.commons.Assert;
import coyote.commons.CipherUtil;
import coyote.commons.UriUtil;
import coyote.dx.ConfigTag;
import coyote.dx.ftp.RemoteSite;
import coyote.loader.Loader;
import coyote.loader.cfg.ConfigurationException;


public abstract class AbstractFileTransferTask extends AbstractFileTask {

  protected RemoteSite site = null;
  protected String remoteFile = null;
  protected String localFile = null;




  /**
   * Generate a site based on the source URI and any other site attributes in 
   * our configuration.
   * 
   * @param uri the URI representing the remote host to create
   * 
   * @return a remote site based on the given URI and the other applicable 
   *         configuration attributes 
   */
  public RemoteSite configureSite(URI uri) throws ConfigurationException {
    RemoteSite retval = null;

    // The source configuration must be a URI
    retval = new RemoteSite(uri);

    // Support the separate setting /override of other site attributes

    if (contains(ConfigTag.HOST)) {
      retval.setHost(getString(ConfigTag.HOST));
    }

    if (contains(ConfigTag.PORT)) {
      retval.setPort(getInteger(ConfigTag.PORT));
    }

    if (contains(ConfigTag.USERNAME)) {
      site.setUsername(getString(ConfigTag.USERNAME));
    }

    if (contains(ConfigTag.PASSWORD)) {
      retval.setPassword(getString(ConfigTag.PASSWORD));
    }

    if (contains(ConfigTag.PROTOCOL)) {
      retval.setProtocol(getString(ConfigTag.PROTOCOL));
    }

    if (contains(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      // a little more streamlined, uniform way to handle encrypted values
      retval.setUsername(CipherUtil.decryptString(getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)));
    }

    if (contains(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      retval.setPassword(CipherUtil.decryptString(getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)));
    }

    return retval;
  }




  /**
   * Calculate the absolute file name of the file represented by the given URL.
   * 
   * @param text the URI text to parse and calculate the full file path
   *  
   * @return an absolute path to the file represented by the given URI or null 
   *         if the URI does not represent a file.
   * 
   * @throws ConfigurationException it there are problems parsing the URI text 
   */
  public String getLocalFile(String text) throws ConfigurationException {
    String retval = null;

    try {
      Assert.notBlank(text, "Local file URI cannot be null or empty");

      // Try to parse the target as a URI, failures result in a null
      if (UriUtil.parse(text) == null) {
        // Windows systems often have a drive letter in fully qualified filenames
        if (text.charAt(1) == ':') {
          // convert it to a file URI
          File f = new File(text);
          retval = f.getAbsolutePath();
        } else {
          throw new ConfigurationException("Local file URI is not a valid URI '" + text + "'");
        }
      } else {
        retval = UriUtil.getFilePath(new URI(text));
      }
    } catch (Exception e) {
      throw new ConfigurationException("Could not determine local file for '" + text + "'", e);
    }

    return retval;
  }

}

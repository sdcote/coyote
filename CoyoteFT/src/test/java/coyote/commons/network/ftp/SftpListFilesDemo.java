/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.network.ftp;

import java.util.List;

import coyote.commons.SystemPropertyUtil;
import coyote.commons.network.FileTransferException;
import coyote.commons.network.RemoteFile;
import coyote.commons.network.RemoteSite;


/**
 * 
 */
public class SftpListFilesDemo {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // Load properties to set system properties telling SNAPI to use a
    // proxy and what authentication to use
    SystemPropertyUtil.load("snowstorm");

    String host = "server.domain.org";
    // int port = 22;
    String user = "jdoe";
    String pass = "secret";
    String protocol = RemoteSite.SFTP;
    String directory = "/home/jdoe";

    // Create a remote site object
    RemoteSite site = new RemoteSite();
    site.setHost(host);
    //site.setPort( port );
    site.setUsername(user);
    site.setPassword(pass);
    site.setProtocol(protocol);
    System.out.println(site.toFormattedString());

    // get a list of files on the remote site
    try {
      List<RemoteFile> entries = site.listFiles(directory);
      for (RemoteFile file : entries) {
        System.out.println(file.getName() + " - " + file.getModifiedTime());
      }

      // we are done with the connection, so close it.
      site.close();

    } catch (FileTransferException e) {
      e.printStackTrace();
    }
    finally {
      // close the site when we are through
      site.close();
    }
  }

}

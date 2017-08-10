/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import coyote.commons.SystemPropertyUtil;


/**
 * 
 */
public class FileTransferDemo {

  /**
   * @param args
   * @throws UnknownHostException 
   */
  public static void main(String[] args) throws UnknownHostException {

    // Load properties to set system properties telling SNAPI to use a
    // proxy and what authentication to use
    SystemPropertyUtil.load("snowstorm");

    String host = "coast.cs.purdue.edu";
    int port = 21;
    String user = "anonymous";
    String pass = "adent@" + InetAddress.getLocalHost().getHostName();;
    String protocol = RemoteSite.FTP;
    String directory = "/pub/doc";

    // Create a remote site object
    RemoteSite site = new RemoteSite();
    site.setHost(host);
    site.setPort(port);
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

    } catch (FileTransferException e) {
      e.printStackTrace();
    }
    finally {
      // close the site when we are through
      site.close();
    }
  }
}

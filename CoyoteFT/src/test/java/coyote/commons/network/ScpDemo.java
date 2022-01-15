/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.network;

import coyote.commons.network.RemoteSite;

/**
 *
 */
public class ScpDemo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String host = "ecdt1.aepsc.com";
        int port = 22;
        String user = "ansible";
        String pass = "An5iblebackwardsnospaces";
        String protocol = RemoteSite.SCP;

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
            site.retrieveFile("/var/log/httpd/access_log.aepdeveloper", "access.log");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close the site when we are through
            site.close();
        }
    }

}

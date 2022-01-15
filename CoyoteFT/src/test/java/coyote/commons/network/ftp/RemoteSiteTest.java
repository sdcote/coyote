package coyote.commons.network.ftp;

//import static org.junit.Assert.*;

import coyote.commons.network.RemoteSite;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


public class RemoteSiteTest {

    @Test
    public void testRemoteSite() {
        RemoteSite site = new RemoteSite();
        assertNotNull(site);

        URI uri;
        try {
            uri = new URI("sftp://username:password@host:23/path/to/file.txt");
            site = new RemoteSite(uri);
            assertEquals("username", site.getUsername());
            assertEquals("password", site.getPassword());
            assertEquals("host", site.getHost());
            assertEquals("sftp", site.getProtocol());
            assertTrue(site.getPort() == 23);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            uri = new URI("sftp://username:password@host/path/to/file.txt");
            site = new RemoteSite(uri);
            assertEquals("username", site.getUsername());
            assertEquals("password", site.getPassword());
            assertEquals("host", site.getHost());
            assertEquals("sftp", site.getProtocol());
            assertTrue(site.getPort() == 22);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            uri = new URI("ftp://username:password@host/path/to/file.txt");
            site = new RemoteSite(uri);
            assertEquals("username", site.getUsername());
            assertEquals("password", site.getPassword());
            assertEquals("host", site.getHost());
            assertEquals("ftp", site.getProtocol());
            assertTrue(site.getPort() == 21);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            uri = new URI("sftp://username:password@host:33");
            site = new RemoteSite(uri);
            assertEquals("username", site.getUsername());
            assertEquals("password", site.getPassword());
            assertEquals("host", site.getHost());
            assertEquals("sftp", site.getProtocol());
            assertTrue(site.getPort() == 33);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRemoteSCPSite() {
        RemoteSite site = new RemoteSite();
        assertNotNull(site);

        URI uri;
        try {
            uri = new URI("scp://username:password@host/path/to/file.txt");
            site = new RemoteSite(uri);
            assertEquals("username", site.getUsername());
            assertEquals("password", site.getPassword());
            assertEquals("host", site.getHost());
            assertEquals("scp", site.getProtocol());
            assertTrue(site.getPort() == 22);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}

package coyote.commons.network;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

/**
 * A simple facade for interacting with a remote host via SSH.
 */
public class SSHUtil {

    /**
     * Retrieve a file from the remote site.
     *
     * @param site           The remote site from which to retrieve the file.
     * @param remoteFilename name of the remote file to retrieve
     * @param localFilename  destination on the local file system
     * @return true if the command returns successfully, false if there was an error
     */
    public static boolean retrieveFile(RemoteSite site, String remoteFilename, String localFilename) {
        boolean retval = false;
        SSHSession session = new SSHSession(site.getUsername(), site.getHost(), site.getPort(), site.getPassword(), 30000);
        session.open();
        try {
            session.copyRemoteToLocal(remoteFilename, localFilename);
            retval = true;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return retval;
    }


    /**
     * Send a file to the remote system
     *
     * @param site           The remote site
     * @param localFileName  name of the local file to publish
     * @param remoteFileName name of the destination file
     * @return true if the command returns successfully, false if there was an error
     */
    public static boolean publishFile(RemoteSite site, String localFileName, String remoteFileName) {
        boolean retval = false;
        SSHSession session = new SSHSession(site.getUsername(), site.getHost(), site.getPort(), site.getPassword(), 30000);
        session.open();
        try {
            session.copyLocalToRemote(localFileName, remoteFileName);
            retval = true;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return retval;
    }

    /**
     * An example use case using keyfiles.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        try {
            String remoteA = "/tmp/scp/remote-a/";
            String remoteB = "/tmp/scp/remote-b/";
            String local = "/tmp/scp/local/";
            String file = "abc.txt";

            String userName = "user";
            String host = "10.28.117.142";
            String keyFilePath = "/home/javadev/.ssh/id_rsa";
            String keyPassword = null;
            int timeOut = 60000;

            SSHSession session = new SSHSession(userName, host, 22, keyFilePath, keyPassword, timeOut);
            session.open();
            session.copyRemoteToLocal(remoteA + file, local);
            String command = "rm /tmp/scp/remote-a/abc.txt";
            session.sendCommand(command);
            session.copyLocalToRemote(local + file, remoteB);

            session.close();
            System.out.println("Done !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Simple no-op signature to make this utility class match others.
     *
     * @param site the remote site to close.
     */
    public static void close(RemoteSite site) {
    }

}
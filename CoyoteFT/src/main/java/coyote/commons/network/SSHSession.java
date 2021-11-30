package coyote.commons.network;

import com.jcraft.jsch.*;

import java.io.*;

/**
 * Represents an SSH connection to a remote system.
 */
public class SSHSession {
    private final JSch jschSSHChannel;
    private final String userName;
    private final String password;
    private final String host;
    private final int port;
    private final int timeOut;
    private Session jschSession = null;

    /**
     * @param userName
     * @param host
     * @param port
     * @param keyFilePath
     * @param keyPassword
     * @param timeOut
     */
    public SSHSession(String userName, String host, int port, String keyFilePath, String keyPassword, int timeOut) {

        this.userName = userName;
        this.password = null;
        this.host = host;
        this.port = port;
        this.timeOut = timeOut;

        jschSSHChannel = new JSch();

        try {
            if (keyFilePath != null) {
                if (keyPassword != null) {
                    jschSSHChannel.addIdentity(keyFilePath, keyPassword);
                } else {
                    jschSSHChannel.addIdentity(keyFilePath);
                }
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }


    public SSHSession(String userName, String host, int port, String password, int timeOut) {
    this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.timeOut = timeOut;
        jschSSHChannel = new JSch();
    }


    /**
     * Open the connection
     */
    public void open() {
        try {
            jschSession = jschSSHChannel.getSession(userName, host, port);
            if (password != null) jschSession.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);
            jschSession.connect(timeOut);
        } catch (JSchException jschX) {
            jschX.printStackTrace();
        }
    }


    /**
     * Check for acknowledgement.
     *
     * @param in The inputstream to read
     * @return 0 for success, 1 for error, or 2 for fatal error
     * @throws IOException if the ACK character could not be read from the inputstream
     */
    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb);
            }
            if (b == 2) { // fatal error
                System.out.print(sb);
            }
        }
        return b;
    }


    /**
     * Copy a file from the remote to the local file system.
     *
     * @param remoteFileName name of the remote file
     * @param localFileName  name of the local file
     * @throws JSchException
     * @throws IOException
     */
    public void copyRemoteToLocal(String remoteFileName, String localFileName) throws JSchException, IOException {
        if (jschSession == null) open();

        String prefix = null;
        if (new File(localFileName).isDirectory()) {
            prefix = localFileName + File.separator;
        }

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + remoteFileName;
        Channel channel = jschSession.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            System.out.println("file-size=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            FileOutputStream fos = new FileOutputStream(prefix == null ? localFileName : prefix + file);
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            try {
                if (fos != null) fos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        channel.disconnect();
    }


    /**
     * Copy a file from the local file system to the remote file system.
     *
     * @param localFileName
     * @param remoteFileName
     * @throws JSchException
     * @throws IOException
     */
    public void copyLocalToRemote(String localFileName, String remoteFileName) throws JSchException, IOException {
        if (jschSession == null) open();

        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFileName;
        Channel channel = jschSession.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            System.exit(0);
        }

        File _lfile = new File(localFileName);

        if (ptimestamp) {
            command = "T" + (_lfile.lastModified() / 1000) + " 0";
            command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                System.exit(0);
            }
        }

        // send "C0644 filesize filename", where filename should not include '/'
        long filesize = _lfile.length();
        command = "C0644 " + filesize + " ";
        if (localFileName.lastIndexOf('/') > 0) {
            command += localFileName.substring(localFileName.lastIndexOf('/') + 1);
        } else {
            command += localFileName;
        }

        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            System.exit(0);
        }

        // send a content of lfile
        FileInputStream fis = new FileInputStream(localFileName);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) break;
            out.write(buf, 0, len); //out.flush();
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        if (checkAck(in) != 0) {
            System.exit(0);
        }
        out.close();

        try {
            if (fis != null) fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        channel.disconnect();
    }


    /**
     * Send the command to the remote system.
     *
     * @param command the command to execute on the remote system
     * @return return the output from the command's execution.
     */
    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = jschSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        } catch (IOException ioX) {
            ioX.printStackTrace();
            return null;
        } catch (JSchException jschX) {
            jschX.printStackTrace();
            return null;
        }

        return outputBuffer.toString();
    }


    /**
     * Close the connection to the remote system.
     */
    public void close() {
        jschSession.disconnect();
        jschSession = null;
    }

}

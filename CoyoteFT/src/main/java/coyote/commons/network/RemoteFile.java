package coyote.commons.network;

import java.util.Date;


public class RemoteFile implements Comparable<RemoteFile> {

    public static final char separatorChar = '/';

    private String filename;
    private FileAttributes attrs;


    /**
     * @param filename this is the absolute filename
     * @param attrs
     */
    public RemoteFile(final String filename, final FileAttributes attrs) {
        setName(filename);
        setAttrs(attrs);
    }


    /**
     * Returns the last name in the filename path.
     *
     * @return The name of the file or directory denoted by this abstract
     * pathname, or the empty string if this pathname's name sequence
     * is empty
     */
    public String getName() {
        if (filename != null) {
            int index = filename.lastIndexOf(separatorChar);

            if (index >= 0) {
                return filename.substring(index + 1);
            } else {
                if (filename == null) {
                    return "";
                } else {
                    return filename;
                }
            }
        } else {
            return "";
        }
    }

    protected void setName(final String filename) {
        this.filename = filename;
    }

    /**
     * Returns the pathname string of this abstract pathname's parent, or
     * <code>null</code> if this pathname does not name a parent directory.
     *
     * @return The pathname string of the parent directory named by this
     * abstract pathname, or <code>null</code> if this pathname
     * does not name a parent
     */
    public String getParent() {
        int index = filename.lastIndexOf(separatorChar);

        if (index > 0) {
            return filename.substring(0, index);
        } else {
            return null;
        }

    }

    /**
     * @return the directory and file name
     */
    public String getAbsolutePath() {
        return filename;
    }

    public FileAttributes getAttrs() {
        return attrs;
    }


    protected void setAttrs(final FileAttributes attrs) {
        this.attrs = attrs;
    }


    public long getSize() {
        return attrs.getSize();
    }


    public int getAtime() {
        return attrs.atime;
    }


    public int getMtime() {
        return attrs.mtime;
    }


    public String getModifiedTimeString() {
        return attrs.getModifiedTimeString();
    }


    public Date getModifiedTime() {
        return attrs.getModifiedTime();
    }


    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(filename);
        if (attrs != null) {
            b.append(" ");
            b.append(attrs);
        }
        return b.toString();
    }


    public boolean isDirectory() {
        return attrs.isDirectory();
    }


    public boolean isLink() {
        return attrs.isLink();
    }


    public boolean isAbsolute() {
        return filename.charAt(0) == separatorChar;
    }


    public boolean isRelative() {
        return !isAbsolute();
    }


    @Override
    public int compareTo(final RemoteFile o) throws ClassCastException {
        return filename.compareTo(o.getName());
    }

}
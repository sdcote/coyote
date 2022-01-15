package coyote.commons.network;

import java.util.Date;


/**
 * A common model of file attributes for both FTP and SFTP.
 * 
 * <p>Each protocol is slightly different in the attributes supported, and each 
 * server tends to behave slightly differently so not all attributes will be 
 * supported except for name, size and modified time.</p>
 */
public class FileAttributes {

  protected static final int S_ISUID = 04000; // set user ID on execution
  protected static final int S_ISGID = 02000; // set group ID on execution
  protected static final int S_ISVTX = 01000; // sticky bit

  protected static final int S_IRUSR = 00400; // read by owner
  protected static final int S_IWUSR = 00200; // write by owner
  protected static final int S_IXUSR = 00100; // execute/search by owner
  protected static final int S_IREAD = 00400; // read by owner
  protected static final int S_IWRIT = 00200; // write by owner
  protected static final int S_IEXEC = 00100; // execute/search by owner

  protected static final int S_IRGRP = 00040; // read by group
  protected static final int S_IWGRP = 00020; // write by group
  protected static final int S_IXGRP = 00010; // execute/search by group

  protected static final int S_IROTH = 00004; // read by others
  protected static final int S_IWOTH = 00002; // write by others
  protected static final int S_IXOTH = 00001; // execute/search by others

  private static final int pmask = 0xFFF;

  public static final int FILE_ATTR_SIZE = 0x00000001;
  public static final int FILE_ATTR_UIDGID = 0x00000002;
  public static final int FILE_ATTR_PERMISSIONS = 0x00000004;
  public static final int FILE_ATTR_ACMODTIME = 0x00000008;
  public static final int FILE_ATTR_EXTENDED = 0x80000000;

  protected static final int S_IFMT = 0xf000;
  protected static final int S_IFIFO = 0x1000;
  protected static final int S_IFCHR = 0x2000;
  protected static final int S_IFDIR = 0x4000;
  protected static final int S_IFBLK = 0x6000;
  protected static final int S_IFREG = 0x8000;
  protected static final int S_IFLNK = 0xa000;
  protected static final int S_IFSOCK = 0xc000;

  private int flags = 0;
  private long size;
  private int uid;
  private int gid;
  private String user = null;
  private String group = null;
  private int permissions;
  protected int atime;
  protected int mtime;
  private String[] extended = null;




  /**
   * Factory Method to create a FileAttributes object from a byte buffer.
   * 
   * @param buf
   * 
   * @return the file attributes represented by the data in the buffer
   */
  public static FileAttributes getAttributes(final Buffer buf) {
    final FileAttributes attr = new FileAttributes();
    attr.flags = buf.getInt();
    if ((attr.flags & FILE_ATTR_SIZE) != 0) {
      attr.size = buf.getLong();
    }
    if ((attr.flags & FILE_ATTR_UIDGID) != 0) {
      attr.uid = buf.getInt();
      attr.gid = buf.getInt();
    }
    if ((attr.flags & FILE_ATTR_PERMISSIONS) != 0) {
      attr.permissions = buf.getInt();
    }
    if ((attr.flags & FILE_ATTR_ACMODTIME) != 0) {
      attr.atime = buf.getInt();
    }
    if ((attr.flags & FILE_ATTR_ACMODTIME) != 0) {
      attr.mtime = buf.getInt();
    }
    if ((attr.flags & FILE_ATTR_EXTENDED) != 0) {
      final int count = buf.getInt();
      if (count > 0) {
        attr.extended = new String[count * 2];
        for (int i = 0; i < count; i++) {
          attr.extended[i * 2] = ByteUtils.byte2str(buf.getString());
          attr.extended[(i * 2) + 1] = ByteUtils.byte2str(buf.getString());
        }
      }
    }
    return attr;
  }




  /**
   * 
   */
  public FileAttributes() {}




  /**
   * Dump the data in this attributes object to a buffer suitable for generating a deep copy.
   * @param buf
   */
  public void dump(final Buffer buf) {
    buf.putInt(flags);
    if ((flags & FILE_ATTR_SIZE) != 0) {
      buf.putLong(size);
    }
    if ((flags & FILE_ATTR_UIDGID) != 0) {
      buf.putInt(uid);
      buf.putInt(gid);
    }
    if ((flags & FILE_ATTR_PERMISSIONS) != 0) {
      buf.putInt(permissions);
    }
    if ((flags & FILE_ATTR_ACMODTIME) != 0) {
      buf.putInt(atime);
    }
    if ((flags & FILE_ATTR_ACMODTIME) != 0) {
      buf.putInt(mtime);
    }
    if ((flags & FILE_ATTR_EXTENDED) != 0) {
      final int count = extended.length / 2;
      if (count > 0) {
        for (int i = 0; i < count; i++) {
          buf.putString(ByteUtils.str2byte(extended[i * 2]));
          buf.putString(ByteUtils.str2byte(extended[(i * 2) + 1]));
        }
      }
    }
  }




  public Date getAccessedTime() {
    return new Date(atime * 1000L);
  }




  public String getAccessedTimeString() {
    return (getAccessedTime().toString());
  }




  public int getAccessTime() {
    return atime;
  }




  public String[] getExtended() {
    return extended;
  }




  public int getFlags() {
    return flags;
  }




  public int getGID() {
    return gid;
  }




  /**
   * @return the group
   */
  public String getGroup() {
    return group;
  }




  public Date getModifiedTime() {
    return new Date(mtime * 1000L);
  }




  public String getModifiedTimeString() {
    return (getModifiedTime().toString());
  }




  public int getMtime() {
    return mtime;
  }




  public int getPermissions() {
    return permissions;
  }




  public String getPermissionsString() {
    final StringBuffer buf = new StringBuffer(10);

    if (isDirectory()) {
      buf.append('d');
    } else if (isLink()) {
      buf.append('l');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IRUSR) != 0) {
      buf.append('r');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IWUSR) != 0) {
      buf.append('w');
    } else {
      buf.append('-');
    }

    if ((permissions & S_ISUID) != 0) {
      buf.append('s');
    } else if ((permissions & S_IXUSR) != 0) {
      buf.append('x');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IRGRP) != 0) {
      buf.append('r');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IWGRP) != 0) {
      buf.append('w');
    } else {
      buf.append('-');
    }

    if ((permissions & S_ISGID) != 0) {
      buf.append('s');
    } else if ((permissions & S_IXGRP) != 0) {
      buf.append('x');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IROTH) != 0) {
      buf.append('r');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IWOTH) != 0) {
      buf.append('w');
    } else {
      buf.append('-');
    }

    if ((permissions & S_IXOTH) != 0) {
      buf.append('x');
    } else {
      buf.append('-');
    }
    return (buf.toString());
  }




  public long getSize() {
    return size;
  }




  public int getUID() {
    return uid;
  }




  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }




  /**
   * @return true if the file handle is a Block special file
   */
  public boolean isBlock() {
    return isType(S_IFBLK);
  }




  /**
   * @return true if the file handle is a Character special file
   */
  public boolean isCharacter() {
    return isType(S_IFCHR);
  }




  /**
   * @return true if the file handle is a directory
   */
  public boolean isDirectory() {
    return isType(S_IFDIR);
  }




  /**
   * @return true if the file handle is a FIFO
   */
  public boolean isFifo() {
    return isType(S_IFIFO);
  }




  /**
   * @return true if the file handle is a symbolic link
   */
  public boolean isLink() {
    return isType(S_IFLNK);
  }




  /**
   * @return true if the file handle is a regular file
   */
  public boolean isRegular() {
    return isType(S_IFREG);
  }




  /**
   * @return true if the file handle is a socket
   */
  public boolean isSock() {
    return isType(S_IFSOCK);
  }




  private boolean isType(final int mask) {
    return ((flags & FILE_ATTR_PERMISSIONS) != 0) && ((permissions & S_IFMT) == mask);
  }




  public int length() {
    int len = 4;

    if ((flags & FILE_ATTR_SIZE) != 0) {
      len += 8;
    }
    if ((flags & FILE_ATTR_UIDGID) != 0) {
      len += 8;
    }
    if ((flags & FILE_ATTR_PERMISSIONS) != 0) {
      len += 4;
    }
    if ((flags & FILE_ATTR_ACMODTIME) != 0) {
      len += 8;
    }
    if ((flags & FILE_ATTR_EXTENDED) != 0) {
      len += 4;
      final int count = extended.length / 2;
      if (count > 0) {
        for (int i = 0; i < count; i++) {
          len += 4;
          len += extended[i * 2].length();
          len += 4;
          len += extended[(i * 2) + 1].length();
        }
      }
    }
    return len;
  }




  public void setAccessTime(final int atime) {
    flags |= FILE_ATTR_ACMODTIME;
    this.atime = atime;
  }




  public void setFLAGS(final int flags) {
    this.flags = flags;
  }




  public void setGID(final int gid) {
    flags |= FILE_ATTR_UIDGID;
    this.gid = gid;
  }




  public void setGroup(final String value) {
    group = value;
  }




  public void setModifiedTime(final int mtime) {
    flags |= FILE_ATTR_ACMODTIME;
    this.mtime = mtime;
  }




  public void setPERMISSIONS(int permissions) {
    flags |= FILE_ATTR_PERMISSIONS;
    permissions = (this.permissions & ~pmask) | (permissions & pmask);
    this.permissions = permissions;
  }




  public void setSize(final long size) {
    flags |= FILE_ATTR_SIZE;
    this.size = size;
  }




  public void setUID(final int uid) {
    flags |= FILE_ATTR_UIDGID;
    this.uid = uid;
  }




  public void setUser(final String value) {
    user = value;
  }




  @Override
  public String toString() {
    return (getPermissionsString() + " " + getUID() + " " + getGID() + " " + getSize() + " " + getModifiedTimeString());
  }

}

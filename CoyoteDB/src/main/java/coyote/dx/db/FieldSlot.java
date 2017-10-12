/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

/**
 * 
 */
public class FieldSlot {
  private String sysid = null;
  private String parent = null;
  private boolean active = true;
  private String name = null;
  private short type = 0;
  private String value = null;
  private int sequence = 0;




  /**
   * @param sysid
   * @param parent
   * @param sequence
   * @param active
   * @param name
   * @param type
   * @param value
   */
  public FieldSlot(String sysid, String parent, int sequence, boolean active, String name, short type, String value) {
    this.sysid = sysid;
    this.parent = parent;
    this.active = active;
    this.name = name;
    this.type = type;
    this.value = value;
    this.sequence = sequence;
  }




  /**
   * @return the name of the field
   */
  public String getName() {
    return name;
  }




  /**
   * @param name the name of the field to set
   */
  public void setName(String name) {
    this.name = name;
  }




  /**
   * @return the type of the field
   */
  public short getType() {
    return type;
  }




  /**
   * @param type the type of the field to set
   */
  public void setType(short type) {
    this.type = type;
  }




  /**
   * @return the value of the field
   */
  public String getValue() {
    return value;
  }




  /**
   * @param value the value of the field to set
   */
  public void setValue(String value) {
    this.value = value;
  }




  /**
   * @return the sequence of the field in the frame
   */
  public int getSequence() {
    return sequence;
  }




  /**
   * @param sequence the sequence of the field i the frame to set
   */
  public void setSequence(int sequence) {
    this.sequence = sequence;
  }




  /**
   * @return the system identifier of the field
   */
  public String getSysId() {
    return sysid;
  }




  /**
   * @param sysid the system identifier of the field to set
   */
  public void setSysId(String sysid) {
    this.sysid = sysid;
  }




  /**
   * @return the parent
   */
  public String getParent() {
    return parent;
  }




  /**
   * @param parent the parent to set
   */
  public void setParent(String parent) {
    this.parent = parent;
  }




  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }




  /**
   * @param active the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("Field:");
    b.append(this.getSysId());
    b.append(" Parent:");
    b.append(this.getParent());
    b.append(" Seq:");
    b.append(this.getSequence());
    b.append(" Active:");
    b.append(this.isActive());
    b.append(" Name:");
    b.append(this.getName());
    b.append(" Type:");
    b.append(this.getType());
    b.append(" Value:");
    b.append(this.getValue());
    return b.toString();
  }

}

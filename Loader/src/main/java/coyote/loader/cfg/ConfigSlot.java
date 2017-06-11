/*
 * Copyright Stephan D. Cote' 2008 - All rights reserved.
 *
 * Copyright (C) 2003 Stephan D. Cote' - All rights reserved.
 */
package coyote.loader.cfg;

import coyote.dataframe.DataField;


/**
 * Class ConfigSlot a description of a configuration option.
 */
public class ConfigSlot {
  protected String name = null;
  protected String description = null;
  protected SlotType type = SlotType.STRING;
  protected Object defaultValue = null;
  protected String message = null;
  protected boolean required = false;




  /**
   * Constructor ConfigSlot
   */
  public ConfigSlot() {}




  /**
   * Constructor ConfigSlot
   *
   * @param slot
   */
  public ConfigSlot( final ConfigSlot slot ) {
    name = slot.name;
    description = slot.description;
    type = slot.type;
    defaultValue = slot.defaultValue;
  }




  /**
   * Constructor ConfigSlot
   *
   * @param name The name of the attribute slot
   * @param description A string of descriptive text for the use/meaning of this attribute
   * @param dflt the default object value of this attribute
   */
  public ConfigSlot( final String name, final String description, final Object dflt ) {
    if ( name != null ) {
      setName( name );
      setDescription( description );
      try {
        new DataField( dflt );
        setDefaultValue( dflt );
      } catch ( final Exception e ) {
        throw new IllegalArgumentException( "Unsupported default value type: " + e.getMessage() );
      }
    } else {
      throw new IllegalArgumentException( "ConfigSlot name is null" );
    }
  }




  /**
   * @return the default value for this configuration attribute
   */
  public Object getDefaultValue() {
    return defaultValue;
  }




  /**
   * @return the description of this configuration attribute
   */
  public String getDescription() {
    return description;
  }




  /**
   * @return  the user-defined message for this slot.
   */
  public String getMessage() {
    return message;
  }




  /**
   * @return the name of this configuration attribute
   */
  public String getName() {
    return name;
  }




  /**
   * @return the type of this slots value
   */
  public SlotType getType() {
    return type;
  }




  /**
   * @return if this is a required configuration attribute
   */
  public boolean isRequired() {
    return required;
  }




  /**
   * @param value the default value for this configuration attribute
   */
  public void setDefaultValue( final Object value ) {
    defaultValue = value;
  }




  /**
   * @param desc the description of this configuration attribute
   */
  public void setDescription( final String desc ) {
    description = desc;
  }




  /**
   * Set a user-defined message for this slot.
   *
   * <p>Many times, the ConfigSlot is used to represent a mutable Attribute
   * instance, as in GUIs, where using an Attribute instance can be prohibitive
   * in its type checking. In such cases, it is useful to be able to pass an
   * ConfigSlot instead and then create an Attribute after all edits are
   * completed. In such cases, the ability to pass a user-defined message field
   * is useful as in the case where value failed some validity check and the
   * ConfigSlot is passed back to the GUI with the invalid value in the
   * defaultValue field and an error message in the Message field.
   *
   * @param message
   */
  public void setMessage( final String message ) {
    this.message = message;
  }




  /**
   * @param name the name of this configuration attribute
   */
  public void setName( final String name ) {
    if ( name != null ) {
      this.name = name;
    } else {
      throw new IllegalArgumentException( "ConfigSlot name is null" );
    }
  }




  /**
   * @param flag true indicates this is a required configuration attribute,
   *             false means this is optional
   */
  public void setRequired( final boolean flag ) {
    required = flag;
  }




  /**
   * @param type the data type of this slots value
   */
  public void setType( final SlotType type ) {
    this.type = type;
  }

}

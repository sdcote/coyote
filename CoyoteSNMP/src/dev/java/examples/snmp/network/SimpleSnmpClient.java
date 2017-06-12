/*
 * Copyright (c) 2004 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package examples.snmp.network;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.FontUIResource;

import coyote.commons.network.snmp.SnmpClient;
import coyote.commons.network.snmp.SnmpObject;
import coyote.commons.network.snmp.SnmpObjectIdentifier;
import coyote.commons.network.snmp.SnmpOctetString;
import coyote.commons.network.snmp.SnmpSequence;
import coyote.commons.network.snmp.SnmpVarBindList;


/**
 * Class SimpleSnmpClient.
 */
public class SimpleSnmpClient extends JFrame implements ActionListener, Runnable
{

  /** Field getDataButton, getTreewalkDataButton, getTableButton, getNextButton, setValueButton */
  JButton getDataButton, getTreewalkDataButton, getTableButton, getNextButton, setValueButton;

  /** Field clearButton */
  JButton clearButton;

  /** Field messagesArea */
  JTextArea messagesArea;

  /** Field messagesScroll */
  JScrollPane messagesScroll;

  /** Field hostIDField, communityField, OIDField, valueField */
  JTextField hostIDField, communityField, OIDField, valueField, hostPortField;

  /** Field authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel */
  JLabel authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel, hostPortLabel;

  /** Field valueTypeBox */
  JComboBox valueTypeBox;

  /** Field theMenubar */
  MenuBar theMenubar;

  /** Field fileMenu */
  Menu fileMenu;

  /** Field quitItem */
  MenuItem quitItem;

  /** Field treewalkThread */
  Thread treewalkThread;

  /** Field comInterface */
  SnmpClient comInterface;

  /** Field community */
  String community;

  /** Field hostAddress */
  InetAddress hostAddress;

  /** Field version */
  int version;

  // WindowCloseAdapter to catch window close-box closings

  /**
   * Class WindowCloseAdapter.
   *
   * @version $Revision:$
   */
  private class WindowCloseAdapter extends WindowAdapter
  {

    /**
     * Method windowClosing.
     *
     * @param e
     */
    public void windowClosing( WindowEvent e )
    {
      System.exit( 0 );
    }
  }
  ;

  /**
   * Constructor SimpleSnmpClient.
   */
  public SimpleSnmpClient()
  {
    treewalkThread = new Thread( this );

    setUpDisplay();

  }




  /**
   * Method setUpDisplay.
   */
  private void setUpDisplay()
  {

    this.setTitle( "SNMP Client" );

    this.getRootPane().setBorder( new BevelBorder( BevelBorder.RAISED ) );

    // set fonts to smaller-than-normal size, for compaction!
    UIManager manager = new UIManager();
    FontUIResource appFont = new FontUIResource( "SansSerif", Font.PLAIN, 10 );
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    Enumeration keys = defaults.keys();

    while( keys.hasMoreElements() )
    {
      String nextKey = (String)( keys.nextElement() );
      if( ( nextKey.indexOf( "font" ) > -1 ) || ( nextKey.indexOf( "Font" ) > -1 ) )
      {
        UIManager.put( nextKey, appFont );
      }
    }

    // add WindowCloseAdapter to catch window close-box closings
    addWindowListener( new WindowCloseAdapter() );

    theMenubar = new MenuBar();

    this.setMenuBar( theMenubar );

    fileMenu = new Menu( "File" );

    quitItem = new MenuItem( "Quit" );

    quitItem.setActionCommand( "quit" );
    quitItem.addActionListener( this );
    fileMenu.add( quitItem );

    theMenubar.add( fileMenu );

    hostIDLabel = new JLabel( "Device address:" );
    hostIDField = new JTextField( 20 );
    hostIDField.setText( "10.0.1.1" );
    hostIDField.setEditable( true );

    hostPortLabel = new JLabel( "Port:" );
    hostPortField = new JTextField( 4 );
    hostPortField.setText( "161" );
    hostPortField.setEditable( true );

    OIDLabel = new JLabel( "OID:" );
    OIDField = new JTextField( 20 );

    OIDField.setEditable( true );

    valueLabel = new JLabel( "Value (for Set):" );
    valueField = new JTextField( 20 );

    valueField.setEditable( true );

    communityLabel = new JLabel( "Community:" );
    communityField = new JTextField( 20 );

    communityField.setText( "public" );
    communityField.setEditable( true );

    authorLabel = new JLabel( " Bralyn Systems " );

    authorLabel.setFont( new Font( "SansSerif", Font.ITALIC, 8 ) );

    getDataButton = new JButton( "Get OID value" );

    getDataButton.setActionCommand( "get data" );
    getDataButton.addActionListener( this );

    setValueButton = new JButton( "Set OID value" );

    setValueButton.setActionCommand( "set value" );
    setValueButton.addActionListener( this );

    getTableButton = new JButton( "Get table" );

    getTableButton.setActionCommand( "get table" );
    getTableButton.addActionListener( this );

    getNextButton = new JButton( "Get next OID value" );

    getNextButton.setActionCommand( "get next" );
    getNextButton.addActionListener( this );

    getTreewalkDataButton = new JButton( "Get all OID values" );

    getTreewalkDataButton.setActionCommand( "get treewalk data" );
    getTreewalkDataButton.addActionListener( this );

    clearButton = new JButton( "Clear responses" );

    clearButton.setActionCommand( "clear messages" );
    clearButton.addActionListener( this );

    messagesArea = new JTextArea( 10, 60 );
    messagesScroll = new JScrollPane( messagesArea );

    valueTypeBox = new JComboBox();

    valueTypeBox.addItem( "SnmpInteger" );
    valueTypeBox.addItem( "SnmpCounter32" );
    valueTypeBox.addItem( "SnmpCounter64" );
    valueTypeBox.addItem( "SnmpGauge32" );
    valueTypeBox.addItem( "SnmpOctetString" );
    valueTypeBox.addItem( "SnmpIpAddress" );
    valueTypeBox.addItem( "SnmpNsapAddress" );
    valueTypeBox.addItem( "SnmpObjectIdentifier" );
    valueTypeBox.addItem( "SnmpTimeTicks" );
    valueTypeBox.addItem( "SnmpUInteger32" );

    // now set up display

    // set params for layout manager
    GridBagLayout theLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new Insets( 2, 2, 2, 2 );
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0;
    c.weighty = 0;

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;

    theLayout.setConstraints( getDataButton, c );
    buttonPanel.add( getDataButton );

    c.gridx = 2;
    c.gridy = 1;

    theLayout.setConstraints( getNextButton, c );
    buttonPanel.add( getNextButton );

    c.gridx = 3;
    c.gridy = 1;

    theLayout.setConstraints( getTableButton, c );
    buttonPanel.add( getTableButton );

    c.gridx = 4;
    c.gridy = 1;

    theLayout.setConstraints( getTreewalkDataButton, c );
    buttonPanel.add( getTreewalkDataButton );

    c.gridx = 5;
    c.gridy = 1;

    theLayout.setConstraints( setValueButton, c );
    buttonPanel.add( setValueButton );

    JPanel hostPanel = new JPanel();
    hostPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;
    theLayout.setConstraints( hostIDLabel, c );
    hostPanel.add( hostIDLabel );
    c.gridx = 2;
    c.gridy = 1;
    theLayout.setConstraints( hostIDField, c );
    hostPanel.add( hostIDField );

    c.gridx = 3;
    c.gridy = 1;
    theLayout.setConstraints( hostPortLabel, c );
    hostPanel.add( hostPortLabel );
    c.gridx = 4;
    c.gridy = 1;
    theLayout.setConstraints( hostPortField, c );
    hostPanel.add( hostPortField );


    c.gridx = 1;
    c.gridy = 2;

    theLayout.setConstraints( communityLabel, c );
    hostPanel.add( communityLabel );

    c.gridx = 2;
    c.gridy = 2;

    theLayout.setConstraints( communityField, c );
    hostPanel.add( communityField );

    JPanel oidPanel = new JPanel();
    oidPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;

    theLayout.setConstraints( OIDLabel, c );
    oidPanel.add( OIDLabel );

    c.gridx = 2;
    c.gridy = 1;

    theLayout.setConstraints( OIDField, c );
    oidPanel.add( OIDField );

    c.gridx = 1;
    c.gridy = 2;

    theLayout.setConstraints( valueLabel, c );
    oidPanel.add( valueLabel );

    c.gridx = 2;
    c.gridy = 2;

    theLayout.setConstraints( valueField, c );
    oidPanel.add( valueField );

    c.gridx = 3;
    c.gridy = 2;

    theLayout.setConstraints( valueTypeBox, c );
    oidPanel.add( valueTypeBox );

    c.gridwidth = 1;
    c.anchor = GridBagConstraints.CENTER;

    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.WEST;

    JLabel messagesLabel = new JLabel( "Responses:" );
    theLayout.setConstraints( messagesLabel, c );
    messagesPanel.add( messagesLabel );

    c.gridx = 2;
    c.gridy = 1;
    c.anchor = GridBagConstraints.EAST;

    theLayout.setConstraints( clearButton, c );
    messagesPanel.add( clearButton );

    c.fill = GridBagConstraints.BOTH;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 2;
    c.weightx = .5;
    c.weighty = .5;
    c.anchor = GridBagConstraints.CENTER;

    theLayout.setConstraints( messagesScroll, c );
    messagesPanel.add( messagesScroll );

    c.gridwidth = 1;
    c.weightx = 0;
    c.weighty = 0;

    this.getContentPane().setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;

    theLayout.setConstraints( hostPanel, c );
    this.getContentPane().add( hostPanel );

    c.gridx = 1;
    c.gridy = 2;

    theLayout.setConstraints( oidPanel, c );
    this.getContentPane().add( oidPanel );

    c.gridx = 1;
    c.gridy = 3;

    theLayout.setConstraints( buttonPanel, c );
    this.getContentPane().add( buttonPanel );

    c.fill = GridBagConstraints.BOTH;
    c.gridx = 1;
    c.gridy = 4;
    c.weightx = .5;
    c.weighty = .5;

    theLayout.setConstraints( messagesPanel, c );
    this.getContentPane().add( messagesPanel );

    c.fill = GridBagConstraints.NONE;
    c.gridx = 1;
    c.gridy = 5;
    c.weightx = 0;
    c.weighty = 0;

    theLayout.setConstraints( authorLabel, c );
    this.getContentPane().add( authorLabel );

  }




  /**
   * Method actionPerformed.
   *
   * @param theEvent
   */
  public void actionPerformed( ActionEvent theEvent )
  // respond to button pushes, menu selections
  {
    String command = theEvent.getActionCommand();

    if( command == "quit" )
    {
      System.exit( 0 );
    }

    if( command == "clear messages" )
    {
      messagesArea.setText( "" );
    }

    if( command == "get data" )
    {
      try
      {

        String community = communityField.getText();
        int version = 0;  // SNMPv1
        InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );
        int hostPort = Integer.parseInt( hostPortField.getText() );
        SnmpClient comInterface = new SnmpClient( version, hostAddress, hostPort, community );

        StringTokenizer st = new StringTokenizer( OIDField.getText(), " ,;" );

        while( st.hasMoreTokens() )
        {
          String itemID = st.nextToken();
          SnmpVarBindList newVars = comInterface.getMIBEntry( itemID );
          SnmpSequence pair = (SnmpSequence)( newVars.getSnmpObjectAt( 0 ) );
          SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSnmpObjectAt( 0 );
          SnmpObject snmpValue = pair.getSnmpObjectAt( 1 );
          String typeString = getLocalJavaName(snmpValue.getClass().getName());
System.out.println("Typestring: "+typeString);
          if( typeString.equals( "SnmpOctetString" ) )
          {
            String snmpString = snmpValue.toString();

            // truncate at first null character
            int nullLocation = snmpString.indexOf( '\0' );
            if( nullLocation >= 0 )
            {
              snmpString = snmpString.substring( 0, nullLocation );
            }

            messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString );
            messagesArea.append( "  (hex: " + ( (SnmpOctetString)snmpValue ).toHexString() + ")\n" );
          }
          else
          {
            messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue );
            messagesArea.append( "\n" );
          }
        }
      }
      catch( InterruptedIOException e )
      {
        messagesArea.append( "Interrupted during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }
      catch( Exception e )
      {
        messagesArea.append( "Exception during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }

    }

    if( command == "get next" )
    {
      try
      {

        String community = communityField.getText();
        int version = 0;  // SNMPv1
        InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );
        int hostPort = Integer.parseInt( hostPortField.getText() );
        SnmpClient comInterface = new SnmpClient( version, hostAddress, hostPort, community );

        StringTokenizer st = new StringTokenizer( OIDField.getText(), " ,;" );

        while( st.hasMoreTokens() )
        {
          String itemID = st.nextToken();
          SnmpVarBindList newVars = comInterface.getNextMIBEntry( itemID );
          SnmpSequence pair = (SnmpSequence)( newVars.getSnmpObjectAt( 0 ) );
          SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSnmpObjectAt( 0 );
          SnmpObject snmpValue = pair.getSnmpObjectAt( 1 );
          String typeString = getLocalJavaName(snmpValue.getClass().getName());
System.out.println("Typestring: "+typeString);

          if( typeString.equals( "SnmpOctetString" ) )
          {
            String snmpString = snmpValue.toString();

            // truncate at first null character
            int nullLocation = snmpString.indexOf( '\0' );
            if( nullLocation >= 0 )
            {
              snmpString = snmpString.substring( 0, nullLocation );
            }

            messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString );
            messagesArea.append( "  (hex: " + ( (SnmpOctetString)snmpValue ).toHexString() + ")\n" );
          }
          else
          {
            messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue );
            messagesArea.append( "\n" );
          }
        }
      }
      catch( InterruptedIOException e )
      {
        messagesArea.append( "Interrupted during retrieval:  " + e + "\n" );
        e.printStackTrace();
     }
      catch( Exception e )
      {
        messagesArea.append( "Exception during retrieval:  " + e + "\n" );
        e.printStackTrace();
     }

    }

    if( command == "get table" )
    {
      try
      {

        String community = communityField.getText();
        int version = 0;  // SNMPv1
        InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );
        int hostPort = Integer.parseInt( hostPortField.getText() );
        SnmpClient comInterface = new SnmpClient( version, hostAddress, hostPort, community );

        String itemID = OIDField.getText();

        SnmpVarBindList newVars = comInterface.retrieveMIBTable( itemID );

        // print the retrieved stuff
        for( int i = 0; i < newVars.size(); i++ )
        {
          SnmpSequence pair = (SnmpSequence)( newVars.getSnmpObjectAt( i ) );

          SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSnmpObjectAt( 0 );
          SnmpObject snmpValue = pair.getSnmpObjectAt( 1 );
          String typeString = getLocalJavaName(snmpValue.getClass().getName());
System.out.println("Typestring: "+typeString);

          if( typeString.equals( "SnmpOctetString" ) )
          {
            String snmpString = snmpValue.toString();

            // truncate at first null character
            int nullLocation = snmpString.indexOf( '\0' );
            if( nullLocation >= 0 )
            {
              snmpString = snmpString.substring( 0, nullLocation );
            }

            messagesArea.append( new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString );
            messagesArea.append( "  (hex: " + ( (SnmpOctetString)snmpValue ).toHexString() + ")\n" );
          }
          else
          {
            messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue );
            messagesArea.append( "\n" );
          }

        }
      }
      catch( InterruptedIOException e )
      {
        messagesArea.append( "Interrupted during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }
      catch( Exception e )
      {
        messagesArea.append( "Exception during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }

    }

    if( command == "set value" )
    {
      try
      {

        String community = communityField.getText();
        int version = 0;  // SNMPv1
        InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );
        int hostPort = Integer.parseInt( hostPortField.getText() );
        SnmpClient comInterface = new SnmpClient( version, hostAddress, hostPort, community );

        String itemID = OIDField.getText();
        String valueString = valueField.getText();
        String valueTypeString = (String)valueTypeBox.getSelectedItem();
        valueTypeString = "net.bralyn.network.snmp." + valueTypeString;

        SnmpObject itemValue;
        Class valueClass = Class.forName( valueTypeString );
        itemValue = (SnmpObject)valueClass.newInstance();

        itemValue.setValue( valueString );

        SnmpVarBindList newVars = comInterface.setMIBEntry( itemID, itemValue );
        SnmpSequence pair = (SnmpSequence)( newVars.getSnmpObjectAt( 0 ) );
        SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSnmpObjectAt( 0 );
        SnmpObject snmpValue = pair.getSnmpObjectAt( 1 );
        String typeString = getLocalJavaName(snmpValue.getClass().getName());
System.out.println("Typestring: "+typeString);

        messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue );

        if( typeString.equals( "SnmpOctetString" ) )
        {
          messagesArea.append( "  (hex: " + ( (SnmpOctetString)snmpValue ).toHexString() + ")\n" );
        }
        else
        {
          messagesArea.append( "\n" );
        }

      }
      catch( InterruptedIOException e )
      {
        messagesArea.append( "Interrupted during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }
      catch( Exception e )
      {
        messagesArea.append( "Exception during retrieval:  " + e + "\n" );
        e.printStackTrace();
      }

    }

    if( command == "get treewalk data" )
    {
      if( !treewalkThread.isAlive() )
      {
        treewalkThread = new Thread( this );

        treewalkThread.start();
        getTreewalkDataButton.setText( "Stop OID retrieval" );
      }
      else
      {
        treewalkThread.interrupt();
      }
    }

  }




  /**
   * Method run.
   */
  public void run()
  {

    try
    {

      String community = communityField.getText();
      int version = 0;  // SNMPv1
      InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );
      int hostPort = Integer.parseInt( hostPortField.getText() );
      SnmpClient comInterface = new SnmpClient( version, hostAddress, hostPort, community );

      // String itemID = "1.3.6.1.2.1.1.1.0";  // start with device name
      String itemID = "";
      String retrievedID = "1.3.6.1.2.1";  // start point

      while( !Thread.interrupted() && !retrievedID.equals( itemID ) )
      {
        itemID = retrievedID;

        SnmpVarBindList newVars = comInterface.getNextMIBEntry( itemID );

        SnmpSequence pair = (SnmpSequence)( newVars.getSnmpObjectAt( 0 ) );
        SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)pair.getSnmpObjectAt( 0 );
        SnmpObject snmpValue = pair.getSnmpObjectAt( 1 );
        retrievedID = snmpOID.toString();
        String typeString = getLocalJavaName(snmpValue.getClass().getName());
System.out.println("Typestring: "+typeString);

        if( typeString.equals( "SnmpOctetString" ) )
        {
          String snmpString = snmpValue.toString();

          // truncate at first null character
          int nullLocation = snmpString.indexOf( '\0' );
          if( nullLocation >= 0 )
          {
            snmpString = snmpString.substring( 0, nullLocation );
          }

          messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpString );
          messagesArea.append( "  (hex: " + ( (SnmpOctetString)snmpValue ).toHexString() + ")\n" );
        }
        else
        {
          messagesArea.append(  new Date().toString() + " OID: " + snmpOID + "  type: " + typeString + "  value: " + snmpValue );
          messagesArea.append( "\n" );
        }
      }

    }
    catch( InterruptedIOException e )
    {
      messagesArea.append( "Interrupted during retrieval:  " + e + "\n" );
      e.printStackTrace();
    }
    catch( Exception e )
    {
      messagesArea.append( "Exception during retrieval:  " + e + "\n" );
      e.printStackTrace();
    }
    catch( Error err )
    {
      messagesArea.append( "Error during retrieval:  " + err + "\n" );
      err.printStackTrace();
    }

    getTreewalkDataButton.setText( "Get all OID values" );

  }




  /**
   * Method hexByte.
   *
   * @param b
   *
   * @return
   */
  private String hexByte( byte b )
  {
    int pos = b;
    if( pos < 0 )
    {
      pos += 256;
    }

    String returnString = new String();
    returnString += Integer.toHexString( pos / 16 );
    returnString += Integer.toHexString( pos % 16 );

    return returnString;
  }



  public static String getLocalJavaName( String text)
  {
    int indx = text.lastIndexOf( '.' );
    return ( indx != -1 ) ? text.substring( indx + 1 ) : text;
  }

  

  /**
   * Method main.
   *
   * @param args
   */
  public static void main( String args[] )
  {
    try
    {
      SimpleSnmpClient theApp = new SimpleSnmpClient();
      theApp.pack();
      theApp.setSize( 600, 500 );
      theApp.show();
    }
    catch( Exception e )
    {
    }
  }

}

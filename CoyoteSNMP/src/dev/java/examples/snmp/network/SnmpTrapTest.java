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
import java.util.Enumeration;

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

import coyote.commons.network.snmp.DefaultTrapListener;
import coyote.commons.network.snmp.DefaultTrapSender;
import coyote.commons.network.snmp.ITrapListener;
import coyote.commons.network.snmp.SnmpIpAddress;
import coyote.commons.network.snmp.SnmpObject;
import coyote.commons.network.snmp.SnmpObjectIdentifier;
import coyote.commons.network.snmp.SnmpTimeTicks;
import coyote.commons.network.snmp.SnmpTrapPDU;
import coyote.commons.network.snmp.SnmpVarBindList;
import coyote.commons.network.snmp.SnmpVariablePair;


/**
 * Class SnmpTrapTest.
 */
public class SnmpTrapTest extends JFrame implements ActionListener, ITrapListener
{

  /** Field sendTrapButton */
  JButton sendTrapButton;

  /** Field clearButton */
  JButton clearButton;

  /** Field messagesArea */
  JTextArea messagesArea;

  /** Field messagesScroll */
  JScrollPane messagesScroll;

  /** Field hostIDField, communityField, OIDField, valueField, enterpriseField, agentField */
  JTextField hostIDField, communityField, OIDField, valueField, enterpriseField, agentField;

  /** Field authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel, enterpriseLabel, agentLabel, genericTrapLabel, specificTrapLabel */
  JLabel authorLabel, hostIDLabel, communityLabel, OIDLabel, valueLabel, enterpriseLabel, agentLabel, genericTrapLabel, specificTrapLabel;

  /** Field valueTypeBox, genericTrapBox, specificTrapBox */
  JComboBox valueTypeBox, genericTrapBox, specificTrapBox;

  /** Field theMenubar */
  MenuBar theMenubar;

  /** Field fileMenu */
  Menu fileMenu;

  /** Field aboutItem, quitItem */
  MenuItem aboutItem, quitItem;

  /** Field trapListenerInterface */
  DefaultTrapListener trapListenerInterface;

  /** Field trapSenderInterface */
  DefaultTrapSender trapSenderInterface;

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
   * Constructor SnmpTrapTest.
   */
  public SnmpTrapTest()
  {
    setUpDisplay();

    try
    {
      trapListenerInterface = new DefaultTrapListener();

      trapListenerInterface.addTrapListener( this );
      trapListenerInterface.startReceiving();

      trapSenderInterface = new DefaultTrapSender();
    }
    catch( Exception e )
    {
      messagesArea.append( "Problem starting Trap Test: " + e.toString() + "\n" );
    }
  }




  /**
   * Method setUpDisplay.
   */
  private void setUpDisplay()
  {

    this.setTitle( "SNMP Trap Listener" );

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

    aboutItem = new MenuItem( "About..." );

    aboutItem.setActionCommand( "about" );
    aboutItem.addActionListener( this );
    fileMenu.add( aboutItem );

    fileMenu.addSeparator();

    quitItem = new MenuItem( "Quit" );

    quitItem.setActionCommand( "quit" );
    quitItem.addActionListener( this );
    fileMenu.add( quitItem );

    theMenubar.add( fileMenu );

    hostIDLabel = new JLabel( "Trap receiver address:" );
    hostIDField = new JTextField( 20 );

    hostIDField.setText( "10.0.1.1" );
    hostIDField.setEditable( true );

    OIDLabel = new JLabel( "additional variable OID:" );
    OIDField = new JTextField( 20 );

    OIDField.setEditable( true );

    valueLabel = new JLabel( "Value for additional variable:" );
    valueField = new JTextField( 20 );

    valueField.setEditable( true );

    communityLabel = new JLabel( "Community:" );
    communityField = new JTextField( 20 );

    communityField.setText( "public" );
    communityField.setEditable( true );

    authorLabel = new JLabel( " Bralyn Systems " );

    authorLabel.setFont( new Font( "SansSerif", Font.ITALIC, 8 ) );

    sendTrapButton = new JButton( "Send trap" );

    sendTrapButton.setActionCommand( "send trap" );
    sendTrapButton.addActionListener( this );

    clearButton = new JButton( "Clear messages" );

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

    enterpriseLabel = new JLabel( "Enterprise OID:" );
    enterpriseField = new JTextField( 20 );

    enterpriseField.setEditable( true );

    agentLabel = new JLabel( "Agent IP address:" );
    agentField = new JTextField( 20 );

    agentField.setEditable( true );

    genericTrapLabel = new JLabel( "Generic trap:" );
    genericTrapBox = new JComboBox();

    genericTrapBox.addItem( "Cold start (0)" );
    genericTrapBox.addItem( "Warm start (1)" );
    genericTrapBox.addItem( "Link down (2)" );
    genericTrapBox.addItem( "Link up (3)" );
    genericTrapBox.addItem( "Authentication failure (4)" );
    genericTrapBox.addItem( "EGP neighbor loss (5)" );
    genericTrapBox.addItem( "Enterprise specific (6)" );

    specificTrapLabel = new JLabel( "Specific trap:" );
    specificTrapBox = new JComboBox();

    specificTrapBox.addItem( "0" );
    specificTrapBox.addItem( "1" );
    specificTrapBox.addItem( "2" );
    specificTrapBox.addItem( "3" );
    specificTrapBox.addItem( "4" );
    specificTrapBox.addItem( "5" );

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

    theLayout.setConstraints( sendTrapButton, c );
    buttonPanel.add( sendTrapButton );

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

    theLayout.setConstraints( enterpriseLabel, c );
    oidPanel.add( enterpriseLabel );

    c.gridx = 2;
    c.gridy = 1;

    theLayout.setConstraints( enterpriseField, c );
    oidPanel.add( enterpriseField );

    c.gridx = 1;
    c.gridy = 2;

    theLayout.setConstraints( agentLabel, c );
    oidPanel.add( agentLabel );

    c.gridx = 2;
    c.gridy = 2;

    theLayout.setConstraints( agentField, c );
    oidPanel.add( agentField );

    c.gridx = 1;
    c.gridy = 3;

    theLayout.setConstraints( genericTrapLabel, c );
    oidPanel.add( genericTrapLabel );

    c.gridx = 2;
    c.gridy = 3;

    theLayout.setConstraints( genericTrapBox, c );
    oidPanel.add( genericTrapBox );

    c.gridx = 1;
    c.gridy = 4;

    theLayout.setConstraints( specificTrapLabel, c );
    oidPanel.add( specificTrapLabel );

    c.gridx = 2;
    c.gridy = 4;

    theLayout.setConstraints( specificTrapBox, c );
    oidPanel.add( specificTrapBox );

    c.gridx = 1;
    c.gridy = 5;

    theLayout.setConstraints( OIDLabel, c );
    oidPanel.add( OIDLabel );

    c.gridx = 2;
    c.gridy = 5;

    theLayout.setConstraints( OIDField, c );
    oidPanel.add( OIDField );

    c.gridx = 1;
    c.gridy = 6;

    theLayout.setConstraints( valueLabel, c );
    oidPanel.add( valueLabel );

    c.gridx = 2;
    c.gridy = 6;

    theLayout.setConstraints( valueField, c );
    oidPanel.add( valueField );

    c.gridx = 3;
    c.gridy = 6;

    theLayout.setConstraints( valueTypeBox, c );
    oidPanel.add( valueTypeBox );

    c.gridwidth = 1;
    c.anchor = GridBagConstraints.CENTER;

    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.WEST;

    JLabel messagesLabel = new JLabel( "Received traps:" );
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

    if( command == "about" )
    {
      // AboutDialog aboutDialog = new AboutDialog(this);
    }

    if( command == "send trap" )
    {
      try
      {

        String community = communityField.getText();
        int version = 0;  // SNMPv1
        InetAddress hostAddress = InetAddress.getByName( hostIDField.getText() );

        SnmpObjectIdentifier enterpriseOID = new SnmpObjectIdentifier( enterpriseField.getText() );
        SnmpIpAddress agentAddress = new SnmpIpAddress( agentField.getText() );
        int genericTrap = genericTrapBox.getSelectedIndex();
        int specificTrap = specificTrapBox.getSelectedIndex();
        SnmpTimeTicks timestamp = new SnmpTimeTicks( SnmpTimeTicks.currentTimeStamp() );

        // see if have any additional variable pairs to send, and add them to
        // the VarBindList if so
        SnmpVarBindList varBindList = new SnmpVarBindList();

        String itemIDString = OIDField.getText();

        if( !itemIDString.equals( "" ) )
        {
          SnmpObjectIdentifier itemID = new SnmpObjectIdentifier( itemIDString );

          String valueString = valueField.getText();
          String valueTypeString = (String)valueTypeBox.getSelectedItem();
          valueTypeString = "snmp." + valueTypeString;

          SnmpObject itemValue;
          Class valueClass = Class.forName( valueTypeString );
          itemValue = (SnmpObject)valueClass.newInstance();

          itemValue.setValue( valueString );

          varBindList.addSnmpObject( new SnmpVariablePair( itemID, itemValue ) );
        }

        // create trap pdu
        SnmpTrapPDU pdu = new SnmpTrapPDU( enterpriseOID, agentAddress, genericTrap, specificTrap, timestamp, varBindList );

        // and send it
        trapSenderInterface.sendTrap( hostAddress, community, pdu );

        messagesArea.append( "Sent trap to " + hostIDField.getText() + ":\n" );

        messagesArea.append( "  enterprise OID:     " + pdu.getEnterpriseOID().toString() + "\n" );
        messagesArea.append( "  agent address:      " + pdu.getAgentAddress().toString() + "\n" );
        messagesArea.append( "  generic trap:       " + pdu.getGenericTrap() + "\n" );
        messagesArea.append( "  specific trap:      " + pdu.getSpecificTrap() + "\n" );
        messagesArea.append( "  timestamp:          " + pdu.getTimestamp() + "\n" );
        messagesArea.append( "  supplementary vars: " + pdu.getVarBindList().toString() + "\n" );

        messagesArea.append( "\n" );

      }
      catch( InterruptedIOException e )
      {
        messagesArea.append( "Interrupted during trap send:  " + e + "\n" );
      }
      catch( Exception e )
      {
        messagesArea.append( "Exception during trap send:  " + e + "\n" );
      }
    }

  }




  /**
   * Method processTrap.
   *
   * @param pdu
   */
  public void processTrap( SnmpTrapPDU pdu )
  {
    messagesArea.append( "Got trap:\n" );

    messagesArea.append( "  enterprise OID:     " + pdu.getEnterpriseOID().toString() + "\n" );
    messagesArea.append( "  agent address:      " + pdu.getAgentAddress().toString() + "\n" );
    messagesArea.append( "  generic trap:       " + pdu.getGenericTrap() + "\n" );
    messagesArea.append( "  specific trap:      " + pdu.getSpecificTrap() + "\n" );
    messagesArea.append( "  timestamp:          " + pdu.getTimestamp() + "\n" );
    messagesArea.append( "  supplementary vars: " + pdu.getVarBindList().toString() + "\n" );

    messagesArea.append( "\n" );

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
      SnmpTrapTest theApp = new SnmpTrapTest();
      theApp.pack();
      theApp.setSize( 600, 500 );
      theApp.show();
    }
    catch( Exception e )
    {
    }
  }

}

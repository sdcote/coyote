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
import java.net.InetAddress;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.FontUIResource;

import coyote.commons.network.snmp.DefaultAgent;
import coyote.commons.network.snmp.IRequestListener;
import coyote.commons.network.snmp.SnmpBadValueException;
import coyote.commons.network.snmp.SnmpBerCodec;
import coyote.commons.network.snmp.SnmpGetException;
import coyote.commons.network.snmp.SnmpObject;
import coyote.commons.network.snmp.SnmpObjectIdentifier;
import coyote.commons.network.snmp.SnmpOctetString;
import coyote.commons.network.snmp.SnmpPdu;
import coyote.commons.network.snmp.SnmpRequestException;
import coyote.commons.network.snmp.SnmpSequence;
import coyote.commons.network.snmp.SnmpSetException;
import coyote.commons.network.snmp.SnmpVariablePair;


/**
 * This shows how to build a SNMP agent with the SNMP classes.
 * 
 * <p>This runs a GUI that shows SNMP activity through this agent in the 
 * message area.</p>
 * 
 * <p>Only 2 OIDs are supported by this listener: &quot;1.3.6.1.2.1.99.0&quot; 
 * and &quot;1.3.6.1.2.1.100.0&quot;.
 *
 * @version $Revision:$
 */
public class SnmpAgentTest extends JFrame implements ActionListener, IRequestListener
{

  /** Field clearButton */
  JButton clearButton;

  /** Field messagesArea */
  JTextArea messagesArea;

  /** Field messagesScroll */
  JScrollPane messagesScroll;

  /** Field authorLabel */
  JLabel authorLabel;

  /** Field theMenubar */
  MenuBar theMenubar;

  /** Field fileMenu */
  Menu fileMenu;

  /** Field aboutItem, quitItem */
  MenuItem aboutItem, quitItem;

  /** Field agentInterface */
  DefaultAgent agentInterface;

  /** Field communityName */
  String communityName = "public";

  /** Field storedSNMPValue */
  SnmpOctetString storedSNMPValue;

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
   * Constructor SnmpAgentTest.
   */
  public SnmpAgentTest()
  {
    setUpDisplay();

    storedSNMPValue = new SnmpOctetString( "Original value" );

    try
    {
      int version = 0;  // SNMPv1

      agentInterface = new DefaultAgent( version );

      agentInterface.addRequestListener( this );
      agentInterface.startReceiving();

    }
    catch( Exception e )
    {
      messagesArea.append( "Problem starting Agent Test: " + e.toString() + "\n" );
    }
  }




  /**
   * Method setUpDisplay.
   */
  private void setUpDisplay()
  {

    this.setTitle( "SNMP Agent Test" );

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

    clearButton = new JButton( "Clear messages" );

    clearButton.setActionCommand( "clear messages" );
    clearButton.addActionListener( this );

    authorLabel = new JLabel( " Bralyn Systems " );

    authorLabel.setFont( new Font( "SansSerif", Font.ITALIC, 8 ) );

    messagesArea = new JTextArea( 10, 60 );
    messagesScroll = new JScrollPane( messagesArea );

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

    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout( theLayout );

    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.WEST;

    JLabel messagesLabel = new JLabel( "Received requests:" );
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

    c.fill = GridBagConstraints.BOTH;
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = .5;
    c.weighty = .5;

    theLayout.setConstraints( messagesPanel, c );
    this.getContentPane().add( messagesPanel );

    c.fill = GridBagConstraints.NONE;
    c.gridx = 1;
    c.gridy = 2;
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

  }




  /**
   * Handles Get- or Set- request messages. 
   *
   * @param pdu
   * @param communityName
   *
   * @return the sequence
   *
   * @throws SnmpGetException
   * @throws SnmpSetException
   */
  public SnmpSequence processRequest( SnmpPdu pdu, String communityName, InetAddress source ) throws SnmpGetException, SnmpSetException
  {
    messagesArea.append( "Got Request PDU:\n" );
    messagesArea.append( "  Source:     " + source + "\n" );
    messagesArea.append( "  Community:  " + communityName + "\n" );
    messagesArea.append( "  RequestId:  " + pdu.getRequestID() + "\n" );
    messagesArea.append( "  PDU type:   " );

    byte pduType = pdu.getPDUType();
    switch( pduType )
    {

      case SnmpBerCodec.SNMPGETREQUEST:
      {
        messagesArea.append( "SNMPGETREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPGETNEXTREQUEST:
      {
        messagesArea.append( "SNMPGETNEXTREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPSETREQUEST:
      {
        messagesArea.append( "SNMPSETREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPGETRESPONSE:
      {
        messagesArea.append( "SNMPGETRESPONSE\n" );

        break;
      }

      case SnmpBerCodec.SNMPTRAP:
      {
        messagesArea.append( "SNMPTRAP\n" );

        break;
      }

      default:
      {
        messagesArea.append( "unknown\n" );

        break;
      }

    }

    SnmpSequence varBindList = pdu.getVarBindList();
    SnmpSequence responseList = new SnmpSequence();

    for( int i = 0; i < varBindList.size(); i++ )
    {
      SnmpSequence variablePair = (SnmpSequence)varBindList.getSnmpObjectAt( i );
      SnmpObjectIdentifier snmpOID = (SnmpObjectIdentifier)variablePair.getSnmpObjectAt( 0 );
      SnmpObject snmpValue = (SnmpObject)variablePair.getSnmpObjectAt( 1 );

      messagesArea.append( "       OID:           " + snmpOID + "\n" );
      messagesArea.append( "       value:         " + snmpValue + "\n" );

      // check to see if supplied community name is ours; if not, we'll just silently
      // ignore the request by not returning anything
      if( !communityName.equals( this.communityName ) )
      {
        continue;
      }

      // we'll only respond to requests for OIDs 1.3.6.1.2.1.99.0 and 1.3.6.1.2.1.100.0

      // OID 1.3.6.1.2.1.99.0: it's read-only
      if( snmpOID.toString().equals( "1.3.6.1.2.1.99.0" ) )
      {
        if( pduType == SnmpBerCodec.SNMPSETREQUEST )
        {
          // got a set-request for our variable; throw an exception to indicate the
          // value is read-only - the DefaultAgent will create the appropriate
          // error message using our supplied error index and status
          // note that error index starts at 1, not 0, so it's i+1
          int errorIndex = i + 1;
          int errorStatus = SnmpRequestException.VALUE_READ_ONLY;
          throw new SnmpSetException( "Trying to set a read-only variable!", errorIndex, errorStatus );
        }
        else if( pduType == SnmpBerCodec.SNMPGETREQUEST )
        {
          // got a get-request for our variable; send back a value - just a string
          try
          {
            SnmpVariablePair newPair = new SnmpVariablePair( snmpOID, new SnmpOctetString( "Boo" ) );
            responseList.addSnmpObject( newPair );
          }
          catch( SnmpBadValueException e )
          {
            // won't happen...
          }
        }

      }

      if( snmpOID.toString().equals( "1.3.6.1.2.1.100.0" ) )
      {
        if( pduType == SnmpBerCodec.SNMPSETREQUEST )
        {
          // got a set-request for our variable; supplied value must be a string
          if( snmpValue instanceof SnmpOctetString )
          {
            // assign new value
            storedSNMPValue = (SnmpOctetString)snmpValue;

            // return SnmpVariablePair to indicate we've handled this OID
            try
            {
              SnmpVariablePair newPair = new SnmpVariablePair( snmpOID, storedSNMPValue );
              responseList.addSnmpObject( newPair );
            }
            catch( SnmpBadValueException e )
            {
              // won't happen...
            }

          }
          else
          {
            int errorIndex = i + 1;
            int errorStatus = SnmpRequestException.BAD_VALUE;
            throw new SnmpSetException( "Supplied value must be SnmpOctetString", errorIndex, errorStatus );
          }

        }
        else if( pduType == SnmpBerCodec.SNMPGETREQUEST )
        {
          // got a get-request for our variable; send back a value - just a string
          try
          {
            SnmpVariablePair newPair = new SnmpVariablePair( snmpOID, storedSNMPValue );
            responseList.addSnmpObject( newPair );
          }
          catch( SnmpBadValueException e )
          {
            // won't happen...
          }
        }

      }

    }

    messagesArea.append( "\n" );

    // return the created list of variable pairs
    return responseList;

  }




  /**
   * Method processGetNextRequest.
   *
   * @param pdu
   * @param communityName
   * @param source The source of the request to be used for access controls
   *
   * @return the sequence
   *
   * @throws SnmpGetException
   */
  public SnmpSequence processGetNextRequest( SnmpPdu pdu, String communityName, InetAddress source ) throws SnmpGetException
  {
    messagesArea.append( "Got Get-Next PDU:\n" );
    messagesArea.append( "  Source:     " + source + "\n" );
    messagesArea.append( "  Community:  " + communityName + "\n" );
    messagesArea.append( "  RequestId:  " + pdu.getRequestID() + "\n" );
    messagesArea.append( "  PDU type:   " );

    byte pduType = pdu.getPDUType();
    switch( pduType )
    {

      case SnmpBerCodec.SNMPGETREQUEST:
      {
        messagesArea.append( "SNMPGETREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPGETNEXTREQUEST:
      {
        messagesArea.append( "SNMPGETNEXTREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPSETREQUEST:
      {
        messagesArea.append( "SNMPSETREQUEST\n" );

        break;
      }

      case SnmpBerCodec.SNMPGETRESPONSE:
      {
        messagesArea.append( "SNMPGETRESPONSE\n" );

        break;
      }

      case SnmpBerCodec.SNMPTRAP:
      {
        messagesArea.append( "SNMPTRAP\n" );

        break;
      }

      default:
      {
        messagesArea.append( "unknown\n" );

        break;
      }

    }

    SnmpSequence varBindList = pdu.getVarBindList();
    SnmpSequence responseList = new SnmpSequence();

    for( int i = 0; i < varBindList.size(); i++ )
    {
      SnmpSequence variablePair = (SnmpSequence)varBindList.getSnmpObjectAt( i );
      SnmpObjectIdentifier suppliedOID = (SnmpObjectIdentifier)variablePair.getSnmpObjectAt( 0 );
      SnmpObject suppliedObject = (SnmpObject)variablePair.getSnmpObjectAt( 1 );

      messagesArea.append( "       OID:           " + suppliedOID + "\n" );
      messagesArea.append( "       value:         " + suppliedObject + "\n" );

      // check to see if supplied community name is ours; if not, we'll just silently
      // ignore the request by not returning anything
      if( !communityName.equals( this.communityName ) )
      {
        continue;
      }

      // we'll only respond to requests for OID 1.3.6.1.2.1.99.0, and it's read-only;
      // for get-next request, we'll return the value for 1.3.6.1.2.1.100.0
      if( suppliedOID.toString().equals( "1.3.6.1.2.1.99.0" ) )
      {
        if( pduType == SnmpBerCodec.SNMPGETNEXTREQUEST )
        {
          // got a get-next-request for our variable; send back a value for OID 1.3.6.1.2.1.100.0
          try
          {
            // create SnmpVariablePair for the next OID and its value
            SnmpObjectIdentifier nextOID = new SnmpObjectIdentifier( "1.3.6.1.2.1.100.0" );
            SnmpVariablePair innerPair = new SnmpVariablePair( nextOID, storedSNMPValue );

            // now create a pair containing the supplied OID and the variable 
            // pair containing the following OID and its value; this allows the 
            // Snmpv1AgentInterface to know which of the supplied OIDs the new 
            // OID corresponds to (follows).
            SnmpVariablePair outerPair = new SnmpVariablePair( suppliedOID, innerPair );

            // add the "compound" SnmpVariablePair to the response list
            responseList.addSnmpObject( outerPair );
          }
          catch( SnmpBadValueException e )
          {
            // won't happen...
          }
        }

      }

    }

    messagesArea.append( "\n" );

    // return the created list of variable pairs
    return responseList;
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
      SnmpAgentTest theApp = new SnmpAgentTest();
      theApp.pack();
      theApp.setSize( 600, 500 );
      theApp.show();
    }
    catch( Exception e )
    {
    }
  }

}

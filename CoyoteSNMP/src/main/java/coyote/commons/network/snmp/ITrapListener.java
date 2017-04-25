/*
 * $Id:$
 */
package coyote.commons.network.snmp;

/**
 * ITrapListener is an interface that must be implemented by any class that 
 * wishes to act as a "listener" for trap messages sent from remote SNMP 
 * entities using the SNMPv1TrapInterface class. 
 * 
 * <p>The ITrapListener class listens for trap messages, and passes any it 
 * receives on to ITrapListener subclasses that have registered with it through 
 * its addTrapListener() method.</p>
 */
public interface ITrapListener
{

  /**
   * Method processTrap.
   *
   * @param trapPDU
   */
  public void processTrap( SnmpTrapPDU trapPDU );

}

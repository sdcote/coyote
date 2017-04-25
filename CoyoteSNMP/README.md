# BatchSNMP

This is a collection of SNMP tools for the Coyote Batch Toolkit which allow interacting with network devices using the SNMP protocol.

Tis is mostly used as a PoC project to demonstrate the utility of the platform beyond that of data exchange, integration and general ETL tasks. This platform seeks to enable:

* Discovering SNMP device agents on the network
* Reading data from SNMP devices discovered on the network and writing then to a data store.
* Writing Events (Traps) to the network.
* Reading requests from the network for MIB data and writing those responses back
* Responding to Web Service requests for MIB data on other devices on the network (Network Management Gateway).
* Reading a SNMP request, making a request using another protocol and responding back with and SNMP response.


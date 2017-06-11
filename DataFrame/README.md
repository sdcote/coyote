[![Build Status](https://drone.io/github.com/sdcote/dataframe/status.png)](https://drone.io/github.com/sdcote/dataframe/latest)
[![Download](https://api.bintray.com/packages/sdcote/maven/DataFrame/images/download.png) ](https://bintray.com/sdcote/maven/DataFrame/_latestVersion)

DataFrame
=========

A data marshaling toolkit.

Data Frame is a compact, efficient, hierarchical, self-describing and utilitarian data format with the ability to marshal other formats.

This toolkit was conceived in 2002 to implement the Data Transfer Object (DTO) design pattern in distributed applications; passing a DataFrame as both argument and return values in remote service calls. Using this implementation of a DTO allowed for more efficient transfer of data between distributed components, reducing latency, improving throughput and decoupling not only the components of the system, but moving business logic out of the data model.

A DataFrame can also be used as a Value Object design pattern implementation, providing access to data without carrying any business logic. Value Objects tend to make service interfaces simpler and can many times reduce the number of calls to remote services through the population of the entire data model thereby avoiding multiple calls to the service. Value Objects can also be used to reduce the number of parameters on service methods.

DataFrame uses a binary wire format which is more efficient to parse and transfer. Marshaling occurs only when the data is accessed so unnecessary conversions are avoided.


Prerequisites:
--------
  * JDK 1.6 or later installed
  * Ability to run bash scripts
  * Assumes you do not have gradle installed (if you do, you can replace gradlew with gradle)

# Road Map

This is a list of capabilities currently planned for the Coyote Data Exchange Toolkit. THeir implementation tileline is based on developer availability and clinet projects. If we have a clinet who wants Salesforce integation today, then that will be worked on next. Otherwise the order here is roughly the current priority.

## RunJob Tasks
This task executes a specific Transfer Job if its condition is met. This provided the capability to chain together complex orchestrations of Jobs.

#### Features
* Jobs can be run in serial or parallel, each with their own set of data
* Processing results from Jobs can be accessed in other Jobs via Job Contexts. 

## CoyoteJS - JavaScript Processing
There is no need to develop custom Java component to implement your business logic. Transformers can be written in JavaScript to perform just about anything you need.

This bridges the gap between configuration and custom development with simple stripts to handle more than the basic components. Of course, you can create complex scripts, but you have complete control over when to make the jume into developing your own custom components in Java, Scala or other compiler, JVM compatible language. 

Maybe you just want to prototype an idea and test the waters before setting up a development project. It's possible all you need is a slight tweak to an existing job. In any case, running a scripted component may be the best approach to your customization needs.

#### Features
* centralized script management
* ability to include other libraries 
 
## CoyoteGS - Groovy Scripting
If JavaScript is not for you, it is possible to implement your custom business logic in Groovy. While you can create compiled components in Groovy, you may want to prototype functionality in in scripts first and move to compiled bytecode later.

## CoyoteWQ - Worker Queues
A set of application components which enable setting up horizontally scalable solutions that execute jobs in the cloud or your data center. Set up any number of workers to read job requests from a queue and perform data transfer jobs remotely.

Not just for massive integrations, Worker Queues can implement nearly any processing asysnchronously. If an event comes in one of the queues or topics, a Coyote worker can execue a specific job for that event. For example, If a customers mobile device sends a geofencing event to your workers, they can trigger almost any processing you configure. This means customers with your mobile application can send you an event when they enter your store and a worker can process that event and send them a coupon to trigger an impulse sale. the possibilities are endless.

#### Features
* Implement your own IFTTT servers

## CoyoteSF Salesforce
Just like the ServiceNow support (CoyoteSN) this set of components allows a variety of integration capabilities through the Coyote Toolkit. Maybe you want to combine Salesforce data into some of your Transfer Jobs; this provides the ability to efficiently access any and all your Salesforce data/
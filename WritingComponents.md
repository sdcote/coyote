# Writing Coyote DX Components

## Readers
Extend from `AbstractFrameReader` to inherit all the basic plumbing. Implement `read(DataFrame)`  and `eof()`: 
```java
public class WebPageMetricReader extends AbstractFrameReader implements FrameReader {
  @Override
  public DataFrame read(TransactionContext context) {
    return null;
  }
  @Override
  public boolean eof() {
    return true;
  }
}
```
Next implement `open(TransformContext)` as it will be your initialization method. From this method you ca read from your configuration (handled by `AbstractFrameReader`) and get everything setup for your first `read(DataFrame)` call.

Be sure to call `super.open(context);` first off, so your base class will be properly initialized first.

As a reader, you will be interested in your "source"; from where you will be reading. the most common way to do that is by looking in your configuration. use the `getString(String)` to get the string value of a named configuration parameter:
```java
String source = getString(ConfigTag.SOURCE);
Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));
```
Any and all configuration parameters can be retrieve using `getString(String)` where the primary argument is the name of the configuration parameter to retrieve. The name search is case insensitive so it is easier to configure your reader.

Because all readers need a "source", the `AbstractFrameReader` class has a method to retrieve the "source" configuration property. Just call `getSource()` and you will get the source URI configured for your reader.  

You may have noticed that there are no exceptions thrown with `open(TransformContext)`, this is by design. Throwing exceptions at this phase may prevent some features from running fully, so Coyote DX uses error messages in the `TransformContext`. Just place an error message in the `TransformContext` and the transform engine will generate the appropriate error message, abort the run loop, and exist gracefully. The following is an example of a common practice; logging a more complete error message when the error occurs and echoing that message in the transform context error:  
```java
Log.error("Hey! This component could not initialize - "+msg);
context.setError(msg);
```

### Reader Life Cycle
Readers are opened first. If the context is not in error, the transform engine enters a read loop by checking the `eof()` method of the reader. If while the readers `eof()` method returns false, the transform engine calls `read(TransactionContext)` expecting a `DataFrame` to be returned representing the record read. If the record is not null, it is passed through the pipeline. After passing the record through the pipeline, the read loop cycles and EOF is checked again.

When `eof()` returns true, the the read loop exists and the components in the transform engine have their `close()` methods called to clean up their resources and terminate. 


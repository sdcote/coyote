/**
 * Filters are components which are passed the records immediately after 
 * reading and are intended to remove data from the transaction context (via 
 * the working frame) to reduce processing on the other components.
 * 
 * <p>Since they are run before other components, filters have the chance to 
 * see all data in the context and perform processing before any transformation
 * activities occur.</p>
 */
package coyote.batch.filter;
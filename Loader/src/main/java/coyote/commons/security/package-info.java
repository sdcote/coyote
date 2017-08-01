/**
 * This is a basic encryption package which allows for a pluggable encryption
 * model.
 *
 * <p>This package is meant to achieve a reasonable level of security without
 * the expenditure of resources in excess of the value of the data being
 * protected. It is just one element of a comprehensive defense in depth
 * security strategy meant to close gaps in data exposure as a result of using
 * this API.</p>
 *
 * <p>This package is not meant to be government-grade security, but reasonable
 * measures to protect data as opposed to exposing it openly. The goal is to
 * make it harder for unauthorized actors to access sensitive data, not prevent
 * it completely. Doing so would take more than a generic API and belongs
 * outside this problem domain.</p>
 *
 * <p>Blowfish has been chosen as the cipher for this platform for the
 * following reasons:<ul>
 * <li>Variable key length - for exportability</li>
 * <li>Public Domain - for open use, analysis, and exportability</li>
 * <li>Block Cipher - Streams are not necessary</li>
 * <li>Performance - runs with few resources</li>
 * <li>Simplicity - implementation is easy to represent in a single class</li>
 * <li>Reasonable strength to resource ratio -we don't need military-grade
 * encryption.</li></ul>
 *
 * <p>Blowfish is classified as public domain and has been analyzed extensively
 * for decades and has been found fast and reliable. It uses variable length
 * keys which makes it capable of both domestic and exportable use.</p>
 *
 * <p>If stronger encryption is desired, it is recommended that a separate
 * library be utilized, preferably one which has undergone external evaluation
 * and certification and that training is acquired to employ it correctly.</p>
 */

package coyote.commons.security;
/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.i13n;

/**
 * 
 */
public class StatBoardPerfTest {

  /**
   * Run a 10 second test.
   * 
   * @return the actual elapsed time.
   */
  private static long runTest( StatBoard scorecard ) {
    long started = System.currentTimeMillis();
    long end = started + 10000;
    while ( System.currentTimeMillis() <= end ) {
      scorecard.increment( "DemoCounter" );
    }
    return System.currentTimeMillis() - started;
  }




  public static void main( String[] args ) {
    StatBoard scorecard = new StatBoardImpl();

    System.out.println( "Initialized - starting test..." );

    // don't include counter creation in the measures
    Counter counter = scorecard.getCounter( "DemoCounter" );

    long totalElapsed = 0;
    long totalCount = 0;

    int runs = 10;
    for ( int x = 0; x < runs; x++ ) {
      totalElapsed += runTest( scorecard );
      totalCount += counter.getValue();
      counter.reset();
    }

    //  2,322,454.52 calls per second on a Pentium 3 JVM 1.4
    // 23,356,136.00 calls per second on a i7 Haswell JVM 1.7  =  0.04 microseconds per call?

    System.out.println( "Throughput = " + ( (float)( ( (float)totalCount / (float)totalElapsed ) * 10000 ) / runs ) + " calls per second" );
  }

}

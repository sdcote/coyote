/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.text.graph;

import coyote.TestUtil;
import coyote.commons.FileUtil;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 *
 */
public class TextGraphTest {


  private static final double[] sinWaveSeries = new double[120];
  private static final double[] randomWaveSeries = new double[]{
          13, 14, 15, 19, 6, 19, 14, 6, 0, 7, 13, 19, 1, 3, 8, 3, 0, 2, 5, 1,
          14, 10, 5, 5, 16, 13, 9, 13, 9, 6, 6, 0, 1, 16, 19, 4, 0, 4, 0, 10,
          12, 19, 5, 17, 2, 2, 5, 18, 12, 10, 13, 0, 12, 0, 17, 0, 1, 3, 9, 19,
          14, 5, 2, 7, 17, 11, 19, 0, 7, 4, 11, 1, 18, 6, 13, 4, 4, 6, 12, 15,
          5, 2, 10, 8, 8, 0, 8, 12, 9, 1, 19, 12, 15, 3, 16, 0, 17, 5, 7, 9}; // 100 Random numbers between 0-20

  static {
    // Compute Sin Wave Series
    for (int i = 0; i < sinWaveSeries.length; i++)
      sinWaveSeries[i] = 15 * Math.sin(i * ((Math.PI * 4) / sinWaveSeries.length));
  }


  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    //Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
  }


  @Test
  public void testSinWaveFullHeight() throws IOException {
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("graphs/sinWaveFullHeight.txt")),
            TestUtil.commonizeLineEndings(TextGraph.fromSeries(sinWaveSeries).plot())
    );
  }


  @Test
  public void testSinWaveHalfHeight() throws IOException {
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("graphs/sinWaveHalfHeight.txt")),
            TestUtil.commonizeLineEndings(TextGraph.fromSeries(sinWaveSeries).withNumRows(15).plot())
    );
  }


  @Test
  public void testRandomWave() throws IOException {
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("graphs/randomWave.txt")),
            TestUtil.commonizeLineEndings(TextGraph.fromSeries(randomWaveSeries).plot())
    );
  }


  @Ignore
  public void Example() throws IOException {
    Random r = new Random();
    int low = 0;
    int high = 20;

    double[] series = new double[100];
    for (int x = 0; x < 100; x++) {
      series[x] = r.nextInt(high - low) + low;
    }
    StringBuffer b = new StringBuffer("private static final double[] randomWaveSeries = new double[]{");
    for (int x = 0; x < 100; x++) {
      b.append(series[x]);
      if (x + 1 < 100) b.append(", ");
    }
    b.append("}; // 100 Random numbers between 0-20");
    System.out.println(b);

    System.out.println(TextGraph.fromSeries(series).plot());

    FileUtil.stringToFile(TextGraph.fromSeries(series).plot(), "randomWave.txt", "UTF-8");
  }

}

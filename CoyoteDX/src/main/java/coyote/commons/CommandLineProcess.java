/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import coyote.loader.log.Log;


/**
 * Execute commands at the operating system level.
 */
public class CommandLineProcess {

  private static Object mutex = new Object();




  /**
   * Private constructor for a singleton fixture.
   */
  private CommandLineProcess() {
    super();
  }




  /**
   * Execute the given command.
   * 
   * <p>This is a blocking call and will not return until the process returns. 
   * It has therefore been made thread-safe so that may different threads can 
   * make calls to this fixture to improce performance.
   * 
   * <p>Each call spawns another thread in the JRE to handle reading the 
   * STDERR stream while the calling thread reads the STDOUT stream.
   * 
   * <p><strong>NOTE:</strong> Care must be given to ensure the called process 
   * does not block for input, which will prevent the calling thread from 
   * returning. 
   *
   * @param command the command and arguments to process
   *
   * @return a list of strings representing the output from the process
   */
  public static Result exec(String command) {

    if (Log.isLogging(Log.DEBUG_EVENTS)) {
      Log.debug("EXEC (String) called with command \"" + command + "\"");
    }

    String[] outArray = new String[0];
    String[] errArray = new String[0];

    Process process = null;
    long startTime = -1;
    int exitValue = -1;
    long duration = -1;
    try {
      startTime = System.currentTimeMillis();

      Log.debug("EXEC calling runtime exec");
      process = execSync(command);
      Log.debug("EXEC runtime exec returned");

      // start reading errors in a separate thread
      ErrorReader errreader = new CommandLineProcess.ErrorReader();
      errreader.printing = Log.isLogging(Log.DEBUG_EVENTS);
      errreader.stream = StreamUtil.getCharBufferedReader(process.getErrorStream());
      errreader.collecting = true;
      errreader.start();

      // wait for the process to complete, this is where we can block for a while
      Log.debug("EXEC calling waitFor");
      exitValue = process.waitFor();
      duration = System.currentTimeMillis() - startTime;
      Log.debug("EXEC waitFor returned");

      // after the process is complete, read everything from STDOUT
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while ((line = reader.readLine()) != null) {
        outArray = (String[])ArrayUtil.addElement(outArray, line);
      }

      process = null;

      errArray = errreader.collected;
    } catch (Exception exception) {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        StringBuffer b = new StringBuffer("*** exec Exception");
        b.append("\n  command: " + command);
        b.append("\n     exit: " + exitValue);
        b.append("\nOUT lines: " + outArray.length);
        b.append("\nERR lines: " + errArray.length);
        b.append("\n    error: " + exception.toString());
        Log.debug(b);
      }
    }

    if (process != null) {
      process.destroy();
    }

    if (Log.isLogging(Log.DEBUG_EVENTS)) {
      StringBuffer b = new StringBuffer("*** exec results");
      b.append("\n  command: " + command);
      b.append("\n     exit: " + exitValue);
      b.append("\nOUT lines: " + outArray.length);
      b.append("\nERR lines: " + errArray.length);
      b.append("\n     time: " + duration + "ms");
      Log.debug(b);
    }

    return new Result(command, startTime, duration, outArray, errArray, exitValue);
  }




  /**
   * Execute the command in a separate process.
   *
   * @param line the command to call and its arguments in one string
   *
   * @return the process being executed
   *
   * @throws IOException
   */
  protected static Process execSync(String line) throws IOException {
    Process process;
    synchronized (mutex) {
      process = Runtime.getRuntime().exec(line);
    }
    return process;
  }




  /**
   * Execute the command in a separate process.
   *
   * @param args the command to call and its arguments
   *
   * @return the process being executed
   *
   * @throws IOException
   */
  protected static Process execSync(String[] args) throws IOException {
    Process process;

    synchronized (mutex) {
      process = Runtime.getRuntime().exec(args);
    }

    return process;
  }

  /**
   * Reads errors in a separate thread,
   */
  private static class ErrorReader extends Thread {
    public BufferedReader stream;
    public boolean printing;
    public boolean collecting;
    public String[] collected;
    public int collectedMax;




    /**
     * Default constructor.
     */
    public ErrorReader() {
      this(true, true, 500);
    }




    /**
     * Constructor specifying operation.
     * 
     * @param print print the errors as they are received
     * @param collect collect the errors for later retrieval
     * @param max the maximum number of errors to collect.
     */
    public ErrorReader(boolean print, boolean collect, int max) {
      printing = print;
      collecting = collect;
      collected = new String[0];
      collectedMax = max;
    }




    /**
     * Start reading from the (error) stream.
     */
    public void run() {
      try {
        String s;
        while ((s = stream.readLine()) != null) {
          if (printing) {
            System.err.println(s);
          }
          if (collecting) {
            collected = (String[])ArrayUtil.addElement(collected, s);
            if (collected.length > collectedMax) {
              collected = (String[])ArrayUtil.removeElementAt(collected, 0);
            }
          }
        }
        return;
      } catch (Exception exception) {
        Log.error("Error reading from error stream: " + exception.getMessage());
      }
    }
  }

  /**
   * Processing results from the shell execution.
   */
  public static class Result {
    private final String command;
    private final long startTime;
    private final long duration;
    private final String[] output;
    private final String[] error;
    private final int exitCode;




    Result(String cmd, long start, long dur, String[] out, String[] err, int code) {
      command = cmd;
      startTime = start;
      duration = dur;
      output = out;
      error = err;
      exitCode = code;
    }




    /**
     * @return the command which was executed
     */
    public String getCommand() {
      return command;
    }




    /**
     * @return the time the command was executed
     */
    public long getStartTime() {
      return startTime;
    }




    /**
     * @return how long (in milliseconds) the command took to process
     */
    public long getDuration() {
      return duration;
    }




    /**
     * @return the output from the process
     */
    public String[] getOutput() {
      return output;
    }




    /**
     * @return the errors from the process
     */
    public String[] getError() {
      return error;
    }




    /**
     * @return the exit code from the process
     */
    public int getExitCode() {
      return exitCode;
    }
  }

}
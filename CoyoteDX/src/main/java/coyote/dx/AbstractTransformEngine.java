/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.ExceptionUtil;
import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dx.context.ContextKey;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.mapper.DefaultFrameMapper;
import coyote.dx.mapper.MappingException;
import coyote.dx.validate.ValidationException;
import coyote.i13n.AppEvent;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 *
 */
public abstract class AbstractTransformEngine extends AbstractConfigurableComponent implements TransformEngine, ConfigurableComponent {
    static final String FORMAT_SYMBOL_LOOKUP_TAG = "FormatSymbol";
    /**
     * A symbol table to support basic template functions
     */
    protected final SymbolTable symbols = new SymbolTable();
    /**
     * Globally unique identifier for this instance.
     */
    private final String instanceId = GUID.randomGUID().toString();
    /**
     * Tasks to perform prior to the transform. (e.g. Read from FTP site)
     */
    protected List<TransformTask> preProcesses = new ArrayList<TransformTask>();
    /**
     * The component which will read frames into the transformation engine.
     */
    protected FrameReader reader = null;
    /**
     * The component which will read (history) frames into components to provide them with previous/historic values on which to base their calculations.
     */
    protected FrameReader preloader = null;
    /**
     * List of filters which will remove unwanted frames from the transformation stream
     */
    protected List<FrameFilter> filters = new ArrayList<FrameFilter>();
    /**
     * List of validation rule from the stream. The frame must match all validators to continue on through the stream.
     */
    protected List<FrameValidator> validators = new ArrayList<FrameValidator>();
    /**
     * The list of transformations to be applied to the frame.
     */
    protected List<FrameTransform> transformers = new ArrayList<FrameTransform>();
    protected List<FrameAggregator> aggregators = new ArrayList<FrameAggregator>();
    /**
     * The mapping of the frame to a new frame; the component which create the desired frame.
     */
    protected FrameMapper mapper = null;
    /**
     * The list of writers which will be given the chance to record the frame somewhere.
     */
    protected List<FrameWriter> writers = new ArrayList<FrameWriter>();
    /**
     * The list of tasks to perform after the transformation is complete. (e.g. posting a file to a FTP site)
     */
    protected List<TransformTask> postProcesses = new ArrayList<TransformTask>();
    /**
     * The context for the entire transformation instance (i.e. job)
     */
    protected TransformContext transformContext = null;
    /**
     * A list of components interested in context events
     */
    protected List<ContextListener> listeners = new ArrayList<ContextListener>();
    /**
     * The current frame number
     */
    protected volatile long currentFrameNumber = 0;

    /**
     * The facade to log management functions
     */
    protected LogManager logManager = null;
    /**
     * The directory this engine uses for file operations
     */
    private File jobDirectory = null;
    /**
     * The shared directory where multiple engines may read and write data.
     */
    private File workDirectory = null;
    /**
     * the loader which loaded this engine
     */
    private Loader loader = null;
    /**
     * How many times the engine has been run.
     */
    private long runCount = 0;


    public AbstractTransformEngine() {
        symbols.readSystemProperties();
        Template.putStatic(FORMAT_SYMBOL_LOOKUP_TAG, new FormatSymbol(symbols));
    }

    /**
     * Format the string using the default formatting for all actions.
     *
     * @param date The date to format.
     * @return The formatted date string.
     */
    public static String formatDateTime(Date date) {
        if (date == null)
            return "null";
        else
            return new SimpleDateFormat(CDX.DEFAULT_DATETIME_FORMAT).format(date);
    }

    /**
     * Format the date only returning the date portion of the date  (i.e. no time representation).
     *
     * @param date the date/time to format
     * @return only the date portion formatted
     */
    public static String formatDate(Date date) {
        if (date == null)
            return "null";
        else
            return new SimpleDateFormat(CDX.DEFAULT_DATE_FORMAT).format(date);
    }

    /**
     * Format the date returning only the time portion of the date (i.e. no month, day or year).
     *
     * @param date the date/time to format
     * @return only the time portion formatted
     */
    public static String formatTime(Date date) {
        if (date == null)
            return "null";
        else
            return new SimpleDateFormat(CDX.DEFAULT_TIME_FORMAT).format(date);
    }

    /**
     * Return the working directory for this engine.
     *
     * @see coyote.dx.TransformEngine#getJobDirectory()
     */
    @Override
    public File getJobDirectory() {
        return jobDirectory;
    }

    /**
     * Set the private directory for this engine.
     *
     * @param dir the private directory to set
     */
    @SuppressWarnings("unchecked")
    public void setJobDirectory(File dir) {
        jobDirectory = dir;
        if (dir != null) {
            symbols.put(Symbols.JOB_DIRECTORY, jobDirectory.getAbsolutePath());
        } else {
            symbols.put(Symbols.JOB_DIRECTORY, null);
        }
    }

    /**
     * Return the common working directory.
     *
     * @see coyote.dx.TransformEngine#getWorkDirectory()
     */
    @Override
    public File getWorkDirectory() {
        return workDirectory;
    }

    /**
     * @param dir the common working directory to set
     */
    @SuppressWarnings("unchecked")
    public void setWorkDirectory(File dir) {
        workDirectory = dir;
        if (dir != null) {
            symbols.put(Symbols.WORK_DIRECTORY, workDirectory.getAbsolutePath());
        } else {
            symbols.put(Symbols.WORK_DIRECTORY, null);
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        // increment instance run count
        runCount++;

        // Initialize the context
        contextInit();

        // Open the log manager with the current transform context
        if (logManager != null) {
            logManager.open(getContext());
        }

        Log.info("Engine '" + getName() + "' (" + getInstanceId() + ") running...");
        int transactionErrors = 0;
        Log.trace("Engine '" + getName() + "' starting transform");

        // fire the transformation start event
        getContext().start();

        // Execute all the pre-processing tasks
        preprocess();

        try {
            // If pre-processing completed without error, start opening the rest of the
            // tooling
            if (getContext().isNotInError()) {

                // If the reader is not null, open the core components using this context
                // to share data. If the reader is null, there is no need to open the
                // mapper and the writer
                if (reader != null) {
                    readerInit();
                    mapperInit();
                    aggregatorInit();
                    writerInit();
                }

                filterInit();
                validatorInit();
                transformInit();

                // run the preload reader and pass read-in frames to components to
                // prime them with historic records
                preLoad();

                Log.trace("Engine '" + getName() + "' entering read loop");

                // loop through all data read in by the reader until EOF or an error in
                // the transform context occurs.
                getContext().setState("Process");
                while (getContext().isNotInError() && reader != null && !reader.eof()) {

                    // Create a new Transaction context with the list of listeners to react
                    // to events in the transaction.
                    TransactionContext txnContext = new TransactionContext(getContext());

                    // place a reference to the transaction in the transform context
                    getContext().setTransaction(txnContext);

                    // Create a component to place in the Templates to give them access to
                    // all the data in the contexts and advanced functions
                    TemplateAccess access = new TemplateAccess(getContext());
                    Template.putStatic("Context", access);

                    // Start the clock and fire event listeners for the beginning of the
                    // transaction
                    txnContext.start();
                    txnContext.setState("Read");

                    // Read a frame into the given context (source frame)
                    DataFrame retval = reader.read(txnContext);

                    // Sometimes readers read empty lines and the like, skip null dataframes
                    if (retval != null) {

                        // Set the returned dataframe into the transaction context
                        txnContext.setSourceFrame(retval);
                        getContext().setRow(++currentFrameNumber);
                        getContext().getSymbols().put(Symbols.CURRENT_FRAME, currentFrameNumber);
                        getContext().getSymbols().put(Symbols.LAST_FRAME, txnContext.isLastFrame());
                        txnContext.fireRead(txnContext, reader);

                        filter(txnContext);

                        // If the working frame did not get filtered out...
                        if (txnContext.getWorkingFrame() != null) {
                            validate(txnContext);
                            if (txnContext.isNotInError()) {
                                transform(txnContext);
                                map(txnContext);
                                if (aggregators.size() > 0) {
                                    aggregateAndwrite(txnContext);
                                } else {
                                    write(txnContext);
                                }
                            } // passed validators
                        } // passed filters

                        // Now end the transaction which should fire any context listeners
                        txnContext.end();

                        if (txnContext.isInError()) {
                            transactionErrors++;
                        }

                    } // if something was read in

                } // Reader !eof and context is without error

            } // transformContext ! err after pre-processing

            if (transactionErrors > 0) {
                getContext().setError("Transform experienced " + transactionErrors + " transaction errors");
            }

            Log.trace("Engine '" + getName() + "' reads completed - Error=" + getContext().isInError() + " Reads=" + getContext().getRow() + " TxnErrors=" + transactionErrors);

            if (getContext().isInError()) {
                reportTransformContextError(getContext());
            } else {

                // close any internal components like readers and writers which may
                // interfere with post-processing tasks from completing properly
                closeInternalComponents();

                getContext().setState("Post-Process");

                // Execute all the post-processing tasks
                for (TransformTask task : postProcesses) {
                    try {
                        if (task.isEnabled()) {
                            task.open(getContext());
                            task.execute();
                        }
                    } catch (TaskException e) {
                        getContext().setError(e.getMessage());
                    }
                }

                // Close all the tasks after post-processing is done regardless of outcome
                for (TransformTask task : postProcesses) {
                    try {
                        if (task.isEnabled()) {
                            task.close();
                        }
                    } catch (IOException e) {
                        Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_postprocess_task", task.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
                    }
                }
                getContext().setState("Complete");

                if (getContext().isInError()) {
                    reportTransformContextError(getContext());
                }
            }
        } finally {
            // signal the end of the context
            getContext().end();

            // close all the tooling, it will be re-opened when we run the next time (if scheduled)
            closeTooling();

            // reset the frame pointer
            currentFrameNumber = 0;
        }

        if (getContext().isInError()) {
            String msg = "Engine '" + getName() + "' (" + getInstanceId() + ") completed with errors: " + getContext().getErrorMessage();
            Log.info(msg);
            if( getLoader() != null){
                loader.getStats().createEvent(getSymbolTable().getString(Symbols.APPID),getSymbolTable().getString(Symbols.SYSID),getSymbolTable().getString(Symbols.CMPID),msg, AppEvent.MAJOR,0,0,"Engine");
            }
        } else {
            Log.info("Engine '" + getName() + "' (" + getInstanceId() + ") completed successfully");
        }

        // Close the loggers associated with this job
        if (logManager != null) {
            try {
                logManager.close();
            } catch (Throwable t) {
                System.err.println("Problems closing job logger(s): " + t.getMessage());
            }
        }
    }

    /**
     * Read in historic data to prime (preload) components so they can base
     * their calculations based on previous / historic frames.
     */
    private void preLoad() {
        getContext().setState("Preload");
        if (preloader != null) {
            preloader.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
            } else {
                while (getContext().isNotInError() && !preloader.eof()) {
                    TransactionContext txnContext = new TransactionContext(getContext());
                    DataFrame frame = preloader.read(txnContext);
                    preloadListeners(frame);
                    preloadTransformers(frame);
                }
            }
        }
    }

    /**
     * @param frame
     */
    private void preloadTransformers(DataFrame frame) {
        for (FrameTransform transformer : transformers) {
            try {
                transformer.preload(frame);
            } catch (Exception e) {
                StringBuilder b = new StringBuilder();
                if (StringUtil.isNotBlank(getContext().getErrorMessage())) {
                    b.append(getContext().getErrorMessage());
                }
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(transformer.getClass().getSimpleName());
                b.append(": ");
                b.append(e.getMessage());
                getContext().setError(b.toString());
            }
        }
        if (getContext().isInError()) {
            Log.debug("TRANSFORM PRELOAD ERRORS: " + getContext().getErrorMessage());
        }
    }

    /**
     * @param frame
     */
    private void preloadListeners(DataFrame frame) {
        for (ContextListener listener : listeners) {
            try {
                listener.preload(frame);
            } catch (Exception e) {
                StringBuilder b = new StringBuilder();
                if (StringUtil.isNotBlank(getContext().getErrorMessage())) {
                    b.append(getContext().getErrorMessage());
                }
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(listener.getClass().getSimpleName());
                b.append(": ");
                b.append(e.getMessage());
                getContext().setError(b.toString());
            }
        }
        if (getContext().isInError()) {
            Log.debug("LISTENER PRELOAD ERRORS: " + getContext().getErrorMessage());
        }
    }

    /**
     * Initialize the transform context
     */
    @SuppressWarnings("unchecked")
    @Override
    public TransformContext contextInit() {

        symbols.put(Symbols.JOB_ID, getInstanceId());

        // figure out our job directory
        determineJobDirectory();

        // prime the symbol table with the run date/time
        Date rundate = setRunDate();

        // Make sure the engine has a name
        if (StringUtil.isBlank(getName())) {
            setName(instanceId);
        }

        symbols.put(Symbols.JOB_NAME, getName());

        // Throw an error if there are writers but no reader
        if (reader == null && writers.size() > 0) {
            throw new IllegalStateException("No reader configured, nothing to write");
        }

        // Make sure we have a context
        initContext();

        // get the command line arguments from the symbol table and post the array
        // in the context for other components to use
        getCommandLineArguments();

        // set our list of listeners in the context
        getContext().setListeners(listeners);

        // Set this engine as the context's engine
        getContext().setEngine(this);

        // Set the symbol table to the one this engine uses
        getContext().setSymbols(symbols);

        // Open / initialize the context - This is done before the listeners are
        // opened so the listeners can be opened within an initialized context and
        // have their configuration arguments resolved. The trade-off is that
        // listeners will never have their open event fired on the opening of the
        // Transform Context, only Transaction Contexts.
        getContext().open();

        // Open all the listeners first so the transform context will trigger
        initListeners();

        // Set the run date in the context after it was opened (possibly loaded)
        getContext().set(Symbols.DATETIME, rundate);

        return getContext();
    }

    /**
     *
     */
    private void initContext() {
        if (getContext() == null) {
            // Create a transformation context for components to share data
            setContext(new TransformContext());
        } else {
            // reset the context in case it was used previously
            getContext().reset();

            // place any existing context variables in the symbol table
            for (String name : getContext().getKeys()) {
                symbols.put(name, getContext().getAsString(name));
                Log.debug("Populating symbols with existing context property '" + name + "', value = '" + getContext().getAsString(name) + "'");
            }
        }
    }

    /**
     *
     */
    private void initListeners() {
        for (ContextListener listener : listeners) {
            listener.open(getContext());
            if (getContext().isInError()) {
                getContext().setState("Listener Initialization Error");
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void preprocess() {
        getContext().setState("Pre-Process");
        for (TransformTask task : preProcesses) {
            try {
                if (task.isEnabled()) {
                    task.open(getContext());
                    task.execute();
                }
            } catch (TaskException e) {
                getContext().setError(e.getMessage());
                break;
            }
        }
        // Close all the tasks after pre-processing is done regardless of outcome
        for (TransformTask task : preProcesses) {
            try {
                if (task.isEnabled()) {
                    task.close();
                }
            } catch (IOException e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_preprocess_task", task.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     *
     */
    private void readerInit() {
        getContext().setState("Reader Init");
        reader.open(getContext());
        if (getContext().isInError()) {
            reportTransformContextError(getContext());
            return;
        }
    }

    /**
     *
     */
    private void transformInit() {
        getContext().setState("Transform Init");
        for (FrameTransform transformer : transformers) {
            transformer.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void validatorInit() {
        getContext().setState("Validator Init");
        for (FrameValidator validator : validators) {
            validator.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void filterInit() {
        getContext().setState("Filter Init");
        for (FrameFilter filter : filters) {
            filter.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void writerInit() {
        getContext().setState("Writer Init");
        for (FrameWriter writer : writers) {
            writer.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void aggregatorInit() {
        getContext().setState("Aggregator Init");
        for (FrameAggregator aggregator : aggregators) {
            aggregator.open(getContext());
            if (getContext().isInError()) {
                reportTransformContextError(getContext());
                return;
            }
        }
    }

    /**
     *
     */
    private void mapperInit() {
        getContext().setState("Mapper Init");
        if (mapper == null) {
            Log.debug("No mapper defined...using default settings");
            mapper = new DefaultFrameMapper();
        }
        mapper.open(getContext());
        if (getContext().isInError()) {
            reportTransformContextError(getContext());
            return;
        }
    }

    /**
     * Filter out working frames.
     *
     * @param txnContext the transaction context containing the data to filter
     */
    private void filter(TransactionContext txnContext) {
        txnContext.setState("Filter");
        for (FrameFilter filter : filters) {
            if (filter.isEnabled()) {
                if (!filter.process(txnContext)) {
                    // filter signaled to discontinue filter checks (early exit)
                    break;
                }
                if (txnContext.getWorkingFrame() == null) {
                    // no need to continue, the working record was removed from
                    // the transaction context
                    break;
                }
            }
        }
    }

    /**
     * Validate the data in the transaction context.
     *
     * @param txnContext the transaction context containing the data to validate
     */
    private void validate(TransactionContext txnContext) {
        // pass it through the validation rules
        txnContext.setState("Validate");
        boolean passed = true;
        List<String> errors = new ArrayList<String>();
        for (FrameValidator validator : validators) {
            try {
                if (!validator.process(txnContext)) {
                    passed = false;
                    String error = validator.getDescription();
                    if (StringUtil.isBlank(error)) {
                        error = validator.getClass().getName();
                    }
                    errors.add(error);
                }
            } catch (ValidationException e) {
                txnContext.setError(e.getMessage());
            }
        }

        // if there were validation errors
        if (!passed) {
            StringBuffer b = new StringBuffer("Validation errors:");
            for (int x = 0; x < errors.size(); x++) {
                b.append(errors.get(x));
                if (x + 1 < errors.size()) {
                    b.append(", ");
                }
            }
            txnContext.setError(b.toString());
            getContext().fireFrameValidationFailed(txnContext);
        }
    }

    /**
     * Transform the working frame contained in the given transaction context
     * with the currently configured transformers.
     *
     * @param txnContext the transaction context containing the data to transform
     */
    private void transform(TransactionContext txnContext) {
        txnContext.setState("Transform");
        // Pass the working frame through the transformers
        for (FrameTransform transformer : transformers) {
            try {
                // Have the transformer process the frame
                DataFrame resultFrame = transformer.process(txnContext.getWorkingFrame());

                // place the results of the transformation in the context
                txnContext.setWorkingFrame(resultFrame);

            } catch (Exception e) {
                StringBuilder b = new StringBuilder();
                b.append(transformer.getClass().getSimpleName());
                b.append(": ");
                b.append(e.getMessage());
                if (e instanceof NullPointerException) {
                    b.append("\n");
                    b.append(ExceptionUtil.stackTrace(e));
                }
                txnContext.setError(b.toString());
            }
        }
        if (txnContext.isInError()) {
            Log.error("TRANSFORM ERRORS: " + txnContext.getErrorMessage());
        }
    }

    /**
     * Map the working frame to the target frame.
     *
     * @param txnContext the transaction context containing the data to map
     */
    private void map(TransactionContext txnContext) {
        // Pass it through the mapper - only the required fields should
        // exist in the target frame after the mapper is done.
        if (txnContext.isNotInError()) {
            txnContext.setState("Map");
            // Map / Move fields from the working to the target frame
            try {
                mapper.process(txnContext);
                txnContext.fireMap(txnContext);
            } catch (MappingException e) {
                txnContext.setError(e.getMessage());
            }
        }
    }

    /**
     * Pass the target frame in the given transaction context to the aggregators
     * and write out any frames the aggregators emit.
     *
     * @param txnContext the transaction context containing the data to
     *                   aggregate and write
     */
    private void aggregateAndwrite(TransactionContext txnContext) {
        // Aggregators process each frame and emit a frame to pass on to the next
        // aggregator and the writers.
        if (txnContext.isNotInError() && aggregators.size() > 0) {
            txnContext.setState("Aggregation");
            List<DataFrame> frames = new ArrayList<DataFrame>();
            frames.add(txnContext.getTargetFrame());
            for (FrameAggregator aggregator : aggregators) {
                if (frames != null) {
                    try {
                        // process the frames emitted from the previous aggregators
                        frames = aggregator.process(frames, txnContext);
                    } catch (Exception e) {
                        Log.error(LogMsg.createMsg(CDX.MSG, "Engine.aggregation_error", e.getClass().getSimpleName(), e.getMessage(), ExceptionUtil.stackTrace(e)));
                        e.printStackTrace();
                        txnContext.setError(e.getMessage());
                        frames = null;
                    } // try-catch
                } // if frame !null
            } // for each aggregator

            // write each of the frames
            for (DataFrame frame : frames) {
                txnContext.setTargetFrame(frame);
                write(txnContext);
            }

        } // if no errors and aggregators exist
    }

    /**
     * Write the given transaction context to all the writers.
     *
     * @param txnContext the transaction context containing the data to write
     */
    private void write(TransactionContext txnContext) {
        if (txnContext.isNotInError() && txnContext.getTargetFrame() != null && writers.size() > 0) {
            txnContext.setState("Write");
            // Pass the frame to all the enabled writers
            for (FrameWriter writer : writers) {
                if (writer.isEnabled()) {
                    try {
                        // Write the target (new) frame
                        writer.write(txnContext.getTargetFrame());
                        txnContext.fireWrite(txnContext, writer);
                    } catch (Exception e) {
                        Log.error(LogMsg.createMsg(CDX.MSG, "Engine.write_error", e.getClass().getSimpleName(), e.getMessage(), ExceptionUtil.stackTrace(e)));
                        e.printStackTrace();
                        txnContext.setError(e.getMessage());
                    }
                } else {
                    if (Log.isLogging(Log.DEBUG_EVENTS)) {
                        Log.notice(LogMsg.createMsg(CDX.MSG, "Engine.writer_skipped_disabled", writer.getClass().getSimpleName()));
                    }
                }
            }
        }
    }

    /**
     * Retrieves a list of command line arguments as set in the symbol table and
     * places them in the context.
     *
     * <p>If there are no command line arguments, then there will be nothing
     * placed in the context.
     */
    private void getCommandLineArguments() {
        List<String> list = new ArrayList<String>();

        // retrieve a list of command line arguments from the symbol table in order
        for (int x = 0; x < 1024; x++) {
            Object obj = getSymbolTable().get(Symbols.COMMAND_LINE_ARG_PREFIX + x);
            if (obj != null) {
                list.add(obj.toString());
            } else {
                break; // ran out of arguments
            }
        }

        if (list.size() > 0) {
            getContext().set(ContextKey.COMMAND_LINE_ARGS, list.toArray(new String[list.size()]));
        }
    }

    /**
     * Determines the proper value for this engine's working directory.
     * <p>
     * If there is no working directory set, use DX home
     * and create a working directory within it.
     */
    private void determineJobDirectory() {
        File jobDir;

        if (getJobDirectory() == null) {

            if (getWorkDirectory() == null) {
                Log.debug("The work directory is not set, calculating...");
                determineWorkDirectory();
            }
            Log.debug("Job directory will be set in the working directory of " + getWorkDirectory().getAbsolutePath());

            String name = getName();
            if (StringUtil.isNotBlank(name)) {
                Log.debug("Using a job name of \"" + name + "\" to determine the job directory.");

                // replace the illegal filename characters by only allowing simple names
                String dirname = name.trim().replaceAll("[^a-zA-Z0-9.-]", "_");
                jobDir = new File(getWorkDirectory(), dirname);

                try {
                    jobDir.mkdirs();
                    setJobDirectory(jobDir);
                    setWorkDirectory(jobDir.getParentFile());
                    Log.debug(LogMsg.createMsg(CDX.MSG, "Engine.calculated_job_directory", jobDir.getAbsolutePath(), getName()));
                } catch (final Exception e) {
                    Log.error(e.getMessage());
                }
            } else {
                Log.debug("Unnamed jobs must use the current directory");
                jobDir = new File(System.getProperty("user.dir"));
                setJobDirectory(jobDir);
                setWorkDirectory(jobDir);
                Log.debug("Set both the Job directory and the working directory to " + jobDir.getAbsolutePath());
            }
        }
    }

    /**
     * Determines the proper work directory for this job.
     *
     * <p>This is normally the {@code wrk} directory under APP_HOME, but can be
     * located in the same directory as the configuration file particularly if
     * the file is located outside the {@code cfg} directory, also under
     * APP_HOME.
     */
    private void determineWorkDirectory() {
        File workDir;

        // Make sure we have a home directory
        if (StringUtil.isBlank(System.getProperty(Loader.APP_HOME))) {
            System.setProperty(Loader.APP_HOME, System.getProperty("user.dir"));
            Log.debug("APP.HOME is not set, setting it to the current directory: " + System.getProperty(Loader.APP_HOME));
        }

        // Make sure we have a work directory
        if (StringUtil.isBlank(System.getProperty(Job.APP_WORK))) {
            System.setProperty(Job.APP_WORK, System.getProperty(Loader.APP_HOME) + System.getProperty("file.separator") + "wrk");
            Log.debug("APP.WORK is not set, setting it to " + System.getProperty(Job.APP_WORK));
        }
        workDir = new File(System.getProperty(Job.APP_WORK));
        try {
            workDir.mkdirs();
            setWorkDirectory(workDir);
            Log.debug(LogMsg.createMsg(CDX.MSG, "Engine.calculated_work_directory", workDir.getAbsolutePath(), getName()));
        } catch (final Exception e) {
            Log.error(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    protected Date setRunDate() {

        // Place date and time values in the symbol table
        Calendar cal = Calendar.getInstance();

        Date rundate = new Date();
        if (rundate != null) {
            cal.setTime(rundate);

            symbols.put(Symbols.DATE, formatDate(rundate));
            symbols.put(Symbols.TIME, formatTime(rundate));
            symbols.put(Symbols.DATETIME, formatDateTime(rundate));

            // duplicates the above, but in the case of persistent contexts, make for more readable configuration files when combined with PREVIOUS_RUN_*
            symbols.put(Symbols.CURRENT_RUN_DATE, formatDate(rundate));
            symbols.put(Symbols.CURRENT_RUN_TIME, formatTime(rundate));
            symbols.put(Symbols.CURRENT_RUN_DATETIME, formatTime(rundate));
            symbols.put(Symbols.CURRENT_RUN_MILLIS, rundate.getTime());
            symbols.put(Symbols.CURRENT_RUN_SECONDS, rundate.getTime() / 1000);

            symbols.put(Symbols.MONTH, String.valueOf(cal.get(Calendar.MONTH) + 1));
            symbols.put(Symbols.DAY, String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            symbols.put(Symbols.YEAR, String.valueOf(cal.get(Calendar.YEAR)));
            symbols.put(Symbols.HOUR, String.valueOf(cal.get(Calendar.HOUR)));
            symbols.put(Symbols.MINUTE, String.valueOf(cal.get(Calendar.MINUTE)));
            symbols.put(Symbols.SECOND, String.valueOf(cal.get(Calendar.SECOND)));
            symbols.put(Symbols.MILLISECOND, String.valueOf(cal.get(Calendar.MILLISECOND)));
            symbols.put(Symbols.MONTH_MM, StringUtil.zeropad(cal.get(Calendar.MONTH) + 1, 2));
            symbols.put(Symbols.DAY_DD, StringUtil.zeropad(cal.get(Calendar.DAY_OF_MONTH), 2));
            symbols.put(Symbols.YEAR_YYYY, StringUtil.zeropad(cal.get(Calendar.YEAR), 4));
            symbols.put(Symbols.HOUR_24, StringUtil.zeropad(cal.get(Calendar.HOUR_OF_DAY), 2));
            symbols.put(Symbols.HOUR_HH, StringUtil.zeropad(cal.get(Calendar.HOUR), 2));
            symbols.put(Symbols.MINUTE_MM, StringUtil.zeropad(cal.get(Calendar.MINUTE), 2));
            symbols.put(Symbols.SECOND_SS, StringUtil.zeropad(cal.get(Calendar.SECOND), 2));
            symbols.put(Symbols.MILLISECOND_ZZZ, StringUtil.zeropad(cal.get(Calendar.MILLISECOND), 3));

            // go back to midnight
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            symbols.put(Symbols.SECONDS_PAST_MIDNIGHT, StringUtil.zeropad((rundate.getTime() - cal.getTimeInMillis()) / 1000, 5));

            // go to tomorrow midnight
            cal.add(Calendar.DATE, +1);
            symbols.put(Symbols.SECONDS_TILL_MIDNIGHT, StringUtil.zeropad((cal.getTimeInMillis() - rundate.getTime()) / 1000, 5));

            // reset to todays date/time
            cal.setTime(rundate);

            // go back one day and get the "previous day" Month Day and Year
            cal.add(Calendar.DATE, -1);
            symbols.put(Symbols.PREV_MONTH_PM, StringUtil.zeropad(cal.get(Calendar.MONTH) + 1, 2));
            symbols.put(Symbols.PREV_DAY_PD, StringUtil.zeropad(cal.get(Calendar.DAY_OF_MONTH), 2));
            symbols.put(Symbols.PREV_YEAR_PYYY, StringUtil.zeropad(cal.get(Calendar.YEAR), 4));

            // reset to today's date/time
            cal.setTime(rundate);

            // go back a month
            cal.add(Calendar.MONTH, -1);
            symbols.put(Symbols.PREV_MONTH_LM, StringUtil.zeropad(cal.get(Calendar.MONTH) + 1, 2));
            symbols.put(Symbols.PREV_YEAR_LMYY, StringUtil.zeropad(cal.get(Calendar.YEAR), 4));
        }
        return rundate;
    }

    protected void reportTransformContextError(TransformContext context) {
        Log.error(context.getState() + " Error - " + context.getErrorMessage());
    }

    /**
     * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
     */
    @Override
    public void open(TransformContext context) {
        transformContext = context;
    }

    /**
     * @see coyote.dx.Component#getContext()
     */
    @Override
    public TransformContext getContext() {
        return transformContext;
    }

    @Override
    public void setContext(TransformContext context) {
        transformContext = context;
    }

    /**
     * Close all the components.
     */
    private void closeTooling() {
        closeReader();
        closeWriters();
        closeMapper();
        closeFilters();
        closeValidators();
        closeTransformers();
        closeListeners();
    }

    /**
     * Close the reader.
     */
    private void closeReader() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_reader", reader.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Close all the writers.
     */
    private void closeWriters() {
        for (FrameWriter writer : writers) {
            try {
                writer.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_writer", writer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Close the mapper.
     */
    private void closeMapper() {
        if (mapper != null) {
            try {
                mapper.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_mapper", mapper.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Close all the filters.
     */
    private void closeFilters() {
        for (FrameFilter filter : filters) {
            try {
                filter.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_filter", filter.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Close all the validators.
     */
    private void closeValidators() {
        for (FrameValidator validator : validators) {
            try {
                validator.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_validator", validator.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * close all the transformers.
     */
    private void closeTransformers() {
        for (FrameTransform transformer : transformers) {
            try {
                transformer.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_transformer", transformer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Close all the listeners.
     */
    private void closeListeners() {
        for (ContextListener listener : listeners) {
            try {
                listener.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_listener", listener.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {

        // close the connections in the transform contexts
        if (getContext() != null) {
            getContext().close();
        }

        if (logManager != null)
            logManager.close();

    }

    /**
     * Close any internal components like readers and writers which may interfere
     * with post-processing tasks from completing properly such as moving or
     * deleting files.
     */
    private void closeInternalComponents() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_reader", reader.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }

        for (FrameWriter writer : writers) {
            try {
                writer.close();
            } catch (Exception e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Engine.problems_closing_writer", writer.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * @see coyote.dx.TransformEngine#setPreloader(coyote.dx.FrameReader)
     */
    public void setPreloader(FrameReader reader) {
        this.preloader = reader;
    }

    /**
     * @see coyote.dx.TransformEngine#addWriter(coyote.dx.FrameWriter)
     */
    @Override
    public void addWriter(FrameWriter writer) {
        writers.add(writer);
    }

    /**
     * @see coyote.dx.TransformEngine#addAggregator(coyote.dx.FrameAggregator)
     */
    @Override
    public void addAggregator(FrameAggregator aggregator) {
        aggregators.add(aggregator);
    }

    /**
     * @see coyote.dx.TransformEngine#addListener(coyote.dx.context.ContextListener)
     */
    @Override
    public void addListener(ContextListener listener) {
        listeners.add(listener);
    }

    /**
     * @see coyote.dx.TransformEngine#getMapper()
     */
    @Override
    public FrameMapper getMapper() {
        return mapper;
    }

    /**
     * @see coyote.dx.TransformEngine#setMapper(coyote.dx.FrameMapper)
     */
    @Override
    public void setMapper(FrameMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @see coyote.dx.TransformEngine#getReader()
     */
    @Override
    public FrameReader getReader() {
        return reader;
    }

    /**
     * @see coyote.dx.TransformEngine#setReader(coyote.dx.FrameReader)
     */
    @Override
    public void setReader(FrameReader reader) {
        this.reader = reader;
    }

    /**
     * @see coyote.dx.TransformEngine#getName()
     */
    @Override
    public String getName() {
        return configuration.getString(ConfigTag.NAME);
    }

    /**
     * @see coyote.dx.TransformEngine#setName(java.lang.String)
     */
    @Override
    public void setName(String value) {
        configuration.put(ConfigTag.NAME, value);
    }

    /**
     * @see coyote.dx.TransformEngine#getSymbolTable()
     */
    @Override
    public SymbolTable getSymbolTable() {
        return symbols;
    }


    /**
     * @see coyote.dx.TransformEngine#addPreProcessTask(coyote.dx.TransformTask)
     */
    @Override
    public int addPreProcessTask(TransformTask task) {
        if (task != null) {
            preProcesses.add(task);
            return preProcesses.size() - 1;
        } else {
            return 0;
        }
    }


    /**
     * @see coyote.dx.TransformEngine#addPostProcessTask(coyote.dx.TransformTask)
     */
    @Override
    public int addPostProcessTask(TransformTask task) {
        if (task != null) {
            postProcesses.add(task);
            return postProcesses.size() - 1;
        } else {
            return 0;
        }
    }


    /**
     * @see coyote.dx.TransformEngine#addValidator(coyote.dx.FrameValidator)
     */
    @Override
    public void addValidator(FrameValidator validator) {
        if (validator != null) {
            validators.add(validator);
        }
    }


    /**
     * @see coyote.dx.TransformEngine#addTransformer(coyote.dx.FrameTransform)
     */
    @Override
    public void addTransformer(FrameTransform transformer) {
        if (transformer != null) {
            transformers.add(transformer);
        }
    }


    /**
     * Called by the Loader when the runtime terminates to signal long-running
     * processes to terminate and to force any clean-up.
     *
     * <p>This is part of the Loader life cycle and not a part of the DX life
     * cycle. It will always be called when the JRE terminates even after normal
     * termination of the transform engine. You shouldn't need to call this.</p>
     *
     * <p>This is a good place to set a shutdown flag in the main loop so that
     * the job can terminate cleanly when the user presses [ctrl-c] or the
     * process is signaled to terminate via the operating system such as SIG HUP
     * or the host is shutting down.</p>
     *
     * @see coyote.dx.TransformEngine#shutdown()
     */
    @Override
    public void shutdown() {
        // not called
    }


    /**
     * @see coyote.dx.TransformEngine#shutdown(coyote.dataframe.DataFrame)
     */
    @Override
    public void shutdown(DataFrame params) {
        // not called
    }

    /**
     * @see coyote.dx.TransformEngine#getLogManager()
     */
    @Override
    public LogManager getLogManager() {
        return logManager;
    }

    /**
     * @see coyote.dx.TransformEngine#setLogManager(coyote.dx.LogManager)
     */
    @Override
    public void setLogManager(LogManager logmgr) {
        logManager = logmgr;
    }

    /**
     * @see coyote.dx.TransformEngine#addFilter(coyote.dx.FrameFilter)
     */
    @Override
    public int addFilter(FrameFilter filter) {
        if (filter != null) {
            filters.add(filter);
            return filters.size() - 1;
        } else {
            return 0;
        }
    }


    /**
     * @see coyote.dx.TransformEngine#getInstanceId()
     */
    @Override
    public String getInstanceId() {
        return instanceId;
    }


    /**
     * @see coyote.dx.TransformEngine#getLoader()
     */
    @Override
    public Loader getLoader() {
        return loader;
    }


    /**
     * @see coyote.dx.TransformEngine#setLoader(coyote.loader.Loader)
     */
    @Override
    public void setLoader(Loader loader) {
        this.loader = loader;
    }


    /**
     * @return the number of times this instance has been run.
     */
    @Override
    public long getInstanceRunCount() {
        return runCount;
    }

    /**
     * @return the list of pre-processing tasks
     */
    @Override
    public List<TransformTask> getPreprocessTasks() {
        return preProcesses;
    }

    /**
     * @return the list of post-processing tasks.
     */
    @Override
    public List<TransformTask> getPostprocessTasks() {
        return postProcesses;
    }

    /**
     * @return the preloader used to prime the transform engine.
     */
    @Override
    public FrameReader getPreloader() {
        return preloader;
    }

    /**
     * @return the filters used to limit frames processed.
     */
    @Override
    public List<FrameFilter> getFilters() {
        return filters;
    }

    /**
     * @return the currently set list of validation rules.
     */
    @Override
    public List<FrameValidator> getValidators() {
        return validators;
    }

    /**
     * @return the list of currently set frame transformers.
     */
    @Override
    public List<FrameTransform> getTransformers() {
        return transformers;
    }

    /**
     * @return the aggregators used to alter the output.
     */
    @Override
    public List<FrameAggregator> getAggregators() {
        return aggregators;
    }

    /**
     * @return the currently set listeners.
     */
    @Override
    public List<ContextListener> getListeners() {
        return listeners;
    }


    /**
     * @return a list of writers for this engine
     */
    @Override
    public List<FrameWriter> getWriters() {
        return writers;
    }

}

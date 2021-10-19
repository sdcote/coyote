/*
 * Copyright (c) 2020 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.CDX;
import coyote.dx.FrameTransform;
import coyote.dx.TransformException;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.util.ArrayList;
import java.util.List;


/**
 * This is an example of a custom transform. It converts a dataframe received
 * from an Alert Manager webhook call into a frame format suitable for sending
 * to Cisco WebEx Teams.
 *
 * <p>Using this transform as a template, you can craft simple jobs that perform
 * complex transformations on data read from a variety of sources.</p>
 */
public class AlertManager2Markdown extends AbstractFieldTransform implements FrameTransform {

    private static final String MESSAGE = "markdown";

    private static final String STATUS_FIELD = "status";
    private static final String COMMON_LABELS = "commonLabels";
    private static final String ALERT_NAME_FIELD = "alertname";
    private static final String INSTANCE_FIELD = "instance";
    private static final String MONITOR_FIELD = "monitor";
    private static final String SEVERITY_FIELD = "severity";
    private static final String COMMON_ANNOTATIONS = "commonAnnotations";
    private static final String SUMMARY_FIELD = "summary";
    private static final String STARTS_AT_FIELD = "startsAt";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String GENERATOR_FIELD = "generatorURL";
    private static final String ALERTS = "alerts";

    private static final String LABELS = "labels";
    private static final String ANNOTATIONS = "annotations";
    private static final String UNSPECIFIED = "unspecified";
    private static final String UNKNOWN = "unknown";


    /**
     * @see AbstractFieldTransform#open(TransformContext)
     */
    @Override
    public void open(final TransformContext context) {
        super.open(context);

        // TODO our stuff


    }


    /**
     * @see FrameTransform#process(DataFrame)
     */
    @Override
    public DataFrame process(final DataFrame frame) throws TransformException {

        DataFrame retval = frame;

        // If there is a conditional expression
        if (getExpression() != null) {
            try {
                // if the condition evaluates to true
                if (evaluator.evaluateBoolean(getExpression())) {
                    retval = generateMessageFrom(frame); // generate the message
                }
            } catch (final IllegalArgumentException e) {
                Log.warn(LogMsg.createMsg(CDX.MSG, "Transform.Set_boolean_evaluation_error", e.getMessage()));
            }
        } else {
            retval = generateMessageFrom(frame); // unconditionally generate the message
        }
        return retval;
    }

    /**
     * Generate a Markdown formatted message from the given AlertManager
     * dataframe.
     *
     * @param frame The data frame received from an alert manager
     * @return a dataframe for sending to a WebEx Teams webhook.
     */
    private DataFrame generateMessageFrom(DataFrame frame) {
        DataFrame retval = new DataFrame();

        // Take data from the frame and generate the message
        DataFrame commonAnnotations = getCommonAnnotations(frame);
        DataFrame commonLabels = getCommonLabels(frame);
        List<Alert> alerts = getAlerts(frame);

        StringBuffer buffer = new StringBuffer("# ");
        buffer.append(alerts.size());
        if (alerts.size() == 1)
            buffer.append(" alert is ");
        else
            buffer.append(" alerts are ");
        buffer.append(frame.getAsString(STATUS_FIELD));
        if (commonLabels.contains(MONITOR_FIELD)) {
            buffer.append(" in ");
            buffer.append(commonLabels.getAsString(MONITOR_FIELD));
        }
        buffer.append("\n");
        for (Alert alert : alerts) {
            buffer.append("\n");
            buffer.append(render(alert));
        }

        retval.put(MESSAGE, buffer.toString());
        return retval;
    }

    /**
     * This renders the Alert into a MarkDow formatted string.
     *
     * <p>Override this method to change the formatting.</p>
     *
     * @param alert the alert to render.
     * @return MarkDown text representing the given alert.
     */
    protected String render(Alert alert) {
        StringBuffer retval = new StringBuffer("**");
        retval.append(alert.getName());
        retval.append("**");
        String source = alert.getGeneratorUrl();
        if (StringUtil.isNotEmpty(source)) {
            retval.append(" - [source](");
            retval.append(source);
            retval.append(")");
        }
        retval.append("  \n");
        retval.append(alert.getSummary());
        retval.append("  \n**Severity:** ");
        retval.append(alert.getSeverity());
        retval.append("  \n**Status:** ");
        retval.append(alert.getStatus());
        retval.append("  \n**Instance:** ");
        retval.append(alert.getInstance());
        retval.append("  \n**Timestamp:** ");
        retval.append(alert.getTimestamp());
        retval.append("  \n");
        retval.append(alert.getDescription());
        retval.append("  \n");
        return retval.toString();
    }

    private List<Alert> getAlerts(DataFrame frame) {
        List<Alert> retval = new ArrayList<>();
        try {
            DataFrame alerts = frame.getAsFrame(ALERTS);
            for (int x = 0; x < alerts.getFieldCount(); x++) {
                try {
                    DataFrame alertFrame = alerts.getAsFrame(x);
                    if( alertFrame != null ) {
                        retval.add(new Alert(alertFrame));
                    }
                } catch (DataFrameException e) {
                    Log.warn("Could not retrieve individual alert: " + e.getLocalizedMessage() + "\n" + JSONMarshaler.toFormattedString(alerts));
                }
            }
        } catch (DataFrameException e) {
            Log.warn("Could not retrieve alerts: " + e.getLocalizedMessage() + "\n" + JSONMarshaler.toFormattedString(frame));
        }
        return retval;
    }

    private DataFrame getCommonLabels(DataFrame frame) {
        return getFrame(COMMON_LABELS, frame);
    }

    private DataFrame getCommonAnnotations(DataFrame frame) {
        return getFrame(COMMON_ANNOTATIONS, frame);
    }

    private DataFrame getFrame(String name, DataFrame frame) {
        DataFrame retval;
        try {
            retval = frame.getAsFrame(name);
        } catch (DataFrameException e) {
            retval = new DataFrame();
        }
        return retval;
    }

    /**
     * Simple class to encapsulate data retrieval.
     */
    private static class Alert {
        DataFrame source;
        DataFrame labels;
        DataFrame annotations;

        Alert(DataFrame frame) {
            if (Log.isLogging(Log.DEBUG)) Log.debug("Parsing alert from:\r\n" + JSONMarshaler.toFormattedString(frame));
            if (frame != null) {
                source = frame;
                try {
                    labels = frame.getAsFrame(LABELS);
                } catch (DataFrameException e) {
                    labels = new DataFrame();
                }
                try {
                    annotations = frame.getAsFrame(ANNOTATIONS);
                } catch (DataFrameException e) {
                    annotations = new DataFrame();
                }
            } else {
                Log.warn(this.getClass().getSimpleName() + " - No source frame provided to Alert constructor");
            }
            // prevent NPE by making sure there are frames to query
            if( labels == null) labels = new DataFrame();
            if( annotations == null) annotations = new DataFrame();
            if( source == null) source = new DataFrame();
            if (Log.isLogging(Log.DEBUG)) Log.debug("Alert Source:\r\n" + JSONMarshaler.toFormattedString(source)+"\r\nAlert labels:\r\n" + JSONMarshaler.toFormattedString(labels)+"\r\nAlert Annotations:\r\n" + JSONMarshaler.toFormattedString(annotations)+"\r\n");
        }

        public String getSeverity() {
            String retval = labels.getAsString(SEVERITY_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNKNOWN;
            return retval;
        }

        public String getName() {
            String retval = labels.getAsString(ALERT_NAME_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNSPECIFIED;
            return retval;
        }

        public String getSummary() {
            String retval = annotations.getAsString(SUMMARY_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNSPECIFIED;
            return retval;
        }

        public String getInstance() {
            String retval = labels.getAsString(INSTANCE_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNKNOWN;
            return retval;
        }

        public String getTimestamp() {
            String retval = source.getAsString(STARTS_AT_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNSPECIFIED;
            return retval;
        }

        public String getDescription() {
            String retval = annotations.getAsString(DESCRIPTION_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNSPECIFIED;
            return retval;
        }

        public String getGeneratorUrl() {
            String retval = source.getAsString(GENERATOR_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNKNOWN;
            return retval;
        }

        public String getStatus() {
            String retval = source.getAsString(STATUS_FIELD);
            if (StringUtil.isBlank(retval)) retval = UNKNOWN;
            return retval;
        }
    }
}

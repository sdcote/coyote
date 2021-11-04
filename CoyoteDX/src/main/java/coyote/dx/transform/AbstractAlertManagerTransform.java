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
import coyote.dx.ConfigTag;
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
 *
 * <p>The following is an example of an AlertManager alert:</p>
 * <pre>
 * {
 * 	"receiver": "webhook",
 * 	"status": "firing",
 * 	"alerts": [
 *                {
 * 			"status": "firing",
 * 			"labels": {
 * 				"alertname": "JenkinsTooManyPluginsNeedUpate",
 * 				"instance": "ecdp3:8088",
 * 				"job": "jenkins",
 * 				"monitor": "ECD",
 * 				"severity": "notify"
 *            },
 * 			"annotations": {
 * 				"description": "ecdp3:8088 has 91 plugins that require an update",
 * 				"summary": "Too many plugin updates"
 *            },
 * 			"startsAt": "2021-11-02T16:46:43.509022477Z",
 * 			"endsAt": "0001-01-01T00:00:00Z",
 * 			"generatorURL": "some URL here",
 * 			"fingerprint": "5fc9538c8cade3ed"
 *        }
 * 	],
 * 	"groupLabels": [
 * 	],
 * 	"commonLabels": {
 * 		"alertname": "JenkinsTooManyPluginsNeedUpate",
 * 		"instance": "ecdp3:8088",
 * 		"job": "jenkins",
 * 		"monitor": "ECD",
 * 		"severity": "notify"* 	},
 * 	"commonAnnotations": {
 * 		"description": "ecdp3:8088 has 91 plugins that require an update",
 * 		"summary": "Too many plugin updates"* 	},
 * 	"externalURL": "some URL here",
 * 	"version": "4",
 * 	"groupKey": "{}:{}",
 * 	"truncatedAlerts": 0
 * }
 * </pre>
 */
public abstract class AbstractAlertManagerTransform extends AbstractFieldTransform implements FrameTransform {

    protected static final String DEFAULT_FIELD_NAME = "markdown";
    protected static final String STATUS_FIELD = "status";
    protected static final String COMMON_LABELS = "commonLabels";
    protected static final String ALERT_NAME_FIELD = "alertname";
    protected static final String INSTANCE_FIELD = "instance";
    protected static final String MONITOR_FIELD = "monitor";
    protected static final String SEVERITY_FIELD = "severity";
    protected static final String COMMON_ANNOTATIONS = "commonAnnotations";
    protected static final String SUMMARY_FIELD = "summary";
    protected static final String STARTS_AT_FIELD = "startsAt";
    protected static final String DESCRIPTION_FIELD = "description";
    protected static final String GENERATOR_FIELD = "generatorURL";
    protected static final String ALERTS = "alerts";
    protected static final String LABELS = "labels";
    protected static final String ANNOTATIONS = "annotations";
    protected static final String UNSPECIFIED = "unspecified";
    protected static final String UNKNOWN = "unknown";

    private String targetFieldName = DEFAULT_FIELD_NAME;

    /**
     * @return the name of the field into which the markdown is placed.
     */
    protected String getTargetFieldName() {
        return targetFieldName;
    }

    /**
     * @param name the name of the field into which the message is to be placed.
     */
    protected void setTargetFieldName(String name) {
        this.targetFieldName = name;
    }


    /**
     * @see AbstractFieldTransform#open(TransformContext)
     */
    @Override
    public void open(final TransformContext context) {
        super.open(context);
        // TODO our stuff

        final String targetField = getString(ConfigTag.TARGET);
        if (StringUtil.isNotEmpty(targetField)) {
            setTargetFieldName(targetField);
        }
    }


    /**
     * @see FrameTransform#process(DataFrame)
     */
    @Override
    public DataFrame process(final DataFrame frame) throws TransformException {
        DataFrame retval = frame;
        if (getExpression() != null) {
            try {
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
     * Generate a formatted message from the given AlertManager dataframe.
     *
     * @param frame The data frame received from an alert manager
     * @return a dataframe for sending to a webhook.
     */
    abstract DataFrame generateMessageFrom(DataFrame frame);


    /**
     * Get a list of alerts from the frame.
     *
     * @param frame The frame from the AlertManager
     * @return a List of Alert instances
     */
    protected List<Alert> getAlerts(DataFrame frame) {
        List<Alert> retval = new ArrayList<>();
        try {
            DataFrame alerts = frame.getAsFrame(ALERTS);
            for (int x = 0; x < alerts.getFieldCount(); x++) {
                try {
                    DataFrame alertFrame = alerts.getAsFrame(x);
                    if (alertFrame != null) {
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


    protected DataFrame getCommonLabels(DataFrame frame) {
        return getFrame(COMMON_LABELS, frame);
    }


    protected DataFrame getCommonAnnotations(DataFrame frame) {
        return getFrame(COMMON_ANNOTATIONS, frame);
    }


    protected DataFrame getFrame(String name, DataFrame frame) {
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
     *
     * <p>The following is an example of what an individual alert looks like:</p>
     * <pre>
     * {
     * 	   "status": "firing",
     * 	   "labels": {
     * 	       "alertname": "JenkinsTooManyPluginsNeedUpate",
     * 	        "instance": "ecdp3:8088",
     * 	        "job": "jenkins",
     * 	        "monitor": "ECD",
     * 	        "severity": "notify"
     *     },
     * 	   "annotations": {
     * 	       "description": "ecdp3:8088 has 91 plugins that require an update",
     *         "summary": "Too many plugin updates"
     *     },
     *     "startsAt": "2021-11-02T16:46:43.509022477Z",
     *     "endsAt": "0001-01-01T00:00:00Z",
     *     "generatorURL": "some URL here",
     *     "fingerprint": "5fc9538c8cade3ed"
     * }
     * </pre>
     */
    protected static class Alert {
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
            if (labels == null) labels = new DataFrame();
            if (annotations == null) annotations = new DataFrame();
            if (source == null) source = new DataFrame();
            if (Log.isLogging(Log.DEBUG))
                Log.debug("Alert Source:\r\n" + JSONMarshaler.toFormattedString(source) + "\r\nAlert labels:\r\n" + JSONMarshaler.toFormattedString(labels) + "\r\nAlert Annotations:\r\n" + JSONMarshaler.toFormattedString(annotations) + "\r\n");
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

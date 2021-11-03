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
import coyote.dx.FrameTransform;
import coyote.dx.context.TransformContext;

import java.util.List;


/**
 * This is an example of a custom transform. It converts a dataframe received
 * from an Alert Manager webhook call into a frame format suitable for sending
 * to Cisco WebEx Teams.
 *
 * <p>Using this transform as a template, you can craft simple jobs that perform
 * complex transformations on data read from a variety of sources.</p>
 *
 * <p>This is an example of a configuration:</p>
 * <pre>
 * "Transform": {
 *     "AlertManager2Markdown": { "target": "message"}
 * },
 * </pre>
 */
public class AlertManager2Markdown extends AbstractAlertManagerTransform implements FrameTransform {

    /**
     * @see AbstractFieldTransform#open(TransformContext)
     */
    @Override
    public void open(final TransformContext context) {
        super.open(context);
        // TODO our stuff
    }


    /**
     * Generate a Markdown formatted message from the given AlertManager
     * dataframe.
     *
     * @param frame The data frame received from an alert manager
     * @return a dataframe for sending to a WebEx Teams webhook.
     */
    @Override
    protected DataFrame generateMessageFrom(DataFrame frame) {
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

        retval.put(getTargetFieldName(), buffer.toString());
        return retval;
    }


    /**
     * This renders the Alert into a MarkDown formatted string.
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

}

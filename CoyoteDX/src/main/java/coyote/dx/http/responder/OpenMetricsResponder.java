/*
 * Copyright (c) 2020 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.Service;
import coyote.i13n.StatBoard;
import coyote.loader.log.Log;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;


/**
 * This responder reports the health of the service via OpenMetrics.
 *
 * <p>This is the endpoint Prometheus systems can scrape for data about this service.
 */
public class OpenMetricsResponder extends AbstractCoyoteResponder implements Responder {
    private static final String GAUGE_TYPE = "gauge";
    private static final String COUNTER_TYPE = "counter";

    private static final String VM_AVAIL_MEM = "available_memory";
    private static final String VM_CURR_HEAP = "current_heap";
    private static final String VM_FREE_HEAP = "free_heap";
    private static final String VM_FREE_MEM = "free_memory";
    private static final String VM_HEAP_PCT = "heap_percentage";
    private static final String VM_MAX_HEAP = "max_heap_size";
    private static final String CMPNT_COUNT = "component_count";
    private static final String JOB_COUNT = "job_count";

    private static void writeEscapedHelp(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\"':
                    writer.append("\\\"");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    private String generateMetrics(Service service) {
        StatBoard statboard = service.getStats();

        Writer writer = new StringWriter();

        Map<String, String> labels = new HashMap<>();
        try {
            writeGauge(writer, VM_AVAIL_MEM, statboard.getAvailableMemory(), "memory available to the VM less the total memory currently allocated for the heap", "bytes", labels);
            writeGauge(writer, VM_CURR_HEAP, statboard.getCurrentHeapSize(), "memory currently in use by the heap", "bytes", labels);
            writeGauge(writer, VM_FREE_HEAP, statboard.getFreeHeapSize(), "n approximation of the total amount of memory currently available on the heap for newly allocated objects", "bytes", labels);
            writeGauge(writer, VM_FREE_MEM, statboard.getFreeMemory(), "amount of memory that can be allocated prior to running out of memory in the VM", "bytes", labels);
            writeGauge(writer, VM_MAX_HEAP, statboard.getMaxHeapSize(), "amount of memory that the virtual machine will attempt to use", "bytes", labels);
            writeGauge(writer, VM_HEAP_PCT, statboard.getHeapPercentage(), "percentage of the maximum memory the currently allocated heap occupies", "percent", labels);
            writeGauge(writer, CMPNT_COUNT, service.getComponentCount(), "the number of components currently loaded", "components", labels);
            writeGauge(writer, JOB_COUNT, service.getJobCount(), "the number of jobs currently loaded", "jobs", labels);
            writer.write("# EOF");
        } catch (Throwable e) {
            Log.error("OpenMetricsResponder could not write string");
        }

        return writer.toString();
    }

    private void writeGauge(Writer writer, String name, float value, String description, String units, Map<String, String> labels) throws IOException {
        writeType(writer, name, GAUGE_TYPE);
        writeUnits(writer, name, units);
        writeHelp(writer, name, description);
        writeValue(writer, name, labels, Float.toString(value));
    }

    private void writeGauge(Writer writer, String name, long value, String description, String units, Map<String, String> labels) throws IOException {
        writeType(writer, name, GAUGE_TYPE);
        writeUnits(writer, name, units);
        writeHelp(writer, name, description);
        writeValue(writer, name, labels, Long.toString(value));
    }

    private void writeValue(Writer writer, String name, Map<String, String> labels, String value) throws IOException {
        writer.append(name);
        writer.append(" ");

        Set<String> names = labels.keySet();
        Iterator<String> it = labels.keySet().iterator();
        if (it.hasNext()) {
            writer.write("{ ");
            while (it.hasNext()) {
                String labelName = it.next();
                writer.write(labelName);
                writer.write("=\"");
                writeEscapedLabelValue(writer, labels.get(labelName));
                writer.write("\"");
                if (it.hasNext()) writer.write(",");
            }
            writer.write("} ");
        }
        writer.write(value);
        writer.append("\n");
    }

    private void writeType(Writer writer, String name, String type) throws IOException {
        if (StringUtil.isNotBlank(type.trim())) {
            writer.append("# TYPE ");
            writer.append(name);
            writer.write(' ');
            writeEscapedHelp(writer, type.trim().trim());
            writer.append("\n");
        }
    }

    private void writeHelp(Writer writer, String name, String description) throws IOException {
        if (StringUtil.isNotBlank(description.trim())) {
            writer.append("# HELP ");
            writer.append(name);
            writer.write(' ');
            writeEscapedHelp(writer, description.trim().trim());
            writer.append("\n");
        }
    }

    private void writeUnits(Writer writer, String name, String units) throws IOException {
        if (StringUtil.isNotBlank(units.trim())) {
            writer.append("# UNIT ");
            writer.append(name);
            writer.write(' ');
            writeEscapedHelp(writer, units.trim().trim());
            writer.append("\n");
        }
    }

    @Override
    public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
        Service service = resource.initParameter(0, Service.class);
        return Response.createFixedLengthResponse(getStatus(), MimeType.TEXT.getType(), generateMetrics(service));
    }

}

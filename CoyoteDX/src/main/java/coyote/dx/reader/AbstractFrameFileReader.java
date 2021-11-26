/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import coyote.commons.FileUtil;
import coyote.commons.LineIterator;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.*;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * THis reads in the entire text file into memory and iterates over each line.
 *
 * <p><b>This is for small-ish files only.</b> For larger files use the AbstractFrameStreamFileReader.</p>
 */
public abstract class AbstractFrameFileReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {
    LineIterator lines = null;


    @Override
    public void open(TransformContext context) {
        super.open(context);

        String source = getString(ConfigTag.SOURCE);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));

        if (StringUtil.isNotBlank(source)) {
            File sourceFile = null;
            URI uri = UriUtil.parse(source);
            if (uri != null) {
                sourceFile = UriUtil.getFile(uri);
                if (sourceFile != null) {
                    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath()));
                } else {
                    // try again with the file scheme in case they forgot
                    uri = UriUtil.parse("file://" + source);
                    sourceFile = UriUtil.getFile(uri);
                    if (sourceFile != null) {
                        Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath()));
                    } else {
                        String msg = LogMsg.createMsg(CDX.MSG, "Reader.source_uri_not_file", getClass().getName(), source).toString();
                        Log.error(msg);
                        context.setError(msg);
                    }
                }
            } else {
                sourceFile = new File(source);
                Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_file", sourceFile.getAbsolutePath()));
            }

            // if not absolute, use the CDX fixture to attemt to resolve the relative file
            if (!sourceFile.isAbsolute()) {
                sourceFile = CDX.resolveFile(sourceFile, getContext());
            }

            if (sourceFile.exists() && sourceFile.canRead()) {
                lines = FileUtil.lineIterator(sourceFile);
            } else {
                String msg = LogMsg.createMsg(CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), sourceFile.getAbsolutePath()).toString();
                Log.error(msg);
                context.setError(msg);
            }
        } else {
            String msg = LogMsg.createMsg(CDX.MSG, "Reader.no_source_specified", getClass().getName()).toString();
            Log.error(msg);
            context.setError(msg);
        }

        // Now setup our field definitions
        DataFrame fieldcfg = getFrame(ConfigTag.FIELDS);
        if (fieldcfg != null) {

            // flag to trim values
            boolean trim = true;

            List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

            for (DataField field : fieldcfg.getFields()) {
                try {
                    DataFrame fielddef = (DataFrame) field.getObjectValue();

                    // determine if values should be trimmed = defaults to true
                    trim = true;
                    if (fielddef.containsIgnoreCase(ConfigTag.TRIM)) {
                        try {
                            trim = fielddef.getAsBoolean(ConfigTag.TRIM);
                        } catch (Exception e) {
                            trim = true;
                        }
                    }

                    fields.add(new FieldDefinition(field.getName(), fielddef.getAsInt(ConfigTag.START), fielddef.getAsInt(ConfigTag.LENGTH), fielddef.getAsString(ConfigTag.TYPE), fielddef.getAsString(ConfigTag.FORMAT), trim));
                } catch (Exception e) {
                    context.setError("Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage());
                    return;
                }
            }
            Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.configured_field_definitions", fields.size()));
        } else {
            DataFrame selectorcfg = getFrame(ConfigTag.LINEMAP);
            if (selectorcfg != null) {
                Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.line_map_configured"));
            } else {
                context.setError("There are no fields or line map configured in the reader");
                return;
            }
        }
    }


    /**
     * @return true if there are no more lines to read
     */
    @Override
    public boolean eof() {
        return !lines.hasNext();
    }

}

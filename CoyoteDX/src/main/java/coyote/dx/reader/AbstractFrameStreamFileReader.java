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

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 *
 */
public abstract class AbstractFrameStreamFileReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {
protected BufferedReader reader = null;


    /**
     *
     * @param context The transformation context in which this component should be opened.
     */
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

            // if not absolute, use the CDX fixture to attempt to resolve the relative file
            if (!sourceFile.isAbsolute()) {
                sourceFile = CDX.resolveFile(sourceFile, getContext());
            }

            if (sourceFile.exists() && sourceFile.canRead()) {
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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

    }


    /**
     * @return true if there are no more lines to read
     */
    @Override
    public boolean eof(){
        boolean retval = true;
        if(reader != null){
            try {
                retval = !reader.ready();
            } catch (IOException e) {
                retval = true;
            }
        } else {
            retval = true;
        }
        return retval;
    }







    /**
     * @return the URI representing the source from which data is to be read
     */
    public String getSource() {
        return configuration.getAsString(ConfigTag.SOURCE);
    }




    /**
     * Set the URI representing the source from which data is to be read.
     *
     * @param value The URI from which data is to be read.
     */
    public void setSource(String value) {
        configuration.put(ConfigTag.SOURCE, value);
    }



}

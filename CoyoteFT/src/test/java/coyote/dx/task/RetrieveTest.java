/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;


/**
 *
 */
public class RetrieveTest {

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    }


    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // cleanup the work directories
        //FileUtil.deleteDirectory(new File("RetrieveTest"));
    }


    @Test
    public void base() throws ConfigurationException, TaskException, IOException {
        final TransformContext context = new TransformContext();

        final Config cfg = new Config();
        cfg.put(ConfigTag.SOURCE, "scp://username:password@hostname.example.com/var/log/httpd/access_log");
        cfg.put(ConfigTag.TARGET, "access.log");
        Log.info("\"Retrieve\":" + cfg);

        try (Retrieve task = new Retrieve()) {
            task.setConfiguration(cfg);
            task.open(context);
        }

    }

}

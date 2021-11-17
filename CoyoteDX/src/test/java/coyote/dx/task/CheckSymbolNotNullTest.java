/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.commons.template.SymbolTable;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;


/**
 * Unit tests for the CheckSymbolNotNull task
 */
public class CheckSymbolNotNullTest {
    private static final TransformContext context = new TransformContext();


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
        context.setSymbols(new SymbolTable());
    }


    @Test
    public void normalUseCase() throws ConfigurationException, TaskException, IOException {
        Config cfg = new Config();
        cfg.put(ConfigTag.SYMBOL, "ship.name");
        //System.out.println(cfg);

        context.getSymbols().clear();
        context.getSymbols().put("ship.name", "Rocinante");

        try (CheckSymbolNotNull task = new CheckSymbolNotNull()) {
            task.setConfiguration(cfg);
            task.open(context);
            task.execute();
        }
    }


    @Test
    public void missingUseCase() throws ConfigurationException, TaskException, IOException {
        Config cfg = new Config();
        cfg.put(ConfigTag.SYMBOL, "ship.name");
        context.getSymbols().clear();
        try (CheckSymbolNotNull task = new CheckSymbolNotNull()) {
            task.setConfiguration(cfg);
            task.open(context);
            task.execute();
            Assert.fail("Task should have thrown an exception for missing symbol");
        } catch (TaskException e) {
            // System.out.println(e.getMessage());
        }
    }


    @Test
    public void missingConfigParam() throws ConfigurationException, TaskException, IOException {
        Config cfg = new Config();
        context.getSymbols().clear();
        try (CheckSymbolNotNull task = new CheckSymbolNotNull()) {
            task.setConfiguration(cfg);
            Assert.fail("Task should have thrown an exception for missing configuration element");
        } catch (ConfigurationException e) {
            // System.out.println(e.getMessage());
        }
    }


    @Test
    public void blankConfigParam() throws ConfigurationException, TaskException, IOException {
        Config cfg = new Config();
        cfg.put(ConfigTag.SYMBOL, " ");
        context.getSymbols().clear();
        try (CheckSymbolNotNull task = new CheckSymbolNotNull()) {
            task.setConfiguration(cfg);
            Assert.fail("Task should have thrown an exception for blank configuration element");
        } catch (ConfigurationException e) {
            // System.out.println(e.getMessage());
        }
    }


    @Test
    public void emptyConfigParam() throws ConfigurationException, TaskException, IOException {
        Config cfg = new Config();
        cfg.put(ConfigTag.SYMBOL, "");
        context.getSymbols().clear();
        try (CheckSymbolNotNull task = new CheckSymbolNotNull()) {
            task.setConfiguration(cfg);
            Assert.fail("Task should have thrown an exception for empty configuration element");
        } catch (ConfigurationException e) {
            // System.out.println(e.getMessage());
        }
    }

}

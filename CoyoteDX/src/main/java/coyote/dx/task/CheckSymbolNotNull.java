/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.LogMsg;

/**
 * Designed to abort a job if a symbol is missing from the symbol table of the
 * transformation context.
 */
public class CheckSymbolNotNull extends AbstractTransformTask {


    /**
     * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
     */
    @Override
    public void setConfiguration(Config cfg) throws ConfigurationException {
        super.setConfiguration(cfg);

        if (cfg.contains(ConfigTag.SYMBOL)) {
            if (StringUtil.isBlank(cfg.getString(ConfigTag.SYMBOL))) {
                throw new ConfigurationException(LogMsg.createMsg(CDX.MSG, "Task.checksymbolnotnull.empty_cfg_param", getClass().getSimpleName(), ConfigTag.SYMBOL).toString());
            }
            // we have a symbol name to check
        } else {
            throw new ConfigurationException(LogMsg.createMsg(CDX.MSG, "Task.checksymbolnotnull.missing_cfg_param", getClass().getSimpleName(), ConfigTag.SYMBOL).toString());
        }

    }


    /**
     * @return the name of the symbol to check.
     */
    private String getSymbolName() {
        String retval = null;
        if (getConfiguration().containsIgnoreCase(ConfigTag.SYMBOL)) {
            retval = getConfiguration().getString(ConfigTag.SYMBOL);
        }
        return retval;
    }


    /**
     * @see coyote.dx.task.AbstractTransformTask#performTask()
     */
    @Override
    protected void performTask() throws TaskException {
        if (StringUtil.isEmpty(getContext().getSymbols().getString(getSymbolName())))
            throw new TaskException(LogMsg.createMsg(CDX.MSG, "Task.checksymbolnotnull.symbol_is_empty", getSymbolName()).toString());
    }

}

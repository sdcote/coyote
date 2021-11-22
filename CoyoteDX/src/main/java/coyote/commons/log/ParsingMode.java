/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.log;

import coyote.commons.StringUtil;


/**
 * The supported data types for database columns.
 */
public enum ParsingMode {
    /**
     * Apache HTTP Server
     */
    APACHE("Apache"),
    /**
     * FreeForm
     */
    FREE_FORM("FreeForm");

    /**
     * The name of this mode
     */
    private final String name;


    /**
     * Create a parsing more with the given name
     */
    ParsingMode(final String name) {
        this.name = name;
    }

    /**
     * Get the parsing mode by name.
     *
     * @param name name of the mode to return
     * @return the parsing mode with the given mane or null if not found.
     */
    public static ParsingMode getModeByName(final String name) {
        ParsingMode retval = FREE_FORM;
        if (name != null) {
            for (final ParsingMode type : ParsingMode.values()) {
                if (StringUtil.equalsIgnoreCase(name, type.toString())) {
                    retval = type;
                    break;
                }
            }
        }
        return retval;
    }

    public String getName() {
        return name;
    }


    /**
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

}

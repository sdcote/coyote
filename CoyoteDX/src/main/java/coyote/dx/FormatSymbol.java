/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;

/**
 * This is a class that is intended to be placed in Symbol tables for the purposes of formatting symbols in that table.
 */
public class FormatSymbol {
    private final SymbolTable symbols;

    /**
     * A constructor that takes the symbol table to process.
     *
     * @param symbols the Symbol table from which the symbols will be retrieved.
     */
    public FormatSymbol(SymbolTable symbols) {
        this.symbols = symbols;
    }


    /**
     * Format the symbol with the given name as all upper case letters.
     *
     * @param symbolName the symbol to retrieve.
     * @return the value of the named symbol in all upper case or an empty string if the symbol does not exist or if the
     * symbol name was empty or null.
     */
    public String toUpper(String symbolName) {
        if (!StringUtil.isNotEmpty(symbolName)) {
            return symbols.getString(symbolName).toUpperCase();
        } else {
            return "";
        }
    }


    /**
     * Format the symbol with the given name as all lower case letters.
     *
     * @param symbolName the symbol to retrieve.
     * @return the value of the named symbol in all lower case or an empty string if the symbol does not exist or if the
     * symbol name was empty or null.
     */
    public String toLower(String symbolName) {
        if (!StringUtil.isNotEmpty(symbolName)) {
            return symbols.getString(symbolName).toLowerCase();
        } else {
            return "";
        }
    }


    /**
     * Format the symbol with the given name as a file URI.
     *
     * <p>It is assumed that the value is a path or a filename.</p>
     *
     * <p>If you are converting from a file path to a URI reference:</p>
     * <ul>
     * <li>Absolute paths from Unix-like filesystem (/etc/hosts): Use u3 notation file:///etc/hosts</li>
     * <li>Relative paths from Unix-like filesystem (../src/main/): Use relative reference ../src/main</li>
     * <li>Absolute paths from Windows filesystem (C:\Documents and Settings\): Use u3 notation file:///C:/Documents%20and%20Settings/</li>
     * <li>Relative paths from Windows filesystem (..\My Documents\test): Use relative reference ../My%20Documents/test</li>
     * <li>UNC paths from Windows (\\laptop\My Documents\Some.doc): Use u2 notation file://laptop/My%20Documents/Some.doc</li>
     * </ul>
     *
     * @param symbolName the symbol to retrieve.
     * @return the value of the named symbol as a file URI or an empty string if the symbol does not exist or if the
     * symbol name was empty or null.
     */
    public String toFileURI(String symbolName) {
        StringBuilder retval = new StringBuilder();
        if (StringUtil.isNotEmpty(symbolName)) {
            String pathname = symbols.getString(symbolName);
            if (StringUtil.isNotBlank(pathname)) {
                pathname = pathname.trim().replace(" ","%20");
                if (pathname.contains("\\")) {
                    // Assume Windows path
                    if (pathname.startsWith("\\\\")) {
                        // assume UNC Windows path
                        retval.append("file://");
                        pathname = pathname.substring(2);
                    } else if (pathname.charAt(1) == ':') {
                        // assume absolute Windows path
                        retval.append("file:///");
                    } else {
                        // assume relative Windows path
                    }
                    pathname = pathname.replace("\\","/");
                } else {
                    // Assume Unix path
                    if (pathname.charAt(0) == '/') {
                        // assume absolute Unix path
                        retval.append("file://");
                    } else {
                        // assume relative Unix path
                    }
                }
                retval.append(pathname);
            }
        }

        return retval.toString();
    }

}

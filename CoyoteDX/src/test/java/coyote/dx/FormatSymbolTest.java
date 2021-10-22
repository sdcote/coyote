/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 */
public class FormatSymbolTest {
    private static final SymbolTable SYMBOLS = new SymbolTable();

    /**
     * @throws java.lang.Exception like never
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Template.putStatic("FormatSymbol", new FormatSymbol(SYMBOLS));
        SYMBOLS.put("dosPath", "M:\\Users\\s180313\\Code\\Coyote\\Process\\ETL");
        SYMBOLS.put("unixPath", "/home/s180313/Code/Coyote/Process/ETL");
        SYMBOLS.put("dosFile", "M:\\Programs Files\\Docker\\Security Instructions.docx");

    }


    /**
     *
     */
    @Test
    public void testOne() {
        // Create a template which call a method on the object
        String text = "Format dospath [#FormatSymbol.toFileURI(dosPath)#] <<";

        // Resolve the text using the given symbols
        String formattedText = Template.resolve(text, SYMBOLS);
        // System.out.println(formattedText);
        assertEquals("Format dospath file:///M:/Users/s180313/Code/Coyote/Process/ETL <<", formattedText);

        text = "Format unixpath [#FormatSymbol.toFileURI(unixPath)#] <<";
        formattedText = Template.resolve(text, SYMBOLS);
        // System.out.println(formattedText);
        assertEquals("Format unixpath file:///home/s180313/Code/Coyote/Process/ETL <<", formattedText);

        text = "Format dosfile [#FormatSymbol.toFileURI(dosFile)#] <<";
        formattedText = Template.resolve(text, SYMBOLS);
        // System.out.println(formattedText);
        assertEquals("Format dosfile file:///M:/Programs%20Files/Docker/Security%20Instructions.docx <<", formattedText);

    }

}

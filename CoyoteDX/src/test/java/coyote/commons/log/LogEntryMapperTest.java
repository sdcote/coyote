/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.log;

import coyote.commons.eval.DoubleEvaluator;
import org.junit.Test;


/**
 *
 */
public class LogEntryMapperTest {

    @Test
    public void test() {
    }


    //@Test(expected = IllegalArgumentException.class)
    public void testInvalidBrackets() {
        new DoubleEvaluator().evaluate("[(0.5)+(0.5)]");
    }


}

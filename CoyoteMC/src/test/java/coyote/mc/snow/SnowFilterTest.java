/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

//import static org.junit.Assert.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class SnowFilterTest {


    @Test
    public void snowFilter() {
        SnowFilter filter = new SnowFilter();
        assertNotNull(filter);
    }


    /**
     *
     */
    @Test
    public void testAndFilter() {
        SnowFilter filter = new SnowFilter();
        assertNotNull(filter);
        assertTrue(filter.isEmpty());
        filter.and("caller_id", Predicate.IS, "12345")
                .and("active", Predicate.IS, "true")
                .or("name", Predicate.IS, "printer");

        assertFalse(filter.isEmpty());

        System.out.println(filter.toString());
        System.out.println(filter.toEncodedString());

    }


    /**
     *
     */
    @Test
    public void testLikeFilter() {
        SnowFilter filter = new SnowFilter();
        assertNotNull(filter);
        assertTrue(filter.isEmpty());
        filter.and("caller_id", Predicate.LIKE, "12345");
        assertFalse(filter.isEmpty());
        assertEquals("caller_idLIKE12345", filter.toString());
        assertEquals("caller_idLIKE12345", filter.toEncodedString());
    }


}

/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote;

import org.junit.Test;

import coyote.testdata.DataSet;
import coyote.testdata.DataFactory;
import coyote.testdata.GenderGenerator;
import coyote.testdata.StaticStringGenerator;
import coyote.testdata.credential.PasswordGenerator;
import coyote.testdata.credential.UserNameGenerator;
import coyote.testdata.format.CSVFormatter;
import coyote.testdata.format.Formatter;
import coyote.testdata.name.FirstNameGenerator;
import coyote.testdata.name.LastNameGenerator;
import coyote.testdata.name.MiddleInitialGenerator;
import coyote.testdata.network.EmailGenerator;


/**
 * 
 */
public class SimpleDataGenerator {

  /**
   * This shows how to use the API
   */
  @Test
  public void test() {

    // Create a DataSet 
    final DataSet table = DataFactory.createDataSet( 15 );

    // Add each of the columns with its associated generator
    table.addColumn( "FirstName", new FirstNameGenerator() );
    table.addColumn( "LastName", new LastNameGenerator() );
    table.addColumn( "MiddleInitial", new MiddleInitialGenerator() );
    table.addColumn( "Gender", new GenderGenerator() );

    // Generate the data and populate the dataset
    DataFactory.populate( table, 10 );

    // Create a formatter to write our data to a specific format
    final Formatter formatter = new CSVFormatter();

    // format the data into a CSV string 
    final String data = formatter.format( table );
    System.out.println( data );

  }

}

/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote;

import java.util.Calendar;

import org.junit.Test;

import coyote.testdata.DataFactory;
import coyote.testdata.DataSet;
import coyote.testdata.DateGenerator;
import coyote.testdata.FloatGenerater;
import coyote.testdata.GenderGenerator;
import coyote.testdata.GuidGenerator;
import coyote.testdata.LongGenerater;
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
public class ArrangeColumns {

  /**
   * This shows how to use the API
   */
  @Test
  public void test() {

    // Create a DataSet 
    final DataSet table = DataFactory.createDataSet( 15 );

    // The order is only slightly important. Some generators will key off the 
    // values of existing columns. For example, The First Name Generator might 
    // look to see if there is a gender set in the row and attempt to match the 
    // first name generated with the gender. The user name generator might try 
    // to find columns names FirstName and LastName or some variant thereof to 
    // generate a username which matches the name of the record. 
    table.addColumn( "Gender", new GenderGenerator() );
    table.addColumn( "FirstName", new FirstNameGenerator() );
    table.addColumn( "LastName", new LastNameGenerator() );
    table.addColumn( "MiddleInitial", new MiddleInitialGenerator() );
    table.addColumn( "Username", new UserNameGenerator() );
    table.addColumn( "Password", new PasswordGenerator() );
    table.addColumn( "eMail", new EmailGenerator() );
    table.addColumn( "License", new GuidGenerator() );
    table.addColumn( "Rating", new FloatGenerater( 5 ) );
    Calendar cal = Calendar.getInstance();
    cal.add( Calendar.YEAR, -10 );
    table.addColumn( "Joined", new DateGenerator( cal.getTime() ) );
    table.addColumn( "Visits", new LongGenerater( 0, 1000 ) );
    table.addColumn( "Role", new StaticStringGenerator( "USER", "USER", "USER", "USER", "USER", "USER", "USER", "USER", "USER", "USER", "USER", "USER", "ADMIN", "HOLDER", "HOLDER", "HOLDER" ) );

    // Generate the data a populate the dataset
    DataFactory.populate( table, 50 );

    // Now we have the opportunity to re-arrange the columns to match our 
    // output needs e.g. match the expected spreadsheet format for CSV data
    String[] columnOrder = { "Role", "FirstName", "LastName", "MiddleInitial", "Gender", "Username", "Password", "eMail", "MasterCode" };
    table.arrangeColumns( columnOrder );

    // Create a formatter to write our data to a specific format
    final Formatter formatter = new CSVFormatter();

    // format the data into a CSV string 
    final String data = formatter.format( table );
    System.out.println( data );

  }

}

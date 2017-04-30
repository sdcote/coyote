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

import org.junit.Test;

import coyote.testdata.DataSet;
import coyote.testdata.DataFactory;
import coyote.testdata.GenderGenerator;
import coyote.testdata.NullGenerator;
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
public class DependantRows {

  /**
   * This shows how to generate values from the previous generation of another 
   * dataset.
   * 
   * <p>In this example, we generate one row in the teacher dataset, and use 
   * the data in that row to populate one of the fields in the student 
   * dataset.</p>
   */
  @Test
  public void test() {

    // Create a DataSet 
    final DataSet table = DataFactory.createDataSet( 1 );

    table.addColumn( "Gender", new GenderGenerator() );
    table.addColumn( "FirstName", new FirstNameGenerator() );
    table.addColumn( "LastName", new LastNameGenerator() );
    table.addColumn( "MiddleInitial", new MiddleInitialGenerator() );
    table.addColumn( "Username", new UserNameGenerator() );
    table.addColumn( "Password", new PasswordGenerator() );
    table.addColumn( "eMail", new EmailGenerator() );
    table.addColumn( "ClassName", new StaticStringGenerator( "Reading" ) );
    table.addColumn( "ClassGrade", new StaticStringGenerator( "4" ) );
    table.addColumn( "SchoolName", new StaticStringGenerator( "Beacon Elementary School" ) );
    table.addColumn( "Role", new StaticStringGenerator( "TEACHER" ) );
    table.addColumn( "TeacherUsername", new NullGenerator() );

    // Generate the data a populate the dataset
    DataFactory.populate( table );

    // Now create a set of student data
    final DataSet studentTable = DataFactory.createDataSet( 5 );

    // get the user name of the teacher
    String teachername = table.getRow( 0 ).getStringValue( "Username" );

    studentTable.addColumn( "Gender", new GenderGenerator() );
    studentTable.addColumn( "FirstName", new FirstNameGenerator() );
    studentTable.addColumn( "LastName", new LastNameGenerator() );
    studentTable.addColumn( "MiddleInitial", new MiddleInitialGenerator() );
    studentTable.addColumn( "Username", new UserNameGenerator() );
    studentTable.addColumn( "Password", new PasswordGenerator() );
    studentTable.addColumn( "eMail", new EmailGenerator() );
    studentTable.addColumn( "ClassName", new StaticStringGenerator( "Reading Wonders" ) );
    studentTable.addColumn( "ClassGrade", new StaticStringGenerator( "4" ) );
    studentTable.addColumn( "SchoolName", new StaticStringGenerator( "Beacon Elementary School" ) );
    studentTable.addColumn( "Role", new StaticStringGenerator( "TEACHER" ) );
    studentTable.addColumn( "TeacherUsername", new NullGenerator() );
    studentTable.addColumn( "TeacherUsername", new StaticStringGenerator( teachername ) );

    DataFactory.populate( studentTable );

    // Add the rows from the student teacherTable to the Teacher teacherTable.
    table.add( studentTable );



    // Create a formatter to write our data to a specific format
    final Formatter formatter = new CSVFormatter();

    // format the data into a CSV string 
    final String data = formatter.format( table );
    System.out.println( data );

  }
}

/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.ui;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import coyote.TestingLoader;
import coyote.commons.Platform;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.cfg.Config;


/**
 * 
 */
public class Simple {

  static WebDriver driver = null;
static TestingLoader loader = null;



  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Config configuration = createConfig();
    System.out.println(configuration);
    
    loader = new TestingLoader();
    loader.configure(configuration);
    loader.background(); 
    
    
    File workDir = new File(System.getProperty("user.dir"));
    if (Platform.isWindows()) {
      System.setProperty("webdriver.chrome.driver", workDir.getAbsolutePath() + "\\lib\\driver\\chromedriver.exe");
    } else if (Platform.isLinux()) {
      System.setProperty("webdriver.chrome.driver", workDir.getAbsolutePath() + "/lib/driver/chromedriver");
    } else {
      System.out.println("Don't know how to handle '" + Platform.getOpSysName() + "'");
    }

    driver = new ChromeDriver();
  }




  /**
   * @return
   */
  private static Config createConfig() {
    DataFrame cfg = new DataFrame() //
        .set("Component",new DataFrame() //
            .set(new DataFrame().set("Class","coyote.loader.Wedge") ) // wedge
            ) // components
        .set("Logging",new DataFrame() //
            .set("StandardOutput", new DataFrame().set("categories","trace,debug,info,notice")) //
            .set("StandardError", new DataFrame().set("categories","warn,err,fatal")) //
            ) //
        ;
    System.out.println(JSONMarshaler.toFormattedString(cfg));
    return new Config(cfg);
  }
  
  




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (driver != null) {
      driver.quit();
    }
    
    if( loader !=null){
     loader.shutdown();
    }
  }




  //@Test
  public void test() {
    //driver.get("http://the-internet.herokuapp.com/login");
    //assertEquals(driver.getTitle(), "The Internet");
    
    driver.get("http://localhost:55290");
    assertEquals(driver.getTitle(), "Coyote");
  }

}

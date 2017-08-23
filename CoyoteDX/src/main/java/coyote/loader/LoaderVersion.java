/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import coyote.dx.CDX;


/**
 * This class is used to display astring representing the version of the 
 * components being loaded.
 */
public class LoaderVersion {
  private Map<String, String> modules = new HashMap<String, String>();




  /**
   * Look for the versions of all known projects
   */
  public LoaderVersion() {
    modules.put("Coyote  ", CDX.VERSION.toString());
    checkVersion("CoyoteFT", "coyote.dx.CFT", modules);
    checkVersion("CoyoteMT", "coyote.dx.CMT", modules);
    checkVersion("CoyoteMQ", "coyote.dx.CMQ", modules);
    checkVersion("CoyoteUI", "coyote.dx.CUI", modules);
    checkVersion("CoyoteWS", "coyote.dx.CWS", modules);
    checkVersion("CoyoteSN", "coyote.dx.CSN", modules);
  }




  /**
   * @param classname
   * @param map
   */
  private void checkVersion(String name, String classname, Map<String, String> map) {
    try {
      Class<?> clazz = Class.forName(classname);
      Constructor<?> ctor = clazz.getConstructor();
      Object obj = ctor.newInstance();
      Method m = clazz.getMethod("getVersion");
      String version = (String)m.invoke(obj);
      map.put(name, version);
    } catch (Exception e) {
      // expected when the JAR is not present
    }
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String> entry : modules.entrySet()) {
      b.append(entry.getKey());
      b.append(" ");
      b.append(entry.getValue());
      b.append("\n");
    }
    return b.toString();
  }

}

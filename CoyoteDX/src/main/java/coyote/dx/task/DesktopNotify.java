/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * A task which generates a log entry.
 */
public class DesktopNotify extends AbstractTransformTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
    if (!SystemTray.isSupported()) {
      throw new ConfigurationException("System tray not supported on this system");
    }
  }




  public String getMessage() {
    if (configuration.containsIgnoreCase(ConfigTag.MESSAGE)) {
      return configuration.getString(ConfigTag.MESSAGE);
    }
    return null;
  }




  public String getCategory() {
    if (configuration.containsIgnoreCase(ConfigTag.CATEGORY)) {
      return configuration.getString(ConfigTag.CATEGORY);
    }
    return null;
  }



  /**
   * @return
   */
  private String getSubtitle() {
    return "CDX Transfer Job";
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    String message = Template.resolve(getMessage(), getContext().getSymbols());
    String subtitle = getSubtitle();

    if (StringUtil.isNotBlank(message)) {
      String level = getCategory();
      if (StringUtil.isNotBlank(level)) {
        if (StringUtil.equalsIgnoreCase("info", level)) {
          displayTray(message, subtitle, MessageType.INFO);
        } else if (StringUtil.equalsIgnoreCase("warn", level)) {
          displayTray(message, subtitle, MessageType.WARNING);
        } else if (StringUtil.equalsIgnoreCase("error", level)) {
          displayTray(message, subtitle, MessageType.ERROR);
        } else if (StringUtil.equalsIgnoreCase("none", level)) {
          displayTray(message, subtitle, MessageType.NONE);
        } else {
          displayTray(message, subtitle, MessageType.NONE);
        }
      } else {
        displayTray(message, subtitle, MessageType.NONE);
      }
    }

  }



  public void displayTray(String message, String subtitle, MessageType type) {
    try {
      SystemTray tray = SystemTray.getSystemTray();
      Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/info.png"));
      TrayIcon trayIcon = new TrayIcon(image, "CDX Data Transfer Job Message");
      trayIcon.setImageAutoSize(true);
      trayIcon.setToolTip("CDX system tray icon");
      tray.add(trayIcon);
      trayIcon.displayMessage(message, subtitle, type);
    } catch (Exception e) {
      Log.warn("Problems sending system tray message: " + ExceptionUtil.stackTrace(e));
    }
  }




  public static void main(String[] args) throws AWTException {
    if (SystemTray.isSupported()) {
      new DesktopNotify().displayTray("Hello from CDX", "CDX test", MessageType.NONE);
    } else {
      System.err.println("System tray not supported!");
    }
  }

}

// $Id: Max7315LedsControl.nc 788 2006-05-04 19:47:54Z cory $
/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */

#include "Max7315.h"

/**
 * Controls the LED settings of the Maxim MAX7315 digital output driver.
 *
 * @author Joe Polastre, Moteiv Corporation <info@moteiv.com>
 */
interface Max7315LedsControl {

  /**
   * Set the configuration register of the MAX7315.
   */
  async command error_t setConfig(max7315_config_t config);
  /**
   * Notification that the configuration register has been set.
   */
  event void setConfigDone();
  /**
   * Get the value of the configuration register.
   */
  async command max7315_config_t getConfig();

  /**
   * Turns everything off for the lowest power consumption.
   * Turns off blink, if enabled, and turns off all LEDs.
   */
  async command error_t allOff();
  /**
   * Notification that everything is now off.
   */
  event void allOffDone();

  /**
   * Set on or off ports input/output configuration.
   */
  async command error_t set(uint8_t port, bool value);
  /**
   * Notification that the set operation has completed.
   */
  event void setDone();
  /**
   * Set all of the LED outputs at once.
   */
  async command error_t setAll(uint8_t value);
  /**
   * Notification that the setAll operation has completed.
   */
  event void setAllDone(uint8_t value);
  /**
   * Get the outputs configuration.
   */
  async command uint8_t get();

  /**
   * Set the Blink0 register for a particular port.
   */
  async command error_t setBlink0(uint8_t port, bool value);
  /**
   * Notification that the Blink0 register has been set.
   */
  event void setBlink0Done();
  /**
   * Set all of the Blink0 port values in the register.
   */
  async command error_t setBlinkAll0(uint8_t values);
  /**
   * Notification that the Blink0 register values have been set.
   */
  event void setBlinkAll0Done();
  /**
   * Returns the current value of the Blink0 register.
   */
  async command error_t getBlink0();

  /**
   * Set the Blink1 register for a particular port.
   */
  async command error_t setBlink1(uint8_t port, bool value);
  /**
   * Notification that the Blink1 register has been set.
   */
  event void setBlink1Done();
  /**
   * Set all of the Blink1 port values in the register.
   */
  async command error_t setBlinkAll1(uint8_t values);
  /**
   * Notification that the Blink1 register values have been set.
   */
  event void setBlinkAll1Done();
  /**
   * Returns the current value of the Blink1 register.
   */
  async command uint8_t getBlink1();

  /**
   * Set the global intensity register.
   */
  async command error_t setGlobalIntensity(uint8_t value);
  /**
   * Notification that the global intensity register has been set.
   */
  event void setGlobalIntensityDone();
  /** 
   * Get the value of the global intensity register.
   */
  async command uint8_t getGlobalIntensity();

  /**
   * Set the intensity for a specific port.
   */
  async command error_t setIntensity(uint8_t port, uint8_t value);
  /**
   * Notification that the intensity has been set for the specified port.
   */
  event void setIntensityDone();

}


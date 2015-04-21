// $Id: AD524X.nc 788 2006-05-04 19:47:54Z cory $
/*
 * "Copyright (c) 2000-2005 The Regents of the University  of California.
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 *
 */

/**
 * The AD524X interface provides commands for using the
 * Analog Devices AD524X family of potentiometers.
 * <p>
 * The lower 2 bits (AD1 and AD0) must be provided as the address.
 * The full address may be provided as well, but all other bits will be
 * stripped (addr = addr & 0x03)
 *
 * @author Joe Polastre, Moteiv Corporation <info@moteiv.com>
 */

#include "AD524X.h"

interface AD524X {
  /**
   * Start the AD5242 device.  This sets the SD bit to enable the device
   * via the I2C bus.  This command does not alter the physical shutdown
   * pin of the device.  The StdControl interface is responsible for
   * the physical shutdown of the device.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param type Type of address -- only some AD524X devices can be started
   * @return SUCCESS if the request was accepted
   */
  command error_t start(uint8_t addr, ad524x_type_t type);
  /**
   * Notification that there was an attempt to set the SD bit.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param result SUCCESS if the bit was actually set, FAIL if the
   *               device could not be reached or the operation failed
   * @param type Type of device
   */
  event void startDone(uint8_t addr, error_t result, ad524x_type_t type);

  /**
   * Stop the AD5242 device.  This clears the SD bit to enable the device
   * via the I2C bus.  This command does not alter the physical shutdown
   * pin of the device.  The StdControl interface is responsible for
   * the physical shutdown of the device.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param type Type of address -- only some AD524X devices can be stopped
   * @return SUCCESS if the request was accepted
   */
  command error_t stop(uint8_t addr, ad524x_type_t type);
  /**
   * Notification that there was an attempt to clear the SD bit.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param result SUCCESS if the bit was actually cleared, FAIL if the
   *               device could not be reached or the operation failed
   * @param type Type of device
   */
  event void stopDone(uint8_t addr, error_t result, ad524x_type_t type);

  /**
   * Set the value of one of the output pins.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param high TRUE if O should be set, FALSE if it should be cleared
   * @param output FALSE if O1 should be used, TRUE for 02
   * @param Type of device -- only some AD524X devices have outputs
   * @return SUCCESS if the request was accepted
   */
  command error_t setOutput(uint8_t addr, bool output, bool high, ad524x_type_t type);

  /**
   * Notification that the state of an output pin may have changed.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param output FALSE for O1, TRUE for O2
   * @param result SUCCESS if the output O was successfully changed
   * @param Type of device -- only some AD524X devices have outputs
   */
  event void setOutputDone(uint8_t addr, bool output, error_t result, ad524x_type_t type);

  /**
   * Get the value of an output pin.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param output FALSE for O1, TRUE for 02
   * @param type of device -- only some AD524X devices have outputs
   * @return TRUE if the bit is set, FALSE otherwise
   */
  command bool getOutput(uint8_t addr, bool output, ad524x_type_t type);

  /**
   * Set the value of an RDAC
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param rdac FALSE for RDAC1, TRUE for RDAC2 if exits
   *             parameter ignored if type != AD5242
   * @param value A 256-bit value corresponding to the wiper position
   *              For 128-bit pots, the value must be *left* justified
   *              (the LSB will be discarded)
   * @return SUCCESS if the request was accepted
   */
  command error_t setPot(uint8_t addr, bool rdac, uint8_t value, ad524x_type_t type);

  /**
   * Notification that RDAC may be set to a new value
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param SUCCESS if the value of RDAC was changed
   */
  event void setPotDone(uint8_t addr, bool rdac, error_t result, ad524x_type_t type);

  /**
   * Get the value of an RDAC
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param rdac FALSE for RDAC1, TRUE for RDAC2
   * @param type The device type
   * @return SUCCESS if the request was accepted
   */
  command error_t getPot(uint8_t addr, bool rdac, ad524x_type_t type);

  /**
   * Result of the get operation with the value of the RDAC potentiometer.
   *
   * @param addr Lower 2 bits (AD1,AD0) of the device I2C address
   * @param rdac The potentiometer that qas requested
   * @param value A 256-bit value corresponding to the wiper position
   *              For 128-bit potentiometers, value is left-justified
   *              (shifted left by 1 bit)
   * @param result SUCCESS if the value was correctly obtained from the
   *               device.  If FAIL is returned, the value is not valid.
   * @param type The type of the device
   */
  event void getPotDone(uint8_t addr, bool rdac, uint8_t value, error_t result, ad524x_type_t type);

}

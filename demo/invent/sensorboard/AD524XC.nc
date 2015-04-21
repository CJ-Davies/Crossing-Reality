// $Id: AD524XC.nc 788 2006-05-04 19:47:54Z cory $
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
 */

/**
 * AD524XC provides access to primitives for the AD524X family of
 * potentiometers. StdControl sets the physical hardware pin to turn
 * the device on or off via the shutdown pin (if supported by the
 * underlying platform).
 * <p>
 * You *must* define the "AD524X_SD" pin in an included file, presumably
 * your sensorboard.h file.  The AD524X_SD pin is used to put the device
 * into and out of shutdown when StdControl start() and stop() are called.
 * These functions:
 * <pre>
 *   TOSH_MAKE_AD524X_SD_OUTPUT()
 *   TOSH_MAKE_AD524X_SD_INPUT()
 *   TOSH_SET_AD524X_SD_PIN()
 *   TOSH_CLR_AD524X_SD_PIN()
 * </pre>
 * May be defined as empty functions for platforms that do not support the
 * AD524X shutdown pin.
 * <p>
 * The AD524X driver counts the number of users for systems with multiple
 * pots and only causes the physical pin to initiate a shutdown when all
 * users of the pot have called stop (in other words #start() == #stop())
 * <p>
 * It is recommended that you use the SD bit in the AD524X by calling
 * AD524X.start() and AD524X.stop() rather than toggling the actual
 * shutdown pin.  By setting the pin in the particular device, you can
 * ensure that device has been shutdown.
 *
 * @author Joe Polastre <info@moteiv.com>
 */

configuration AD524XC {
  provides {
    interface AD524X;
    interface SplitControl;
  }
}
implementation
{
  components AD524XM;
  components new Msp430I2CC() as I2CBus;
  
  AD524X = AD524XM;
  SplitControl = AD524XM;
  
  AD524XM.I2CPacket -> I2CBus;
  AD524XM.Resource  -> I2CBus;
  
  components LedsC as Leds;
  // components NoLedsC as Leds;
  AD524XM.Leds -> Leds;
}


// $Id: LedsM.nc 788 2006-05-04 19:47:54Z cory $
/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
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
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
/**
 * @author Jason Hill
 * @author David Gay
 * @author Philip Levis
 * @author Joe Polastre <info@moteiv.com>
 */
#include "sensorboard.h"

module LedsM {
  provides {
    interface Leds;
    interface Init;
  }
  uses {
    interface SplitControl as LowerControl;
    interface Max7315LedsControl as LedsControl;
  } 
}
implementation
{
  uint8_t state;
  
  enum {
    RED_BIT = 1,
    GREEN_BIT = 2,
    YELLOW_BIT = 4
  };
  
  enum {
    IDLE = 0,
    READY = 1,
    START = 2,
    INIT = 3,
  };
  
  command error_t Init.init() {
    atomic state = READY;
    call LowerControl.start();
    return SUCCESS;
  }
  
  event void LowerControl.startDone(error_t error) {
    call LedsControl.setAll(0xF8);
  }
  
  event void LowerControl.stopDone(error_t error) { }
  
  async command void Leds.led0On() {
    dbg(DBG_LED, "LEDS: Red on.\n");
    call LedsControl.setBlink0(TSB_RED, FALSE);
  }
  
  async command void Leds.led0Off() {
    dbg(DBG_LED, "LEDS: Red off.\n");
    call LedsControl.setBlink0(TSB_RED, TRUE);
  }
  
  async command void Leds.led0Toggle() {
    uint8_t ledsval = call LedsControl.getBlink0();
    if (ledsval & (1 << TSB_RED)) 
      call Leds.led0On();
    else
      call Leds.led0Off();
  }
  
  async command void Leds.led1On() {
    dbg(DBG_LED, "LEDS: Green on.\n");
    call LedsControl.setBlink0(TSB_GREEN, FALSE);
  }
  
  async command void Leds.led1Off() {
    dbg(DBG_LED, "LEDS: Green off.\n");
    call LedsControl.setBlink0(TSB_GREEN, TRUE);
  }
  
  async command void Leds.led1Toggle() {
    uint8_t ledsval = call LedsControl.getBlink0();
    if (ledsval & (1 << TSB_GREEN)) 
      call Leds.led1On();
    else
      call Leds.led1Off();
  }
  
  async command void Leds.led2On() {
    dbg(DBG_LED, "LEDS: Yellow on.\n");
    call LedsControl.setBlink0(TSB_BLUE, FALSE);
  }
  
  async command void Leds.led2Off() {
    dbg(DBG_LED, "LEDS: Yellow off.\n");
    call LedsControl.setBlink0(TSB_BLUE, TRUE);
  }
  
  async command void Leds.led2Toggle() {
    uint8_t ledsval = call LedsControl.getBlink0();
    if (ledsval & (1 << TSB_BLUE)) 
      call Leds.led2On();
    else
      call Leds.led2Off();
  }
  
  async command uint8_t Leds.get() {
    return call LedsControl.get();
  }
  
  async command void Leds.set(uint8_t ledsNum) {
    ledsNum = ~ledsNum;
    call LedsControl.setBlinkAll0(ledsNum & 0x07);
  }
  
  /* default event handlers for LedsControl */
  event void LedsControl.setConfigDone() { }
  event void LedsControl.allOffDone() { }
  event void LedsControl.setDone() { }
  event void LedsControl.setAllDone(uint8_t value) { 
    uint8_t _state;
    atomic _state = state;
    if (_state == INIT) {
      // turn all the LEDs off
      call LedsControl.setBlinkAll0(0xff);
    }
  }
  event void LedsControl.setBlink0Done() { }
  event void LedsControl.setBlinkAll0Done() { 
    bool _done;
    atomic {
      if (state == INIT) {
        state = READY;
        _done = TRUE;
      }
      else {
        _done = FALSE;
      }
    }
  }
  event void LedsControl.setBlink1Done() { }
  event void LedsControl.setBlinkAll1Done() { }
  event void LedsControl.setIntensityDone() { }
  event void LedsControl.setGlobalIntensityDone() { }

}

// $Id: PhotoDriverM.nc 788 2006-05-04 19:47:54Z cory $
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
 */

#include "Photo.h"

/**
 * Implementation of the light sensor driver and gain control for
 * the human visible photodiode sensor on Tmote Invent.
 *
 * @author Joe Polastre <info@moteiv.com>
 */
module PhotoDriverM
{
  provides {
    interface SplitControl;
    interface Potentiometer;
    interface AdcConfigure<const msp430adc12_channel_config_t*> as AdcPhoto;
  }
  uses {
    interface AD524X;
    interface SplitControl as AD524XControl;
    interface Leds;
  }
}

implementation
{
  const msp430adc12_channel_config_t config = {
      inch: INPUT_CHANNEL_A3,
      sref: REFERENCE_VREFplus_AVss,
      ref2_5v: REFVOLT_LEVEL_2_5,
      adc12ssel: SHT_SOURCE_ACLK,
      adc12div: SHT_CLOCK_DIV_1,
      sht: SAMPLE_HOLD_4_CYCLES,
      sampcon_ssel: SAMPCON_SOURCE_SMCLK,
      sampcon_id: SAMPCON_CLOCK_DIV_1
  };
  
  async command const msp430adc12_channel_config_t* AdcPhoto.getConfiguration()
  {
    return &config;
  }
  
  enum {
    OFF = 0,
    IDLE,
    START,
    START_O,
    STOP,
    STOP_O,
    GAIN
  };
  
  uint8_t state = OFF;
  uint8_t gain = 0xFF >> 1;
  
  command error_t SplitControl.start() {
    uint8_t _state = OFF;
    atomic {
      if (state == OFF) {
        state = START;
        _state = state;
      }
    }
    if (_state == START) {
      call AD524XControl.start();
      return SUCCESS;
    }
    return FAIL;
  }
  
  event void AD524XControl.startDone(error_t error) {
    call AD524X.start(PHOTO_ADDR, PHOTO_TYPE);
  }
  
  event void AD524X.startDone(uint8_t _addr, error_t _result, ad524x_type_t type) {
    uint8_t _state = OFF;
    atomic {
      if (state == START) {
        state = START_O;
        _state = state;
      }
    }
    if (_state == START_O) {
      if (!(call AD524X.setOutput(PHOTO_ON_ADDR, PHOTO_ON_OUTPUT, TRUE, PHOTO_ON_TYPE))) {
        atomic state = IDLE;
        signal SplitControl.startDone(SUCCESS);
      }
    }
  }
  
  command error_t SplitControl.stop() {
    uint8_t _state = OFF;
    atomic {
      if (state == IDLE) {
        state = STOP;
        _state = state;
      }
    }
    if (_state == STOP) {
      call AD524X.stop(PHOTO_ADDR, PHOTO_TYPE);
      return SUCCESS;
    }
    return FAIL;
  }

  event void AD524X.stopDone(uint8_t addr, error_t result, ad524x_type_t type) {
    uint8_t _state = OFF;
    atomic {
      if (state == STOP) {
        state = STOP_O;
        _state = state;
      }
    }
    if (_state == STOP_O) {
      if (!(call AD524X.setOutput(PHOTO_ON_ADDR, PHOTO_ON_OUTPUT, FALSE, PHOTO_ON_TYPE))) {
        atomic state = IDLE;
        call AD524XControl.stop();
      } else {
        signal SplitControl.stopDone(FAIL);
      }
    } else {
      signal SplitControl.stopDone(FAIL);
    }
  }
  
  event void AD524XControl.stopDone(error_t error) {
    signal SplitControl.stopDone(error);
  }
  
  event void AD524X.setOutputDone(uint8_t _addr, bool _output, error_t _result, ad524x_type_t _type) { 
    uint8_t _state = OFF;
    atomic {
      if ((state == START_O) || (state == STOP_O)) {
        _state = state;
        state = IDLE;
      }
    }
    
    if (_state == START_O) {
      signal SplitControl.startDone(SUCCESS);
    } else if (_state == STOP_O) {
      // turn off the potentiometer
      call AD524XControl.stop();
    }
  }
  
  event void AD524X.setPotDone(uint8_t _addr, bool _rdac, error_t _result, ad524x_type_t _type) { 
    uint8_t _state = OFF;
    atomic {
      if (state == GAIN) {
        _state = state;
        state = IDLE;
      }
    }
    
    if (_state == GAIN) {
      signal Potentiometer.setDone(gain, _result);
    }
  }
  
  event void AD524X.getPotDone(uint8_t addr, bool rdac, uint8_t value, error_t result, ad524x_type_t type) { }
  
  command error_t Potentiometer.set(uint8_t _gain) {
    uint8_t _state = OFF;
    atomic {
      if (state == IDLE) {
        state = GAIN;
        _state = state;
      }
    }
    if (_state == GAIN) {
      atomic gain = _gain;
      return call AD524X.setPot(PHOTO_GAIN_ADDR, PHOTO_GAIN_RDAC, gain, PHOTO_GAIN_TYPE);
    }
    return FAIL;
  }
  
  command uint8_t Potentiometer.get() {
    return gain;
  }
  
  default event void Potentiometer.setDone(uint8_t _gain, uint8_t _result) { }
}


/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */
#ifndef _H_sensorboard_h
#define _H_sensorboard_h

#include "msp430hardware.h"

enum {
  TSB_RED = 0,
  TSB_GREEN = 1,
  TSB_BLUE = 2,
} tsb_leds;

enum {
  MAX7315_ADDR = 0x20,
};

// bug in nesc1.2, not needed for nesc1.1
#undef norace

// sensorboard dependent connection of AD524X shutdown pin
TOSH_ASSIGN_PIN(AD524X_SD, 3, 5);

// Accel interrupt pin
TOSH_ASSIGN_PIN(ACCEL_INT, 2, 3);

// Microphone interrupt pin
TOSH_ASSIGN_PIN(MIC_INT, 2, 6);

#endif

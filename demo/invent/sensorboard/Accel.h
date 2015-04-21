/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */
#ifndef _H_ACCEL_H
#define _H_ACCEL_H

#include "AD524X.h"
//#include "MSP430ADC12.h"

enum {
  ACCEL_ADDR = 0x03, // 0x2F 1Mohm pot
  ACCEL_TYPE = TYPE_AD5241,

  ACCEL_ON_ADDR = 0x03,
  ACCEL_ON_OUTPUT = 0x00,
  ACCEL_ON_TYPE = TYPE_AD5241,

  ACCEL_INT_THRESH_ADDR = 0x03,
  ACCEL_INT_THRESH_RDAC = 0,
  ACCEL_INT_THRESH_TYPE = TYPE_AD5241,
};

#endif

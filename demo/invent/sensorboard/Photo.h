/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */
#ifndef _H_PHOTO_H
#define _H_PHOTO_H

#include "Msp430Adc12.h"
#include "AD524X.h"

enum {
  PHOTO_ADDR = 0x02, // 0x2E
  PHOTO_TYPE = TYPE_AD5241,

  PHOTO_ON_ADDR = 0x03,
  PHOTO_ON_OUTPUT = 0x01,
  PHOTO_ON_TYPE = TYPE_AD5241,

  PHOTO_GAIN_ADDR = 0x02,
  PHOTO_GAIN_RDAC = 0,
  PHOTO_GAIN_TYPE = TYPE_AD5247,
};

#endif

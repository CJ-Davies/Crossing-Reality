// $Id: Max7315.h 788 2006-05-04 19:47:54Z cory $
/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */

/*
 * @author Joe Polastre <info@moteiv.com>
 * Revision:  $Revision: 788 $
 *
 */
#ifndef _H_MAX7315_H
#define _H_MAX7315_H

typedef struct
{
  unsigned int intstatus : 1;
  unsigned int reserved : 1;
  unsigned int intout : 2;
  unsigned int intenable : 1;
  unsigned int global : 1;
  unsigned int blinkflip : 1;
  unsigned int blinkenable : 1;
} __attribute__ ((packed)) max7315_config_t;

enum {
  MAX7315_REG_INPUT = 0x00,
  MAX7315_REG_CONFIG = 0x0F,
  MAX7315_REG_PORTS = 0x03,
  MAX7315_REG_BLINK0 = 0x01,
  MAX7315_REG_BLINK1 = 0x09,
  MAX7315_REG_MASTER = 0x0E,
  MAX7315_REG_OUT1 = 0x10,
  MAX7315_REG_OUT2 = 0x11,
  MAX7315_REG_OUT3 = 0x12,
  MAX7315_REG_OUT4 = 0x13
};

#endif

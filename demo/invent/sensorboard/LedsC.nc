// $Id: LedsC.nc 788 2006-05-04 19:47:54Z cory $
/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */
/**
 * Configuration for changing the state of the LEDs on a device.
 * Use this configuration, and the Leds interface provided, to set and
 * clear LED lights on the Tmote platforms.
 *
 * @author Cory Sharp, Moteiv Corporation <info@moteiv.com>
 */
configuration LedsC {
  provides interface Leds;
}
implementation
{
  components MainC;
  components LedsM, PlatformLedsC, Max7315M;
  components new Msp430I2CC() as I2CBus;
  
  Leds = LedsM;
  
  LedsM.Init <- PlatformLedsC.Init;
  LedsM.LowerControl -> Max7315M;
  LedsM.LedsControl  -> Max7315M;
  
  Max7315M.I2CPacket -> I2CBus;
  Max7315M.Resource  -> I2CBus;
}


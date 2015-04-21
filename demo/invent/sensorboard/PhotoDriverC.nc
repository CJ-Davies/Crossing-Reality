// $Id: PhotoDriverC.nc 788 2006-05-04 19:47:54Z cory $
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
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */

#include "Photo.h"
#include "sensorboard.h"

/**
 * Driver for the human-visible-light sensor on Tmote Invent.
 * <p>
 * <b>Only available on Moteiv's Tmote Invent</b>
 * <p>
 * Before use, be sure to start the sensor using the SplitControl
 * interface.  If you would like to start the sensor on system boot,
 * use the MainControl generic component like so:
 * <pre>
 *  components new MainControl() as PhotoControl;
 *  components PhotoDriverC;
 *  PhotoControl.SplitControl -> PhotoDriverC;
 * </pre>
 * The 'ADC as Photo' interface provides raw data from the sensor.
 * <p>
 * The Potentiometer interface is used to set the gain on the output
 * of the photo sensor.  See the Tmote Invent datasheet for more information
 * on how to compute the raw potentiometer values and corresponding gain.
 *
 * @author Joe Polastre, Moteiv Corporation <info@moteiv.com>
 */
configuration PhotoDriverC
{
  provides {
    interface SplitControl;
    interface Read<uint16_t> as Photo;
    interface Potentiometer;
  }
}
implementation
{
  components PhotoDriverM, AD524XC, LedsC;
  
  PhotoDriverM.Leds -> LedsC;
  
  SplitControl = PhotoDriverM;
  Potentiometer = PhotoDriverM;
  
  components new AdcReadClientC() as PhotoSensor;
  Photo = PhotoSensor;
  PhotoSensor.AdcConfigure -> PhotoDriverM.AdcPhoto;
  
  PhotoDriverM.AD524X -> AD524XC;
  PhotoDriverM.AD524XControl -> AD524XC;

}

// $Id: PhotoGain.nc 788 2006-05-04 19:47:54Z cory $
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
 * @author Joe Polastre
 * Revision:  $Revision: 788 $
 *
 */
interface PhotoGain {

  /**
   * Set the gain on the photodiode.  The gain is a relative value between
   * 0 and 255 where 0 is minimal gain and 255 is maximum gain.  These values
   * correspond to:
   *     0 =  0 ohm resistence
   *   255 = 10kohm resistence
   * Since V=IR, the output voltage of the photodiode depends on the amount
   * of light (I) and the gain (R).
   *
   * @param gain The amount of gain, relative, between 0 and 255.
   *
   * @return SUCCESS if the request was accepted
   */
  command result_t set(uint8_t gain);
  /**
   * Notification that the gain of the photo sensor has been changed.
   *
   * @param gain New gain setting
   * @param result SUCCESS if the new gain setting was successfully set,
   *               FAIL if the gain could not be set (then ignore "gain"
   *               parameter)
   */
  event void setDone(uint8_t gain, result_t result);

  /**
   * Get the current gain of the Photo sensor.
   *
   * @return The current gain value between 0 and 255 where 0 is the 
   *         minimum gain and 255 is the maximum gain.
   */
  command uint8_t get();

}

// $Id: Potentiometer.nc 788 2006-05-04 19:47:54Z cory $
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
 * Interface for setting the value of a variable resistor (potentiometer).
 *
 * @author Joe Polastre, Moteiv Corporation <info@moteiv.com>
 */
interface Potentiometer {
  /**
   * Set the potentiometer to an 8-bit value.
   *
   * @param value New value for the potentiometer.
   *
   * @return SUCCESS if the request was accepted, FAIL if the system is busy.
   */
  command error_t set(uint8_t value);
  /**
   * Notification that the value of the potentiometer may have changed.
   *
   * @param value New value for the potentiometer.
   * @param result SUCCESS if the new value is now in place, FAIL if an
   *               error prevented the new value from being set.
   */
  event void setDone(uint8_t value, error_t result);

  /**
   * Get the current value of the potentiometer.
   *
   * @return 8-bit raw potentiometer value.
   */
  command uint8_t get();
}

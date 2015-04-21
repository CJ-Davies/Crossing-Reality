/*
 * Copyright (c) 2008, Technische Universitaet Berlin
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the Technische Universitaet Berlin nor the names 
 *   of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * - Revision -------------------------------------------------------------
 * $Revision: 1.1 $
 * $Date: 2008/06/16 18:00:31 $
 * @author: Jan Hauer <hauer@tkn.tu-berlin.de>
 * ========================================================================
 */

#include "TKN154.h"
interface MLME_DISASSOCIATE {

  /**
   * Request disassociation from a PAN.
   * 
   * @param DeviceAddrMode The addressing mode of the device to which to send 
   *                       the disassociation notification command.
   * @param DevicePANID The PAN identifier of the device to which to send the
   *                    disassociation notification command.
   * @param DeviceAddress The address of the device to which to send the
   *                      disassociation notification command
   * @param DisassociateReason The reason for the disassociation
   * @param TxIndirect TRUE if disassociation notification command is to be sent
   *                   indirectly
   * @param security   The security options (NULL means security is 
   *                   disabled)
   * @return       IEEE154_SUCCESS if the request succeeded and a confirm event
   *               will be signalled, an appropriate error code otherwise 
   *               (no confirm event will be signalled in this case)
   * @see          confirm
   */
  command ieee154_status_t request  (
                          uint8_t DeviceAddrMode,
                          uint16_t DevicePANID,
                          ieee154_address_t DeviceAddress,
                          ieee154_disassociation_reason_t DisassociateReason,
                          bool TxIndirect,
                          ieee154_security_t *security
                        );

  /**
   * Signals that a device has requested disassociation.
   *
   * @param DeviceAddress the 64-bit address of the requesting device
   * @param DisassociateReason Reason for the disassociation
   * @param security      The security options (NULL means security is 
   *                      disabled)
   */
  event void indication (
                          uint64_t DeviceAddress,
                          ieee154_disassociation_reason_t DisassociateReason,
                          ieee154_security_t *security
                        );

  /**
   * Confirmation of the disassociation attempt.
   *
   * @param status The status of the disassociation attempt
   * @param DeviceAddrMode The addressing mode of the device that has either
   *                       requested disassociation or been instructed to
   *                       disassociate by its coordinator.
   * @param DevicePANID The PAN identifier of the device that has either 
   *                    requested disassociation or been instructed to 
   *                    disassociate by its coordinator.
   * @param DeviceAddress The address of the device that has either requested 
   *                       disassociation or been instructed to disassociate 
   *                       by its coordinator.
   */
  event void confirm    (
                          ieee154_status_t status,
                          uint8_t DeviceAddrMode,
                          uint16_t DevicePANID,
                          ieee154_address_t DeviceAddress
                        );

}

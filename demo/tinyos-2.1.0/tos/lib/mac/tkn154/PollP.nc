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
 * $Date: 2008/06/16 18:00:28 $
 * @author Jan Hauer <hauer@tkn.tu-berlin.de>
 * ========================================================================
 */


#include "TKN154_MAC.h"

module PollP
{
  provides
  {
    interface Init;
    interface MLME_POLL;
    interface FrameRx as DataRx;
    interface DataRequest as DataRequest[uint8_t client];
  }
  uses
  {
    interface FrameTx as PollTx;
    interface FrameExtracted as DataExtracted;
    interface FrameUtility;
    interface Pool<ieee154_txframe_t> as TxFramePool;
    interface Pool<ieee154_txcontrol_t> as TxControlPool;
    interface Ieee802154Debug as Debug;
    interface MLME_GET;
    interface Get<uint64_t> as LocalExtendedAddress;
  }
}
implementation
{
  enum {
    HANDLE_MLME_POLL_REQUEST = 0xFF,
    HANDLE_MLME_POLL_SUCCESS = 0xFE,
  };
  int m_numPending;
  uint8_t m_dataRequestCmdID = CMD_FRAME_DATA_REQUEST;
  void buildDataRequestFrame( uint8_t destAddrMode, uint16_t destPANId,
      uint8_t* DstAddr, uint8_t srcAddrMode, ieee154_txframe_t *txFrame);

  command error_t Init.init()
  {
    m_numPending = 0;
    return SUCCESS;
  }

  command ieee154_status_t MLME_POLL.request  (
                          uint8_t coordAddrMode,
                          uint16_t coordPANID,
                          ieee154_address_t coordAddress,
                          ieee154_security_t *security      
                        )
  {
    ieee154_txframe_t *txFrame;
    ieee154_txcontrol_t *txControl;
    uint8_t srcAddrMode = 2;
    ieee154_status_t txStatus = IEEE154_SUCCESS;
    uint8_t coordAddressLE[8]; // little endian is what we want

    if (security && security->SecurityLevel)
      txStatus = IEEE154_UNSUPPORTED_SECURITY;
    else if (coordAddrMode < 2 || coordAddrMode > 3 || coordPANID == 0xFFFF)
      txStatus = IEEE154_INVALID_PARAMETER; 
    else if (!(txFrame = call TxFramePool.get()))
      // none of the predefined return value really fits
      txStatus = IEEE154_TRANSACTION_OVERFLOW; 
    else if (!(txControl = call TxControlPool.get())){
      call TxFramePool.put(txFrame);
      txStatus = IEEE154_TRANSACTION_OVERFLOW;
    } else {
      txFrame->header = &txControl->header;
      txFrame->metadata = &txControl->metadata;
      if (coordAddrMode == ADDR_MODE_SHORT_ADDRESS)
        *((nxle_uint16_t*) &coordAddressLE) = coordAddress.shortAddress;
      else 
        call FrameUtility.convertToLE(coordAddressLE, &coordAddress.extendedAddress);
      txFrame->handle = HANDLE_MLME_POLL_REQUEST;
      if (call MLME_GET.macShortAddress() >= 0xFFFE)
        srcAddrMode = 3;
      buildDataRequestFrame(coordAddrMode, coordPANID, coordAddressLE, srcAddrMode, txFrame);
      if ((txStatus = call PollTx.transmit(txFrame)) != IEEE154_SUCCESS){
        call TxFramePool.put(txFrame);
        call TxControlPool.put(txControl);
        call Debug.log(LEVEL_IMPORTANT, PollP_ALLOC_FAIL1, 0, 0, 0);
      } else 
        m_numPending++;
    }
    return txStatus;
  }

  command ieee154_status_t DataRequest.poll[uint8_t client](uint8_t CoordAddrMode, 
      uint16_t CoordPANId, uint8_t *CoordAddressLE, uint8_t srcAddrMode)
  {
    ieee154_txframe_t *txFrame;
    ieee154_txcontrol_t *txControl;
    ieee154_status_t status = IEEE154_TRANSACTION_OVERFLOW;
    call Debug.log(LEVEL_INFO, PollP_INTERNAL_POLL, CoordAddrMode, client, m_numPending);
    if (client == SYNC_CLIENT && m_numPending != 0){
      // no point in auto-requesting if user request is pending
      signal DataRequest.pollDone[client]();
      return IEEE154_SUCCESS;
    } else if ((txFrame = call TxFramePool.get())){
      if (!(txControl = call TxControlPool.get()))
        call TxFramePool.put(txFrame);
      else {
        txFrame->header = &txControl->header;
        txFrame->metadata = &txControl->metadata;
        txFrame->handle = client;
        buildDataRequestFrame(CoordAddrMode, CoordPANId, 
            CoordAddressLE, srcAddrMode, txFrame);
        if ((status = call PollTx.transmit(txFrame)) != IEEE154_SUCCESS){
          call TxControlPool.put(txControl);
          call TxFramePool.put(txFrame);
          call Debug.log(LEVEL_IMPORTANT, PollP_ALLOC_FAIL2, 0, 0, 0);
        } else 
          m_numPending++;
      }
    }
    if (status != IEEE154_SUCCESS)
      signal DataRequest.pollDone[client]();
    return status;
  }

  void buildDataRequestFrame(uint8_t destAddrMode, uint16_t destPANId,
      uint8_t* destAddrPtrLE, uint8_t srcAddrMode, ieee154_txframe_t *txFrame)
  {
    // destAddrPtrLE points to an address in little-endian format !
    ieee154_address_t srcAddress;
    uint8_t *mhr;
    uint16_t srcPANId;
    ieee154_address_t DstAddr;
    srcPANId = call MLME_GET.macPANId();

    memcpy(&DstAddr, destAddrPtrLE, destAddrMode == 2 ? 2 : 8);
    mhr = txFrame->header->mhr;
    mhr[MHR_INDEX_FC1] = FC1_FRAMETYPE_CMD | FC1_ACK_REQUEST;
    if (destAddrMode >= 2 && srcAddrMode >= 2 && destPANId == srcPANId)
      mhr[MHR_INDEX_FC1] |= FC1_PAN_ID_COMPRESSION;
    mhr[MHR_INDEX_FC2] = destAddrMode << FC2_DEST_MODE_OFFSET;
    mhr[MHR_INDEX_FC2] |= srcAddrMode << FC2_SRC_MODE_OFFSET;
    if (srcAddrMode == 2)
      srcAddress.shortAddress = call MLME_GET.macShortAddress();
    else  
      srcAddress.extendedAddress = call LocalExtendedAddress.get();
    txFrame->headerLen = call FrameUtility.writeHeader(
          txFrame->header->mhr,
          destAddrMode,
          destPANId,
          &DstAddr,
          srcAddrMode,
          srcPANId,
          &srcAddress,
          (mhr[MHR_INDEX_FC1] & FC1_PAN_ID_COMPRESSION) ? TRUE: FALSE);
    txFrame->payload = &m_dataRequestCmdID;
    txFrame->payloadLen = 1;
  }

  event message_t* DataExtracted.received(message_t* frame, ieee154_txframe_t *txFrame)
  {
    if (!txFrame){
      call Debug.log(LEVEL_CRITICAL, PollP_INTERNAL_ERROR, 0, 0, 0);
      return frame;
    } else
      call Debug.log(LEVEL_INFO, PollP_SUCCESS, 0, 0, 0);
    if (txFrame->handle == HANDLE_MLME_POLL_REQUEST)
      signal MLME_POLL.confirm(IEEE154_SUCCESS);
    else
      signal DataRequest.pollDone[txFrame->handle]();
    txFrame->handle = HANDLE_MLME_POLL_SUCCESS; // mark as processed
    // TODO: check if pending bit is set (then initiate another POLL)
    call Debug.log(LEVEL_IMPORTANT, PollP_RX, txFrame->handle, 0, 0);
    return signal DataRx.received(frame);
  }

  event void PollTx.transmitDone(ieee154_txframe_t *txFrame, ieee154_status_t status)
  {
    call Debug.log(LEVEL_IMPORTANT, PollP_TXDONE, status, txFrame->handle, 0);
    m_numPending--;
    if (txFrame->handle != HANDLE_MLME_POLL_SUCCESS){
      // didn't receive a DATA frame from the coordinator
      if (status == IEEE154_SUCCESS) // TODO: can this happen if a frame other than DATA was extracted?
        status = IEEE154_NO_DATA;
      if (txFrame->handle == HANDLE_MLME_POLL_REQUEST)
        signal MLME_POLL.confirm(status);
      else
        signal DataRequest.pollDone[txFrame->handle]();
    }
    call TxControlPool.put((ieee154_txcontrol_t*) ((uint8_t*) txFrame->header - offsetof(ieee154_txcontrol_t, header)));
    call TxFramePool.put(txFrame);
  }
  default event void MLME_POLL.confirm(ieee154_status_t status){}
  default event void DataRequest.pollDone[uint8_t client](){}
}

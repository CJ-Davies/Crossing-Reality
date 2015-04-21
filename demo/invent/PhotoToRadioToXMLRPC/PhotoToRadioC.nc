#include <Timer.h>
#include "PhotoToRadio.h"
 
module PhotoToRadioC {
	uses interface Boot;
	uses interface Leds;
	uses interface Timer<TMilli> as Timer0;
	uses interface Packet;
	uses interface AMPacket;
	uses interface AMSend;
	uses interface SplitControl as AMControl; 
	uses interface Read<uint16_t> as Photo;
	uses interface SplitControl as PhotoControl;
}

implementation {
	bool busy = FALSE;
	message_t pkt;

	uint16_t lastTransmit = 1;

	uint16_t counter = 0;
 
	event void Boot.booted() {
		call AMControl.start();
	}
 
	event void AMControl.startDone(error_t err) {
		if (err == SUCCESS) {
			call Timer0.startPeriodic(5120);
			call PhotoControl.start();
		}
		else {
			call AMControl.start();
		}
	}

	event void AMControl.stopDone(error_t err) {
		
	}

	event void AMSend.sendDone(message_t* msg, error_t error) {
		if (&pkt == msg) {
			busy = FALSE;
		}
	}

	event void Timer0.fired() {
		call Photo.read();
	}

	event void Photo.readDone(error_t result, uint16_t data) {
		counter++;
		call Leds.set(counter);
		
		if (!busy) {
			PhotoToRadioMsg* ptrpkt = (PhotoToRadioMsg*)(call Packet.getPayload(&pkt, NULL));
			ptrpkt->nodeid = TOS_NODE_ID;
			ptrpkt->photoreading = data;
			
			if (((lastTransmit + ((lastTransmit/100)*50)) > data) || ((lastTransmit - ((lastTransmit/100)*50)) < data)) {
				lastTransmit = data;
				if (call AMSend.send(AM_BROADCAST_ADDR, &pkt, sizeof(PhotoToRadioMsg)) == SUCCESS) {
					busy = TRUE;
				}
			}
		}
	}

	event void PhotoControl.startDone(error_t error) {
	}
  
	event void PhotoControl.stopDone(error_t error) {
	}

}

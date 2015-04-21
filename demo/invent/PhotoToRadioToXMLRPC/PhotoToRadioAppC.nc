#include <Timer.h>
#include "PhotoToRadio.h"

configuration PhotoToRadioAppC {
}

implementation {
	components MainC;
	components LedsC;
	components PhotoToRadioC as App;
	components new TimerMilliC() as Timer0;

	components ActiveMessageC;
	components new AMSenderC(AM_PHOTOTORADIOMSG);
	
	App.Boot -> MainC;
	App.Leds -> LedsC;
	App.Timer0 -> Timer0;

	App.Packet -> AMSenderC;
	App.AMPacket -> AMSenderC;
	App.AMSend -> AMSenderC;
	App.AMControl -> ActiveMessageC;
	
	components PhotoDriverC;
	App.PhotoControl -> PhotoDriverC;
	App.Photo -> PhotoDriverC.Photo;
}

#ifndef PHOTOTORADIO_H
#define PHOTOTORADIO_H

enum {
	AM_PHOTOTORADIOMSG = 6,
	TIMER_PERIOD_MILLI = 250
};

typedef nx_struct PhotoToRadioMsg {
	nx_uint16_t nodeid;
	nx_uint16_t photoreading;
} PhotoToRadioMsg;

#endif

COMPONENT=PhotoToRadioAppC
BUILD_EXTRA_DEPS += PhotoToRadioMsg.class
BUILD_EXTRA_DEPS += PhotoMsgReaderToXMLRPC.class
CLEAN_EXTRA = *.class PhotoToRadioMsg.java

PhotoMsgReaderToXMLRPC.class: PhotoMsgReaderToXMLRPC.java
	javac PhotoMsgReaderToXMLRPC.java

PhotoToRadioMsg.class: PhotoToRadioMsg.java
	javac PhotoToRadioMsg.java

PhotoToRadioMsg.java:
	mig java -target=null -java-classname=PhotoToRadioMsg PhotoToRadio.h PhotoToRadioMsg -o $@

CFLAGS += -I../sensorboard

include $(MAKERULES)

// $Id: Max7315M.nc 788 2006-05-04 19:47:54Z cory $
/*
 * Copyright (c) 2005 Moteiv Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached MOTEIV-LICENSE     
 * file. If you do not find these files, copies can be found at
 * http://www.moteiv.com/MOTEIV-LICENSE.txt and by emailing info@moteiv.com.
 */

/**
 * @author Joe Polastre <info@moteiv.com>
 * @author Cory Sharp <info@moteiv.com>
 * Revision:  $Revision: 788 $
 *
 */
#include "Max7315.h"
#include "sensorboard.h"
#include "circularQueue.h"
#include "I2C.h"

module Max7315M
{
  provides {
    interface SplitControl;
    interface Max7315LedsControl;
  }
  uses {
    interface I2CPacket<TI2CBasicAddr>;
    interface Resource;
  }
}

implementation
{
  enum {
    IDLE = 0,
    MAX7315_S_CONFIG,
    MAX7315_S_PORTS,
    MAX7315_S_PORTS_ALL,
    MAX7315_S_BLINK0,
    MAX7315_S_BLINK1,
    MAX7315_S_BLINK0_ALL,
    MAX7315_S_BLINK1_ALL,
    MAX7315_S_INTENSITY,
    MAX7315_S_MASTER,
    
    QUEUE_SIZE = 4,
  };
  
  typedef struct {
    uint8_t addr;
    uint8_t data[2];
    uint8_t state;
  } Command_t;
  
  Command_t commands[ QUEUE_SIZE ];
  CircularQueue_t queue;
  
  uint8_t ports;
  uint8_t intensity[4];
  uint8_t blink[2];
  uint8_t gintensity;
  max7315_config_t config;
  
  typedef union {
    max7315_config_t config;
    uint8_t byte;
  } map_config_t;
  
  Command_t* front() {
    atomic return cqueue_isEmpty(&queue) ? NULL : &commands[queue.front];
  }
  
  task void startDone() {
    signal SplitControl.startDone(SUCCESS);
  }
  
  command error_t SplitControl.start() {
    atomic {
      config.intenable = 1;
      config.global = 1;
      ports = 0xFF;
      intensity[0] = 0xFF;
      intensity[1] = 0xFF;
      intensity[2] = 0xFF;
      intensity[3] = 0xFF;
      blink[0] = 0xFF;
      blink[1] = 0xFF;
      gintensity = 0x0F;
      cqueue_init( &queue, QUEUE_SIZE );
    }
    post startDone();
    return SUCCESS;
  }
  
  command error_t SplitControl.stop() {
    return SUCCESS;
  }
  
  event void Resource.granted() {
    Command_t* cmd = front();
    if( cmd != NULL ) {
      if( call I2CPacket.write(0x03, cmd->addr, 2, cmd->data) == SUCCESS )
        return; //release in I2CPacket.writePacketDone
      // if the write failed, enqueue another deferred request for the resource
      call Resource.release();
      call Resource.request();
    }
    // the write failed, release the current resource
    call Resource.release();
  }
  
  error_t startWriteCommand(uint8_t _addr, uint8_t _cmd, uint8_t _data, uint8_t _newstate) {
    error_t result;
    
    atomic {
      if( cqueue_pushBack( &queue ) == SUCCESS ) {
        commands[queue.back].addr    = _addr;
        commands[queue.back].data[0] = _cmd;
        commands[queue.back].data[1] = _data;
        commands[queue.back].state = _newstate;
        result = SUCCESS;
      }
      else {
        result = FAIL;
      }
    }
    
    if( result == SUCCESS ) {
      call Resource.request();
    }
    
    return result;
  }
  
  async command error_t Max7315LedsControl.setConfig(max7315_config_t _config) {
    map_config_t map = { config:_config };
    atomic config = _config;
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_CONFIG, map.byte, MAX7315_S_CONFIG);
  }
  
  async command max7315_config_t Max7315LedsControl.getConfig() {
    atomic return config;
  }
  
  async command error_t Max7315LedsControl.allOff() {
    atomic {
      config.blinkenable = 0;
      gintensity = 0;
    }
    return SUCCESS;
  }
  
  // set on or off ports config
  async command error_t Max7315LedsControl.set(uint8_t port, bool value) {
    uint8_t _ports;
    atomic {
      if (value)
        ports |= (1 << port);
      else
        ports &= ~(1 << port);
      _ports = ports;
    }
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_PORTS, _ports, MAX7315_S_PORTS);
  }
  
  async command error_t Max7315LedsControl.setAll(uint8_t values) {
    uint8_t _ports;
    atomic ports = _ports = values;
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_PORTS, _ports, MAX7315_S_PORTS_ALL);
  }
  
  async command uint8_t Max7315LedsControl.get() {
    atomic return ports;
  }
  
  async command error_t Max7315LedsControl.setBlink0(uint8_t port, bool value) {
    uint8_t _blink;
    atomic {
      if (value)
        blink[0] |= (1 << port);
      else
        blink[0] &= ~(1 << port);
      _blink = blink[0];
    }
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_BLINK0, _blink, MAX7315_S_BLINK0);
  }
  
  async command error_t Max7315LedsControl.setBlinkAll0(uint8_t value) {
    atomic blink[0] = value;
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_BLINK0, value, MAX7315_S_BLINK0_ALL);
  }
  
  async command uint8_t Max7315LedsControl.getBlink0() {
    atomic return blink[0];
  }
  
  async command error_t Max7315LedsControl.setBlink1(uint8_t port, bool value) {
    uint8_t _blink;
    atomic {
      if (value)
        blink[1] |= (1 << port);
      else
        blink[1] &= ~(1 << port);
    }
    _blink = blink[1];
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_BLINK1, _blink, MAX7315_S_BLINK1);
  }
  
  async command error_t Max7315LedsControl.setBlinkAll1(uint8_t value) {
    atomic blink[1] = value;
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_BLINK1, value, MAX7315_S_BLINK1_ALL);
  }
  
  async command uint8_t Max7315LedsControl.getBlink1() {
    atomic return blink[1];
  }
  
  async command error_t Max7315LedsControl.setGlobalIntensity(uint8_t value) {
    uint8_t _gintensity;
    atomic gintensity = _gintensity = (((value << 4) & 0xF0) | 0x0F);
    return startWriteCommand(MAX7315_ADDR, MAX7315_REG_MASTER, _gintensity, MAX7315_S_MASTER);
  }    
  
  async command uint8_t Max7315LedsControl.getGlobalIntensity() {
    atomic return (gintensity >> 4) & 0x0F;
  }
  
  async command error_t Max7315LedsControl.setIntensity(uint8_t port, uint8_t value) {
    uint8_t _reg;
    uint8_t _intensity;
    atomic {
      switch (port) {
      case 0:
        intensity[0] = _intensity = (intensity[0] & 0xF0) | (value & 0x0F);
        _reg = MAX7315_REG_OUT1;
        break;
      case 1:
        intensity[0] = _intensity = (intensity[0] & 0x0F) | ((value << 4) & 0xF0);
        _reg = MAX7315_REG_OUT1;
        break;
      case 2:
        intensity[1] = _intensity = (intensity[1] & 0xF0) | (value & 0x0F);
        _reg = MAX7315_REG_OUT2;
        break;
      case 3:
        intensity[1] = _intensity = (intensity[1] & 0x0F) | ((value << 4) & 0xF0);
        _reg = MAX7315_REG_OUT2;
        break;
      case 4:
        intensity[2] = _intensity = (intensity[2] & 0xF0) | (value & 0x0F);
        _reg = MAX7315_REG_OUT3;
        break;
      case 5:
        intensity[2] = _intensity = (intensity[2] & 0x0F) | ((value << 4) & 0xF0);
        _reg = MAX7315_REG_OUT3;
        break;
      case 6:
        intensity[3] = _intensity = (intensity[3] & 0xF0) | (value & 0x0F);
        _reg = MAX7315_REG_OUT4;
        break;
      case 7:
        intensity[3] = _intensity = (intensity[3] & 0x0F) | ((value << 4) & 0xF0);
        _reg = MAX7315_REG_OUT4;
        break;
      default:
        return FAIL;
      }
    }
    return startWriteCommand(MAX7315_ADDR, _reg, _intensity, MAX7315_S_INTENSITY);
  }
  
  uint8_t spiDoneState;
  
  task void spiDone() {
    uint8_t _state;
    
    atomic _state = spiDoneState;
    
    switch (_state) {
    case MAX7315_S_CONFIG:
      signal Max7315LedsControl.setConfigDone();
      break;
    case MAX7315_S_PORTS:
      signal Max7315LedsControl.setDone();
      break;
    case MAX7315_S_PORTS_ALL: {
      uint8_t _ports;
      atomic _ports = ports;
      signal Max7315LedsControl.setAllDone(_ports);
      break;
    }
    case MAX7315_S_BLINK0:
      signal Max7315LedsControl.setBlink0Done();
      break;
    case MAX7315_S_BLINK0_ALL:
      signal Max7315LedsControl.setBlinkAll0Done();
      break;
    case MAX7315_S_BLINK1:
      signal Max7315LedsControl.setBlink1Done();
      break;
    case MAX7315_S_BLINK1_ALL:
      signal Max7315LedsControl.setBlinkAll1Done();
      break;
    case MAX7315_S_MASTER:
      signal Max7315LedsControl.setGlobalIntensityDone();
      break;
    case MAX7315_S_INTENSITY:
      signal Max7315LedsControl.setIntensityDone();
      break;
    }
    
    atomic {
      if( cqueue_isEmpty(&queue) )
        return;
    }
    
    call Resource.request();
  }
  
  async event void I2CPacket.readDone(error_t error, uint16_t addr, uint8_t length, uint8_t* data) {
    // we never read
  }
  
  async event void I2CPacket.writeDone(error_t error, uint16_t addr, uint8_t length, uint8_t* data) { 
    atomic {
      Command_t* cmd = front();
      // check if the buffer is ours, otherwise leave now
      if( (cmd == NULL) || (data != cmd->data) )
        return;
      spiDoneState = cmd->state;
      cqueue_popFront( &queue );
      call Resource.release();
    }
    
    post spiDone();
  }
}


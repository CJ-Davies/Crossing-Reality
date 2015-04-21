//$Id: DS2411M.nc,v 1.2 2005/08/19 22:08:35 cssharp Exp $

/* "Copyright (c) 2000-2003 The Regents of the University of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement
 * is hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY
 * OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 */

//@author Cory Sharp <cssharp@eecs.berkeley.edu>


/*

  The 1-wire timings suggested by the DS2411 data sheet are incorrect,
  incomplete, or unclear.  The timings provided the app note 522 work:

    http://www.maxim-ic.com/appnotes.cfm/appnote_number/522

*/
module DS2411M
{
  provides interface DS2411;
  uses interface DS2411Pin;
  uses interface Leds;
}
implementation
{
  uint8_t m_id[8];
  
  void uwait( uint16_t usec )
  {
    const uint16_t t0 = TAR;
    while( (TAR-t0) < usec );
  }
  
  enum {
    T_REC = 5,
    T_SLOT = 65,
    
    T_RSTL = (480+640)/2,
    T_PDH = 60,
    T_PDL = 240,
    T_MSP = (60+75)/2,
    
    T_W0L = 60,
    T_W1L = 5,
    T_RL = 6,
    T_MSR = 6,
  };
  
  void init_pins()
  {
    TOSH_MAKE_ONEWIRE_INPUT();
    TOSH_CLR_ONEWIRE_PIN();
  }
  
  bool reset() // >= 960us
  {
    int present;
    call DS2411Pin.output_low();
    uwait(T_RSTL); //t_RSTL
    call DS2411Pin.prepare_read();
    uwait(T_MSP);  //t_MSP
    present = call DS2411Pin.read();
    uwait(T_PDH + T_PDL + T_REC - T_MSP);  //t_REC
    if(present == 0) {
      call Leds.led1Off();
    } else {
      call Leds.led1On();
    }
    return (present == 0);
  }
  
  void write_bit_one() // >= 70us
  {
    call DS2411Pin.output_low();
    uwait(T_W1L);  //t_W1L
    call DS2411Pin.output_high();
    uwait(T_SLOT - T_W1L);  //t_SLOT - t_W1L
  }
  
  void write_bit_zero() // >= 70us
  {
    call DS2411Pin.output_low();
    uwait(T_W0L);  //t_W0L
    call DS2411Pin.output_high();
    uwait(T_SLOT - T_W0L);  //t_SLOT - t_W0L
  }
  
  void write_bit( int is_one ) // >= 70us
  {
    if(is_one)
      write_bit_one();
    else
      write_bit_zero();
  }
  
  bool read_bit() // >= 70us
  {
    int bit;
    call DS2411Pin.output_low();
    uwait(T_RL);  //t_RL
    call DS2411Pin.prepare_read();
    uwait(T_MSR); //near-max t_MSR
    bit = call DS2411Pin.read();
    uwait(T_REC);  //t_REC
    return bit;
  }
  
  void write_byte( uint8_t byte ) // >= 560us
  {
    uint8_t bit;
    for( bit=0x01; bit!=0; bit<<=1 )
      write_bit( byte & bit );
  }
  
  uint8_t read_byte() // >= 560us
  {
    uint8_t byte = 0;
    uint8_t bit;
    for( bit=0x01; bit!=0; bit<<=1 )
    {
      if( read_bit() )
        byte |= bit;
    }
    return byte;
  }
  
  uint8_t crc8_byte( uint8_t crc, uint8_t byte )
  {
    int i;
    crc ^= byte;
    for( i=0; i<8; i++ )
    {
      if( crc & 1 )
        crc = (crc >> 1) ^ 0x8c;
      else
        crc >>= 1;
    }
    return crc;
  }
  
  uint8_t crc8_bytes( uint8_t crc, uint8_t* bytes, uint8_t len )
  {
    uint8_t* end = bytes+len;
    while( bytes != end )
      crc = crc8_byte( crc, *bytes++ );
    return crc;
  }
  
  command error_t DS2411.init() // >= 6000us
  {
    int retry = 5;
    uint8_t id[8];
    
    bzero( m_id, 8 );
    call DS2411Pin.init();
    while( retry-- > 0 )
    {
      int crc = 0;
      if( reset() )
      {
        uint8_t* byte;
        
        write_byte(0x33); //read rom
        for( byte=id+7; byte!=id-1; byte-- )
          crc = crc8_byte( crc, *byte=read_byte() );
        
        memcpy( m_id, id, 8 );
        if( crc == 0 )
        {
          call DS2411Pin.output_high();
          return SUCCESS;
        }
      }
    }
    
    call DS2411Pin.output_high();
    return FAIL;
  }
  
  command uint8_t DS2411.get_id_byte( uint8_t index )
  {
    return (index < 6) ? m_id[index+1] : 0;
  }
  
  command void DS2411.copy_id( uint8_t* id )
  {
    memcpy( id, m_id+1, 6 );
  }
  
  command uint8_t DS2411.get_family()
  {
    return m_id[7];
  }
  
  command uint8_t DS2411.get_crc()
  {
    return m_id[0];
  }
  
  uint8_t calc_crc()
  {
    return crc8_bytes( 0, m_id+1, 7 );
  }
  
  command bool DS2411.is_crc_okay()
  {
    return (call DS2411.get_crc() == calc_crc());
  }
}


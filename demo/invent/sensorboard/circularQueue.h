/* "Copyright (c) 2000-2002 The Regents of the University of California.  
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

// Authors: Cory Sharp
// $Id: circularQueue.h 788 2006-05-04 19:47:54Z cory $

// Description: Provide index manipulation for a circular queue or stack.
// It manipulates integer indices into presumably an array you're keeping
// somewhere else.  If the queue is nonempty, then front and back index to
// the respectively valid entries in the data array.  If the array is empty,
// then front and back both point to the (invalid) element one past the end
// of the array.  pop_front, pop_back, push_front, push_back, is_empty, and
// is_full all do the expected things.  pop_* return FAIL if the resulting
// queue is empty.  push_* return FAIL if queue was already full.

#ifndef _H_cqueue_h
#define _H_cqueue_h

typedef uint8_t CircularQueueIndex_t;

typedef struct
{
  CircularQueueIndex_t front;
  CircularQueueIndex_t back;
  CircularQueueIndex_t size;
} CircularQueue_t;

// if front == back == size, then the list is empty
// otherwise, front points to the current front element
// and back points to the current back element

void cqueue_init( CircularQueue_t* cq, CircularQueueIndex_t size )
{
  cq->front = size;
  cq->back  = size;
  cq->size  = size;
}


CircularQueueIndex_t cqueue_privInc( CircularQueue_t* cq, CircularQueueIndex_t n )
{
  return ((n+1) == cq->size) ? 0 : (n+1);
}


CircularQueueIndex_t cqueue_privDec( CircularQueue_t* cq, CircularQueueIndex_t n )
{
  return n ? (n-1) : (cq->size-1);
}


bool cqueue_isEmpty( CircularQueue_t* cq )
{
  return (cq->front == cq->size) ? TRUE : FALSE;
}


bool cqueue_isFull( CircularQueue_t* cq )
{
  return (cqueue_privDec(cq, cq->back) == cq->front) ? TRUE : FALSE;
}

CircularQueueIndex_t cqueue_numElements( CircularQueue_t* cq )
{
  return cqueue_isEmpty(cq) ? 0
       : (cq->front >= cq->back) ? (cq->front - cq->back + 1)
       : (cq->size + cq->front - cq->back + 1)
       ;
}


// return SUCCESS if cq->front points to a valid (but unassigned) element
// return FAIL if cq is full
error_t cqueue_pushFront( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) == TRUE )
  {
    cq->front = 0;
    cq->back  = 0;
  }
  else
  {
    CircularQueueIndex_t newfront = cqueue_privInc( cq, cq->front );

    if( newfront == cq->back )
      return FAIL;

    cq->front = newfront;
  }

  return SUCCESS;
}


// return SUCCESS if cq->back points to a valid (but unassigned) element
// return FAIL if cq is full
error_t cqueue_pushBack( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) == TRUE )
  {
    cq->front = 0;
    cq->back  = 0;
  }
  else
  {
    CircularQueueIndex_t newback = cqueue_privDec( cq, cq->back );

    if( newback == cq->front )
      return FAIL;

    cq->back = newback;
  }

  return SUCCESS;
}


// return SUCCESS if cq->front now points to a valid element
// return FAIL if cq was empty or is now empty
error_t cqueue_popFront( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) )
    return FAIL;

  if( cq->front == cq->back )
  {
    cq->front = cq->size;
    cq->back  = cq->size;
    return FAIL;
  }

  cq->front = cqueue_privDec( cq, cq->front );
  return SUCCESS;
}


// return SUCCESS if cq->back now points to a valid element
// return FAIL if cq was empty or is now empty
error_t cqueue_popBack( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) )
    return FAIL;

  if( cq->front == cq->back )
  {
    cq->front = cq->size;
    cq->back  = cq->size;
    return FAIL;
  }

  cq->back = cqueue_privInc( cq, cq->back );
  return SUCCESS;
}


// return SUCCESS if cq->front points to a valid (but unassigned) element
// in contrast to push_front_queue, if cq is full this function deletes the
// last element and pushes the back up one.
error_t cqueue_forciblyPushFront( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) == TRUE )
  {
    cq->front = 0;
    cq->back  = 0;
  }
  else
  {
    CircularQueueIndex_t newfront = cqueue_privInc( cq, cq->front );

    if( newfront == cq->back )
      cq->back=cqueue_privInc(cq, cq->back);
    //      return FAIL;

    cq->front = newfront;
  }

  return SUCCESS;
}


// return SUCCESS if cq->back points to a valid (but unassigned) element
// in contrast to push_back_queue, if cq is full this function deletes the
// first element and pushes the front down one.
error_t cqueue_forciblyPushBack( CircularQueue_t* cq )
{
  if( cqueue_isEmpty( cq ) == TRUE )
  {
    cq->front = 0;
    cq->back  = 0;
  }
  else
  {
    CircularQueueIndex_t newback = cqueue_privDec( cq, cq->back );

    if( newback == cq->front )
      cq->front = cqueue_privDec(cq, cq->front);
    //      return FAIL;

    cq->back = newback;
  }

  return SUCCESS;
}


#endif // _H_cqueue_h


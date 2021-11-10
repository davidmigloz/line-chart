package com.davidmiguel.linechart.touch

import android.os.Handler
import android.view.MotionEvent
import com.davidmiguel.linechart.touch.ScrubGestureDetector.ScrubListener
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

class ScrubGestureDetectorTest {

    private lateinit var scrubGestureDetector: ScrubGestureDetector
    private lateinit var scrubListener: ScrubListener
    private lateinit var handler: Handler

    @Before
    fun setup() {
        scrubListener = mock(ScrubListener::class.java)
        handler = mock(Handler::class.java)
        scrubGestureDetector = ScrubGestureDetector(scrubListener, handler, TOUCH_SLOP)
        scrubGestureDetector.setEnabled(true)
    }

    @Test
    fun test_disabled() {
        scrubGestureDetector.setEnabled(false)
        val down = getMotionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, 0, 0)

        // verify all events are rejected at the start of the stream
        assertFalse(scrubGestureDetector.onTouch(null, down))
    }

    @Test
    fun test_moveScrub_success() {
        val downTime = 0L
        val moveTime = ScrubGestureDetector.LONG_PRESS_TIMEOUT_MS
        val scrubX = (TOUCH_SLOP + 1)
        val scrubY = (TOUCH_SLOP + 1)
        val down = getMotionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, 0, 0)
        val move = getMotionEvent(MotionEvent.ACTION_MOVE, scrubX, scrubY, moveTime, downTime)
        val up = getMotionEvent(MotionEvent.ACTION_UP, 0f, 0f, moveTime, downTime)
        assertTrue(scrubGestureDetector.onTouch(null, down))
        assertTrue(scrubGestureDetector.onTouch(null, move))
        assertTrue(scrubGestureDetector.onTouch(null, up))

        // verify scrub then end events
        verify(scrubListener).onScrubbed(scrubX, scrubY)
        verify(scrubListener).onScrubEnded()
        verifyNoMoreInteractions(scrubListener)
    }

    @Test
    fun test_moveScrub_waiting() {
        val downTime = 0L
        // make just under the timeout
        val moveTime = ScrubGestureDetector.LONG_PRESS_TIMEOUT_MS - 1
        // make just under our touch-slop
        val scrubX = (TOUCH_SLOP - 1)
        val scrubY = (TOUCH_SLOP - 1)
        val down = getMotionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, 0, 0)
        val move = getMotionEvent(MotionEvent.ACTION_MOVE, scrubX, scrubY, moveTime, downTime)
        assertTrue(scrubGestureDetector.onTouch(null, down))
        assertTrue(scrubGestureDetector.onTouch(null, move))

        // verify no scrub events
        verifyNoMoreInteractions(scrubListener)
    }

    @Test
    fun test_moveScrub_failure() {
        val downTime = 0L
        // make just under the timeout
        val moveTime = ScrubGestureDetector.LONG_PRESS_TIMEOUT_MS - 1
        // make just over the touch-slop
        val scrubX = 9f
        val scrubY = 9f
        val down = getMotionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, 0, 0)
        val move = getMotionEvent(MotionEvent.ACTION_MOVE, scrubX, scrubY, moveTime, downTime)
        assertTrue(scrubGestureDetector.onTouch(null, down))
        assertFalse(scrubGestureDetector.onTouch(null, move))

        // verify no scrub events
        verifyNoMoreInteractions(scrubListener)
    }

    @Test
    fun test_timeScrub_success() {
        // mock handler runs runnable immediately
        `when`(handler.postDelayed(any(Runnable::class.java), anyLong())).then(
            Answer { invocation: InvocationOnMock ->
                val r = invocation.arguments[0] as Runnable
                r.run()
                true
            } as Answer<Boolean>
        )
        val scrubX = 10f
        val scrubY = 10f
        val down = getMotionEvent(MotionEvent.ACTION_DOWN, scrubX, scrubY, 0, 0)
        assertTrue(scrubGestureDetector.onTouch(null, down))

        // verify single scrub event from handler/runnable timer
        verify(scrubListener).onScrubbed(scrubX, scrubY)
        verifyNoMoreInteractions(scrubListener)
    }

    private fun getMotionEvent(action: Int, x: Float, y: Float, eventTime: Long, downTime: Long): MotionEvent {
        val motionEvent = mock(MotionEvent::class.java)
        `when`(motionEvent.x).thenReturn(x)
        `when`(motionEvent.y).thenReturn(y)
        `when`(motionEvent.eventTime).thenReturn(eventTime)
        `when`(motionEvent.downTime).thenReturn(downTime)
        `when`(motionEvent.actionMasked).thenReturn(action)
        return motionEvent
    }

    companion object {
        private const val TOUCH_SLOP = 8f
    }
}
package nachos.threads;
import java.util.PriorityQueue;
import nachos.machine.*;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * 
	 * 
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */

	public Alarm() {

		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	
	private PriorityQueue<threadTime> waitQueue = new PriorityQueue<threadTime>();

	private class threadTime implements Comparable<threadTime>{
		public KThread thread;
		public long wakeTime;
		public threadTime (KThread thread, long wakeTime) {
			this.thread = thread;
			this.wakeTime = waketime;
		}
		public int compareTo(threadTime thTime) {
			return Long.signum(wakeTime - thTime.wakeTime);
			}
		}
		private KThread thread;
		private long waketime;
	}
	
	public void timerInterrupt() {

		Machine.interrupt().disable();
		long currTime = Machine.timer().getTime();

		while(waitQueue != null && waitQueue.peek().wakeTime <= currTime){
			threadTime thTime = waitQueue.poll();
			KThread thread = thTime.thread;
			if(thread != null)
				thread.ready();
		}
		
		Machine.interrupt().enable();
		KThread.yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		
		// for now, cheat just to get something working (busy waiting is bad)
		Machine.interrupt().disable();
		long wakeTime = Machine.timer().getTime() + x;
		waitQueue.add(new threadTime(KThread.currentThread(), wakeTime));
		KThread.sleep();
		Machine.interrupt().enable();
	}
}

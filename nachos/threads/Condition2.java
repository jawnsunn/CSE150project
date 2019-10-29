package nachos.threads;
import java.util.*;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;

    //create list
    sleep_queue = new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
  boolean intStatus = Machine.interrupt().disable(); //create status

	conditionLock.release();
  //put thread in list
  KThread current = KThread.currentThread();
  sleep_queue.push(current);

  //Put that thread to sleep.
  KThread.sleep();
	conditionLock.acquire();
  //restore machine interrupt
  Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	     Lib.assertTrue(conditionLock.isHeldByCurrentThread());
       boolean intStatus = Machine.interrupt().disable(); //create status

    //Check if threads are in the queue, remove the current from list and get it ready.
      if(!sleep_queue.isEmpty()) {
        KThread awake_thread = conditionQueue.pop(); //pop from the list!
        awake_thread.ready();
      }
      Machine.interrupt().restore(intStatus);

    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	     Lib.assertTrue(conditionLock.isHeldByCurrentThread());
       boolean intStatus = Machine.interrupt().disable(); //create status

       //run while loop to check if there are any threads in list. If there are, then wake all of them
       while(sleep_queue.size() > 0)
        wake();
      Machine.interrupt().restore(intStatus);
    }

    private Lock conditionLock;
    private LinkedList<KThread> sleep_queue;
}

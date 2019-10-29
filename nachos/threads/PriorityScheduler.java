package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
* A scheduler that chooses threads based on their priorities.
*
* <p>
* A priority scheduler associates a priority with each thread. The next thread
* to be dequeued is always a thread with priority no less than any other
* waiting thread's priority. Like a round-robin scheduler, the thread that is
* dequeued is, among all the threads of the same (highest) priority, the
* thread that has been waiting longest.
*
* <p>
* Essentially, a priority scheduler gives access in a round-robin fassion to
* all the highest-priority threads, and ignores all other threads. This has
* the potential to
* starve a thread if there's always a thread waiting with higher priority.
*
* <p>
* A priority scheduler must partially solve the priority inversion problem; in
* particular, priority must be donated through locks, and through joins.
*/
public class PriorityScheduler extends Scheduler {
  /**
  * Allocate a new priority scheduler.
  */
  public PriorityScheduler() {
  }

  /**
  * Allocate a new priority thread queue.
  *
  * @param	transferPriority	<tt>true</tt> if this queue should
  *					transfer priority from waiting threads
  *					to the owning thread.
  * @return	a new priority thread queue.
  */
  public ThreadQueue newThreadQueue(boolean transferPriority) {
    return new PriorityQueue(transferPriority);
  }

  public int getPriority(KThread thread) {
    Lib.assertTrue(Machine.interrupt().disabled());

    return getThreadState(thread).getPriority();
  }

  public int getEffectivePriority(KThread thread) {
    Lib.assertTrue(Machine.interrupt().disabled());

    return getThreadState(thread).getEffectivePriority();
  }

  public void setPriority(KThread thread, int priority) {
    Lib.assertTrue(Machine.interrupt().disabled());

    Lib.assertTrue(priority >= priorityMinimum &&
    priority <= priorityMaximum);

    getThreadState(thread).setPriority(priority);
  }

  public boolean increasePriority() {
    boolean intStatus = Machine.interrupt().disable();

    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMaximum)
    return false;

    setPriority(thread, priority+1);

    Machine.interrupt().restore(intStatus);
    return true;
  }

  public boolean decreasePriority() {
    boolean intStatus = Machine.interrupt().disable();

    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMinimum)
    return false;

    setPriority(thread, priority-1);

    Machine.interrupt().restore(intStatus);
    return true;
  }

  /**
  * The default priority for a new thread. Do not change this value.
  */
  public static final int priorityDefault = 1;
  /**
  * The minimum priority that a thread can have. Do not change this value.
  */
  public static final int priorityMinimum = 0;
  /**
  * The maximum priority that a thread can have. Do not change this value.
  */
  public static final int priorityMaximum = 7;

  /**
  * Return the scheduling state of the specified thread.
  *
  * @param	thread	the thread whose scheduling state to return.
  * @return	the scheduling state of the specified thread.
  */
  protected ThreadState getThreadState(KThread thread) {
    if (thread.schedulingState == null)
      thread.schedulingState = new ThreadState(thread);

    return (ThreadState) thread.schedulingState;
  }

  /**
  * A <tt>ThreadQueue</tt> that sorts threads by priority.
  */
  protected class PriorityQueue extends ThreadQueue {

    protected ArrayList thread_list = new ArrayList(); //wait queue
    protected KThread head_thread = null; //Will be considered the "current" thread of the queue
    protected KThread temp_thread = null; //Used as a temporary holder for waitForAccess so that priority is distributed.

    PriorityQueue(boolean transferPriority) {
      this.transferPriority = transferPriority;
    }

    public void waitForAccess(KThread thread) {
      Lib.assertTrue(Machine.interrupt().disabled());
      thread_list.add(thread); //Thread gets added to wait queue

      for (int i = 0; i < threadList.size(); i++){
        temp_thread = (KThread)thread_list.get(i); //gets the current thread at i and puts it into temporary thread slot
        getThreadState(thread).resetPriority(); //reset effective priority of the thread
      }

      getThreadState(thread).waitForAccess(this);
    }

    public void acquire(KThread thread) {
      Lib.assertTrue(Machine.interrupt().disabled());
      head_thread = thread;
      getThreadState(thread).acquire(this);
    }

    public KThread nextThread() {
      //Lib.assertTrue(Machine.interrupt().disabled());
      boolean intStatus = Machine.interrupt().disable(); //decreasePriority uses this same technique... I like it.

      // implement me
      // check if we have an idle thread, if so, then take it out of the list because it's not what we're looking for.
      if (head_thread != null)
        getThreadState(head_thread).removeQueue(this);

      //if the queue is empty, then make head_thread empty and return the function as null
      if (thread_list == null) {
        head_thread = null;
        Machine.interrupt().restore(intStatus);
        return null;
      }

      head_thread = pickNextThread.thread(); //Moves to 2nd spot of the current queue

      //If the head_thread contains an actual thread, then remove from list and give it access through acquire function call.
      if (head_thread != null) {
        getThreadState(head_thread).resetPriority(); //reset priority
        thread_list.remove(head_thread);
        acquire(head_thread);
      }

      Machine.interrupt().restore(intStatus);
      return head_thread; //Return the head of the CURRENT queue
    }

    /**
    * Return the next thread that <tt>nextThread()</tt> would return,
    * without modifying the state of this queue.
    *
    * @return	the next thread that <tt>nextThread()</tt> would
    *		return.
    */
    protected ThreadState pickNextThread() {
      // implement me
      return null;
    }

    public void print() {
      Lib.assertTrue(Machine.interrupt().disabled());
      // implement me (if you want)
    }

    /**
    * <tt>true</tt> if this queue should transfer priority from waiting
    * threads to the owning thread.
    */
    public boolean transferPriority;
  }

  /**
  * The scheduling state of a thread. This should include the thread's
  * priority, its effective priority, any objects it owns, and the queue
  * it's waiting for, if any.
  *
  * @see	nachos.threads.KThread#schedulingState
  */
  protected class ThreadState {
    /**
    * Allocate a new <tt>ThreadState</tt> object and associate it with the
    * specified thread.
    *
    * @param	thread	the thread this state belongs to.
    */
    public ThreadState(KThread thread) {
      this.thread = thread;

      setPriority(priorityDefault);
    }

    /**
    * Return the priority of the associated thread.
    *
    * @return	the priority of the associated thread.
    */
    public int getPriority() {
      return priority;
    }

    /**
    * Return the effective priority of the associated thread.
    *
    * @return	the effective priority of the associated thread.
    */
    public int getEffectivePriority() {
      // implement me
      return priority;
    }

    /**
    * Set the priority of the associated thread to the specified value.
    *
    * @param	priority	the new priority.
    */
    public void setPriority(int priority) {
      if (this.priority == priority)
      return;

      this.priority = priority;

      // implement me
    }

    /**
    * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
    * the associated thread) is invoked on the specified priority queue.
    * The associated thread is therefore waiting for access to the
    * resource guarded by <tt>waitQueue</tt>. This method is only called
    * if the associated thread cannot immediately obtain access.
    *
    * @param	waitQueue	the queue that the associated thread is
    *				now waiting on.
    *
    * @see	nachos.threads.ThreadQueue#waitForAccess
    */
    public void waitForAccess(PriorityQueue waitQueue) {
      // implement me
    }

    /**
    * Called when the associated thread has acquired access to whatever is
    * guarded by <tt>waitQueue</tt>. This can occur either as a result of
    * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
    * <tt>thread</tt> is the associated thread), or as a result of
    * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
    *
    * @see	nachos.threads.ThreadQueue#acquire
    * @see	nachos.threads.ThreadQueue#nextThread
    */
    public void acquire(PriorityQueue waitQueue) {
      // implement me
    }

    //This is a recursive function used in ThreadState, used to reset priority into effective priority.
    public void resetPriority() {
      effectivePriority = recalcPriority;
    }

    /** The thread with which this object is associated. */
    protected KThread thread;
    /** The priority of the associated thread. */
    protected int priority;
  }
}

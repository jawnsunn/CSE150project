package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	while(listening == 0 || message != null) { 
    		Speak.sleep();//while there are no listeners and no messages sleep speak
    	}
    	lock.acquire();//lock while speaking word
    	message = new Integer(word);
    	lock.release();//release after the word is spoken
    	Listen.wake();//wake up listen thread
    	return;
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	listening++;//adds listening message
    	while(message == null) {//while there are no messages
    		Speak.wake();
    		Listen.sleep();
    	}
    	lock.acquire();//lock while receiving message
    	int MessageReceived = message.intValue();//process message
    	message = 0;//message set to 0
    	lock.release();//release lock after message is received
    	listening--;//remove listening message
	return MessageReceived;//return received message
    }   
    private int listening = 0; // number of people listening
    private Integer message = null; //messages in the Queue
    private Lock lock = new Lock(); //lock system
    private Condition2 Speak = new Condition2(lock);//condition for Speak 
    private Condition2 Listen = new Condition2(lock);//condition for Listen
}

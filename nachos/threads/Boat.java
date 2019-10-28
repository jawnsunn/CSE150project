package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {

	static BoatGrader bg;

	public static void selfTest() {

		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);

	}

	public static void begin(int adults, int children, BoatGrader b) { //Here we will initialze the starting conditions and our logic

		// Store the externally generated autograder in a class
		// variable to be accessible by children.

		bg = b;

		// Instantiate global variables here

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		OahuAd = adults;
		OahuCh = children;

		PeopleOnBoat = Empty;

		IsTheBoatOnOahu = true;

		boatLock = new Lock(); //create a new lock called boatLock that takes from the Lock class

		Runnable r = new Runnable() {
			public void run() {
				SampleItinerary();
			}
		};
		
		KThread t = new KThread(r);
		t.setName("Sample Boat Thread");
		t.fork();

	}

	static void AdultItinerary() {
		/*
		 * This is where you should put your solutions. Make calls to the BoatGrader to
		 * show that it is synchronized. For example: bg.AdultRowToMolokai(); indicates
		 * that an adult has rowed the boat across to Molokai
		 */
	}

	static void ChildItinerary() {
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

	// Creating our Global variables

	static int OahuAd; // amount of adults on Oahu
	static int OahuCh; // amount of children on oahu

	static int PeopleOnBoat; // later we will set this equal to either empty, half, or full

	static final int Empty = 0; // 0 people, or we can just throw this away and use 0
	static final int HalfFull = 1; // 1 person, or we can throw this away and just use 1
	static final int Full = 2; // 2 people, or we can throw this away and just use 2

	static boolean IsTheBoatOnOahu; // will be T or F statement

	static Lock boatLock; // lock for all components

	static Semaphore complete; // this thread/process has finished

	static Condition2 sleepForOahuAd; // puts adults on Oahu to sleep
	static Condition2 sleepForOahuCh; // puts children on Oahu to sleep

	static Condition2 sleepForMoloAd; // puts adults on Molokai to sleep
	static Condition2 sleepForMoloCh; // puts children on Molokai to sleep

}

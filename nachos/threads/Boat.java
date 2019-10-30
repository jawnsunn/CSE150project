package nachos.threads;
import nachos.ag.BoatGrader;
import java.util.LinkedList;

import nachos.machine.*;


public class Boat
{
	static BoatGrader bg;

	public static void selfTest()
	{
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);
	}


	public static void begin( int adults, int children, BoatGrader b )
	{
		bg = b;

		// Instantiate global variables here      
		int childrenOnOahu = children;
		int adultsOnOahu = adults;
		conLock = new Lock();
		waitingAdultsOnOahu = new Condition(conLock);
		waitingChildrenOnOahu = new Condition(conLock);
		waitingChildrenOnMolokai = new Condition(conLock);
		waitingAdultsOnMolokai = new Condition(conLock);

		boatOnMolokai = false;
		boat = empty;


		for(int i = 0; i < adults; i++){
			Runnable r = new Runnable() {
				public void run() {
					AdultItinerary();
				}
			};
			KThread t = new KThread(r);
			t.setName("Adult Thread is on Oahu");
			t.fork(); 	
		}

		for(int i = 0; i < children; i++){
			Runnable r = new Runnable() {
				public void run() {
					ChildItinerary();
				}
			};
			KThread t = new KThread(r);
			t.setName("Child Thread is on Oahu");
			t.fork(); 
		}


	}



	static void AdultItinerary(){	
		conLock.acquire();
		while(true) {
			if(boat == empty && boatOnMolokai == false && childrenOnOahu < 2) {
				adultsOnOahu--;
				bg.AdultRideToMolokai();
				boatOnMolokai = true;
				waitingChildrenOnMolokai.wakeAll();
				waitingAdultsOnMolokai.sleep();
			}
			else
				waitingAdultsOnOahu.sleep();

		}
	}


	static void ChildItinerary(){
		boolean onMolokai = false;
		conLock.acquire();
		while(true) {
			if(onMolokai == false && boatOnMolokai == false && boat == empty) {
				childrenOnOahu--;
				bg.ChildRowToMolokai();
				onMolokai = true;
				if(childrenOnOahu > 0) {
					boat = halfFull;
					waitingChildrenOnOahu.wakeAll();
				}
				else {
					boatOnMolokai = true;
					boat = empty;
					waitingChildrenOnMolokai.wakeAll();
				}
				waitingChildrenOnMolokai.sleep();
			}
			else if(onMolokai == false && boatOnMolokai == false && boat == halfFull) {
				childrenOnOahu--;
				bg.ChildRideToMolokai();
				onMolokai = true;
				boatOnMolokai = true;
				boat = empty;
				waitingChildrenOnMolokai.wakeAll();
				waitingChildrenOnMolokai.sleep();
			}
			else if(onMolokai == false && boatOnMolokai == false && boat == full)
				waitingChildrenOnOahu.sleep();
			else {
				if(boatOnMolokai == true) {
					childrenOnOahu++;
					bg.ChildRideToOahu();
					boatOnMolokai = false;
					waitingAdultsOnOahu.wakeAll();
					waitingChildrenOnOahu.wakeAll();
				}
			}
		}
	}

	static void SampleItinerary(){
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

	private static Condition waitingAdultsOnOahu;
	private static Condition waitingChildrenOnOahu;
	private static Condition waitingAdultsOnMolokai;
	private static Condition waitingChildrenOnMolokai;
	private static Lock conLock;

	private static boolean boatOnMolokai;

	private static int adultsOnOahu, childrenOnOahu;
	private static int boat, empty = 0, halfFull = 1, full = 2; 

}

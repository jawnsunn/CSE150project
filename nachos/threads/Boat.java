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
		int totalOfChildren = children;
		int totalOfAdults = adults;
		conLock = new Lock();
		adultsOnOahu = new Condition(conLock);
		childrenOnOahu = new Condition(conLock);
		childrenOnMolokai = new Condition(conLock);
		waitingOnBoat = new Condition(conLock);
		bProb = new Condition(conLock);
		childWaiting = new LinkedList<KThread>();
		location = "Oahu";
		
		bLocationOahu = true;
		boat = empty;
		

		rChildrenOnMolokai = 0;
		rAdultsOnMolokai = 0;     
		rChildrenOnOahu = 0; 
		rAdultsOnOahu = 0;	
		lChildrenOnOahu = 0;


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

		done = false;
		waitingNext = false;
		nextToBeOnBoat = false;
		onBoat = false;

		conLock.acquire();

		if(locationOahu == false && rAdultsOnMolokai == totalOfAdults && rChildrenOnMolokai == totalOfChildren){
			isDone = true;
		}
		else{
			isDone = false;
		}

		while(isDone == false){ 
			bProb.sleep();
		}

		done = true; 
		childrenOnOahu.wakeAll();
		childrenOnMolokai.wakeAll();
		adultsOnOahu.wakeAll();
		conLock.release();
	}



	static void AdultItinerary()
	{	
		conLock.acquire();
		rAdultsOnOahu++; 	
		
		while(locationOahu == true) {
			if(boat == empty && bLocationOahu && totalOfChildren <=1) {
				locationOahu = false;
				totalOfAdults--;
				bLocationOahu = false;
				bg.AdultRowToMolokai();
				if(totalOfAdults == 0 && totalOfChildren == 0) {
					adultsOnOahu.sleep();
				}
				childrenOnMolokai.wakeAll();
				adultsOnOahu.sleep();
			}
			else
				adultsOnOahu.sleep();
		}
		childrenOnMolokai.wake();
		conLock.release();
	}



	static void ChildItinerary(){
		conLock.acquire();
		rChildrenOnOahu++;

		if(locationOahu == true){
			if( KThread.currentThread().getName() == "Child Thread is on Oahu" && childWaiting.size() < 2){
				caseOfChildren = 1;
			}
			else{
				caseOfChildren = 0;
			}
		}
		else{
			if(KThread.currentThread().getName() == "Child Thread on Molokai"){
				caseOfChildren = 1;
			}
			else{
				caseOfChildren = 0; 
			}
		}

		while(!done){
			while(caseOfChildren == 0){      
				if(location == "Oahu"){
					childrenOnOahu.wake(); 
					childrenOnMolokai.sleep();
				}
				else{
					childrenOnMolokai.wake(); 
					childrenOnOahu.sleep();
				}
				adultsOnOahu.wake(); 
			}	

			if(!done){
				if(locationOahu == true){
					if(!waitingNext){ 
						rChildrenOnOahu--;
						lChildrenOnOahu = rChildrenOnOahu;
						lAdultsOnOahu = rAdultsOnOahu;
						bg.ChildRowToMolokai();	
						KThread.currentThread().setName("Child Thread is on the Boat");
						waitingNext = true; 
						nextToBeOnBoat = true;
						childWaiting.add(KThread.currentThread());
					}
					else{
						rChildrenOnOahu--; 	
						lChildrenOnOahu = rChildrenOnOahu;
						lAdultsOnOahu = rAdultsOnOahu;
						bg.ChildRideToMolokai();
						location = "Molokai"; 	
						KThread.currentThread().setName("Child Thread is on Molokai");
						KThread firstChild = childWaiting.removeFirst();
						firstChild.setName("Child Thread is on Molokai");
						rChildrenOnMolokai = rChildrenOnMolokai + 2;
						waitingNext = false;
					}
				}
				else{ 
					bg.ChildRowToOahu();
					rChildrenOnOahu++;
					KThread.currentThread().setName("Child Thread is on Oahu");
					location = "Oahu";
				}

				if(locationOahu == false && lAdultsOnOahu == 0 && lChildrenOnOahu == 0){
					almostDone = true;
				}
				else{
					almostDone = false;
				}

				if(almostDone){    
					bProb.wake();
					adultsOnOahu.wake(); 
					childrenOnMolokai.sleep();
				}
				else{
					if(nextToBeOnBoat){
						childrenOnOahu.wake();
						nextToBeOnBoat = false;
						onBoat = true;
						waitingOnBoat.sleep();			
					}
					else{
						if(locationOahu == true){
							childrenOnOahu.wake();
							adultsOnOahu.wake();
							childrenOnOahu.sleep();
						}
						else{
							if(onBoat){
								waitingOnBoat.wake();
								onBoat = false;
							}
							childrenOnMolokai.wake();	
							adultsOnOahu.wake();
							childrenOnMolokai.sleep();  
						}
					}
				}
			}
		}
		conLock.release();
	}

	static void SampleItinerary(){
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

	private static Condition bProb;
	private static Condition adultsOnOahu;
	private static Condition childrenOnOahu;
	private static Condition childrenOnMolokai;
	private static Condition waitingOnBoat;
	private static Lock conLock;
	private static String location; 
	private static LinkedList<KThread> childWaiting;

	private static boolean locationOahu;
	
	private static boolean done;
	private static boolean waitingNext; 
	private static boolean nextToBeOnBoat;
	private static boolean onBoat;
	private static boolean isDone;
	private static boolean almostDone;
	private static boolean bLocationOahu;

	private static int totalOfAdults, totalOfChildren;
	private static int caseOfChildren;
	private static int boat, empty = 0; 
	private static int lAdultsOnOahu;
	private static int lChildrenOnOahu;
	private static int rChildrenOnOahu; 
	private static int rAdultsOnOahu;   
	private static int rChildrenOnMolokai;
	private static int rAdultsOnMolokai;

}

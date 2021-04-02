package stock_executor;

import java.util.concurrent.Semaphore;

public class clientThread extends Thread{
	//class for executing threads
	private String name;
	private int time;

	private int amount; 
	public clientThread(String name, int time, int amount){
		this.name = name;
		this.time = time;
		this.amount = amount;
	}

	//control input / output stream
	//Control input output stream
	//Assign orders - need to communicate with client to see if it needs to be purchased
	//get stock price from the client --> give the client the orders and then the client can decide which orders to take
	
	//Send messages
	//Mediate availability
	public void run() {
		try {
			//putting thread to sleep until needed
			long sleep_time = time * 1000;
			Thread.sleep(sleep_time);
			//acquiring semaphore
			//sem.acquire();
			//if > 0, purchase
			if (this.amount > 0) {
				System.out.println("[" + java.time.LocalTime.now() + "] Starting purchase of " + amount 
						+ " shares of " + name);
			}
			//else sell
			else {
				int curr_ = -amount;
				System.out.println("[" + java.time.LocalTime.now() + "] Starting sale of " + curr_ 
						+ " shares of " + name);	
			}
			//sleeping for 1 second
			Thread.sleep(1000);
			//printing out when finished
			if (amount > 0) {
				System.out.println("[" + java.time.LocalTime.now() + "] Finished purchase of " + amount 
						+ " shares of " + name);
			}
			else {
				int curr_ = -amount;
				System.out.println("[" + java.time.LocalTime.now() + "] Finished sale of " + curr_ 
						+ " shares of " + name);	
			}
			//releasing semaphore
			//sem.release();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}

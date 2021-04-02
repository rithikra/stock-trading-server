package stock_executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class serverThread extends Thread{
	//class for executing threads
	private static ConcurrentHashMap<Integer, Boolean> availability = new ConcurrentHashMap<>(); 
	private ArrayList<Socket> s;
	private static server server_;
	private ArrayList<brokerTask> tasks;
	private int time;
	private static ArrayList<Double> balances;
	private static ArrayList<brokerTask>  incomplete_trades;

	public serverThread(ArrayList<Socket> sockets, server server_, ArrayList<brokerTask> tasks, int time, 
			Map<Integer, Boolean> availability, ArrayList<Double> balances){
		this.s = sockets;
		this.server_ = server_;
		this.tasks = tasks;
		this.time = time;
		if (serverThread.availability.isEmpty()) {
			incomplete_trades = new ArrayList<brokerTask>();
			System.out.println("balances is being updated - if u see this more than once, program has error");
			for (Entry<Integer, Boolean> it: availability.entrySet()) {
				serverThread.availability.put(it.getKey(), it.getValue());	
				System.out.println("AVAILABILITY TESTING: " + serverThread.availability.get(it.getKey()));
			}
			//serverThread.availability = (ConcurrentHashMap<Integer, Boolean>) availability;
			serverThread.balances = balances;
		}
		//this.start();
		
	}
	public ArrayList<brokerTask> getTaskList(){
		return tasks;
	}
	public synchronized void receiveMessage(String line, traderThread current, int trader) {
		//if (line == "done"){
			//availability.remove(trader);
			availability.replace(trader, true);
		//}			
	}

	//control input / output stream
	//Control input output stream
	//Assign orders - need to communicate with client to see if it needs to be purchased
	//get stock price from the client --> give the client the orders and then the client can decide which orders to take
	
	//Send messages
	//Mediate availability
	
	//create receive message functionality
	
	//insert read message function
	@SuppressWarnings("static-access")
	public void run() {
		try {
			/*for (int i = 0; i < tasks.size(); i++) {
				System.out.println("Current Company: " + tasks.get(i).getCompany());
				System.out.println("Current Date: " + tasks.get(i).getDate());
				System.out.println("Current Amount: " + tasks.get(i).getAmount());
				System.out.println();
				System.out.println();
			}*/
			Thread.sleep(time * 1000);
			int tasks_assigned = 0;
			ArrayList<brokerTask> traderThreadTasks = new ArrayList<brokerTask>();
			ArrayList<traderThread> allThreads = new ArrayList<traderThread>();
			//int current_assigned_task = 0;
			
			//iterate through each task
			int assignedTrader = 0;
			//int same_trader = 0;
			while (tasks_assigned <  tasks.size()  && assignedTrader < balances.size()){
				System.out.println("Testing availability for trader: " + assignedTrader);
				for (Entry<Integer, Boolean> it: availability.entrySet()) {
					//.availability.put(it.getKey(), it.getValue());	
					System.out.println("AVAILABILITY TESTING: " +  it.getKey() + "  " + it.getValue());
				}
				while (availability.get(assignedTrader) == false) {
					assignedTrader++;
					if(assignedTrader >= balances.size()) {
						break;
					}
					//continue;
				}
				brokerTask current_task = tasks.get(tasks_assigned);
				double traderBalance = balances.get(assignedTrader);
				double stockPrice = server_.stockPrice(current_task.getCompany(), current_task.getDate());
				double totalCost = stockPrice * current_task.getAmount();
				//could potentially optimize algorithm - is unoptimized right now with lots of incomplete trades - fix this later
				if (totalCost < traderBalance) {
					//System.out.println("inside if statement");
					traderThreadTasks.add(current_task);
					tasks_assigned++;
					if (totalCost > 0) {
						balances.set(assignedTrader, traderBalance - totalCost);
					}	
				}
				else {
					//create new  thread
					if (assignedTrader < balances.size()) {
						//availability.remove(assignedTrader);
						availability.replace(assignedTrader, false);
					}
					traderThread newThread = new traderThread(traderThreadTasks, s.get(assignedTrader),
							server_, this, assignedTrader );
					allThreads.add(newThread);
					traderThreadTasks.clear();
					assignedTrader++;
					//clear thread tasks array
					//assigned traders++
				}
				
				
				
			
				
			}
			if (!traderThreadTasks.isEmpty() && assignedTrader < balances.size()) {
				availability.remove(assignedTrader);
				availability.put(assignedTrader, false);
				traderThread newThread = new traderThread(traderThreadTasks, s.get(assignedTrader), server_, this, assignedTrader);
				allThreads.add(newThread);
			}
			for (int i = tasks_assigned; i < tasks.size(); i++) {
				incomplete_trades.add(tasks.get(i));
			}
			for (int i = 0; i < allThreads.size(); i++) {
				allThreads.get(i).start();
				allThreads.get(i).join();
			}
			System.out.println("TASKS ASSIGNED: " + tasks_assigned);
			System.out.println("TASKS WHICH NEEDED TO BE ASSIGNED: " +  tasks.size());
			for (int i = 0; i < incomplete_trades.size(); i++) {
				brokerTask current_task = incomplete_trades.get(i);
				System.out.println("INCOMPLETE TASKS: ");
				System.out.println("Current Company: " + current_task.getCompany());
				System.out.println("Current Date: " + current_task.getDate());
				System.out.println("Current Amount: " + current_task.getAmount());
				System.out.println("Current Time: " + current_task.getTime());
				System.out.println();
				System.out.println();
			}
			
			//add remaining incomplete trades  here after while loop is over
			
			//iterate and run through everything
			/*
			
			while (tasks_assigned < tasks.size()) {
				
				boolean added = false;
				for (int j = 0; j < balances.size(); j++){
					
				//System.out.println("SERVER THREAD RUNNING AND for lopp working");
					if (availability.get(j) == true) {
						boolean contnue = true;
						while (contnue) {
						
					//System.out.println("availability true");
							traderThreadTasks.clear();
						
							//System.out.println("inside while loop");
							double curr_balance = balances.get(j);
							//System.out.println("Task currently being assigned: " + tasks_assigned + " / " + tasks.size() );
							
							if (tasks_assigned >= tasks.size()) {
								break;
							}
							brokerTask current_task = tasks.get(tasks_assigned);
							System.out.println("CURRENT TASK: " + current_task.getCompany() + " " + current_task.getDate());
							double currStockPrice = server_.stockPrice(current_task.getCompany(), current_task.getDate());
							//System.out.println("total stock price " + currStockPrice);
							double total = currStockPrice * current_task.getAmount();
							//System.out.println("current balance: " + curr_balance);
							//System.out.println("total cost: " + total);
							if (total < curr_balance) {
								added = true;
								//System.out.println("inside if statement");
								traderThreadTasks.add(current_task);
								tasks_assigned++;
								if (total > 0) {
									balances.set(j, curr_balance - total);
								}
								//System.out.println("NEW BALANCE: " + balances.get(j));
								//System.out.println("CURRENT TOTAL: " + total);
							}
							else {
								//System.out.println("SERVER THREAD RUNNING AND CREATED NEW THREAD");
								//System.out.println("CURRENT TOTAL: " + total);
								//System.out.println("TASK LIST SIZE: " + traderThreadTasks.size());
								traderThread newThread = new traderThread(traderThreadTasks, s.get(j),
										server_, this, j);
								contnue = false;
								newThread.join();
								newThread.start();
							}
						}
					}
				}
				if (!added) {
					tasks_assigned++;
				}
			}*/
			//System.out.println("TOTAAL TRADER TASKS LEFT: " + traderThreadTasks.size());
			/*if (traderThreadTasks.size() > 0) {
				//System.out.println("TASK LIST SIZE: " + traderThreadTasks.size());
				//System.out.println("appendding last server thread");
				traderThread newThread = new traderThread(traderThreadTasks, s.get(1),
					server_, this, 1);
				newThread.start();
			}*/
			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

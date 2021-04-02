package stock_executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class server{
	static Map<Integer, Boolean> availability; 
	public static double stockPrice(String symbol, String date) throws IOException {
		String linestring = "";
		//String tempurl = "https://api.tiingo.com/tiingo/daily/" + symbol.toLowerCase() + "/prices?startDate=" + date + "&token=41abedc5390bd43a21c1b743bdfdbb54f0443157")
		/*URL url = new URL("https://api.tiingo.com/tiingo/daily/aapl/prices?startDate=2019-01-02&token=41abedc5390bd43a21c1b743bdfdbb54f0443157");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("accept", "application/json");
		Scanner sc = new Scanner(url.openStream());
		String inline = new String();
		while (sc.hasNext()) {
			inline += sc.nextLine();	
		}
		JSONParser parse = new JSONParser();
		JSONObject jobj = new JSONObject();
		JSONArray jarray = new JSONArray();
		try {
			jarray = (JSONArray)parse.parse(inline);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//JSONArray jsonarr_1 = (JSONArray) jobj.get(' '); 

		JSONObject test = (JSONObject) jarray.get(0);
		System.out.println(test.get("close"));
		
		//fix and return the correct value
		return (double) test.get("close");*/
		return 100;
	}
	
	private static List<serverThread> threads_ = new ArrayList<serverThread>();
	public static String readInput(String filename) throws Exception {
		// Create a File instance
		java.io.File file = new java.io.File(filename);

		// Create a scanner for the file
		Scanner input = new Scanner(file);

		// Read data from the file and append to stringBuilder
		StringBuilder stringBuilder = new StringBuilder();
		while (input.hasNext()) {
			String line = input.nextLine();
			stringBuilder.append(line);
		}
		input.close();
		return stringBuilder.toString();
	}
	public void execute(taskList current_trades) throws IOException, InterruptedException {
		traderReader tr = new traderReader();
		tr.read();
		ArrayList<trader> allTraders = tr.getAllTraders();
		int traderCount = tr.traderCount();
		int connections = 0;
		ServerSocket ss = new ServerSocket(3456);
		System.out.println("Listening on port 3456");
		System.out.println("Waiting for traders...");
		ArrayList<Socket> allConnections = new ArrayList<Socket>();
		while (allConnections.size() < traderCount) {
			try {
				Socket s = ss.accept();
				System.out.println("Connection from: " + s.getInetAddress());
				allConnections.add(s);
			}
			catch (IOException ioe) {
				System.out.println("ioe in Server acception: " + ioe.getMessage());
			}
		}
		//assign orders here - iterate through each trade 
		
		//assigning initial availability
		availability = new HashMap<>();
		for (int k = 0; k < traderCount; k++) {
			availability.put(k, true);
		}
		//assigning balances

		ArrayList<Double> balances = new ArrayList<Double>();
		for (int k = 0; k < traderCount; k++) {
			balances.add(allTraders.get(k).getBalance());
		}
		//grouping all tasks together to be done at the same time
		//int i = 0;
		ArrayList<brokerTask> timeSegmentedTasks = new ArrayList<brokerTask>();
		ArrayList<serverThread> allThreads = new ArrayList<serverThread>();
		int curr_time = 0;
		System.out.println("Starting code execution...");
		/*for (int i = 0; i < current_trades.getTasks().size(); i++) {
			System.out.println("Current Company: " + current_trades.getTasks().get(i).getCompany());
			System.out.println("Current Date: " + current_trades.getTasks().get(i).getDate());
			System.out.println("Current Amount: " + current_trades.getTasks().get(i).getAmount());
			System.out.println();
			System.out.println();
		}*/
		boolean initial_task = true;
		int current_time = 0;
		List<brokerTask> taskList = current_trades.getTasks();
		int taskListSize = taskList.size();
		for (int a = 0; a < taskListSize; a++) {
			brokerTask currentTask = taskList.get(a);
			if (initial_task) {
				current_time = currentTask.getTime();
				timeSegmentedTasks.add(currentTask);
				initial_task = false;	
			}
			else if (currentTask.getTime() == current_time) {
				timeSegmentedTasks.add(currentTask);
			}
			else {
				ArrayList<brokerTask> copy = new ArrayList<brokerTask>();
				for (int c = 0; c < timeSegmentedTasks.size(); c++) {
					brokerTask current_task = timeSegmentedTasks.get(c);
					brokerTask copyTask = new brokerTask();
					copyTask.setAmount(current_task.getAmount());
					copyTask.setCompany(current_task.getCompany());
					copyTask.setDate(current_task.getDate());
					copyTask.setTime(current_task.getTime());
					copy.add(copyTask);
				}
				/*for (int b = 0; b < copy.size(); b++) {
					System.out.println("Current Company: " + copy.get(b).getCompany());
					System.out.println("Current Date: " + copy.get(b).getDate());
					System.out.println("Current Amount: " + copy.get(b).getAmount());
					System.out.println("Current Time: " + copy.get(b).getTime());
					System.out.println();
					System.out.println();
				}*/
				serverThread timeThread = new serverThread(allConnections, this, copy, current_time, availability, balances);
				timeSegmentedTasks.clear();
				allThreads.add(timeThread);
				current_time = currentTask.getTime();
				timeSegmentedTasks.add(currentTask);	
			}
				
		}
		
		if  (timeSegmentedTasks.size() > 0) {
			int timeInput = timeSegmentedTasks.get(0).getTime();
			serverThread timeThread = new serverThread(allConnections, this, timeSegmentedTasks, timeInput, availability, balances);
			allThreads.add(timeThread);
			
		}
		System.out.println("BEFORE EXECUTING THREADS EVALUATION");
		System.out.println();
		System.out.println();
		for (int a = 0; a < allThreads.size(); a++) {
			ArrayList<brokerTask> current_thread = allThreads.get(a).getTaskList();
			int timesegmentation = current_thread.get(0).getTime();
			System.out.println("TIME SEGMENTATION: " + timesegmentation);
			for (int b = 0; b < current_thread.size(); b++) {
				System.out.println("Current Company: " + current_thread.get(b).getCompany());
				System.out.println("Current Date: " + current_thread.get(b).getDate());
				System.out.println("Current Amount: " + current_thread.get(b).getAmount());
				System.out.println("Current Time: " + current_thread.get(b).getTime());
				System.out.println();
				System.out.println();
			}
			allThreads.get(a).start();
			//allThreads.get(a).join();
		}
			/*
			if (timeSegmentedTasks.isEmpty()) {
				curr_time = current_trades.getTasks().get(i).getTime();
				timeSegmentedTasks.add(current_trades.getTasks().get(i));
			}
			else {
				brokerTask currTask = current_trades.getTasks().get(i);
				if (currTask.getTime() == curr_time) {
					timeSegmentedTasks.add(current_trades.getTasks().get(i));
				}
				else {
					for (int y = 0; y < timeSegmentedTasks.size(); y++) {
						System.out.println("Current Company: " +timeSegmentedTasks.get(y).getCompany());
						System.out.println("Current Date: " + timeSegmentedTasks.get(y).getDate());
						System.out.println("Current Amount: " + timeSegmentedTasks.get(y).getAmount());
						System.out.println();
						System.out.println();
					}
					ArrayList<brokerTask> copy = timeSegmentedTasks;
					serverThread timeThread = new serverThread(allConnections, this, copy, 
							curr_time, availability, balances);
					timeSegmentedTasks.clear();
					timeThread.start();
					//timeThread.join();
				}
				
			}*/
		
	}

	public static void main (String [] args) throws IOException, InterruptedException {	
		
		stockPrice("APPL", "DAFSDF");
		//creating list of threads
		server test = new server();
		//threads_ = new ArrayList<serverThread>();

		//getting list of current trades
		taskList current_trades = new taskList();
		test.execute(current_trades);
		//executing function
		
		
		//creating a temp list
		//List<stock> temp_list = new ArrayList<stock>();
		//setting all semaphores manually
		/*for (int i = 0; i < stockList.size(); i++) {
			stock current_stock = stockList.get(i);
			current_stock.setSem();
			temp_list.add(current_stock);
		}*/
		//asssigning stock list to temp list
		//stockList = temp_list;
		
		//iterating through each trade
		
		//connecting to server
		
		/*
		int i = 0;
		while (i < current_trades.getTasks().size()) {
			
					
					
				}
					
			}
			while(traderTaskList.size() > 1) {
				for (int i = 0; i < allTraders.size(); i++) {
					int curr_balance = allTraders.get(i).getBalance();
							
				}
			ArrayList<brokerTask> traderTaskList = new ArrayList<brokerTask>();
			brokerTask current_task = current_trades.getTasks().get(i);
			i++;
			
				

				
			}
			*/
			
		
		/*
		for (int i = 0; i < current_trades.getTasks().size(); i++) {
			brokerTask current_task = current_trades.getTasks().get(i);
			serverThread current_thread = new serverThread(current_task.getCompany(), current_task.getTime(), current_task.getAmount());
			threads_.add(current_thread);
			}*/
		
		
		//executing each thread - algorithm to execute trades but how to assign and get price from client
		
		/*for (int i = 0; i < threads_.size(); i++) {
			//System.out.println(i);
			threads_.get(i).start();
		}
		//ensuring that threads wait to finish before executing
		for (int i = 0; i < threads_.size(); i++) {
			try {
				threads_.get(i).join();
			}
			//catching error
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		//printing end statement
		System.out.println("All tasks have finished");
		
		
		
		
		
	}

}

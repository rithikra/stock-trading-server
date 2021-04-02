package stock_executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class client extends Thread{
	private PrintWriter pw;
	private ArrayList<brokerTask> tasks;
	//private ObjectInputStream is;
	private Socket s; 
	private BufferedReader br;
	
	public client(String hostname, int port) {
		try {
			System.out.println("Trying to connect to " + hostname + ": " + port);
			s = new Socket(hostname, port);
			System.out.println("Connected to " + hostname + ": " + port);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			this.start();
			System.out.println("TILL HERE");
			Scanner scan = new Scanner(System.in);
			//add in messages about starting
			
		} 
		catch (IOException ioe) {
			System.out.println("ioe in client constructor: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	public void run() {
		try {
			while(true) {
				//System.out.println("runnign till here");
				String line = br.readLine();
				//System.out.println(line);
				//System.out.println(line);
				//System.out.println("AFTER B");
				if (line != null){
					System.out.println(line);
					//System.out.println("MESSAGE RECEIVED");
					Gson gson = new Gson();
			        ArrayList<brokerTask> tasks = gson.fromJson(line,
			               new TypeToken<ArrayList<brokerTask>>() {}.getType());
					//JSONArray tasks = line.toKS
			        //System.out.print(tasks);
					//is = new ObjectInputStream(s.getInputStream());
					/*tasks = (ArrayList<brokerTask>) is.readObject();*/
					//System.out.println("TILL HERE ADFSJADFAJSFDAFDSF");
					//create for loop for assigned tasks and then execution
					for (int i = 0; i < tasks.size(); i++) {
						brokerTask curr_task = tasks.get(i);
						int amount = curr_task.getAmount();
						String name = curr_task.getCompany();
						//have to insert lines with total amount
						if (amount > 0) {
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
					}
					pw.println("done");
					pw.flush();
				}
				
			}
		} catch (IOException ioe) {
			System.out.println("ioe in Client.run(): " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String [] args) {
		System.out.println("Welcome to SalStocks 2.0");
		System.out.println("Please enter the client and host");
		//insert code to look at the the input - not done yet
		client cc = new client("localhost", 3456);
	}
}
	


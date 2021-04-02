

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class mainServer {
	
	private static List<tradingBot> threads_ = new ArrayList<tradingBot>();
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
	
	public static Slist read_file() throws IOException {
    	Slist stock_list = new Slist();
		String path = null;
		boolean file_exists = false;
		//while there is input file
		while (!file_exists) {
			try {
				//asking for file name
				System.out.println("What is the name of the file containing company information?");
				InputStreamReader isr = new InputStreamReader(System.in);
				BufferedReader test = new BufferedReader(isr);
				String current = test.readLine();
				path = current;
				java.io.File file_ = new java.io.File(path);
				if (!file_.exists()){
					System.out.println("File does not exist.");
					continue;
				}
			}
			catch (IOException e){
				System.out.print("Invalid file");
			}
			try {
				//converting from JSon to GSON
				Gson converter = new GsonBuilder().setPrettyPrinting().create();
				String str = readInput(path);
				stock_list = converter.fromJson(str, Slist.class);
			} 
			catch (Exception e) {
				System.out.println("File could not be parsed");
			}
			finally {
				if (stock_list != null) {
					//setting to true to ensure it worked
					file_exists = true;
					return stock_list;
				}
			}
		}
		return null; 
    	
    }
	public static void main (String [] args) {	
		//creating list of threads
		threads_ = new ArrayList<tradingBot>();

		Slist data_ = new Slist();
		
		try {
			//getting stock data
			data_ = read_file();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	
		//getting stock list of data
		List<stock> stockList = data_.getData();
		//getting list of current trades
		taskList current_trades = new taskList();
		//executing function
		System.out.println("Starting code execution...");
		
		//creating a temp list
		List<stock> temp_list = new ArrayList<stock>();
		//setting all semaphores manually
		for (int i = 0; i < stockList.size(); i++) {
			stock current_stock = stockList.get(i);
			current_stock.setSem();
			temp_list.add(current_stock);
		}
		//asssigning stock list to temp list
		stockList = temp_list;
		
		//iterating through each trade
		for (int i = 0; i < current_trades.getTasks().size(); i++) {
			brokerTask current_task = current_trades.getTasks().get(i);
			boolean stock_exists = false; 
			Semaphore curr_sem = new Semaphore(1);
			//checking if the stock exists
			for (int j = 0; j < stockList.size(); j++) {
				String curr_name = stockList.get(j).getTicker();
				if (curr_name.toLowerCase().equals(current_task.getCompany().toLowerCase())) {
					stock_exists = true;
					curr_sem = stockList.get(j).getSem();
				}
			}
			//if != exist --> print company does not exist and no execution
			if (!stock_exists) {
				System.out.println(current_task.getCompany() + " is not a company");
				continue;
			}
			//otherwise add to list of threads
			else {
				tradingBot current_thread = new tradingBot(current_task.getCompany(), current_task.getTime(), 
						curr_sem, current_task.getAmount());
				threads_.add(current_thread);
			}
			
		}
		//executing each thread
		for (int i = 0; i < threads_.size(); i++) {
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
		}
		//printing end statement
		System.out.print("All tasks have finished");
		
		
		
		
		
	}

}

package stock_executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class traderReader {
	private ArrayList<trader> allTraders;
	public void read() {
		//ArrayList <trader> tempTraders = new ArrayList<trader>();
		boolean valid_file = false;
    	while (!valid_file) {
	    	try {
	    		//code source: https://stackabuse.com/reading-and-writing-csvs-in-java/
	    		System.out.println("What is the name of the file containing the trader information?");
	    		InputStreamReader isr = new InputStreamReader(System.in);
				BufferedReader test = new BufferedReader(isr);
				String current = test.readLine();
				allTraders = new ArrayList<trader>();
				String path = current;
				File csvFile = new File(path);
				//if the file does not exist, ask again
				if (!csvFile.isFile()) {
					System.out.println("File does not exist!");
					continue;
				}
				else {
					//otherwise input into csv
					BufferedReader csvReader = new BufferedReader(new FileReader(path));
					String row;
					//iterating through csv
					while ((row = csvReader.readLine()) != null) {
					    String[] data = row.split(",");
					    //creating new broker task object for each line
					    trader curr = new trader(0, 0);
					    curr.setId(Integer.parseInt(data[0]));
					    curr.setBalance(Double.parseDouble(data[1])); 
					    this.allTraders.add(curr);
					}
					//closing csv reader
					csvReader.close();
					//ending while loop by setting to true
					valid_file = true;
				}	
	    	}
	    	catch (IOException e){
				System.out.print("Invalid file");
			}
    	}
	}
	public ArrayList<trader> getAllTraders() {
		return allTraders;
	}
	public void setAllTraders(ArrayList<trader> allTraders) {
		this.allTraders = allTraders;
	}
	public int traderCount() {
		return allTraders.size();
	}

}

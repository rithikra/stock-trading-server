package stock_executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class taskList {
	//list of tasks to be executed
	List<brokerTask> tasks;
	public List<brokerTask> getTasks() {
    	return tasks;
    }

    public void setTasks(List<brokerTask> data) {
    	this.tasks =  data;
    }
    
    public taskList() {
    	this.tasks = new ArrayList<brokerTask>();
    	boolean valid_file = false;
    	while (!valid_file) {
	    	try {
	    		//code source: https://stackabuse.com/reading-and-writing-csvs-in-java/
	    		System.out.println("What is the name of the file containing the schedule information?");
	    		InputStreamReader isr = new InputStreamReader(System.in);
				BufferedReader test = new BufferedReader(isr);
				String current = test.readLine();
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
					    brokerTask curr = new brokerTask();
					    curr.setTime(Integer.parseInt(data[0]));
					    curr.setCompany(data[1]);
					    curr.setAmount(Integer.parseInt(data[2]));  
					    curr.setDate(data[3]);
					    this.tasks.add(curr);
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

}

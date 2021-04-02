package stock_executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class traderThread extends Thread{
	private BufferedReader br;
	private PrintWriter pw;
	//private ObjectOutputStream os;
	private ArrayList<brokerTask> tasks;
	private Socket s;
	private server server_;
	private serverThread currentThread;
	private int trader;
	public traderThread(ArrayList<brokerTask> tasks, Socket s, server server_, serverThread currentThread, int trader) throws IOException {
		this.s = s;
		this.tasks = tasks;
		this.server_ = server_;
		this.trader = trader;
		this.currentThread = currentThread;
		this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.pw = new PrintWriter(s.getOutputStream());
		//this.os = new ObjectOutputStream(s.getOutputStream());
		//this.start();
	}
	/*public void sendObject(ArrayList<brokerTask> schedule) throws IOException {
		os.writeObject(schedule);
		os.flush();
	}*/
	public void run() {
		//System.out.println("TRADER THREAD WORKING");
		try {
			//Gson gson = new GsonBuilder().create();
			//JSONArray new_tasks = new JSONArray();
			/*for (int i = 0; i < tasks.size(); i++) {
				JSONObject curr_task = new JSONObject();
				brokerTask reference = tasks.get(i);
				curr_task.put("company", reference.getCompany());
				curr_task.put("date", reference.getDate());
				curr_task.put("amount", reference.getAmount());
				((JSONArray) new_tasks).put(curr_task);
			}*/
			/*System.out.println(tasks);
		    String json__ = new Gson().toJson(tasks);
		    System.out.print(json__);*/
		    

			Gson gson = new GsonBuilder().create();
			JsonArray new_tasks = gson.toJsonTree(tasks).getAsJsonArray();
			System.out.println(new_tasks.toString());
			//JSONArray new_tasks = tasks.toJSONARRAY();
			String test = new_tasks.toString();
			this.sendMessage(test);
			//this.sendObject(tasks);
			while (true) {
				String line = br.readLine();
				if(server_ == null) {
					break;
				}
				if (line !=  null) {
					System.out.println("MESSSSAGE RECEIVED");
					System.out.println(line);
					currentThread.receiveMessage(line, this, trader);
				}
				break;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	private synchronized void sendMessage(String string) {
		pw.println(string);
		pw.flush();
	}
}

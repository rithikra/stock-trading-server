package stock_executor;

public class trader {
	public trader(int id, double balance){
		this.id = id;
		this.balance = balance;
	}
	private int id;
	private double balance;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}

}

public class Message {
	public static Integer counter = 0;
	private String m;
	private Integer id;
	private boolean delivered;

	public Message(String m) {
		this.m = m;
		Message.counter += 1;
		this.id = Message.counter;
		this.delivered = false;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public Integer getId() {
		return id;
	}
	
	public boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

}

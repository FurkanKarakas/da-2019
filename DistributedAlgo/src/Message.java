public class Message {
	public static Integer counter = 0;
	private String m;
	private Integer id;

	public Message(String m) {
		this.m = m;
		Message.counter += 1;
		this.id = Message.counter;
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

}


//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class Message implements Serializable {
	private static final long serialVersionUID = 1423627893178345L;
	public static Integer counter = 0;
	private String m;
	private Integer id;
	private boolean delivered;

	public Message(String m, Integer port, InetAddress inetAddr) {
		this.m = m;
		Message.counter += 1;
		this.id = Message.counter;
		this.delivered = false;
	}

	public Message(String m, Integer port, InetAddress inetAddr, Integer id) {
		// For acknowledgments
		this.m = m;
		this.id = id;
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

	public int hashCode() {
		return Objects.hash(id, m, delivered);
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;

		Message msg2 = (Message) o;
		return id == msg2.id;
	}

}

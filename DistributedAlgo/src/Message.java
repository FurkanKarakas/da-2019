
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
	private boolean isAck;
/*
	public Message(String m, Integer port, InetAddress inetAddr, boolean isAck) {
		this.m = m;
		Message.counter += 1;
		this.id = Message.counter;
		this.delivered = false;
		this.isAck = isAck;
	}
*/
	public Message(String m, Integer port, InetAddress inetAddr, Integer id, boolean isAck) {
		// For acknowledgments
		this.m = m;
		this.id = id;
		this.delivered = false;
		this.isAck = isAck;
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

	public boolean isAck() {
		return isAck;
	}

	public void setAck(boolean isAck) {
		this.isAck = isAck;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		Message msg2 = (Message) o;
		return id == msg2.id;
	}

}

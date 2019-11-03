
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
        private Integer destinationPort;
        private InetAddress destinationInetAddr;
        private boolean broadcast;
/*
	public Message(String m, Integer port, InetAddress inetAddr, boolean isAck) {
		this.m = m;
		Message.counter += 1;
		this.id = Message.counter;
		this.delivered = false;
		this.isAck = isAck;
	}
*/
	public Message(String m, Integer destinationPort, InetAddress destinationInetAddr, Integer id, boolean isAck, boolean broadcast) {
		// For acknowledgments
		this.m = m;
		this.id = id;
		this.delivered = false;
		this.isAck = isAck;
                this.destinationInetAddr=destinationInetAddr;
                this.destinationPort=destinationPort;
                this.broadcast=broadcast;
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

        public void setDestinationPort(Integer destinationPort) {
            this.destinationPort = destinationPort;
        }

        public void setDestinationInetAddr(InetAddress destinationInetAddr) {
            this.destinationInetAddr = destinationInetAddr;
        }

        public Integer getDestinationPort() {
            return destinationPort;
        }

        public InetAddress getDestinationInetAddr() {
            return destinationInetAddr;
        }

        public boolean isBroadcast() {
            return broadcast;
        }

        public void setBroadcast(boolean broadcast) {
            this.broadcast = broadcast;
        }
	@Override
	public int hashCode() {
		return Objects.hash(id, m);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		Message msg2 = (Message) o;
                //return(id.equals(msg2.getId()));
		return (this.destinationPort.equals(msg2.getDestinationPort()) & id.equals(msg2.getId()) & this.destinationInetAddr.equals(msg2.getDestinationInetAddr()));
	}

}

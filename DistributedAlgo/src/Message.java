import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

public class Message implements Serializable {
	// To serialize for sending
	private static final long serialVersionUID = 1423627893178345L;

	// Thread ID that started sending this message
	private Long threadId;

	private String m;
	private Integer destinationPort;
	private InetAddress destinationInetAddr;
	private Integer sourcePort;
	private InetAddress sourceInetAddr;
	private Integer id;
	private boolean isAck;
	private boolean broadcast;
	private Integer sender;
	private Integer ackSender;
	private ArrayList<Integer> vectorClock;

	/**
	 * 
	 * @param m                   - Message string
	 * @param destinationPort     - Destination port to send the message
	 * @param destinationInetAddr - Destination address to send the message
	 * @param sourcePort          - Source port of the message
	 * @param sourceInetAddr      - Source address of the message
	 * @param id                  - ID of the message
	 * @param isAck               - Boolean to test if message is acknowledgment
	 * @param broadcast           - Boolean to test if we should broadcast message
	 * @param sender              - Sender process ID that broadcasted the message
	 *                            originally
	 * @param ackSender           - Acknowledgment process ID if this is
	 *                            acknowledgment message
	 */
	public Message(String m, Integer destinationPort, InetAddress destinationInetAddr, Integer sourcePort,
			InetAddress sourceInetAddr, Integer id, boolean isAck, boolean broadcast, Integer sender, Integer ackSender,
			ArrayList<Integer> vectorClock) {
		this.m = m;
		this.destinationPort = destinationPort;
		this.destinationInetAddr = destinationInetAddr;
		this.sourceInetAddr = sourceInetAddr;
		this.sourcePort = sourcePort;
		this.id = id;
		this.isAck = isAck;
		this.broadcast = broadcast;
		this.sender = sender;
		this.ackSender = ackSender;
		this.vectorClock = vectorClock;
	}

	public ArrayList<Integer> getVectorClock() {
		return vectorClock;
	}

	public Integer getId() {
		return id;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public Integer getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(Integer sourcePort) {
		this.sourcePort = sourcePort;
	}

	public InetAddress getSourceInetAddr() {
		return sourceInetAddr;
	}

	public void setSourceInetAddr(InetAddress sourceInetAddr) {
		this.sourceInetAddr = sourceInetAddr;
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

	public Integer getSender() {
		return sender;
	}

	public void setSender(Integer sender) {
		this.sender = sender;
	}

	public Long getThreadId() {
		return threadId;
	}

	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}

	public Integer getAckSender() {
		return ackSender;
	}

	public void setAckSender(Integer ackSender) {
		this.ackSender = ackSender;
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
		return (id.equals(msg2.getId()) && sender.equals(msg2.sender));
	}

}

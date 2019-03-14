/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.DTNSim;


/**
 * Implementation of Spray and wait router as depicted in 
 * <I>Spray and Wait: An Efficient Routing Scheme for Intermittently
 * Connected Mobile Networks</I> by Thrasyvoulos Spyropoulus et al.
 *
 */
public class SprayAndWaitRouter extends ActiveRouter {
	/** identifier for the initial number of copies setting ({@value})*/ 
	public static final String NROF_COPIES = "nrofCopies";
	/** identifier for the binary-mode setting ({@value})*/ 
	public static final String BINARY_MODE = "binaryMode";
	/** SprayAndWait router's settings name space ({@value})*/ 
	public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouter";
	/** Message property key */
	public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." +
		"copies";
	
	protected int initialNrofCopies;
	protected boolean isBinary;

	public SprayAndWaitRouter(Settings s) {
		super(s);
		Settings snwSettings = new Settings(SPRAYANDWAIT_NS);
		initialNrofCopies = snwSettings.getInt(NROF_COPIES);
		isBinary = snwSettings.getBoolean( BINARY_MODE);
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected SprayAndWaitRouter(SprayAndWaitRouter r) {
		super(r);
		this.initialNrofCopies = r.initialNrofCopies;
		this.isBinary = r.isBinary;
	}
	
	@Override
	public int receiveMessage(Message m, DTNHost from) {
		return super.receiveMessage(m, from);
	}
	
	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
		Integer nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);
		
		assert nrofCopies != null : "Not a SnW message: " + msg;
		
		if (isBinary) {
			/* in binary S'n'W the receiving node gets ceil(n/2) copies */
			nrofCopies = (int)Math.ceil(nrofCopies/2.0);
		}
		else {
			/* in standard S'n'W the receiving node gets only single copy */
			nrofCopies = 1;
		}
		
		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
		return msg;
	}
	
	@Override 
	public boolean createNewMessage(Message msg) {
		makeRoomForNewMessage(msg.getSize());

		msg.setTtl(this.msgTtl);
		msg.addProperty(MSG_COUNT_PROPERTY, new Integer(initialNrofCopies));
		addToMessages(msg, true);
		return true;
	}
	
	@Override
	public void update() {
		super.update();
		if (!canStartTransfer() || isTransferring()) {
			return; // nothing to transfer or is currently transferring 
		}

		/* try messages that could be delivered to final recipient */
		if (exchangeDeliverableMessages() != null) {
			return;
		}
		
		/* create a list of SAWMessages that have copies left to distribute */
		@SuppressWarnings(value = "unchecked")
		List<Message> copiesLeft = sortByQueueMode(getMessagesWithCopiesLeft());
		
		if (copiesLeft.size() > 0) {
			/* try to send those messages */
			this.tryMessagesToConnections(copiesLeft, getConnections());
		}
	}
	
	@Override
	protected Connection tryMessagesToConnections(List<Message> messages,
			List<Connection> connections) {
		
		DTNHost currentHost = this.getHost();
		String hostName = this.getHost().toString();
		String neighbourNames = "-";
		ArrayList<String> neighboursMetaData = new ArrayList<String>();
		
		for(int i = 0; i < connections.size(); i++) {
			DTNHost otherHost =  connections.get(i).getOtherNode(this.getHost());
			String otherHostName = otherHost.toString();
			neighbourNames += otherHostName + "-";
			neighboursMetaData.add(otherHost.getHostInfo());
		}
		
		
		for (int i=0, n=connections.size(); i<n; i++) {
			Connection con = connections.get(i);
			Message started = tryAllMessages(con, messages); 
			if (started != null) { 
				
				DTNHost msgDestination = started.getTo();
				String msgDestinationName = msgDestination.toString();
				
				DTNHost selectedNeighbour = con.getOtherNode(this.getHost());
				String selectedNeighbourName = selectedNeighbour.toString();
				
				String timeSinceSimulation = String.valueOf(System.currentTimeMillis() - DTNSim.startTimeOfSimulation);
				
				String key = hostName + "-" + msgDestinationName + "-" + neighbourNames +  "-"  + started.getId() + "-" + selectedNeighbourName + "-" + timeSinceSimulation ;
				System.out.println(key);
				
				
				FilePrinter.printToFileEventData(key, hostName, msgDestinationName, neighbourNames, started.getId(), selectedNeighbourName);
				String _neighbourMetaData = getNeighbourMetaData(neighboursMetaData);
				
				FilePrinter.printToFileEventMetaData(
													  key,
													  currentHost.getHostInfo(),
													  msgDestination.getHostInfo(),
													  _neighbourMetaData,
													  started.getId(),
													  selectedNeighbourName
												  );
				return con;
			}		
		}
		
		return null;
	}
	
	
	private String getNeighbourMetaData(ArrayList<String> neighboursMetaData) {
		// TODO Auto-generated method stub
		String _neighboursMetaData = "";
		for(int i = 0; i < neighboursMetaData.size(); i++) {
			_neighboursMetaData += neighboursMetaData.get(i)+",";
		}
		return _neighboursMetaData;
	}

	/**
	 * Creates and returns a list of messages this router is currently
	 * carrying and still has copies left to distribute (nrof copies > 1).
	 * @return A list of messages that have copies left
	 */
	protected List<Message> getMessagesWithCopiesLeft() {
		List<Message> list = new ArrayList<Message>();

		for (Message m : getMessageCollection()) {
			Integer nrofCopies = (Integer)m.getProperty(MSG_COUNT_PROPERTY);
			assert nrofCopies != null : "SnW message " + m + " didn't have " + 
				"nrof copies property!";
			if (nrofCopies > 1) {
				list.add(m);
			}
		}
		
		return list;
	}
	
	/**
	 * Called just before a transfer is finalized (by 
	 * {@link ActiveRouter#update()}).
	 * Reduces the number of copies we have left for a message. 
	 * In binary Spray and Wait, sending host is left with floor(n/2) copies,
	 * but in standard mode, nrof copies left is reduced by one. 
	 */
	@Override
	protected void transferDone(Connection con) {
		Integer nrofCopies;
		String msgId = con.getMessage().getId();
		/* get this router's copy of the message */
		Message msg = getMessage(msgId);

		if (msg == null) { // message has been dropped from the buffer after..
			return; // ..start of transfer -> no need to reduce amount of copies
		}
		
		/* reduce the amount of copies left */
		nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);
		if (isBinary) { 
			nrofCopies /= 2;
		}
		else {
			nrofCopies--;
		}
		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
	}
	
	@Override
	public void changedConnection(Connection con) {
		
		// TODO Auto-generated method stub
		super.changedConnection(con);	
		if(con.isUp()) {		
			DTNHost otherNode = con.getOtherNode(getHost());
			DTNHost currentHost  = this.getHost();
			String key = currentHost.toString() + "-" + otherNode.toString() + "-" + (System.currentTimeMillis() - DTNSim.startTimeOfSimulation);
			FilePrinter.printToFileConnectionMetaData(key, currentHost.getHostInfo(), otherNode.getHostInfo());
			FilePrinter.printToFileConnectionData(key, currentHost.toString(), otherNode.toString(), "connected_up");
		}
	}
	

	
	@Override
	public SprayAndWaitRouter replicate() {
		return new SprayAndWaitRouter(this);
	}
}

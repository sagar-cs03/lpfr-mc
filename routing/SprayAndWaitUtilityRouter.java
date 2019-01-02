package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import util.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;


public class SprayAndWaitUtilityRouter extends SprayAndWaitRouter {
	
	protected HashMap<Integer, Integer> utilities;
	
	public SprayAndWaitUtilityRouter(Settings s) {
		super(s);
		utilities = new HashMap<Integer, Integer>();
	}
	
	public SprayAndWaitUtilityRouter(SprayAndWaitRouter r) {
		super(r);
		utilities = new HashMap<Integer, Integer>();
	}

	@Override
	public SprayAndWaitRouter replicate() {
		// TODO Auto-generated method stub
		return new SprayAndWaitUtilityRouter(this);
	}

	@Override
	public void changedConnection(Connection con) {
		// TODO Auto-generated method stub
		super.changedConnection(con);
		
		if(con.isUp()) {
			
			//increase the number of Contacts with the other Node
			int nContacts = 0;
			DTNHost otherNode = con.getOtherNode(getHost());
			int otherAddress = otherNode.getAddress();
			
			if(utilities.containsKey(otherAddress)) {
				nContacts = utilities.get(otherAddress);
			}
			utilities.put(otherAddress, nContacts + 1);
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		super.update();
		
		if(!canStartTransfer() || isTransferring()) {
			return; //nothing to transfer or is currently transferring
		}
		
		//try messages that could be delivered to the final recipient
		if(exchangeDeliverableMessages() != null) {
			return ; 
		}
		tryOtherMessages();
	}

	@Override
	protected List<Message> getMessagesWithCopiesLeft() {
		// TODO Auto-generated method stub
		return new ArrayList<Message>();
	}
	
	protected Tuple<Message, Connection> tryOtherMessages() {
		List<Tuple<Message, Connection>> messages = new ArrayList<Tuple<Message, Connection>>();
		Collection<Message> msgCollection = getMessageCollection();
		
		for (Connection con: getConnections()) {
			DTNHost other = con.getOtherNode(getHost());
			SprayAndWaitUtilityRouter otherRouter = (SprayAndWaitUtilityRouter)other.getRouter();
			if(otherRouter.isTransferring()) {
				continue;
			}
			
			for(Message m: msgCollection) {
				if(otherRouter.hasMessage(m.getId())) continue;
		
				int destination = m.getTo().getAddress();
				if(getCopiesLeft(m) > 1 &&  otherRouter.getUtility(destination) > getUtility(destination)) {
					messages.add(new Tuple<Message, Connection>(m, con));
				}
			}
		}
		if(messages.isEmpty()) {
			return null;
		}
		return tryMessagesForConnected(messages);
	}
	
	protected int getUtility(int address) {
		int utility = 0;
		
		if(utilities.containsKey(address)) {
			utility = utilities.get(address);
		}
		
		return utility;
	}
			
	protected int getCopiesLeft(Message m) {
		Integer nrOfCopies =(Integer) m.getProperty(MSG_COUNT_PROPERTY);
		assert nrOfCopies != null : "SnWU message"  + m + "didn't have" + "nrof copies property!";
		return nrOfCopies;
	}
}
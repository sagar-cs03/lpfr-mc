/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import java.util.ArrayList;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.DTNSim;
import core.Message;
import core.Settings;

/**
 * Epidemic message router with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class EpidemicRouter extends ActiveRouter {
	
	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public EpidemicRouter(Settings s) {
		super(s);
		//TODO: read&use epidemic router specific settings (if any)
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected EpidemicRouter(EpidemicRouter r) {
		super(r);
		//TODO: copy epidemic settings here (if any)
	}
			
	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}
		
		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}
		
		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}
	
	@Override
	protected Connection tryMessagesToConnections(List<Message> messages,
			List<Connection> connections) {
		
		DTNHost currentHost = this.getHost();
		String hostName = this.getHost().toString();
		String neighbourNames = "-";
		
		//each element is a string, containing comma separated info about surrounding nodes
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
	
	@Override
	public EpidemicRouter replicate() {
		return new EpidemicRouter(this);
	}

}
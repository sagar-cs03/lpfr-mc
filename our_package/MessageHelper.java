package our_package;

import core.DTNHost;
import core.Message;

import java.util.List;


public class MessageHelper {
	
	public static String getPathTravelled(List<DTNHost> path) {
		String pathNodes = "-";
		for(int i = 0; i < path.size(); i++) {
			pathNodes += path.get(i).toString() + "-";
		}
		return pathNodes;
	}

	public static String getMessageAttributes(Message m) {
		// TODO Auto-generated method stub
		return 			m.getId() + ","
						+m.getSize() + ","
						+m.getFrom() + ","
						+m.getTo() + ","
						+m.getHopCount() + ","
						+m.getCreationTime() + ","
						+m.getReceiveTime() ;
				
				
	}

}

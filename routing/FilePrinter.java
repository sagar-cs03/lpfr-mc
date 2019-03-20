package routing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FilePrinter {


	public static void printToFileConnectionMetaData (String key, String from, String to) {
		String tmp = key + "," + from+","+to+"\n";
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\connection_metadata.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		}catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public static void printToFileEventMetaData(String key,String fromMetaData, String toMetaData, String neighbourMetaData, String messageId, String selectedNeighbourName) {
		String tmp = key + "," + fromMetaData + "," + toMetaData + "," + neighbourMetaData + "," + messageId + "," + selectedNeighbourName + "\n";
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\event_metadata.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		}catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public static void printToFileConnectionData(String key, String from, String to, String eventType) {
		String tmp = key + "," +  from+","+to+","+eventType+"\n";
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\dataset_connection.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		}catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}


	//key - host-destination-neighbours-messageId-selectedNeighbour-time
	//host, destination, max_10_neighbours, messageId, selectedNeighbour, finallyDelivered

	public static void printToFileEventData(String key, String from, String to, String neighbours,
			String messageId, String selectedNeighbour
			) 
	{
		String tmp = key+","+from+","+to+","+neighbours+"," + messageId+","+selectedNeighbour+"\n";
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\dataset_events.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	//status - delivered first or delivered otherNumberOftimes
	public static void printToFileMessageStatus(String messageId, String pathTravelled, String status) 
	{
		String tmp = messageId + ","+ pathTravelled + ","  + status + "\n";
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\message_status.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public static void printToFileEventDataForProphet(String key, String from, String to, String neighbours,
			String messageId, String selectedNeighbour, String myPred, String othPred
			) 
	{
		String tmp = key +"," +
					 from + "," +
					 to + "," + 
					 neighbours +"," + 
					 messageId + "," + 
					 selectedNeighbour + "," + 
					 myPred + "," + 
					 othPred + "," + 
					 "\n";
		
		try {
			File file = new File ("C:\\Users\\i506670\\Projects\\lpfr-mc\\dataset\\dataset_events.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter (file, true);
			BufferedWriter bw = new BufferedWriter (fw);
			bw.write(tmp);
			bw.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}




}

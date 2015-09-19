package vantage.apex.interperter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Interperter {
	final static private Logger log = LoggerFactory.getLogger(Interperter.class);
	private static SocketManager vantage = null;
	//private static AutoSocket apex = null;
	private static Properties props = null;
	
	// STATICS
	static final String VANTAGE_KEEP_ALIVE = "06zs004D";
	static final String APEX_KEEP_ALIVE = "06zs004D";
	
	private static class ApexEvent implements Callback{
		
		public void dataRecieved(String data) {
			log.info("Got event " + data);
			if (data.length() > 5){
				int dataLength = Integer.parseInt(data.substring(0, 2),16);
	            String messageType = data.substring(2,3);
	            String messageSubType = data.substring(3,4);
	            String message = data.substring(4,dataLength-2);
	            String checksum = data.substring(dataLength,dataLength+2);
	            
				if (props.containsKey("" + dataLength)){
					String keypad = props.getProperty("" + dataLength);
					log.info("Sending push to keypad " + keypad);
				}else{
					log.debug("No key for " + dataLength);
				}
			}
			
		}

	}
	
	public static String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void main(String[] args){
		
		ReadProperties reader = new ReadProperties();
		try {
			props = reader.getPropValues();
		} catch (IOException e) {
			log.debug(stackTraceToString(e));
			log.warn("Failed to read property file");
			System.exit(1);
		}
		
		try {
			vantage = new SocketManager(props.getProperty("vantage.ip"),Integer.parseInt(props.getProperty("vantage.port")));
			vantage.setWatchdog(VANTAGE_KEEP_ALIVE,5);
			vantage.registerCallback(new ApexEvent());
			vantage.setCommandTermintor("\n");
		} catch (NumberFormatException | IOException e) {
			log.warn(e.getMessage());
			log.debug(stackTraceToString(e));
			System.exit(2);
		}
		
		//try {
		//	apex = new AutoSocket(props.getProperty("apex.ip"),Integer.parseInt(props.getProperty("apex.port")));
		//	apex.setWatchdog(VANTAGE_KEEP_ALIVE,20);
		//	apex.registerCallback(new ApexEvent());
		//} catch (NumberFormatException | IOException e) {
		//	log.warn(e.getMessage());
		//	log.debug(stackTraceToString(e));
		//	System.exit(2);
		//}
		
			
		//vantage.closeSocket();
		//apex.closeSocket();
		
	}
}

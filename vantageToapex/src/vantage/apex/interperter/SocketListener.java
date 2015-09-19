package vantage.apex.interperter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketListener implements Runnable {
	final static Logger log = LoggerFactory.getLogger(SocketManager.class);
	private SocketWatchdog watchdog = null;
	private SocketManager socket;
	private boolean isRunning = false;
	private Callback callback;
	private static int runningInstances = 0;
	private String commandTerminator = "\n";
	
	public SocketListener(SocketManager socket) {
		this.socket = socket;
	}
	
	public boolean isRunning() {
		return(isRunning);
	}

	public void stop() {
		isRunning = false;	
	}

	public void deregisterWatchdog() {
		watchdog = null;
	}

	public void deregisterCallback() {
		callback = null;
	}

	public void registerCallback(Callback dataManager) {
		this.callback = dataManager;
	}

	public Callback getCallback() {
		return(callback);
	}

	public void registerWatchdog(SocketWatchdog watchdog) {
		this.watchdog = watchdog;
	}

	public SocketWatchdog getWatchdog() {
		return(watchdog);
	}

	public void run() {
		isRunning = true;
		runningInstances++;
		log.debug("Starting Listener Thread for " + socket.toString() + " (" + runningInstances +")");
		try {
	         InputStreamReader inFromServer = new InputStreamReader(socket.getSocket().getInputStream());
	      
	         BufferedReader s_in = new BufferedReader(inFromServer);
	         
	         String data = "";
	         String dataEnd = "";
	         char[] socketChars = new char[10000];
	         int chars = -1;
	         while ( (chars = s_in.read(socketChars)) != -1 && (socket.getSocket().isConnected()) && (isRunning)){   
	        	 int charCount = 0;
	        	 while (charCount < chars){
	        		 
	            	dataEnd += String.valueOf(socketChars[charCount]);
	            	if (dataEnd.length() > commandTerminator.length()){
	            		data += dataEnd.substring(0,1);
	            		dataEnd = dataEnd.substring(1);
	            	}
	            	
	            	
	            	if (dataEnd.equals(commandTerminator)){
	            		log.debug("Full Message from " + socket.toString() + " is " + data);
	    	            if (watchdog != null){
	    	            	watchdog.clearWatchdog();
	    	            }
	    	            if (callback != null){
	    	            	callback.dataRecieved(data);
	    	         	}
	            		data = "";
	            		dataEnd = "";
	            	}
	            	charCount++;
	            }	
	         }
	         
	         log.debug("Listener thread for " + socket.toString() + " exiting");
	         
	      }catch(IOException e){
	    	  log.debug("IOException for " + socket.toString());	  
	    	  socket.reconnect();  	      	  
	      }finally{
	    	  isRunning = false;
		      runningInstances--;
	      }
	}

	public void setlineend(String term) {
		commandTerminator = term;
	}
	
}
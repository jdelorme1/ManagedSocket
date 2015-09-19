package vantage.apex.interperter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SocketManager {
	// The logger
	final static Logger log = LoggerFactory.getLogger(SocketManager.class);
	
	// The socket object that is created for this manager.
	private Socket socket;
	
	// The connection status as the manager knows it
	private boolean isConnected = false;
	
	// The line terminator string for items traversing the socket
	private String commandTerminator = "\n";
	
	private SocketWatchdog watchdog;
	private SocketListener listener;
	
	public SocketManager(String host, int port) throws UnknownHostException,
			IOException {
			this.socket = new Socket(host,port);
			isConnected = true;
	}
	
	public boolean isConnected(){
		return(isConnected);
	}
	
	private void registerCallback(){
		// If there is no listener create one
		if (listener == null){
			listener = new SocketListener(this);
		}
		// if we have a watchdog register it to the listener.. Update it if its different
		if (watchdog != null && !(watchdog.equals(listener.getWatchdog())) ){
			listener.registerWatchdog(watchdog);
		}
		if (!(listener.isRunning())){
			log.debug("Starting Listener thread for callback "+ listener.isRunning());
			new Thread(listener).start();
		}
	}
	
	public void registerCallback(Callback dataManager){
		registerCallback();
		// Register the callback or update it if its different
		if (!(dataManager.equals(listener.getCallback())) ){
			listener.registerCallback(dataManager);
		}
	}
	
	public void deregisterCallback(){
		if (listener != null){
			listener.deregisterCallback();
		}
		if (listener.getWatchdog() == null){
			listener = null;
		}
	}
	
	private void setWatchdog(){
		// If there is no watchdog create one
		if (watchdog == null){
			watchdog = new SocketWatchdog(this);
		}
		// If we dont have a listener create one
		if (listener == null){
			listener = new SocketListener(this);
		}
		// register the watchdog to the listener .. Update it if its different
		if (!(watchdog.equals(listener.getWatchdog())) ){
			listener.registerWatchdog(watchdog);
		}
		if (!(listener.isRunning())){
			log.debug("Starting Listener thread for watchdog "+ listener.isRunning());
			new Thread(listener).start();
		}
		
		if (!(watchdog.isRunning())){
			new Thread(watchdog).start();
		}
		
		
	}
	
	public void setCommandTermintor(String term){
		this.commandTerminator = term;
		listener.setlineend(term);
	}
	
	public void setWatchdog(String keepAlive){
		setWatchdog();
		watchdog.setKeepAlive(keepAlive);
		
	}
	
	public void setWatchdog(String keepAlive, int freq){
		setWatchdog();
		watchdog.setKeepAlive(keepAlive);
		watchdog.setFrequency(freq);
	}
	
	public void stopWatchdog(){
		if (watchdog != null){
			watchdog.stop();
			listener.deregisterWatchdog();
		}	
	}
	
	public void closeSocket(){
		stopWatchdog();
		stopListener();
		isConnected = false;
		try {
			socket.close();
		} catch (IOException e) {
			// Don't care we are exiting
		}
	}
	
	private void stopListener() {
		if (listener != null){
			listener.stop();
		}
	}
	
	public Socket getSocket(){
		return(socket);
	}

	
	public void sendCommand(String command) throws IOException{
		log.debug("Send cmd " + command + commandTerminator + " to device " + this.toString());
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.getSocket().getOutputStream()));
	    bufferedWriter.write(command + commandTerminator);
	    bufferedWriter.flush();
	}
	
	public String toString(){
		String result = new String();
		result = "Socket Connected="+this.getSocket().isConnected() + " Closed=" + this.getSocket().isClosed() +  " IP="+this.getSocket().getRemoteSocketAddress().toString();		
		return(result);
	}
	
	protected void reconnect(){
		// Kill any watchdog or listener threads
		if (isConnected == true){
			isConnected = false;
			
			if (watchdog.isClear() != true && this.getSocket().isConnected() == true){
				// The connection looks pretty dead... We will just tear down the socket and make a new one.
				String ip = this.getSocket().getInetAddress().toString();
				ip = ip.substring(1,ip.length());
				int port = this.getSocket().getPort();
				
				try {
					this.getSocket().close();
				} catch (IOException e) {
					// We cant close the socket?? Thats bizzarre ignoring it should die on its own
				}
				
				while (this.getSocket().isConnected() == false || this.getSocket().isClosed() == true){	
					try {
						this.socket = new Socket(ip,port);
					} catch (UnknownHostException e) {
						log.info("Reconnect failed... It looks like your network has gone away");	
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e1) {
							log.debug(stackTraceToString(e1));
						}
					} catch (IOException e) {
						log.info("Reconnect failed... Unable to establish connection");
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e1) {
							log.debug(stackTraceToString(e1));
						}
					}
				}
			}
				
			if (!(this.getSocket().isConnected())){
				while (!(this.getSocket().isConnected())){
					try {
						this.getSocket().connect(this.getSocket().getRemoteSocketAddress());
					} catch (IOException io){
						log.warn("Unable to reconncet socket: " + io.getMessage());
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e1) {
							log.debug(stackTraceToString(e1));
						}
					}
				}
				
				
			}
			isConnected = true;
			
			if (watchdog != null){
				watchdog.clearWatchdog();
				setWatchdog();
			}
			if (listener != null && listener.getCallback() != null){
				registerCallback();
			}
		}
	}
	
	public String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}


}

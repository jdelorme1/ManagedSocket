package vantage.apex.interperter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketWatchdog implements Runnable {
	final static Logger log = LoggerFactory.getLogger(SocketManager.class);
	private boolean isRunning;
	private SocketManager socketManager;
	private int watchdogFrequency;
	private String keepAlive = "T";
	private boolean watchdogClear = true;
	
	
	public boolean isRunning(){
		return(isRunning);
	}
	
	public boolean isClear(){
		return(watchdogClear);
	}
	
	public SocketWatchdog(SocketManager manager){
		this.socketManager = manager;
	}
	
	public void stop() {
		isRunning = false;
	}

	public void setFrequency(int freq) {
		this.watchdogFrequency = freq;
	}

	public void setKeepAlive(String keepAlive) {
		this.keepAlive = keepAlive;
	}
	
	public void clearWatchdog(){
		watchdogClear = true;
	}

	public void run() {
		isRunning = true;
		log.debug("Started watchdog for " + socketManager.toString());
		while (isRunning){
			if (!(socketManager.getSocket().isConnected()) || watchdogClear != true){
				log.warn("Connection lost trying to reconnect " + socketManager.toString());
				socketManager.reconnect();
				try {
					Thread.sleep((watchdogFrequency*1000));
				}catch (Exception e){
					log.debug("Watchdog Thread reconncet wait interrupted!");
				}
			}else{
				
				try {
					log.debug("Sending keep alive packet for " + socketManager.toString());
					socketManager.sendCommand(keepAlive);
					watchdogClear = false;
				} catch (IOException io){
					log.debug("Watchdog IOException during keep alive command");
					socketManager.reconnect();
				}
				
				try {
					Thread.sleep((watchdogFrequency*1000));
				} catch (InterruptedException e) {
					log.debug("Watchdog Thread interval wait interrupted!");
				}
			}
		}
		
	}	
}
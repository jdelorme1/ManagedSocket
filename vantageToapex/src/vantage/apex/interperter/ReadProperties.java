package vantage.apex.interperter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadProperties {
	final Logger log = LoggerFactory.getLogger(SocketManager.class);
    private String configFile = "config.properties";
    
    public ReadProperties(String configFile){
    	this.configFile = configFile;
    }
    
    public ReadProperties(){
    	// Just start with the defaults
    }
	
	public Properties getPropValues() throws IOException {
    	
        Properties prop = new Properties();
        String propFileName = configFile;
 
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);
        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
 
        Date time = new Date(System.currentTimeMillis());
        log.debug("Read property file " + configFile + " at " + time.toString());
        
        return prop;
    }
}
package cern.c2mon.driver.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * This class represents an alert event received from the database via the dbms_alert package.
 * Alert consists of datatag id, name, value, timestamp, quality and quality description.
 * 
 * @author Aleksandra Wardzinska
 * */
public class Alert {

    /**
     * Separator of the values received in a string from the db alert
     * */
    public static final String SEPARATOR = ";";
    /**
     * This constant is used to indicate that there shouldn't be any timeout while listening for db alert.
     * The value of the timeout is set to 86400000 sec, which corresponds to 1000 days; it was taken from dbms_alert package specification (MAXWAIT). 
     * */
    public static final int MAX_TIMEOUT = 86400000;
    /**
     * Indicator that the timeout occurred on the db side
     * */
    public static final int TIMEOUT_OCCURRED = 1;
    /**
     * Indicator that the alert was sent from the db
     * */
    public static final int ALERT_OCCURRED = 0;
    /**
     * Logger for the Alert class
     * */
    private Logger logger = Logger.getLogger(this.getClass());
    /**
     * Id of the alert == id of the registered datatag == name of the alert in dbms_alert package
     * */
    private Long id;
    /**
     * Name of the alert == name of the registered datatag
     * */
    private String name;
    /**
     * Updated value of the alert (datatag)
     * */
    private String value;
    /**
     * Timestamp of the alert (datatag) (updated by the client on the db side)
     * */
    private Date timestamp;
    /**
     * Quality of the alert (datatag)
     * */
    private short quality;
    /**
     * Quality description of the alert
     * */
    private String qualityDescription;
    

    /**
     * Default constructor
     * */
    public Alert() { }
    
    /**
     * Constructor 
     * @param alertId id of the alert (datatag)
     * @param message full message received from the dbms_alert, composed of the datatag name and value, 
     *                timestamp, quality and quality description
     *                the following is expected: datatagname;datatagvalue;srctimestamp;srcquality;qualitydesc
     * */
    public Alert(final String alertId, final String message) {
        this.id = Long.parseLong(alertId);
        
        String[] tokens = message.split(SEPARATOR, -1);
        if (!tokens[0].equals(""))
            this.name = tokens[0];
        if (!tokens[1].equals(""))
            this.value = tokens[1];
        if (!tokens[2].equals("")) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
            try {
                this.timestamp = df.parse(tokens[2]);
            } catch (ParseException e) {
                logger.error("Error parsing timestamp received in an alert.");
                logger.error(e.getMessage());
            } 
        }
        if (!tokens[3].equals(""))
            this.quality = Short.parseShort(tokens[3]);
        if (!tokens[4].equals(""))
            this.qualityDescription = tokens[4]; 
    }

    /**
     * Converts an alert into string
     * @return a string representation of an alert
     * */
    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        return "[" + this.name + ": " + this.value + ", " 
        + df.format(this.timestamp) + ", " + this.quality + ", " + this.qualityDescription + "]"; 
    }

    /**
     * Gets the id of the alert (datatag)
     * @return id of the alert
     * */
    public Long getId() {
        return id;
    }
    /**
     * Sets the id of the alert (datatag)
     * @param id    id of the alert
     * */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * Gets the name of the alert (datatag)
     * @return alert name
     * */
    public String getName() {
        return this.name;
    }
    /**
     * Sets the name of the alert (datatag)
     * @param name alert name
     * */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * Gets the value of the alert (datatag)
     * @return value of the alert 
     * */
    public String getValue() {
        return this.value;
    }
    /**
     * Sets the value of the alert (datatag)
     * @param value of the alert
     * */
    public void setValue(final String value) {
        this.value = value;
    }
    /**
     * Gets the value of the alert (datatag), equivalent to getValue()
     * @return value of the alert
     * */
    public String getDataTagValue() {
        return getValue();
    }
    /**
     * Gets the timestamp of the alert (datatag)
     * @return timestamp of the alert
     * */
    public Date getTimestamp() {
        return this.timestamp;
    }
    /**
     * Sets the timestamp of the alert (datatag)
     * @param timestamp of the alert 
     * */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * Gets the timestamp of the alert (datatag)
     * @return timestamp of the alert
     * */
    public Date getClientTimestamp() {
        return getTimestamp();
    }
    /**
     * Gets the quality of the alert (datatag)
     * @return quality;
     * */
    public short getQuality() {
        return this.quality;
    }
    /**
     * Sets the quality of the alert (datatag)
     * @param quality of the alert
     * */
    public void setQuality(final short quality) {
        this.quality = quality;
    }
    /**
     * Gets the quality description of the alert (datatag)
     * @return quality description;
     * */
    public String getQualityDescription() {
        return this.qualityDescription;
    }
    /**
     * Sets the quality description of the alert (datatag)
     * @param qualityDescription description of the alert
     * */
    public void setQuality(final String qualityDescription) {
        this.qualityDescription = qualityDescription;
    }
    
    
    
    
}

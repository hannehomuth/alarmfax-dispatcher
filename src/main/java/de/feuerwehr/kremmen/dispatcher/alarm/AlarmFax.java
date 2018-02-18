package de.feuerwehr.kremmen.dispatcher.alarm;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Diese Klasse beschreibt einen eingehenden Alarm
 *
 * @author jhomuth
 */
public class AlarmFax {

    private static final Logger LOG = Logger.getLogger(AlarmFax.class.getSimpleName());

    private Adress address;

    private String situation;

    private List<String> mailAddresses;

    private String alarmKey;

    private static final String LINE_BREAK = System.getProperty("line.separator");

    private Boolean alarmfaxDetected = Boolean.FALSE;

    private Coordinates coordinates;
    
        private String additionalInfo;

    /**
     * Get the value of additionalInfo
     *
     * @return the value of additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Set the value of additionalInfo
     *
     * @param additionalInfo new value of additionalInfo
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }


    /**
     * Get the value of coordinates
     *
     * @return the value of coordinates
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Set the value of coordinates
     *
     * @param coordinates new value of coordinates
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Get the value of alarmfaxDetected
     *
     * @return the value of alarmfaxDetected
     */
    public Boolean isAlarmfaxDetected() {
        return alarmfaxDetected;
    }

    /**
     * Set the value of alarmfaxDetected
     *
     * @param alarmfaxDetected new value of alarmfaxDetected
     */
    public void setAlarmfaxDetected(Boolean alarmfaxDetected) {
        this.alarmfaxDetected = alarmfaxDetected;
    }

    public String convertForBosmon() {
        StringBuilder builder = new StringBuilder();
        builder.append("ALARM_KEY:").append(decode(this.alarmKey)).append(LINE_BREAK);
        if (this.coordinates.isFound()) {
            builder.append("COORDINATES:").append("N").append(this.coordinates.getLatitude().replace(".","")).append("E").append(this.coordinates.getLongitude().replace(".","")).append(LINE_BREAK);
        } else {
            builder.append("STREET:").append(decode(this.address.getStreet())).append(LINE_BREAK);
            if(this.address.getHousenumber() != null && !this.address.getHousenumber().isEmpty()){
                builder.append("HOUSENUMBER:").append(this.address.getHousenumber()).append(LINE_BREAK);                
            }else{
                builder.append("HOUSENUMBER:").append("-").append(LINE_BREAK);                
            }
            builder.append("PLZ:").append(this.address.getPostalCode()).append(LINE_BREAK);
            builder.append("CITY:").append(decode(this.address.getCity())).append(LINE_BREAK);
            if(this.address.getCityArea() != null && !this.address.getCityArea().isEmpty()){
                builder.append("CITYAREA:").append(decode(this.address.getCityArea())).append(LINE_BREAK);
            }else{
                builder.append("CITYAREA:").append("-").append(LINE_BREAK);
            }
        }
//        builder.append("LIVINGAREA:").append(this.address.getLivingarea()).append(LINE_BREAK);
        builder.append("SITUATION:").append(decode(this.situation));
        if(this.additionalInfo != null && !this.additionalInfo.isEmpty()){
            builder.append(" (").append(this.additionalInfo).append(")");
        }
        return builder.toString();
    }
    
    private String decode(String s){
        return s.replaceAll("ß", "ss").replaceAll("ö", "oe").replaceAll("Ö", "Oe").replaceAll("Ä", "Ae").replaceAll("ä", "ae").replaceAll("Ü", "Ue").replaceAll("ü", "ue");
    }

    /**
     * Get the value of alarmKey
     *
     * @return the value of alarmKey
     */
    public String getAlarmKey() {
        return alarmKey;
    }

    /**
     * Set the value of alarmKey
     *
     * @param alarmKey new value of alarmKey
     */
    public void setAlarmKey(String alarmKey) {
        this.alarmKey = alarmKey;
    }

    /**
     * Die Alarmierungszeit
     */
    private Date alarmTime;

    /**
     * Get the value of mailAddresses
     *
     * @return the value of mailAddresses
     */
    public List<String> getMailAddresses() {
        return mailAddresses;
    }

    /**
     * Set the value of mailAddresses
     *
     * @param mailAddresses new value of mailAddresses
     */
    public void setMailAddresses(List<String> mailAddresses) {
        this.mailAddresses = mailAddresses;
    }

    /**
     * Get the value of address
     *
     * @return the value of address
     */
    public Adress getAddress() {
        return address;
    }

    /**
     * Set the value of address
     *
     * @param address new value of address
     */
    public void setAddress(Adress address) {
        this.address = address;
    }

    /**
     * Get the value of situation
     *
     * @return the value of situation
     */
    public String getSituation() {
        return situation;
    }

    /**
     * Set the value of situation
     *
     * @param situation new value of situation
     */
    public void setSituation(String situation) {
        this.situation = situation;
    }

    /**
     * Get the value of alarmTime
     *
     * @return the value of alarmTime
     */
    public Date getAlarmTime() {
        return alarmTime;
    }

    /**
     * Set the value of alarmTime
     *
     * @param alarmTime new value of alarmTime
     */
    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

}

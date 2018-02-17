package de.feuerwehr.kremmen.dispatcher.alarm;

import de.feuerwehr.kremmen.dispatcher.ocr.OCRScanner;
import de.feuerwehr.kremmen.dispatcher.config.Config;
import de.feuerwehr.kremmen.dispatcher.ocr.OCRToAlarmMapper;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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

    
    public String convertForBosmon(){
        StringBuilder builder = new StringBuilder();
        builder.append("ALARM_KEY:").append(this.alarmKey).append(LINE_BREAK);
        builder.append("STREET:").append(this.address.getStreet()).append(LINE_BREAK);
        builder.append("HOUSENUMBER:").append(this.address.getHousenumber()).append(LINE_BREAK);
        builder.append("PLZ:").append(this.address.getPostalCode()).append(LINE_BREAK);
        builder.append("CITY:").append(this.address.getCity()).append(LINE_BREAK);
        builder.append("CITYAREA:").append(this.address.getCityArea()).append(LINE_BREAK);
//        builder.append("LIVINGAREA:").append(this.address.getLivingarea()).append(LINE_BREAK);
        builder.append("SITUATION:").append(this.situation);
        return builder.toString();
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

package de.feuerwehr.kremmen.dispatcher.util;

import de.feuerwehr.kremmen.dispatcher.alarm.AlarmFax;
import java.util.List;

/**
 * Simples observer pattern
 * @author jhomuth
 */
public interface AlarmHeadquarter {
    
    /**
     * Liefert alle Alarme an die registrierten Empfänger aus
     * @param alarms 
     */
    public void deliverAlarms(List<AlarmFax> alarms);
    
    /**
     * Registiert einen Empfänger bei der "Leitstelle"
     * @param recipient 
     */
    public void registerRecipient(Recipient recipient);
    
    /**
     * Derigistiert einen Empfänger bei der Leitstelle
     * @param recipient 
     */
    public void deRegisterRecipient(Recipient recipient);
    
    
    
}

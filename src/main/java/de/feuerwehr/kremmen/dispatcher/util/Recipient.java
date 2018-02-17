package de.feuerwehr.kremmen.dispatcher.util;

import de.feuerwehr.kremmen.dispatcher.alarm.AlarmFax;
import java.util.List;

/**
 * Simples Observer pattern
 * @author jhomuth
 */
public interface Recipient {
    
    public void deliver(List<AlarmFax> alarms);
    
}

package de.feuerwehr.kremmen.dispatcher;

import de.feuerwehr.kremmen.dispatcher.alarm.AlarmFax;
import de.feuerwehr.kremmen.dispatcher.config.Config;
import de.feuerwehr.kremmen.dispatcher.config.ConfigurationException;
import de.feuerwehr.kremmen.dispatcher.telegram.TelegramBot;
import de.feuerwehr.kremmen.dispatcher.util.Recipient;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;

/**
 *
 * @author jhomuth
 */
public class AlarmFaxDispatcher implements Recipient {

    private Postman postman;

    private static final Logger LOG = LoggerFactory.getLogger(AlarmFaxDispatcher.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        LOG.info("Alarmfax-Dispatcher started...");
        try {
            Config.init(new File(args[0]));
        } catch (ConfigurationException ex) {
            LOG.error("Konfiguration nicht valide", ex);
            System.exit(1);
        }
        AlarmFaxDispatcher dispatcher = new AlarmFaxDispatcher();
        dispatcher.process();

    }

    public AlarmFaxDispatcher() {
        postman = new Postman();
        postman.registerRecipient(this);
    }

    public void process() {
        int pollingSeconds = new Integer(Config.get(Config.KEY_POLLING_SECONDS));
        while (true) {
            try {
                postman.fetchMailsAndQuitConnection();
                LOG.trace(String.format("Process finished.... Waiting %s Seconds", pollingSeconds));
                try {
                    Thread.sleep(1000 * pollingSeconds);
                } catch (InterruptedException ex) {
                    LOG.warn("BosMonBrigdeProcess interrupted... Bye");
                    System.exit(1);
                }
            } catch (MessagingException me) {
                LOG.error("Konnte Emails nicht abrufen", me);
            }
        }
    }

    /**
     * Callback when new alarms have arrived
     *
     * @param alarms
     */
    @Override
    public void deliver(List<AlarmFax> alarms) {
        try {
            LOG.info(String.format("%s Alarmfax%sempfangen...", alarms.size(), alarms.size() == 1 ? " " : "e "));
            for (AlarmFax alarm : alarms) {
                if (alarm.isAlarmfaxDetected() && alarm.isValidAlarmTime()) {
                    postman.sendMail(alarm);
                    if (alarm.getTelegramChannelIDs() != null && !alarm.getTelegramChannelIDs().isEmpty()) {
                        for (String channelId : alarm.getTelegramChannelIDs()) {
                            FileInputStream is = null;
                            try {
                                is = new FileInputStream(alarm.getFaxFile());
                                TelegramBot.getInstance().sendDocument("Alarmfax", channelId, is, alarm.getFaxFile().getName());
                            } catch (Exception e) {
                                LOG.warn("Error during telegram send", e);
                            } finally {
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (Exception e) {
                                        LOG.warn("Error during close of Fax Inputstream", e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LOG.warn("Alarmfax detected {}, Time valid {}", alarm.isAlarmfaxDetected(), alarm.isValidAlarmTime());
                }
            }
        } catch (Throwable e) {
            LOG.error("Fehler bei der Verarbeitung", e);
        }

    }
}

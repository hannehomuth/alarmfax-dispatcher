package de.feuerwehr.kremmen.dispatcher;

import de.feuerwehr.kremmen.dispatcher.ocr.OCRScanner;
import de.feuerwehr.kremmen.dispatcher.alarm.AlarmFax;
import de.feuerwehr.kremmen.dispatcher.config.Config;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.smtp.SMTPTransport;
import de.feuerwehr.kremmen.dispatcher.ocr.OCRToAlarmMapper;
import de.feuerwehr.kremmen.dispatcher.util.Recipient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.*;
import java.util.Properties;
import de.feuerwehr.kremmen.dispatcher.util.AlarmHeadquarter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse bietet die grundsätzliche Funktionalität um Email abzuholen und
 * auszuwerten.
 *
 * @author jhomuth
 */
public class Postman implements AlarmHeadquarter {
    
    private static final Logger LOG = LoggerFactory.getLogger(Postman.class);
    private List<Recipient> recipients = new ArrayList<>();

    /**
     * Diese Methode holt die Alarm-Emails ab und stellt Sie den Abonennten zu
     * (Liste recipients)
     *
     * @throws MessagingException im Fall das die Emails nicht abgeholt werden
     * konnten.
     */
    public void fetchMailsAndQuitConnection() throws MessagingException {
        List<AlarmFax> alarmFax = new ArrayList<>();

        String url = Config.get(Config.KEY_PROP_IMAP_SERVER);
        String username = Config.get(Config.KEY_PROP_IMAP_USER);
        String password = Config.get(Config.KEY_PROP_IMAP_PASS);
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        try {
            LOG.trace("Connecting to IMAP server: {}", url);
            store.connect(url, username, password);
            
            String folderName = "INBOX";
            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
            try {
                if (!folder.isOpen()) {
                    /* Wir setzen die Mails auf gelesen also brauchen wir ReadWrite */
                    folder.open(Folder.READ_WRITE);
                }
                
                Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                FetchProfile metadataProfile = new FetchProfile();
                metadataProfile.add(FetchProfile.Item.FLAGS);

                // Lade alle wichtigen Felder
                metadataProfile.add(FetchProfile.Item.ENVELOPE);
                folder.fetch(messages, metadataProfile);
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    boolean isRead = message.isSet(Flags.Flag.SEEN);
                    
                    if (!isRead) {
                        LOG.info("Nachricht empfangen: {}", message.getDataHandler().getContentType());
                        /* Die Nachricht wurde noch nicht gelesen, scheint also neu zu sein */
                        String fromMailAddress = message.getFrom()[0].toString();
                        Boolean foundValidFrom = Boolean.TRUE;
                        if (Boolean.valueOf(Config.get(Config.KEY_SENDER_ADDRESS_VALIDATION))) {
                            foundValidFrom = Boolean.FALSE;
                            String allowedSender = Config.get(Config.KEY_ALLOWED_SENDER);
                            String[] split = allowedSender.split(",");
                            for (String string : split) {
                                LOG.info("Checking email " + string);
                                if (fromMailAddress.contains(string)) {
                                    foundValidFrom = Boolean.TRUE;
                                }
                            }
                        }
                        if (foundValidFrom) {
                            try {
                                AlarmFax alarmMail = new AlarmFax();
                                
                                if (message.getContent() instanceof Multipart) {
                                    Multipart mP = (MimeMultipart) message.getContent();
                                    handleMultipart(alarmMail, (MimeMultipart) mP);
                                    /* check for attachment */
                                    message.getContent();
                                }
                                alarmMail.setAlarmTime(message.getSentDate());
                                alarmFax.add(alarmMail);
                                
                            } catch (Exception ex) {
                                LOG.error("Unable to determine alarm content...", ex);
                            }
                            
                        } else {
                            LOG.warn("Illegal sender detected ({})", fromMailAddress);
                            message.setFlag(Flags.Flag.FLAGGED, Boolean.TRUE);
                        }
                        message.setFlag(Flags.Flag.SEEN, Boolean.TRUE);
                        /* Markiere die Email als gelesen */
                    }
//                        message.setFlag(Flags.Flag.DELETED, Boolean.TRUE);
                }
                
            } finally {
                if (folder.isOpen()) {
                    folder.close(true);
                }
            }
        } finally {
            store.close();
        }
        this.deliverAlarms(alarmFax);
    }

    /**
     * Versucht aus der Multipart Mail den "normalen" Textteil zu finden. Wird
     * der erste Typ text/plain gefunden wird dieser zurück gegeben. Wenn kein
     * Text/Plain gefunden wurde, wird "Kein Alarmtext" returned.
     *
     * @param multipart
     * @throws MessagingException
     * @throws IOException
     */
    public static void handleMultipart(AlarmFax alarm, MimeMultipart multipart) throws Exception {
        LOG.debug("Found {} parts", String.valueOf(multipart.getCount()));
        List<MimeBodyPart> bodyParts = new ArrayList<>();
        for (int i = 0, n = multipart.getCount(); i < n; i++) {
            MimeBodyPart p = (MimeBodyPart) (multipart.getBodyPart(i));
            bodyParts.add(p);
            if (p.getContentType().contains("multipart/mixed")) {
                MimeMultipart content = (MimeMultipart) p.getContent();
                for (int j = 0; j < content.getCount(); j++) {
                    MimeBodyPart s = (MimeBodyPart) (content.getBodyPart(i));
                    bodyParts.add(s);
                    
                }
            }
        }
        for (MimeBodyPart p : bodyParts) {
            if (p.getContentType().startsWith("application/pdf")) {
                /* having attachment */
                LOG.info("Found attachment in mail");
                InputStream is = p.getInputStream();
                File f = new File(System.getProperty("java.io.tmpdir"), p.getFileName());
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buf)) != -1) {
                        fos.write(buf, 0, bytesRead);
                    }
                }
                File pdfToTxt = OCRScanner.pdfToTxt(f);
                OCRToAlarmMapper.mapOCRToAlarm(pdfToTxt, alarm);
            }
            
        }
    }

    /**
     * @see AlarmHeadquarter#deliverAlarms(java.util.List)
     */
    @Override
    public void deliverAlarms(List<AlarmFax> alarms) {
        if (alarms != null && !alarms.isEmpty()) {
            for (Recipient recipient : recipients) {
                recipient.deliver(alarms);
            }
        } else {
            LOG.trace("Keine neuen Alarme empfangen...");
        }
    }

    /**
     * Registriert einen neuen Empfänger für eingehende Alarme
     *
     * @param recipient
     */
    @Override
    public void registerRecipient(Recipient recipient) {
        recipients.add(recipient);
    }

    /**
     * Enfernt einen Empfänger aus der Liste der Empfänger für eingehende Alarme
     *
     * @param recipient
     */
    @Override
    public void deRegisterRecipient(Recipient recipient) {
        recipients.remove(recipient);
    }
    
    public void sendMail(AlarmFax fax) {

        // Sender's email ID needs to be mentioned
        String from = Config.get(Config.KEY_SMTP_FROM);

        // Setup mail server
     
        // Get the default Session object.
        Properties props = System.getProperties();
        props.put("mail.smtps.host",Config.get(Config.KEY_SMTP_HOST));
        props.put("mail.smtps.auth","true");
        Session session = Session.getInstance(props);
        
        for (String toAddress : fax.getMailAddresses()) {
            
            try {
                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);

                // Set From: header field of the header.
                message.setFrom(new InternetAddress(from));

                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));

                // Set Subject: header field
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                message.setSubject(fax.getAlarmKey() + sdf.format(new Date()));
                message.setSentDate(new Date());
                // Now set the actual message
                String messageText = fax.convertForBosmon();
                message.setText(messageText);
                LOG.info("Going to send following message\n\n{}",messageText);
                SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
                transport.connect(Config.get(Config.KEY_SMTP_HOST), Config.get(Config.KEY_SMTP_USER), Config.get(Config.KEY_SMTP_PASS));
                // Send message
                transport.sendMessage(message,message.getAllRecipients());
                LOG.info("Sent message successfully to {}", toAddress);
            } catch (MessagingException mex) {
                LOG.info("Unable to send message", mex);
            }
        }
    }
    
}

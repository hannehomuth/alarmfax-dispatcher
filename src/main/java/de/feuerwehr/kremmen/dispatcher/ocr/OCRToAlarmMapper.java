package de.feuerwehr.kremmen.dispatcher.ocr;

import de.feuerwehr.kremmen.dispatcher.alarm.Adress;
import de.feuerwehr.kremmen.dispatcher.alarm.AlarmFax;
import de.feuerwehr.kremmen.dispatcher.alarm.Coordinates;
import de.feuerwehr.kremmen.dispatcher.config.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jhomuth
 */
public class OCRToAlarmMapper {

    private static final Logger LOG = LoggerFactory.getLogger(OCRToAlarmMapper.class);

    /**
     * Method tries to find relevant fields from txt file
     *
     * @param txtFile
     * @param alarm
     */
    public static void mapOCRToAlarm(File txtFile, AlarmFax alarm) throws OCRException {
        try {
            /* Copy file content to string */
            String faxContent = readFile(txtFile.getAbsolutePath(), Charset.forName("UTF-8"));
            /* Remove spaces */
            String alarmFaxText = faxContent.trim().replaceAll(" +", " ");
            LOG.debug("Found following alarmtext\n{}", faxContent);
            if (alarmFaxText.toLowerCase().contains("alarmfax")) {
                alarm.setAlarmfaxDetected(Boolean.TRUE);
                alarm.setMailAddresses(getPossibleRics(alarmFaxText));
                alarm.setTelegramChannelIDs(getPossibleTelegramChannels(alarmFaxText));
                alarm.setAlarmKey(determineAlarmKey(alarmFaxText));
                alarm.setCoordinates(getCoordinates(alarmFaxText));
                alarm.setAdditionalInfo(determineAdditionalInfo(txtFile));
                alarm.setAddress(determineAddress(txtFile));
                alarm.setSituation(determineAlarmText(txtFile));
                alarm.setAlarmTime(getPossibleAlarmTime(txtFile));
            }
        } catch (IOException ex) {
            throw new OCRException("", ex);
        }
    }

    private static Date getPossibleAlarmTime(File txtFile) throws FileNotFoundException {
        Date d = null;
        Map<Integer, String> faxContentMap = getFileContentMap(txtFile);
        int lineOfDate = getLineNumberOf("Alarmzeit:", faxContentMap);
        String line = flatten(getLine(lineOfDate, faxContentMap)).toLowerCase();
        String replace = line.replace("alarmzeit:", "");

        if (replace.length() != 16) {
            LOG.warn("Illegal Date format in Alarmfax {}", replace);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyHH:mm:ss");
            try {
                d = format.parse(replace);
            } catch (ParseException ex) {
                LOG.error("Unable to parse date");
            }
        }
        return d;
    }

    private static Coordinates getCoordinates(String alarmFaxText) throws OCRException {
        Coordinates coordinates = new Coordinates();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(Config.get(Config.KEY_SPECIAL_STREET_FILE))));
        } catch (IOException ex) {
            throw new OCRException("Unable to detect special streets", ex);
        }
        String flattenAlarmText = flatten(alarmFaxText);
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            String flattenString = flatten(keyString);
            if (flattenAlarmText.contains(flattenString)) {
                String latlong = props.getProperty(keyString);
                coordinates.setLatitude(latlong.split(",")[0]);
                coordinates.setLongitude(latlong.split(",")[1]);
                coordinates.setFound(true);
                break;
            }
        }
        return coordinates;
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static String determineAlarmKey(String alarmText) throws FileNotFoundException {
        List<String> keyWords = getFileContentLineByLine(new File(Config.get(Config.KEY_KEYWORD_FILE)));
        String flattenAlarmText = flatten(alarmText);
        LOG.debug("Using flattend version \n{}", flattenAlarmText);
        for (String keyWord : keyWords) {
            String keyWordFlatten = flatten(keyWord);
            LOG.debug("Checking whether to find keyword :{}", keyWordFlatten);
            if (flattenAlarmText.contains(keyWordFlatten) || flattenAlarmText.replaceAll("i", "l").replaceAll("hr", "h:").replaceAll("bi", "b:").contains(keyWordFlatten)) {
                return keyWord;
            }
        }
        return "Unbekannt";
    }

    private static List<String> getPossibleRics(String alarmText) throws OCRException {
        List<String> forwardMailAddresses = new ArrayList<>();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(Config.get(Config.KEY_RIC_MAPPING_FILE))));
        } catch (IOException ex) {
            throw new OCRException("Unable to detect rics", ex);
        }
        String flattenAlarmText = flatten(alarmText);
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            if (flattenAlarmText.contains(keyString)) {
                forwardMailAddresses.add(props.getProperty(keyString));
            }
        }
        return forwardMailAddresses;
    }

    private static List<String> getPossibleTelegramChannels(String alarmText) throws OCRException {
        List<String> telegramChants = new ArrayList<>();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(Config.get(Config.KEY_TELEGRAM_CHAT_MAPPING_FILE))));
        } catch (IOException ex) {
            throw new OCRException("Unable to detect telegram chat ids", ex);
        }
        String flattenAlarmText = flatten(alarmText);
        for (Object key : props.keySet()) {
            String keyString = (String) key;
            if (flattenAlarmText.contains(keyString)) {
                telegramChants.add(props.getProperty(keyString));
            }
        }
        return telegramChants;
    }

    private static String determineAlarmText(File txtFile) throws FileNotFoundException {
        Map<Integer, String> faxContentMap = getFileContentMap(txtFile);
        int lineOfEreignis = getLineNumberOf("Ereignis:", faxContentMap);
        String ereignis = getLine(lineOfEreignis, faxContentMap).replace("Ereignis:", "").trim();
        return (ereignis == null || ereignis.isEmpty()) ? "Keine weiteren Angaben" : ereignis;
    }

    private static String determineAdditionalInfo(File txtFile) throws FileNotFoundException {
        Map<Integer, String> faxContentMap = getFileContentMap(txtFile);
        int lineOfCountry = getLineNumberOf("DE-Brandenburg", faxContentMap);
        String additionalInfo = getLine(lineOfCountry - 3, faxContentMap).trim();
        return additionalInfo;

    }

    private static Adress determineAddress(File txtFile) throws FileNotFoundException {
        Adress address = new Adress();
        Map<Integer, String> faxContentMap = getFileContentMap(txtFile);
        int lineOfCountry = getLineNumberOf("DE-Brandenburg", faxContentMap);
        String plzCityAndArea = getLine(lineOfCountry - 1, faxContentMap).replace("Einsatzort:", "").trim();
        String streetHouseNumber = getLine(lineOfCountry - 2, faxContentMap).replace("Einsatzort:", "").trim();
        int i = 0;
        while (i < streetHouseNumber.length() && !Character.isDigit(streetHouseNumber.charAt(i))) {
            i++;
        }
        address.setStreet(streetHouseNumber.substring(0, i).trim());
        address.setHousenumber(streetHouseNumber.substring(i).trim());

        String[] split = plzCityAndArea.split("/");
        if (split.length < 2) {
            return null;
        }
        if (split.length >= 1) {
            String plzCity = split[0];
            address.setPostalCode(plzCity.substring(0, 5));
            address.setCity(plzCity.substring(5).trim());
        }
        if (split.length >= 2) {
            address.setCityArea(split[1].trim());
        }
        if (split.length >= 3) {
            address.setLivingarea(split[2].trim());
        }
        return address;

    }

    private static String getLine(int lineNumber, Map<Integer, String> faxContentMap) {
        return faxContentMap.get((lineNumber));
    }

    private static int getLineNumberOf(String content, Map<Integer, String> faxContentMap) {
        int i = 1;
        for (int key : faxContentMap.keySet()) {
            String line = flatten(faxContentMap.get(key));
            if (line.contains(flatten(content))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static List<String> getFileContentLineByLine(File f) throws FileNotFoundException {
        Scanner s = new Scanner(f);
        ArrayList<String> list = new ArrayList<>();
        while (s.hasNextLine()) {
            list.add(s.nextLine());
        }
        s.close();
        return list;
    }

    private static Map<Integer, String> getFileContentMap(File f) throws FileNotFoundException {
        Scanner s = new Scanner(f);
        Map<Integer, String> list = new HashMap<>();
        int i = 1;
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (!line.trim().isEmpty()) {
                list.put(i, line);
                i++;
            }
        }
        s.close();
        return list;
    }

    private static String flatten(String s) {
        if (s != null) {
            return s.toLowerCase()
                    .replaceAll("ä", "ae")
                    .replaceAll("ö", "oe")
                    .replaceAll("ß", "ss")
                    .replaceAll("ü", "ue")
                    .replaceAll("-", "")
                    .replace(" ", "");
        }
        return s;
    }
}

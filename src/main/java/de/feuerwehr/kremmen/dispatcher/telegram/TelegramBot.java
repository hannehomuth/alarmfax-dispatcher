package de.feuerwehr.kremmen.dispatcher.telegram;

import de.feuerwehr.kremmen.dispatcher.config.Config;
import java.io.InputStream;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.*;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

// TODO: implement a way not to exceed bot messages limit
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class.getSimpleName());


    private static TelegramBotsApi telegramBotsApi;
    private static TelegramBot INSTANCE;

    private TelegramBot() {
        if (telegramBotsApi == null) {
            telegramBotsApi = new TelegramBotsApi();
        }
    }

    public static void init() throws TelegramApiRequestException {
        INSTANCE = new TelegramBot();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        telegramBotsApi.registerBot(INSTANCE);
    }

    public static TelegramBot getInstance() throws TelegramApiRequestException {
        if (INSTANCE == null) {
            TelegramBot.init();
        }
        return INSTANCE;
    }



    public Message sendDocument(String caption, String channelTo, InputStream docStream,
            String filename)
            throws TelegramApiException {
        SendDocument message = new SendDocument()
                .setChatId(channelTo);
        message.setCaption(caption);

        message.setNewDocument(filename, docStream);
        return sendDocument(message);
    }

    @Override
    public String getBotToken() {
        return Config.get(Config.KEY_TELEGRAM_API_TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOG.info("Recevied update {}", update);
    }

    @Override
    public String getBotUsername() {
        return Config.get(Config.KEY_TELEGRAM_BOT_USERNAME);
    }

}

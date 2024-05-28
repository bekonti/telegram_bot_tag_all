package com.example;

import com.example.config.BotConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


@Component
@AllArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {

        long chatId = 0l;
        Integer messageId = 0;
        String textToSend = "";
        String instagramUrl = "https://www.instagram.com/reel";
        String instagramDownloadedUrl = "https://www.ddinstagram.com/reel";
//      Health_checker
        if (update.getMessage().getChatId().equals(547364581l))
            privateMessage(update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();

            if (update.getMessage().getText().toLowerCase(Locale.ROOT).contains("@all")
                    || update.getMessage().getText().toLowerCase(Locale.ROOT).contains("@silkTagAllUsers_Bot".toLowerCase(Locale.ROOT))) {
                textToSend =
                        getAllMembersUsernames(chatId)
                                .stream()
                                .map(String::toString)
                                .collect(Collectors.joining(" "));
                sendMessage(chatId, textToSend, messageId);
            } else if (update.getMessage().getText().toLowerCase(Locale.ROOT).contains(instagramUrl)) {
                long finalChatId = chatId;
                Integer finalMessageId = messageId;
                Arrays.stream(update.getMessage().getText().split("[\\s,]+"))
                        .filter(s -> s.startsWith(instagramUrl))
                        .forEach(s -> {
                            sendMessage(finalChatId, s.replace(instagramUrl, instagramDownloadedUrl), finalMessageId);
                        });
            }

        }
    }

    private void privateMessage(Update update) {
        if (update.getMessage().getText().equals("/getInfo")) {
            StringBuffer ans = new StringBuffer();
            try {
                String[] command = {"ip", "addr"};

                // Start the process
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                // Read the output from the terminal
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    ans.append(line);
                }

                // Wait for the process to complete
                process.waitFor();

                // Close the reader
                reader.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            sendMessage(547364581l, ans.toString(), update.getMessage().getMessageId());
        } else {
            sendMessage(547364581l, "Bot works fine, Don't worry, Be Happy.", update.getMessage().getMessageId());
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    private void sendMessage(Long chatId, String textToSend, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.enableNotification();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
        log.info(textToSend);
    }

    private List<String> getAllMembersUsernames(Long chatId) {
        try {
            return execute(GetChatAdministrators.builder().chatId(chatId).build())
                    .stream()
                    .filter(chatMember -> chatMember.getUser().getIsBot().equals(false))
//                    .filter(chatMember -> chatMember.getUser().getUserName() != null)
                    .map(chatMember -> {
                        User user = chatMember.getUser();
                        StringBuilder usernameBuilder = new StringBuilder();
                        if (user.getUserName() == null) {
//                            return "";
                            usernameBuilder.append("(");
                            if (user.getFirstName() != null)
                                usernameBuilder.append(user.getFirstName());
                            if (user.getLastName() != null)
                                usernameBuilder.append(" ")
                                        .append(user.getLastName());
                            usernameBuilder.append(")");
                            return String.valueOf(usernameBuilder);
                        }
                        return "@" + user.getUserName();
                    })
                    .toList();
        } catch (TelegramApiException e) {
        }
        return new ArrayList<>();
    }

}

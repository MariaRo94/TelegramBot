package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class GetCryptoCurrencyRateBot extends TelegramLongPollingBot {

    private static final String API_BITCOIN = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd";
    private static final String BOT_TOKEN = "8010622979:AAE3GeyCINvdoySpHE0u03E_acwVh7YxgvQ";
    private static final String BOT_USERNAME = "CRYPTO_TRADER_BOT";

    @Override
    public String getBotUsername() {

        return BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        /**
         * Обрабатывает входящие обновления от Telegram
         * Этот метод вызывается автоматически при получении нового сообщения от пользователя.
         * Анализирует текст сообщения и выполняет соответствующие действия:
         * <ul>
         *   <li>На команду {@code /start} отправляет приветственное сообщение</li>
         *   <li>На команду {@code /price} или текст "курс" возвращает текущий курс Биткоина</li>
         *   <li>На неизвестные команды отправляет сообщение с инструкциями</li>
         * </ul>
         *
         * @param update объект {@link Update}, содержащий входящее обновление от Telegram API.
         *              Должен содержать текстовое сообщение для обработки.
         *
         * @implSpec Метод обрабатывает только текстовые сообщения (игнорирует стикеры, голосовые и др.).
         *           Для корректной работы требуется:
         *             <li>Подключенная библиотека telegrambots</li>
         *             <li>Реализованный метод {@code sendMessage}</li>
         *             <li>Доступ к API для получения курса Биткоина</li>
         *           </ul>
         *
         * @see #sendMessage(long, String) Метод для отправки сообщений
         * @see Update Класс входящего обновления
         *
         */
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getChatMember().getChat().getId();

            if ("start".equals(messageText)) {
                sendMessage(chatId, "Привет! Я твой бот-помощник для торговли криптовалютой! Дай мне команды" +
                        "и я помогу тебе в торговле");
            } else if ("/price".equals(messageText) || "курс".equalsIgnoreCase(messageText)) {
                try {
                    String bitcoinPrice = getBitcoinPrice();
                    sendMessage(chatId, "Текущий курс Биткоина: " + bitcoinPrice + " USD");
                } catch (Exception e) {
                    sendMessage(chatId, "Извините, не удалось получить курс Биткоина. Попробуйте позже.");
                    e.printStackTrace();
                }
            } else {
                sendMessage(chatId, "Я не понимаю эту команду. Используйте /price для получения курса Биткоина.");
            }
        }
    }

    private String getBitcoinPrice() throws IOException, InterruptedException {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_BITCOIN))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());

            if (!rootNode.has("bitcoin") || !rootNode.get("bitcoin").has("usd")) {
                throw new RuntimeException("Неверный формат ответа от API");
            }

            double price = rootNode.path("bitcoin").path("usd").asDouble();
            return String.format("%.2f USD", price);

        }
       catch (IOException | InterruptedException e) {
            throw new RuntimeException("Ошибка при получении курса Биткоина: " + e.getMessage(), e);
        }
}


    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

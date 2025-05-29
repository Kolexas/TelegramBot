package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s(.*)");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final NotificationTaskRepository notificationTaskRepository;
    @Autowired

    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Message message = update.message();
            if (message != null) {
                String text = message.text();
                if (text != null && text.equals("/start")) {
                    Chat chat = message.chat();
                    long chatId = chat.id();
                    String welcomeMessage = (chat.username() != null ? chat.username() : chat.firstName()) + ", привет! \nЯ бот, готовый помочь вам. Просто напишите мне что-нибудь!";
                    SendMessage sendMessage = new SendMessage(chatId, welcomeMessage);
                    telegramBot.execute(sendMessage);
                    logger.info("Sent welcome message to chat id: {}", chatId);
                } else if (text != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.matches()) {
                        String dateTimeString = matcher.group(1);
                        String reminderText = matcher.group(2);

                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
                            NotificationTask reminderEntity = new NotificationTask();
                            reminderEntity.setChat_id(message.chat().id());
                            reminderEntity.setDate(dateTime);
                            reminderEntity.setMessage(reminderText);
                            notificationTaskRepository.save(reminderEntity);
                            String confirmationMessage = "Напоминание сохранено: " + dateTimeString + " - " + reminderText;
                            sendMessage(message.chat().id(), confirmationMessage);
                            logger.info("Reminder saved and confirmation sent");

                        } catch (DateTimeParseException e) {
                            logger.error("Failed to parse date and time: {}", dateTimeString, e);
                            sendMessage(message.chat().id(), "Неверный формат даты и времени. Используйте дд.ММ.гггг чч:мм");
                        }
                    } else {
                        sendMessage(message.chat().id(), "Неверный формат сообщения. Используйте дд.ММ.гггг чч:мм Текст напоминания");
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        telegramBot.execute(sendMessage);
    }

    @Scheduled(cron = "0 * * * * *")
    public void processNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        logger.info("Checking for notifications at: {}", now);
        List<NotificationTask> tasks = notificationTaskRepository.findByDate(now);

        if (!tasks.isEmpty()) {
            logger.info("Found {} notifications to process", tasks.size());
            tasks.forEach(task -> {
                try {
                    sendMessage(task.getChat_id(), task.getMessage());
                    logger.info("Sent notification to chat {}: {}", task.getChat_id(), task.getMessage());
                    notificationTaskRepository.delete(task);
                } catch (Exception e) {
                    logger.error("Failed to send notification to chat {}: {}", task.getChat_id(), e.getMessage(), e);
                }
            });
        } else {
            logger.info("No notifications found for this minute.");
        }
    }
}


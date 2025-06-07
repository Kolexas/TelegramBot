package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long chat_id;
    String message;
    LocalDateTime date;

    public NotificationTask(Long id, Long chat_id, String message, LocalDateTime date) {
        this.id = id;
        this.chat_id = chat_id;
        this.message = message;
        this.date = date;
    }

    public NotificationTask() {
    }

    public Long getChat_id() {
        return chat_id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Long getId() {
        return id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTask)) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(chat_id, that.chat_id) && Objects.equals(message, that.message) &&
                Objects.equals(date, that.date) && Objects.equals(id,that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat_id, message, date,id);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "chat_id=" + chat_id +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}

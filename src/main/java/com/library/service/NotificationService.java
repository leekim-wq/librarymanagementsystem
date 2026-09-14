package com.library.service;

import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    public boolean sendToToken(String fcmToken, String title, String body) {
        if (!firebaseEnabled || fcmToken == null || fcmToken.isBlank()) {
            log.debug("Firebase disabled or no token — skipping notification");
            return false;
        }
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("📨 Notification sent: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM send failed: {}", e.getMessage());
            return false;
        }
    }

    // ---------- Library-specific notifications ----------

    public void notifyDueSoon(String fcmToken, String bookTitle, String dueDate) {
        sendToToken(fcmToken,
                "📚 Book Due Soon",
                "\"" + bookTitle + "\" is due on " + dueDate + ". Please return or renew it.");
    }

    public void notifyOverdue(String fcmToken, String bookTitle, double fine) {
        sendToToken(fcmToken,
                "⚠️ Overdue Book",
                "\"" + bookTitle + "\" is overdue. Current fine: $" + String.format("%.2f", fine));
    }

    public void notifyBookReturned(String fcmToken, String bookTitle) {
        sendToToken(fcmToken,
                "✅ Book Returned",
                "Thank you for returning \"" + bookTitle + "\"!");
    }

    public void notifyNewBook(String fcmToken, String bookTitle, String author) {
        sendToToken(fcmToken,
                "📖 New Book Available",
                "\"" + bookTitle + "\" by " + author + " is now available!");
    }
}
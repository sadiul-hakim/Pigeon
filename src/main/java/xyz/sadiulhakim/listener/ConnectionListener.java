package xyz.sadiulhakim.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.notification.Notification;
import xyz.sadiulhakim.notification.NotificationService;
import xyz.sadiulhakim.user.event.ConnectionEvent;

@Component
@RequiredArgsConstructor
class ConnectionListener {

    private final NotificationService notificationService;

    @Async("taskExecutor")
    @EventListener
    void connectionEvent(ConnectionEvent event) {
        Notification notification = new Notification();
        notification.setMessage(event.message());
        notification.setUserId(event.user());
        notificationService.save(notification);
    }
}

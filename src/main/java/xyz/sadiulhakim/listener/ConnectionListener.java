package xyz.sadiulhakim.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.notification.model.NotificationService;
import xyz.sadiulhakim.user.event.ConnectionEvent;

@Component
@RequiredArgsConstructor
class ConnectionListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    void connectionEvent(ConnectionEvent event) {
        notificationService.sendNotification(event.message(), event.type(), event.user());
    }
}

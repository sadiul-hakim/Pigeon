package xyz.sadiulhakim.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.group.event.GroupEvent;
import xyz.sadiulhakim.notification.Notification;
import xyz.sadiulhakim.notification.NotificationService;

@Component
@RequiredArgsConstructor
public class GroupEventListener {

    private final NotificationService notificationService;

    @Async("taskExecutor")
    @EventListener
    void connectionEvent(GroupEvent event) {

        // Other Logic
        Notification notification = new Notification();
        notification.setMessage(event.message());
        notification.setUserId(event.user());
        notificationService.save(notification);
    }
}

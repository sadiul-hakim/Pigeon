package xyz.sadiulhakim.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import xyz.sadiulhakim.user.UserService;

@Component
public class WebSocketEventsListener {

    private final UserService userService;

    public WebSocketEventsListener(UserService userService) {
        this.userService = userService;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        userService.updateStatus(event.getUser().getName(), false);
    }

    @Async("taskExecutor")
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        userService.updateStatus(event.getUser().getName(), true);
    }
}

package xyz.sadiulhakim.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AppListener {

    @Async("taskExecutor")
    @EventListener
    void serverStarted(WebServerInitializedEvent event) {
        System.out.println("Application is running on : http://localhost:" + event.getWebServer().getPort());
    }

    @Async("taskExecutor")
    @EventListener
    void ready(ApplicationReadyEvent event) {
        System.out.printf("Time Taken : %s sec\n", event.getTimeTaken().getSeconds());
    }
}

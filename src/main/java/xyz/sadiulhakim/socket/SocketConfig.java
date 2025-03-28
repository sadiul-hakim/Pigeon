package xyz.sadiulhakim.socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.socket.endpoint:''}")
    private String endpoint;

    @Value("${app.socket.app_prefix:''}")
    private String appPrefix;

    @Value("${app.socket.user_prefix:''}")
    private String userPrefix;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Use a shared scheduled executor
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setThreadFactory(Thread.ofVirtual().factory());
//        scheduler.initialize();

        SimpleAsyncTaskScheduler taskScheduler = new SimpleAsyncTaskScheduler();
        taskScheduler.setVirtualThreads(true);

        registry.enableSimpleBroker("/queue")
                .setTaskScheduler(taskScheduler);

        registry.setApplicationDestinationPrefixes(appPrefix);
        registry.setUserDestinationPrefix(userPrefix);
    }
}

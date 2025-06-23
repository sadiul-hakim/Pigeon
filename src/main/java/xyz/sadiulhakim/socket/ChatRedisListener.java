package xyz.sadiulhakim.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.chat.pojo.RedisMessage;

@Slf4j
@Component
public class ChatRedisListener implements MessageListener {

    @Value("${app.socket.personal_message_channel:''}")
    private String PERSONAL_MESSAGE_CHANNEL;

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatRedisListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {

            // Listen to message publish
            // When message received send through STOMP
            RedisMessage redisMessage = objectMapper.readValue(message.getBody(), RedisMessage.class);

            switch (redisMessage.area()) {
                case PEOPLE -> sendPersonalMessage(redisMessage);
                case GROUP -> sendGroupMessage(redisMessage);
            }
        } catch (Exception e) {
            log.error("ChatRedisListener.onMessage :: error {}", e.getMessage()); // or log
        }
    }

    private void sendGroupMessage(RedisMessage redisMessage) {
        messagingTemplate.convertAndSend("/topic/" + redisMessage.groupChannel(), redisMessage.message());
    }

    private void sendPersonalMessage(RedisMessage redisMessage) {
        for (String recipient : redisMessage.recipients()) {
            messagingTemplate.convertAndSendToUser(
                    recipient,
                    PERSONAL_MESSAGE_CHANNEL,
                    redisMessage.message()
            );
        }
    }
}

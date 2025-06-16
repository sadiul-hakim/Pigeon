package xyz.sadiulhakim.socket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import xyz.sadiulhakim.config.security.CustomUserDetails;

import java.security.Principal;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Get user from your backend security context (e.g. JWT/session)
            // Example using an existing authenticated Principal (e.g. from session):
            Principal user = accessor.getUser();
            if (user instanceof RememberMeAuthenticationToken token) {
                var principal = token.getPrincipal();
                if (principal instanceof CustomUserDetails userDetails) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(), null, userDetails.getAuthorities()
                    );
                    accessor.setUser(auth); // So that later methods can receive Principal
                    SecurityContextHolder.getContext().setAuthentication(auth); // 🔐 This is the key line
                }
            }
        }

        return message;
    }
}

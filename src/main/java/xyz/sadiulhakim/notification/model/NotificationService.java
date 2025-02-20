package xyz.sadiulhakim.notification.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.sadiulhakim.config.security.CustomUserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final NotificationRepository notificationRepository;

    public SseEmitter subscribeCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.putIfAbsent(principal.id(), emitter);

        return emitter;
    }

    public void sendNotification(String message, String event, UUID user) {
        SseEmitter emitter = emitters.get(user);

        if (emitter == null) {
            log.warn("User {} did not subscribe for notification!", user);
            return;
        }

        try {

            emitter.onCompletion(() -> {
                handleErrorCase(user, event, message, emitter);
            });

            emitter.onTimeout(() -> {
                handleErrorCase(user, event, message, emitter);
            });

            emitter.onError((ex) -> {
                handleErrorCase(user, event, message, emitter);
            });

            emitter.send(SseEmitter.event().name(event).data(message));
        } catch (Exception ex) {
            log.error("NotificationService.sendNotification :: error : {}", ex.getMessage());
            emitter.completeWithError(ex);
            emitters.remove(user);
            handleErrorCase(user, event, message, emitter);
        }

        Thread.ofVirtual().name("#NotificationSavingThread-", 0).start(() -> {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setUserId(user);
            save(notification);
        });
    }

    public long count() {
        return notificationRepository.count();
    }

    private void handleErrorCase(UUID user, String event, String message, SseEmitter emitter) {
        emitters.remove(user);

        SseEmitter emitter1 = new SseEmitter(Long.MAX_VALUE);
        try {
            emitter1.send(SseEmitter.event().name(event).data(message));
            emitters.put(user, emitter1);
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public List<Notification> notificationsOfCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return Collections.emptyList();
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return notificationRepository.findAllByUserId(principal.id());
    }

    public Notification findById(long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public void delete(long id) {

        Notification notification = findById(id);
        if (notification == null) return;

        notificationRepository.delete(notification);
    }
}

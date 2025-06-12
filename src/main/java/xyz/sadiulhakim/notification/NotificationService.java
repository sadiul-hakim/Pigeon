package xyz.sadiulhakim.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import xyz.sadiulhakim.config.security.CustomUserDetails;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public long count() {
        return notificationRepository.count();
    }

    public long countByUser(UUID userId) {
        return notificationRepository.countByUserId(userId);
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

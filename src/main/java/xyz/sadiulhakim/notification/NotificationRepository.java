package xyz.sadiulhakim.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserId(UUID user);
    long count();
    long countByUserId(UUID userId);
}

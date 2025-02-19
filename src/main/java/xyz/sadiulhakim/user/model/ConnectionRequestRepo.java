package xyz.sadiulhakim.user.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRequestRepo extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findAllByUser(User userId);

    List<ConnectionRequest> findAllByToUser(User toUser);

    Optional<ConnectionRequest> findByUserAndToUser(User userId, User toUser);
}

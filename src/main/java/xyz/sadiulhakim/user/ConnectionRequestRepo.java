package xyz.sadiulhakim.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRequestRepo extends JpaRepository<ConnectionRequest, Long> {

    List<ConnectionRequest> findAllByUser(User userId);

    List<ConnectionRequest> findAllByToUser(User toUser);

    Optional<ConnectionRequest> findByUserAndToUser(User userId, User toUser);
}

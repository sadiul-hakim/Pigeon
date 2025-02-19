package xyz.sadiulhakim.user.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findByFirstnameContainingOrLastnameContainingOrEmailContaining(String firstName, String lastName,
                                                                              String email, Pageable page);

    @Query(value = "select count(*) from User")
    long numberOfUsers();

    Page<User> findAllByIdIn(List<UUID> ids, Pageable pageable);
}

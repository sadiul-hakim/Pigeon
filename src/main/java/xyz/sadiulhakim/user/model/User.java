package xyz.sadiulhakim.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import xyz.sadiulhakim.converter.ListUUIDConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 45, nullable = false)
    private String firstname;

    @Column(length = 45, nullable = false)
    private String lastname;

    @Column(length = 75, nullable = false, unique = true)
    private String email;

    @Column(length = 130, nullable = false)
    private String password;

    @Column(length = 35, nullable = false)
    private String role;

    @Column(length = 200, nullable = false)
    private String picture;

    @Column(length = 35, nullable = false)
    private String textColor;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = ListUUIDConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private List<UUID> connectedUsers = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
}

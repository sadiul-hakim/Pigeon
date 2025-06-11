package xyz.sadiulhakim.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.sadiulhakim.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private User toUser;

    @Column(length = 1500)
    private String message;

    @Column(length = 120)
    private String filename;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime sendTime;
}

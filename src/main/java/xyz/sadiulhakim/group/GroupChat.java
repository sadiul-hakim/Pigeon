package xyz.sadiulhakim.group;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ChatGroup group;

    @ManyToOne
    private GroupMember sender;

    @Column(length = 1500)
    private String message;

    @Column(length = 120)
    private String filename;

    private LocalDateTime sendTime;
}

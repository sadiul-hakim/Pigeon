package xyz.sadiulhakim.group;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 55)
    private String name;

    @Column(length = 100, nullable = false)
    private String picture;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members;

    private LocalDateTime createdAt;

    public void addMember(GroupMember groupMember) {
        if (groupMember == null ||
                groupMember.getId() == null ||
                groupMember.getGroup() == null ||
                groupMember.getUser() == null) {
            return;
        }

        getMembers().add(groupMember);
    }
}

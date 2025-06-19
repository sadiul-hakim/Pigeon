package xyz.sadiulhakim.group;

import jakarta.persistence.*;
import lombok.*;
import xyz.sadiulhakim.group.enumeration.GroupMemberRole;
import xyz.sadiulhakim.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupMember {

    //  To make sure that each (groupId, userId) pair is unique and acts as a primary key
    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id", columnDefinition = "uuid", nullable = false)
    private ChatGroup group;

    // ManyToOne, Because a user can join multiple group. Under each group user would have a GroupMember record.
    // Meaning user can have multiple GroupMember.
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", columnDefinition = "uuid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private GroupMemberRole role;

    @ManyToOne
    @JoinColumn(name = "added_by_id", columnDefinition = "uuid")
    private User addedBy;  // can be null if user is OWNER

    private LocalDateTime joinedAt;
}


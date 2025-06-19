package xyz.sadiulhakim.group;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberId implements Serializable {
    private UUID groupId;
    private UUID userId;
}

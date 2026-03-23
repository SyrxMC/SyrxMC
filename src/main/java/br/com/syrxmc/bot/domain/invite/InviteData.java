package br.com.syrxmc.bot.domain.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteData {

    private ObjectId id;
    private String guildId;
    private String inviterUserId;
    private String inviteCode;
    private int count;
    private List<String> invitedUserIds;
    private Instant updatedAt;
}

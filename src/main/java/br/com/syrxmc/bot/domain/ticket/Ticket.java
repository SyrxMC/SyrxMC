package br.com.syrxmc.bot.domain.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private ObjectId id;
    private String userId;
    private String channelId;
    private String guildId;
    private TicketType type;
    private TicketStatus status;
    private Instant openedAt;
    private Instant closedAt;
    private String closedBy;
    private Double saleValue;
    private String backupPath;
}

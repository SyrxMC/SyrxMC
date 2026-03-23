package br.com.syrxmc.bot.domain.gold;

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
public class GoldStock {

    private ObjectId id;
    private String guildId;
    private String serverName;
    private long amount;
    private Instant updatedAt;
    private String updatedBy;
}

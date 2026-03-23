package br.com.syrxmc.bot.domain.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildConfig {

    private ObjectId id;
    private String guildId;
    private String token;
    private Channels channels;
    private Roles roles;
    private String ticketCategoryId;
    private Messages messages;
    private boolean inviteEventActive;
    private List<String> ignoredUserIds;
    private String greetMessage;
    private String greetImageUrl;
    private String color;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Channels {
        private String menu;
        private String info;
        private String greet;
        private String invite;
        private String logsCash;
        private String logsGold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Roles {
        private String cash;
        private String gold;
        private String intermedio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Messages {
        private String lastMenuMessageId;
        private String lastLeaderboardMessageId;
        private String lastGoldStockMessageId;
    }
}

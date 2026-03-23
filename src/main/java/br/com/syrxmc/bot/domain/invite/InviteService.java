package br.com.syrxmc.bot.domain.invite;

import br.com.syrxmc.bot.database.repositories.GuildConfigRepository;
import br.com.syrxmc.bot.database.repositories.InviteRepository;
import br.com.syrxmc.bot.domain.guild.GuildConfig;

import java.util.List;
import java.util.Optional;

public class InviteService {

    private final InviteRepository inviteRepository;
    private final GuildConfigRepository guildConfigRepository;

    public InviteService(InviteRepository inviteRepository, GuildConfigRepository guildConfigRepository) {
        this.inviteRepository = inviteRepository;
        this.guildConfigRepository = guildConfigRepository;
    }

    public void upsertInviter(String guildId, String inviteCode, String inviterUserId) {
        inviteRepository.upsertInviter(guildId, inviteCode, inviterUserId);
    }

    public boolean isEventActive(String guildId) {
        Optional<GuildConfig> config = guildConfigRepository.findByGuildId(guildId);
        return config.map(GuildConfig::isInviteEventActive).orElse(false);
    }

    public void recordJoin(String guildId, String inviteCode, String newUserId) {
        inviteRepository.recordJoin(guildId, inviteCode, newUserId);
    }

    public void recordLeave(String guildId, String userId) {
        inviteRepository.recordLeave(guildId, userId);
    }

    public List<InviteData> getLeaderboard(String guildId) {
        return inviteRepository.getLeaderboard(guildId, 5);
    }

    public int getUserInviteCount(String guildId, String userId) {
        List<InviteData> all = inviteRepository.getLeaderboard(guildId, Integer.MAX_VALUE);
        return all.stream()
                .filter(d -> userId.equals(d.getInviterUserId()))
                .mapToInt(InviteData::getCount)
                .sum();
    }
}

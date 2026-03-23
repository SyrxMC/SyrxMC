package br.com.syrxmc.bot.utils;

import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.invite.InviteData;
import br.com.syrxmc.bot.domain.invite.InviteService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LeadboardScheduler implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LeadboardScheduler.class);

    public static final List<String> ignoredIds = List.of(
            "184093192243642368",
            "464980441586466826",
            "398859614596366336",
            "248841176038375424"
    );

    // Static references set by SyrxBot before scheduling
    private static GuildConfigService guildConfigService;
    private static InviteService inviteService;
    private static JDA jda;

    private static String registeredGuildId;

    public static void setServices(GuildConfigService guildConfigService, InviteService inviteService, JDA jda) {
        LeadboardScheduler.guildConfigService = guildConfigService;
        LeadboardScheduler.inviteService = inviteService;
        LeadboardScheduler.jda = jda;
    }

    public static void setRegisteredGuildId(String guildId) {
        LeadboardScheduler.registeredGuildId = guildId;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (guildConfigService == null || inviteService == null || jda == null) {
            logger.warn("LeadboardScheduler services not configured — skipping execution.");
            return;
        }

        // Determine guild ID: use registered or first available guild
        String guildId = registeredGuildId;
        if (guildId == null || guildId.isBlank()) {
            if (jda.getGuilds().isEmpty()) {
                logger.warn("No guilds available in JDA, skipping leaderboard update.");
                return;
            }
            guildId = jda.getGuilds().get(0).getId();
        }

        GuildConfig config;
        try {
            config = guildConfigService.getConfig(guildId);
        } catch (Exception e) {
            logger.warn("Could not get guild config for leaderboard update: {}", e.getMessage());
            return;
        }

        if (!config.isInviteEventActive()) {
            return;
        }

        List<InviteData> top = inviteService.getLeaderboard(guildId);
        // Filter ignored IDs
        top = top.stream()
                .filter(d -> !ignoredIds.contains(d.getInviterUserId()))
                .limit(5)
                .toList();

        if (config.getChannels() == null || config.getChannels().getInvite() == null) {
            logger.warn("Invite channel not configured for guild {}", guildId);
            return;
        }

        TextChannel inviteChannel = jda.getChannelById(TextChannel.class, config.getChannels().getInvite());
        if (inviteChannel == null) {
            logger.warn("Invite channel not found: {}", config.getChannels().getInvite());
            return;
        }

        String color = config.getColor();
        MessageEmbed embed = SyrxEmbeds.leaderboard(top, color);

        String lastMessageId = config.getMessages() != null ? config.getMessages().getLastLeaderboardMessageId() : null;
        final String finalGuildId = guildId;

        if (lastMessageId != null) {
            inviteChannel.editMessageEmbedsById(lastMessageId, embed).queue(
                    success -> logger.debug("Leaderboard updated for guild {}", finalGuildId),
                    error -> {
                        // Message deleted or not found — send a new one
                        inviteChannel.sendMessageEmbeds(embed).queue(msg -> {
                            guildConfigService.updateLastLeaderboardMessageId(finalGuildId, msg.getId());
                        });
                    }
            );
        } else {
            inviteChannel.sendMessageEmbeds(embed).queue(msg -> {
                guildConfigService.updateLastLeaderboardMessageId(finalGuildId, msg.getId());
            });
        }
    }
}

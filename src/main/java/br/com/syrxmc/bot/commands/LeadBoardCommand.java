package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.invite.InviteData;
import br.com.syrxmc.bot.domain.invite.InviteService;
import br.com.syrxmc.bot.utils.LeadboardScheduler;
import br.com.syrxmc.bot.utils.SyrxEmbeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RegisterCommand
public class LeadBoardCommand extends SlashCommand {

    private static final Logger logger = LoggerFactory.getLogger(LeadBoardCommand.class);

    public LeadBoardCommand() {
        super("leadboard", "Cria o leadboard");
        this.addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.ignoreReplyWait();

        String guildId = event.getGuild().getId();
        InviteService inviteService = ServiceRegistry.getInviteService();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();

        List<InviteData> top = inviteService.getLeaderboard(guildId);
        // Filter ignored IDs
        top = top.stream()
                .filter(d -> !LeadboardScheduler.ignoredIds.contains(d.getInviterUserId()))
                .limit(5)
                .toList();

        GuildConfig config;
        try {
            config = guildConfigService.getConfig(guildId);
        } catch (Exception e) {
            logger.warn("Could not load guild config: {}", e.getMessage());
            event.getChannel().sendMessageEmbeds(SyrxEmbeds.error("Configuração do servidor não encontrada.")).queue();
            return;
        }

        String color = config.getColor();
        MessageEmbed embed = SyrxEmbeds.leaderboard(top, color);

        String inviteChannelId = config.getChannels() != null ? config.getChannels().getInvite() : null;
        TextChannel targetChannel;
        if (inviteChannelId != null) {
            targetChannel = event.getGuild().getChannelById(TextChannel.class, inviteChannelId);
        } else {
            targetChannel = event.getTextChannel();
        }

        if (targetChannel == null) {
            targetChannel = event.getTextChannel();
        }

        final TextChannel finalChannel = targetChannel;
        String lastMsgId = config.getMessages() != null ? config.getMessages().getLastLeaderboardMessageId() : null;

        if (lastMsgId != null) {
            finalChannel.editMessageEmbedsById(lastMsgId, embed).queue(
                    success -> {},
                    err -> finalChannel.sendMessageEmbeds(embed).queue(msg ->
                            guildConfigService.updateLastLeaderboardMessageId(guildId, msg.getId()))
            );
        } else {
            finalChannel.sendMessageEmbeds(embed).queue(msg ->
                    guildConfigService.updateLastLeaderboardMessageId(guildId, msg.getId()));
        }
    }
}

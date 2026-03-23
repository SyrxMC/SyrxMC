package br.com.syrxmc.bot.listeners;

import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberJoinListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MemberJoinListener.class);

    private final GuildConfigService guildConfigService;

    public MemberJoinListener(GuildConfigService guildConfigService) {
        this.guildConfigService = guildConfigService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GuildConfig config;
        try {
            config = guildConfigService.getConfig(event.getGuild().getId());
        } catch (Exception e) {
            logger.warn("Could not get guild config for member join event: {}", e.getMessage());
            return;
        }

        if (config.getChannels() == null || config.getChannels().getGreet() == null) {
            return;
        }

        TextChannel channel = event.getGuild().getChannelById(TextChannel.class, config.getChannels().getGreet());
        if (channel == null) {
            logger.warn("Greet channel not found: {}", config.getChannels().getGreet());
            return;
        }

        Member member = event.getMember();
        String author = String.format("%s(%s)", member.getEffectiveName(), member.getId());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author);

        if (config.getGreetMessage() != null && !config.getGreetMessage().isBlank()) {
            embed.setDescription(config.getGreetMessage()
                    .replace("{user}", member.getAsMention())
                    .replace("{username}", member.getEffectiveName()));
        }

        if (config.getGreetImageUrl() != null && !config.getGreetImageUrl().isBlank()) {
            embed.setImage(config.getGreetImageUrl());
        }

        channel.sendMessage(event.getUser().getAsMention()).queue();
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}

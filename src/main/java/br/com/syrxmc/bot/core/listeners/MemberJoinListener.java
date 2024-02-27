package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;

public class MemberJoinListener extends DynamicHandler<GuildMemberJoinEvent> {

    private final Config config;

    public MemberJoinListener(Config config) {
        super(event -> true);
        this.config = config;
    }

    @Override
    public void onEvent(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getGuild().getChannelById(TextChannel.class, config.getGreetingChannelId());

        String author = String.format("%s(%s)", event.getMember().getEffectiveName(), event.getMember().getId());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author);

        channel.sendMessage(event.getUser().getAsMention()).queue();
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}

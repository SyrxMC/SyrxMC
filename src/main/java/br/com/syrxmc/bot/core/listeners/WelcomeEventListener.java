package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;

public class WelcomeEventListener extends DynamicHandler<GuildMemberJoinEvent> {

    public WelcomeEventListener() {
        super(guildMemberJoinEvent -> true);
    }

    @Override
    public void onEvent(GuildMemberJoinEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("""
                Welcome to Blade Community ${user}!
                """
                .replace("${user}", event.getMember().getAsMention()));
        builder.setColor(Color.decode("#5cb85c"));
        builder.setThumbnail(event.getMember().getEffectiveAvatarUrl());
        builder.setFooter(" We hope you can get fun in our discord.");

        event.getGuild().getChannelById(TextChannel.class, "1360969887497060435").sendMessage(event.getMember().getAsMention()).addEmbeds(builder.build()).queue();
    }
}

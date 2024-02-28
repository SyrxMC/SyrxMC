package br.com.syrxmc.bot.core.command;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.SyrxCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class SlashCommandEvent {

    private final SyrxCore core = Main.getSyrxCore();

    private final SlashCommandInteractionEvent event;

    public ReplyCallbackAction reply(String message) {
        return event.reply(message);
    }

    public ReplyCallbackAction reply(String message, Object... format) {
        return event.replyFormat(message, format);
    }

    public ReplyCallbackAction reply(Collection<? extends LayoutComponent> components) {
        return event.replyComponents(components);
    }

    public String getStringOption(String name) {
        return event.getOption(name).getAsString();
    }

    public Member getMemberOption(String name) {
        return event.getOption(name).getAsMember();
    }

    public User getUserOption(String name) {
        return event.getOption(name).getAsUser();
    }

    public ReplyCallbackAction reply(MessageEmbed message) {
        return event.replyEmbeds(message);
    }

    public ReplyCallbackAction defer() {
        return event.deferReply();
    }

    public void ignoreReplyWait() {
        event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
    }


    public Guild getGuild() {
        return event.getGuild();
    }

    public User getAuthor() {
        return event.getUser();
    }

    public Member getMember() {
        return event.getMember();
    }

    public JDA getJda() {
        return event.getJDA();
    }

    public MessageChannel getChannel() {
        return event.getMessageChannel();
    }

    public TextChannel getTextChannel() {
        return event.getChannel().asTextChannel();
    }

}

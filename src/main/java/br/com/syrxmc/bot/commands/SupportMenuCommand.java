package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;

//@RegisterCommand
public class SupportMenuCommand extends SlashCommand {

    public SupportMenuCommand(){
        super("support", "Support Menu", true);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.ignoreReplyWait();
        Button ticket = Button.of(ButtonStyle.SECONDARY,"support:ticket", "Ticket", Emoji.fromUnicode("❓"));
        Button suggestion = Button.of(ButtonStyle.SECONDARY,"support:suggestion", "Sugestão", Emoji.fromUnicode("\uD83D\uDCA1"));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bem vindo ao SAP (Serviço de Atendimento ao Player)");
        builder.setDescription("Em que podemos te ajudar?");
        builder.setColor(Color.decode("#282b30"));
        builder.setImage("https://centraldeajuda.deliverymuch.com.br/hc/article_attachments/4402921158164/unnamed__3_.gif");
        builder.setFooter("SyrxMC - Sempre trazendo o melhor para a sua diversão!");
        event.getChannel().sendMessageEmbeds(builder.build()).addActionRow(ticket, suggestion).queue();
    }
}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.utils.Announcement;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class ServerAnnouncement extends SlashCommand {

    public ServerAnnouncement() {
        super("anuncio", "Fazer anuncio de servidor.");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.STRING, "tipo", "Tipo de anuncio")
                .setRequired(true)
                .addChoice("Servidor", "server")
                .addChoice("Gold", "gold")
                .addChoice("Genérico", "normal")
        );
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.reply("Começando...").setEphemeral(true).queue();
        new Announcement(event.getStringOption("tipo"), event.getAuthor().getId(), event.getChannel().getId(), Main.getSyrxCore().getEventWaiter())
                .startInteraction();

    }
}

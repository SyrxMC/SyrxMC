package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.data.Invites;
import net.dv8tion.jda.api.Permission;

public class DisableInviteEventCommand extends SlashCommand {

    public DisableInviteEventCommand() {
        super("invites", "Ativa/Desabilita os evento de invites");
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        Invites invites = Main.getInvites();

        invites.setActive(!invites.isActive());
        Main.getInvitesDataManager().save(invites);
        Main.reloadConfig();

        event.reply(invites.isActive() ? "Evento ativado": "Evento desativado").setEphemeral(true).queue();
    }
}

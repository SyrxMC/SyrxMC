package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@RegisterCommand
public class RolesCommand extends SlashCommand {

    public RolesCommand() {
        super("roles-menu", "Menu for roles");
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.ignoreReplyWait();
        Button arise4s = Button.primary("1361318367243538623", "Arise4s");
        Button altherianMU = Button.primary("1361319139356180692", "AltherianMU");
        Button primeTera = Button.primary("1361319329563410512", "Prime TERA");
        event.getTextChannel().sendMessage("Select a role")
                .addActionRow(arise4s, altherianMU, primeTera).queue();
    }

}

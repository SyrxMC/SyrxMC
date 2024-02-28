package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandListener extends DynamicHandler<SlashCommandInteractionEvent> {

    private final SyrxCore syrxCore;

    public CommandListener(SyrxCore syrxCore) {
        super(event -> true);
        this.syrxCore = syrxCore;
    }

    @Override
    public void onEvent(SlashCommandInteractionEvent event) {

        SlashCommand command = syrxCore.getCommandManager().getCommand(event.getName());

        if (command == null) {
            return;
        }

        String subcommandName = event.getSubcommandName();

        if (subcommandName != null) {

            SlashSubcommand subcommand = command.getSubcommands().get(subcommandName);

            if (subcommand == null) {
                event.reply("Invalid subcommand").setEphemeral(true).queue();
                return;
            }

            try {
                subcommand.execute(event);
            } catch (Exception e) {
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }

        } else {

            try {
                command.execute(new SlashCommandEvent(event));
            } catch (Exception e) {
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }

        }

    }

}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;

@RegisterCommand
public class PingCommand extends SlashCommand {

    public PingCommand() {
        super("ping", "Ping do Bot");
    }

    @Override
    public void execute(SlashCommandEvent event) {


        event.reply("Pong! %s ms", event.getJda().getGatewayPing())
                .setEphemeral(true)
                .queue();

    }

}

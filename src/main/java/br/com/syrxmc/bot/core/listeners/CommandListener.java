package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener extends DynamicHandler<SlashCommandInteractionEvent> {

    private final SyrxCore syrxCore;

    private final static Logger logger = LoggerFactory.getLogger(CommandListener.class);

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

        // Checagem de permissão/cargo antes de executar o comando
        if (event.getMember() == null) {
            event.reply("Este comando só pode ser usado no servidor.").setEphemeral(true).queue();
            return;
        }

        boolean authorized;
        try {
            var member = event.getMember();
            var requiredPerms = command.getPermissions();
            var requiredRoles = command.getRequiredRoleIds();

            boolean hasPerms = requiredPerms == null || requiredPerms.isEmpty() || member.getPermissions().containsAll(requiredPerms);
            boolean hasRole = false;
            if (requiredRoles != null && !requiredRoles.isEmpty()) {
                hasRole = member.getRoles().stream().anyMatch(r -> requiredRoles.contains(r.getId()));
            }
            // Regra: autorizado se tiver TODAS as permissões exigidas OU pelo menos um dos cargos exigidos
            authorized = hasPerms || hasRole;
        } catch (Exception e) {
            authorized = false;
        }

        if (!authorized) {
            event.reply("Você não tem permissão para usar este comando.").setEphemeral(true).queue();
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
                logger.error("Houve um erro ao executar o comando: {}", command.getName(), e);
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }

        } else {

            try {
                command.execute(new SlashCommandEvent(event));
            } catch (Exception e) {
                logger.error("Houve um erro ao executar o comando: {}", command.getName(), e);
                event.reply("An error occurred: " + e.getMessage()).setEphemeral(true).queue();
            }

        }

    }

}

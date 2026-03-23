package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import net.dv8tion.jda.api.Permission;

@RegisterCommand
public class DisableInviteEventCommand extends SlashCommand {

    public DisableInviteEventCommand() {
        super("invites", "Ativa/Desabilita os evento de invites");
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String guildId = event.getGuild().getId();
        GuildConfigService guildConfigService = ServiceRegistry.getGuildConfigService();

        GuildConfig config = guildConfigService.getConfig(guildId);
        boolean newState = !config.isInviteEventActive();
        guildConfigService.setInviteEventActive(guildId, newState);

        event.reply(newState ? "Evento ativado" : "Evento desativado").setEphemeral(true).queue();
    }
}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import net.dv8tion.jda.api.Permission;

@RegisterCommand
public class SetupCommand extends SlashCommand {

    public SetupCommand() {
        super("setup", "Inicializa a configuração do servidor no banco de dados");
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String guildId = event.getGuild().getId();
        GuildConfigService service = ServiceRegistry.getGuildConfigService();

        GuildConfig config = service.initConfig(guildId);
        boolean isNew = config.getId() == null || config.getChannels() == null
                || config.getChannels().getMenu() == null;

        event.reply("Configuração " + (isNew ? "criada" : "já existente") + " para este servidor.\n" +
                "Use `/config-canais`, `/config-cargos` e `/config-ticket-categoria` para configurar.")
                .setEphemeral(true).queue();
    }
}

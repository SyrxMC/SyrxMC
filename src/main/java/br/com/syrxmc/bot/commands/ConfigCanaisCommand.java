package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class ConfigCanaisCommand extends SlashCommand {

    public ConfigCanaisCommand() {
        super("config-canais", "Configura os canais do servidor");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.CHANNEL, "menu", "Canal do menu de cash", false));
        addOption(new OptionData(OptionType.CHANNEL, "info", "Canal de informações (gold stock)", false));
        addOption(new OptionData(OptionType.CHANNEL, "greet", "Canal de boas-vindas", false));
        addOption(new OptionData(OptionType.CHANNEL, "invite", "Canal público para invites", false));
        addOption(new OptionData(OptionType.CHANNEL, "logs-cash", "Canal de logs de cash", false));
        addOption(new OptionData(OptionType.CHANNEL, "logs-gold", "Canal de logs de gold", false));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        OptionMapping menu = event.getEvent().getOption("menu");
        OptionMapping info = event.getEvent().getOption("info");
        OptionMapping greet = event.getEvent().getOption("greet");
        OptionMapping invite = event.getEvent().getOption("invite");
        OptionMapping logsCash = event.getEvent().getOption("logs-cash");
        OptionMapping logsGold = event.getEvent().getOption("logs-gold");

        if (menu == null && info == null && greet == null && invite == null && logsCash == null && logsGold == null) {
            event.reply("Informe ao menos um canal para configurar.").setEphemeral(true).queue();
            return;
        }

        GuildConfig.Channels channels = new GuildConfig.Channels();
        if (menu != null) channels.setMenu(menu.getAsChannel().getId());
        if (info != null) channels.setInfo(info.getAsChannel().getId());
        if (greet != null) channels.setGreet(greet.getAsChannel().getId());
        if (invite != null) channels.setInvite(invite.getAsChannel().getId());
        if (logsCash != null) channels.setLogsCash(logsCash.getAsChannel().getId());
        if (logsGold != null) channels.setLogsGold(logsGold.getAsChannel().getId());

        ServiceRegistry.getGuildConfigService().updateChannels(event.getGuild().getId(), channels);

        event.reply("Canais atualizados.").setEphemeral(true).queue();
    }
}

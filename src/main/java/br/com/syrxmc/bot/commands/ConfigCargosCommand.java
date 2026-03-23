package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class ConfigCargosCommand extends SlashCommand {

    public ConfigCargosCommand() {
        super("config-cargos", "Configura os cargos de staff dos tickets");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.ROLE, "cash", "Cargo notificado nos tickets de cash", false));
        addOption(new OptionData(OptionType.ROLE, "gold", "Cargo notificado nos tickets de gold", false));
        addOption(new OptionData(OptionType.ROLE, "intermedio", "Cargo notificado nos tickets de intermédio", false));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        OptionMapping cash = event.getEvent().getOption("cash");
        OptionMapping gold = event.getEvent().getOption("gold");
        OptionMapping intermedio = event.getEvent().getOption("intermedio");

        if (cash == null && gold == null && intermedio == null) {
            event.reply("Informe ao menos um cargo para configurar.").setEphemeral(true).queue();
            return;
        }

        GuildConfig.Roles roles = new GuildConfig.Roles();
        if (cash != null) roles.setCash(cash.getAsRole().getId());
        if (gold != null) roles.setGold(gold.getAsRole().getId());
        if (intermedio != null) roles.setIntermedio(intermedio.getAsRole().getId());

        ServiceRegistry.getGuildConfigService().updateRoles(event.getGuild().getId(), roles);

        event.reply("Cargos atualizados.").setEphemeral(true).queue();
    }
}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;

@RegisterCommand
public class ConfigCorCommand extends SlashCommand {

    public ConfigCorCommand() {
        super("config-cor", "Define a cor dos embeds do bot (ex: #FF5733)");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.STRING, "cor", "Cor em hexadecimal (ex: #FF5733)", true));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String cor = event.getStringOption("cor").trim();
        try {
            Color.decode(cor);
        } catch (NumberFormatException e) {
            event.reply("Cor inválida. Use o formato hexadecimal, ex: `#FF5733`.").setEphemeral(true).queue();
            return;
        }
        ServiceRegistry.getGuildConfigService().updateColor(event.getGuild().getId(), cor);
        event.reply("Cor atualizada para `" + cor + "`.").setEphemeral(true).queue();
    }
}

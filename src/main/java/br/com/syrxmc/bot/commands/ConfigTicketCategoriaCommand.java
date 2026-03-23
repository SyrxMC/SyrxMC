package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class ConfigTicketCategoriaCommand extends SlashCommand {

    public ConfigTicketCategoriaCommand() {
        super("config-ticket-categoria", "Define a categoria onde os tickets serão criados");
        addPermissions(Permission.ADMINISTRATOR);
        addOption(new OptionData(OptionType.CHANNEL, "categoria", "Categoria dos tickets", true)
                .setChannelTypes(ChannelType.CATEGORY));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String categoryId = event.getEvent().getOption("categoria").getAsChannel().getId();
        ServiceRegistry.getGuildConfigService().updateTicketCategoryId(event.getGuild().getId(), categoryId);
        event.reply("Categoria de tickets atualizada.").setEphemeral(true).queue();
    }
}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

@RegisterCommand
public class CashMenuCommand extends SlashCommand {

    public CashMenuCommand() {
        super("cashmenu", "Create cash menu", true);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {

        event.defer().complete().deleteOriginal().queue();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Cash");
        builder.setDescription("Selecione abaixo a opção desejada");
        builder.setFooter("Cuidado com vendas não autorizadas de terceiros!");
        builder.setColor(Color.decode("#FFC700"));
        builder.setImage("https://amplologistica.com.br/wp-content/uploads/2018/02/ecommerce-subway-studio-malaysia.gif");

        event.getChannel().sendMessageEmbeds(builder.build()).addActionRow(
                Button.secondary("cashMenu",    "QUERO CASHAR!!"),
                Button.secondary("intermedio",  "INTERMÉDIO"),
                Button.secondary("gold",        "QUERO GOLD!")
        ).queue();
    }
}

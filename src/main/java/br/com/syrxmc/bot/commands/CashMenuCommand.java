package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

@RegisterCommand
public class CashMenuCommand extends SlashCommand {

    public CashMenuCommand() {
        super("cashmenu", "Create cash menu", true);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {

        event.defer().setEphemeral(true).complete().deleteOriginal().queue();

        generateMenu(event.getChannel(), true, (__) -> {
            Main.getGoldStock().setLastMenuMessage(__.getId());
            Main.getGoldStockDataManager().save(Main.getGoldStock());
        });

    }

    public static void generateMenu(MessageChannel channel, boolean generateGold, Consumer<Message> consumer) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Cash");
        builder.setDescription("Selecione abaixo a opção desejada");
        builder.setFooter("Cuidado com vendas não autorizadas de terceiros!");
        builder.setColor(PRIMARY_COLOR);
        builder.setImage("https://amplologistica.com.br/wp-content/uploads/2018/02/ecommerce-subway-studio-malaysia.gif");

        List<ItemComponent> actionRow = new ArrayList<>(List.of(
                Button.secondary("cashMenu", "QUERO CASH").withEmoji(Emoji.fromUnicode("\uD83D\uDCB0")),
                Button.secondary("intermedio", "INTERMÉDIO").withEmoji(Emoji.fromUnicode("\uD83E\uDD1D")))
        );

        if (generateGold)
            actionRow.add(Button.secondary("gold", "QUERO GOLD").withEmoji(Emoji.fromUnicode("\uD83E\uDE99")));

        channel.sendMessageEmbeds(builder.build()).addActionRow(actionRow).queue(consumer);
    }


}

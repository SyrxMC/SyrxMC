package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.GoldStock;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterCommand
public class GoldAddCommand extends SlashCommand {

    public GoldAddCommand() {
        super("addgold", "Adicionar o gold");
        addOption(new OptionData(OptionType.STRING, "servidor", "Servidor do gold", true));
        addOption(new OptionData(OptionType.INTEGER, "quantidade", "Quantidade de gold", true));
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String server = event.getStringOption("servidor");
        long quantity = event.getLongOption("servidor");


        GoldStock goldStock = Main.getGoldStock();
        goldStock.addStock(server.toLowerCase(), quantity);

        Main.getGoldStockDataManager().save(goldStock);
        Main.reloadConfig();

        event.reply("Foi adicionado **%s** de gold no bloco do **%s**", server, quantity).queue();

        //Todo: atulizar a mensagem da quantidade de gold
    }

}

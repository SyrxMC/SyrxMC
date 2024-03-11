package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.GoldStock;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static br.com.syrxmc.bot.utils.Utils.convertToShortScale;

@RegisterCommand
public class GoldRemoveCommand extends SlashCommand {

    public GoldRemoveCommand() {
        super("removegold", "Remove o gold");
        addOption(new OptionData(OptionType.STRING, "servidor", "Servidor do gold", true));
        addOption(new OptionData(OptionType.INTEGER, "quantidade", "Quantidade de gold", true));
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {

        String server = event.getStringOption("servidor").toUpperCase();
        long quantity = event.getLongOption("quantidade");

        GoldStock goldStock = Main.getGoldStock();
        goldStock.removeStock(event.getGuild(), server, quantity);

        Main.getGoldStockDataManager().save(goldStock);
        Main.reloadConfig();

        GoldStock updated = Main.getGoldStock();

        event.reply("Foi removido **%s** de gold no bloco do **%s**. Saldo atual: ***%s*** - __**%s**__", convertToShortScale(quantity), server, convertToShortScale(updated.getGoldStock(server)), updated.getGoldStock(server)).setEphemeral(true).queue();
    }

}

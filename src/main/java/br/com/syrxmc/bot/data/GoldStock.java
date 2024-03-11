package br.com.syrxmc.bot.data;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.commands.CashMenuCommand;
import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.core.listeners.gold.GoldStockUpdate;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static br.com.syrxmc.bot.utils.Utils.convertToShortScale;
import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

@Data
public class GoldStock {

    private String lastMenuMessage;

    private String lastGoldStockMessage;

    private HashMap<String, Long> GoldStock = new HashMap<>();

    private static GoldStockUpdate goldStockUpdate = (stock, guild, server, quantity, lastQuantity, newQuantity) -> {

        TextChannel channel = null;

        if (Main.getSyrxCore().getConfig().getMenuChannel() != null) {
            channel = guild.getTextChannelById(Main.getSyrxCore().getConfig().getMenuChannel());
        }

        if (stock.getLastGoldStockMessage() != null) {
            if (channel != null)
                channel.deleteMessageById(stock.getLastGoldStockMessage()).queue();
        }

        if (channel == null) {
            SyrxCore.logger.info("Não foi possível mandar a mensagem de atualização de estoque, porque o menu não foi encontrado.");
            return;
        }

        if (stock.getLastMenuMessage() != null) {
            channel.deleteMessageById(stock.getLastMenuMessage()).queue();
        }

        CashMenuCommand.generateMenu(channel, stock.GoldStock.values().stream().anyMatch(el -> el != 0), message -> {
            stock.setLastMenuMessage(message.getId());
        });

//        channel.sendMessage("@everyone").setAllowedMentions(Set.of(Message.MentionType.EVERYONE))
//                .queue(message -> message.delete().queueAfter(2, TimeUnit.SECONDS));

        channel.sendMessageEmbeds(stock.display()).queue(message -> {
            stock.setLastGoldStockMessage(message.getId());
            Main.getGoldStockDataManager().save(stock);
            Main.reloadConfig();
        });

    };

    public Long getGoldStock(String server) {
        return GoldStock.getOrDefault(server, 0L);
    }

    public void addStock(Guild guild, String server, Long quantity) {

        long lastQuantity = getGoldStock(server);

        GoldStock.put(server, getGoldStock(server) + quantity);

        if (goldStockUpdate != null)
            goldStockUpdate.onUpdate(this, guild, server, quantity, lastQuantity, getGoldStock(server));
    }

    public void removeStock(Guild guild, String server, Long quantity) {

        long lastQuantity = getGoldStock(server);

        GoldStock.put(server, Math.max(0, getGoldStock(server) - quantity));

        if (goldStockUpdate != null)
            goldStockUpdate.onUpdate(this, guild, server, quantity, lastQuantity, getGoldStock(server));
    }


    public MessageEmbed display() {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(PRIMARY_COLOR);
        embedBuilder.setTitle("Gold disponível por bloco");

        getGoldStock().entrySet().stream().sorted(comparingByKey()).forEach((entry) ->
                embedBuilder.addField(entry.getKey(), "Quantidade: **" + convertToShortScale(entry.getValue()) + "**", false)
        );

        return embedBuilder.build();
    }

    public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> comparingByKey() {
        return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c2.getKey().compareTo(c1.getKey());
    }

}

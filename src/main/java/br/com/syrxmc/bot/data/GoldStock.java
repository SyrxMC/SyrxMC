package br.com.syrxmc.bot.data;

import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashMap;

import static br.com.syrxmc.bot.utils.Utils.convertToShortScale;
import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

@Data
public class GoldStock {

    private String lastMenuMessage;

    private String lastGoldStockMessage;

    private HashMap<String, Long> GoldStock = new HashMap<>();

    public Long getGoldStock(String server) {
        return GoldStock.getOrDefault(server, 0L);
    }

    public void addStock(String server, Long quantity) {
        GoldStock.put(server, getGoldStock(server) + quantity);
    }

    public void removeStock(String server, Long quantity) {
        GoldStock.put(server, Math.max(0, getGoldStock(server) - quantity));
    }


    public MessageEmbed display(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(PRIMARY_COLOR);

        getGoldStock().forEach((s, aLong) -> {
                embedBuilder.addField(s, "Quantidade: **" + convertToShortScale(aLong) + "**", false);
        });
        return embedBuilder.build();
    }

}

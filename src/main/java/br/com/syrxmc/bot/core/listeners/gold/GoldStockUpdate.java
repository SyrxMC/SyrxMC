package br.com.syrxmc.bot.core.listeners.gold;

import br.com.syrxmc.bot.data.GoldStock;
import net.dv8tion.jda.api.entities.Guild;

@FunctionalInterface
public interface GoldStockUpdate {
    void onUpdate(GoldStock stock, Guild guild, String server, Long quantity, Long lastQuantity, Long newQuantity);
}

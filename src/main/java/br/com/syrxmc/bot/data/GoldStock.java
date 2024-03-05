package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.HashMap;

@Data
public class GoldStock {

    private HashMap<String, Long> GoldStock = new HashMap<>();

    private Long getGoldStock(String server) {
        return GoldStock.getOrDefault(server, 0L);
    }

    private void addStock(String server, Long quantity) {
        GoldStock.put(server, getGoldStock(server) + quantity);
    }

    private void removeStock(String server, Long quantity) {
        GoldStock.put(server, Math.max(0, getGoldStock(server) - quantity));
    }

}

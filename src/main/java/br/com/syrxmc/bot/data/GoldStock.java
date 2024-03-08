package br.com.syrxmc.bot.data;

import lombok.Data;

import java.util.HashMap;

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

}

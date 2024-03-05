package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.data.GoldStock;
import br.com.syrxmc.bot.utils.DataManager;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


@Getter
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static DataManager<Cash> cashManager;

    @Getter
    private static Cash cash;

    @Getter
    private static DataManager<GoldStock> goldStockDataManager;

    @Getter
    private static GoldStock goldStock;

    @Getter
    private static SyrxCore syrxCore;

    public static void main(String[] args) throws IOException {

        logger.info("Iniciando o bot...");

        DataManager<Config> configDataManager = new DataManager<>("config.json", Config::new).create();

        cashManager = new DataManager<>("cashTickets.json", Cash::new).create();
        cash = cashManager.get();

        goldStockDataManager = new DataManager<>("goldStock.json", GoldStock::new).create();
        goldStock = goldStockDataManager.get();

        syrxCore = new SyrxCore(configDataManager.get());

        syrxCore.initialize();
    }

    @SneakyThrows
    public static void reloadConfig() {
        cashManager = new DataManager<>("cashTickets.json", Cash::new).create();
    }

}
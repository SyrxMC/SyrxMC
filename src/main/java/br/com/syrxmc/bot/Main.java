package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.utils.DataManager;
import lombok.Getter;
import lombok.SneakyThrows;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


@Getter
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static SyrxCore syrxCore;

    public static void main(String[] args) throws IOException, SchedulerException {

        logger.info("Iniciando o bot...");

        DataManager<Config> configDataManager = new DataManager<>("config.json", Config::new).create();

        configs();

        syrxCore = new SyrxCore(configDataManager.get());

        syrxCore.initialize();

    }

    @SneakyThrows
    public static void reloadConfig() {
        configs();
    }

    public static void configs() throws IOException {
    }

}

package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.utils.ConfigLoader;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static SyrxCore syrxCore;

    public static void main(String[] args) {
        logger.info("Iniciando o bot...");
        syrxCore = new SyrxCore(new ConfigLoader<Config>().loadFile("config.json", Config.class, Config::new));

        syrxCore.inicialize();
    }
}
package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static SyrxCore syrxCore;

    public static void main(String[] args) {
        logger.info("Iniciando o bot...");
        syrxCore = new SyrxCore();

        syrxCore.inicialize();
    }
}
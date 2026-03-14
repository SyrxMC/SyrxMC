package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener extends DynamicHandler<GuildReadyEvent> {

    private final static Logger logger = LoggerFactory.getLogger(ReadyListener.class);

    public ReadyListener() {
        super(event -> true);
    }

    @Override
    public void onEvent(GuildReadyEvent event) {
       logger.info("Bot iniciado com sucesso!");
       logger.info("Ininicado como {}", event.getJDA().getSelfUser().getName());
    }
}

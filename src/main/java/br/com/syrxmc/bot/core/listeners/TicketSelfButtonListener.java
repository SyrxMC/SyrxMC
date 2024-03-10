package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Objects;

public class TicketSelfButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    private final Config config;

    public TicketSelfButtonListener(Config config) {
        super(event -> Objects.equals(event.getButton().getId(), "closeSelf"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        event.reply("Hello").queue();
    }
}

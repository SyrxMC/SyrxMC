package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CashTicketButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    public CashTicketButtonListener() {
        super(event -> event.getButton().getCustomId() != null &&
                (event.getButton().getCustomId().startsWith("cash:pixcopy:") ||
                 event.getButton().getCustomId().startsWith("cash:login:")));
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        String id = event.getButton().getCustomId();
        if (id == null) return;

        if (id.startsWith("cash:pixcopy:")) {
            String value = id.substring("cash:pixcopy:".length());
            event.reply(value).setEphemeral(true).queue();
        } else if (id.startsWith("cash:login:")) {
            String login = id.substring("cash:login:".length());
            event.reply(login).setEphemeral(true).queue();
        }
    }
}

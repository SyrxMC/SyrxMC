package br.com.syrxmc.bot.core.listeners.events.handlers;

import br.com.syrxmc.bot.core.listeners.events.IDynamicHandler;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
public class SelectRadioHandler extends DynamicHandler<StringSelectInteractionEvent> {

    private final IDynamicHandler<StringSelectInteractionEvent> handler;

    public SelectRadioHandler(StringSelectMenu selectMenu, IDynamicHandler<StringSelectInteractionEvent> handler) {
        super(event -> Objects.equals(event.getSelectMenu().getId(), selectMenu.getId()), false);
        this.handler = handler;
    }

    @Override
    public void onEvent(StringSelectInteractionEvent event) {
        handler.onEvent(event);
    }
}

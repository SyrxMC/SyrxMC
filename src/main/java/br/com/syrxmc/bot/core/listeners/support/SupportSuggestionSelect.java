package br.com.syrxmc.bot.core.listeners.support;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class SupportSuggestionSelect extends DynamicHandler<StringSelectInteractionEvent> {


    public SupportSuggestionSelect() {
        super(event -> event.getComponentId().equals("suggestion_options"));
    }

    @Override
    public void onEvent(StringSelectInteractionEvent event) {
        String type = event.getSelectedOptions().get(0).getValue();
        String title = "Faça sua sugestão de " + type;
        String label = "Faça sua sugestão";

        generateModal(event, label, type, title);
    }

    private static <T extends GenericComponentInteractionCreateEvent> void generateModal(T event, String label, String type, String title) {

        Modal.Builder modal = Modal.create("suggestion:" + type, title);
        TextInput text = TextInput.create("text", label, TextInputStyle.PARAGRAPH).build();


        if(type.equals("mod")){
            TextInput mod = TextInput.create("mod", "Link do mod", TextInputStyle.SHORT).build();
            modal.addActionRow(mod);
        }

        modal.addActionRow(text);

        event.replyModal(modal.build()).queue();
    }
}

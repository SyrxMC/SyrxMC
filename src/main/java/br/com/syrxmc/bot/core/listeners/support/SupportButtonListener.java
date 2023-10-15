package br.com.syrxmc.bot.core.listeners.support;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class SupportButtonListener extends DynamicHandler<ButtonInteractionEvent> {


    public SupportButtonListener() {
        super(event -> event.getButton().getId().contains("support:"));
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        String type = event.getButton().getId().split(":")[1];

        if(type.equals("suggestion")){
            StringSelectMenu choice = StringSelectMenu.create("suggestion_options")
                    .setPlaceholder("Tipo de sugestão")
                    .setRequiredRange(1, 1)
                    .addOption("Mod", "mod", Emoji.fromUnicode("\uD83D\uDEE0\uFE0F"))
                    .addOption("Evento", "evento", Emoji.fromUnicode("U+1F3B2"))
                    .addOption("Outros", "outros", Emoji.fromUnicode("\uD83D\uDCA1"))
                    .build();

            event.replyComponents(ActionRow.of(choice)).setEphemeral(true).queue();
            return;
        }

        String title = "Sistema de Ticket";
        String label = "Descreva sua dúvida";


        TextInput nickname = TextInput.create("nikcname", "nickname", TextInputStyle.SHORT).build();


        TextInput text = TextInput.create("text", label, TextInputStyle.PARAGRAPH).build();


        Modal.Builder modal = Modal.create("ticket", title)
                .addComponents(ActionRow.of(nickname), ActionRow.of(text));

        event.replyModal(modal.build()).queue();
    }
}

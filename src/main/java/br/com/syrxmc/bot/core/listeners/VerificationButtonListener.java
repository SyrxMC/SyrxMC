package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.Objects;
import java.util.UUID;

public class VerificationButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    public VerificationButtonListener() {
        super(event -> Objects.equals(event.getButton().getId(), "verification"));
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        TextInput confirmCode = TextInput.create("code", "Código", TextInputStyle.SHORT)
                .setPlaceholder("Digite o código de verificação acima").setMaxLength(9).build();


        Modal.Builder verification = Modal.create("verification:" + code, String.format("Código de verificação [%s]", code))
                .addComponents(ActionRow.of(confirmCode));
        event.replyModal(verification.build()).queue();
    }
}

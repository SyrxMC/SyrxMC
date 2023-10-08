package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class VerificationModalListener extends DynamicHandler<ModalInteractionEvent> {

    public VerificationModalListener() {
        super(event -> event.getModalId().contains("verification"));
    }

    @Override
    public void onEvent(ModalInteractionEvent event) {
        String code = event.getModalId().split(":")[1];
        String confirmCode = event.getValue("code").getAsString();

        if(code.equalsIgnoreCase(confirmCode)){
            event.getGuild().addRoleToMember(event.getMember().getUser(), event.getGuild().getRoleById("1160294617799999570")).queue();
            event.reply("<a:checked:1160676561343762553> Verificação feita com sucesso")
                    .setEphemeral(true).queue();
        } else {
            event.reply("<a:x:1160677444030828624> Verificação falhou. Tente novamente")
                    .setEphemeral(true).queue();
        }

    }
}

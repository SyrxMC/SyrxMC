package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;

public class ButtonsEventListener extends DynamicHandler<ButtonInteractionEvent> {

    public ButtonsEventListener() {
        super(guildMemberJoinEvent -> true);
    }

    private List<String> rolesId = List.of("1361318367243538623", "1361319139356180692", "1361319329563410512");

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        try {
            Role roleById = event.getGuild().getRoleById(event.getComponentId());

            if (!event.getMember().getRoles().contains(roleById)) {
                event.getGuild().addRoleToMember(event.getMember(), roleById).queue();
                event.reply("{role} has been add".replace("{role}", roleById.getName())).setEphemeral(true).queue();
            } else {
                event.getGuild().removeRoleFromMember(event.getMember(), roleById).queue();
                event.reply("{role} has been removed".replace("{role}", roleById.getName())).setEphemeral(true).queue();
            }

            rolesId.stream().filter(s -> !s.equals(roleById.getId()))
                    .forEach(s -> {
                        Role toRemove = event.getGuild().getRoleById(s);
                        event.getGuild().removeRoleFromMember(event.getMember(), toRemove).queue();
                    });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

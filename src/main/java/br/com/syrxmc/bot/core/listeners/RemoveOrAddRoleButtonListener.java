package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.Optional;

public class RemoveOrAddRoleButtonListener extends DynamicHandler<ButtonInteractionEvent> {

   private List<String> types = List.of("gold", "server", "normal");

    public RemoveOrAddRoleButtonListener() {
        super(ev -> true);
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        System.out.println(event.getButton().getId());
        if(!types.contains(event.getButton().getId())){
            return;
        }

        String type = event.getButton().getId();

        String roleId = null;

        if(type.equals("server")){
            roleId = Main.getSyrxCore().getConfig().getServerRole();
        }
        if(type.equals("gold")){
            roleId = Main.getSyrxCore().getConfig().getGoldRole();
        }
        if(type.equals("normal")){
            roleId = Main.getSyrxCore().getConfig().getAnnouncementRole();
        }

        String finalRoleId = roleId;
        Optional<Role> role = event.getMember().getRoles().stream().filter(r -> r.getId().equals(finalRoleId)).findFirst();

        if(role.isPresent()){
            event.getGuild().removeRoleFromMember(event.getMember(), role.get()).queue();
            event.reply("Cargo removido com sucesso!").setEphemeral(true).queue();
        } else {
            event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleId)).queue();
            event.reply("Cargo adicionado com sucesso!").setEphemeral(true).queue();
        }
    }
}

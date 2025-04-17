package br.com.syrxmc.bot.core.scheduler;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.data.Clients;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientExpirationScheduler implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Main.reloadConfig();
        Clients clients = Main.getClientsData().get();

        if(Main.getClients().getClients() == null || Main.getClients().getClients().isEmpty()) {
            return;
        }

        List<String> toRemove = new ArrayList<>();

        clients.getClients().forEach((s, instant) -> {
            if(instant.isBefore(LocalDateTime.now())){
                SyrxCore core = Main.getSyrxCore();
                Guild guild = core.getGuildById(core.getConfig().getGuildId());
                Role roleById = guild.getRoleById(core.getConfig().getClientRoleId());
                Member memberById = guild.getMemberById(s);

                if(memberById != null){
                    guild.removeRoleFromMember(memberById, roleById).queue();
                    memberById.modifyNickname(null).queue();
                    toRemove.add(s);
                }
            }
        });

        toRemove.forEach((s) -> clients.getClients().remove(s));
        Main.getClientsData().save(clients);
        Main.reloadConfig();
    }
}

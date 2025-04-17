package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Clients;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

@RegisterCommand
public class AddRoleCommand extends SlashCommand {

    public AddRoleCommand() {
        super("addrole", "Adicionar cargo ao novo cliente");
        addOption(new OptionData(OptionType.MENTIONABLE, "cliente", "Cliente que comprou a chave", true));
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        Main.reloadConfig();
        Clients clients = Main.getClientsData().get();
        Role role = event.getGuild().getRoleById(event.getCore().getConfig().getClientRoleId());
        Member client = event.getMemberOption("cliente");
        List<Role> clientRole = client.getRoles().stream().filter((__) ->  __.getId().equals(event.getCore().getConfig().getClientRoleId()))
                .toList();

        if(clientRole.isEmpty()) {
            event.getGuild().addRoleToMember(client, role).queue();
            client.modifyNickname("Cliente").queue();
        }

        clients.addClient(client.getId());
        Main.getClientsData().save(clients);
        Main.reloadConfig();

        event.reply("Cargo de cliente adicionado com sucesso").setEphemeral(true).queue();
    }
}

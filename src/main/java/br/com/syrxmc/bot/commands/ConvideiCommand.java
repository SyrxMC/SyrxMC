package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.ServiceRegistry;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.domain.invite.InviteService;

@RegisterCommand
public class ConvideiCommand extends SlashCommand {

    public ConvideiCommand() {
        super("convidei", "Mostra quantas pessoas você convidou");
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        String guildId = event.getGuild().getId();
        String userId = event.getAuthor().getId();

        InviteService inviteService = ServiceRegistry.getInviteService();
        int count = inviteService.getUserInviteCount(guildId, userId);

        if (count > 0) {
            event.reply("Você convidou " + count + " pessoa(s).").setEphemeral(true).queue();
        } else {
            event.reply("Você não convidou ninguém ainda.").setEphemeral(true).queue();
        }
    }
}

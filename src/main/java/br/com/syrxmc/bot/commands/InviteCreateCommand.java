package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Invites;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@RegisterCommand
public class InviteCreateCommand extends SlashCommand {

    public InviteCreateCommand() {
        super("convidar", "Convide amigos e participe do evento");
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        Invites invite = Main.getInvites();

        String codeByUserId = invite.getCodeByUserId(event.getAuthor().getId());

        event.reply("O link de convite foi gerado e enviado no seu privado").setEphemeral(true).queue();

        if(codeByUserId != null){
            event.getAuthor().openPrivateChannel().complete()
                    .sendMessage("***Você já tem um convite gerado para o evento, use o link:*** `https://discord.gg/".concat(codeByUserId).concat("`\n")
                            .concat("***Use esse link para convidar pessoas para dentro do servidor para poder participar do evento.***")).queue();
            return;
        }


        Invite complete = event.getGuild().getChannelById(TextChannel.class, "1170028603153600681").createInvite()
                .setMaxAge(0).complete();


        invite.createInvite(event.getAuthor().getId(), complete.getCode());
        Main.getInvitesDataManager().save(invite);
        Main.getInvitesDataManager().reload();

        event.getAuthor().openPrivateChannel().complete().sendMessage("***Convite gerado:*** `" + complete.getUrl() + "`\n" +
                "***Use esse link para convidar pessoas para dentro do servidor para poder participar do evento.***").queue();
    }
}

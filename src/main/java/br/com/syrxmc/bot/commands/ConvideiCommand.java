package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Invites;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RegisterCommand
public class ConvideiCommand extends SlashCommand {

    public ConvideiCommand() {
        super("convidei", "Mostra quantas pessoas você convidou");
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        List<String> codeByUserId = Main.getInvites().getCodeByUserId(event.getAuthor().getId());

        AtomicReference<Long> value = new AtomicReference<>(0L);

        if (codeByUserId != null) {
            codeByUserId.forEach((code) -> {
                Invites.InviteData invite = Main.getInvites().getInvite(code);

                value.set(value.get() + invite.getCount());
            });

            if (value.get() > 0L) {
                event.reply("Você convidou " + value.get() + " pessoas.").setEphemeral(true).queue();

            } else {
                event.reply("Você não convidou ninguém ainda.").setEphemeral(true).queue();
            }
        } else {
            event.reply("Você não convidou ninguém ainda.").setEphemeral(true).queue();
        }
    }
}

package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

@RegisterCommand
public class VerificationMenuCommand extends SlashCommand {

    public VerificationMenuCommand() {
        super("verification", "Create verification menu", true);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {

        event.defer().complete().deleteOriginal().queue();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Verificação");
        builder.setDescription("Olá, desculpa por todo esse transtorno, agora que você já leu nossas regras você poderá se verificar no servidor, basta clicar no botão abaixo e você receberá o cargo e poderá disfrutar de todo o nosso servidor, qualquer dúvida ou problema basta contactar a STAFF no canal de SUPORTE.");
        builder.setFooter("SyrxMC - Sempre trazendo o melhor para a sua diversão!");
        builder.setColor(Color.decode("#282b30"));
        builder.setImage("https://cdn.discordapp.com/attachments/338713959433502732/1160364493705064478/factcheckers_thumbnail_final.gif?ex=653464a5&is=6521efa5&hm=ba0162f2dedf9b1d09fb861b19ad47f2f149203ce8c0ea9e6623db1c6081bc80&");
        event.getChannel().sendMessageEmbeds(builder.build())
                .addActionRow(Button.success("verification", "Verificação")).queue();
    }
}

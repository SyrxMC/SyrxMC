package br.com.syrxmc.bot.commands;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.annotations.RegisterCommand;
import br.com.syrxmc.bot.data.Invites;
import br.com.syrxmc.bot.utils.ConvertNumbersEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static br.com.syrxmc.bot.utils.LeadboardScheduler.ignoredIds;

@RegisterCommand
public class LeadBoardCommand extends SlashCommand {

    public LeadBoardCommand() {
        super("leadboard", "Cria o leadboard");
        this.addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(SlashCommandEvent event) throws Exception {
        event.ignoreReplyWait();

        Invites invites = Main.getInvites();
        Main.reloadConfig();
        List<Invites.InviteData> data = new ArrayList<>();


        Main.getInvites().getInvites().forEach((key, value) -> data.add(value));

        Map<String, Long> userTotalValues = new HashMap<>();

        for (Invites.InviteData datum : data) {
            String userId = datum.getUserId();
            long totalValue = datum.getCount();
            userTotalValues.put(userId, userTotalValues.getOrDefault(userId, 0L) + totalValue);
        }

        StringBuilder stringBuilder = new StringBuilder();
        EmbedBuilder builder = new EmbedBuilder();
        AtomicInteger i = new AtomicInteger(0);
        stringBuilder.append("Segue abaixo as TOP 5 pessoas que mais convidaram nesse evento.\n\n");

        userTotalValues.entrySet().stream()
                .filter(entry -> !ignoredIds.contains(entry.getKey()))
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .forEach(stringLongEntry ->
                        stringBuilder.append(ConvertNumbersEnum.values()[i.getAndIncrement()].getDescription()).append("<@")
                                .append(stringLongEntry.getKey()).append("> `").append(stringLongEntry.getValue())
                                .append("` ***convidado(s)***").append(i.get() - 1 == 0? " \uD83D\uDC51" : "" ).append("\n\n"));

        builder.setDescription(stringBuilder);
        builder.setTitle("Ranking de convites");
        builder.setFooter("Caso você não esteja no ranking e queira saber quantas pessoas você convidou, utilize o comando /convidei, na sala #comandos");
        builder.setColor(Color.decode("#D4AF37"));
        builder.setThumbnail("https://cdn.discordapp.com/icons/1240266588352024607/a_f37eed73b2585af0d1d2cc14a9446060.gif?size=2048");
        event.getChannel().sendMessageEmbeds(builder.build()).queue(message -> invites.setLastMessageId(message.getId()));

    }
}

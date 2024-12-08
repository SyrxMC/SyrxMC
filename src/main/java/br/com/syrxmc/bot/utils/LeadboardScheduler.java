package br.com.syrxmc.bot.utils;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.data.Invites;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LeadboardScheduler implements Job {

    public static List<String> ignoredIds = List.of("184093192243642368", "464980441586466826", "398859614596366336", "248841176038375424");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Main.reloadConfig();
        Invites invites = Main.getInvites();
        if (!invites.isActive()) return;

        List<Invites.InviteData> data = new ArrayList<>();
        Config config = Main.getSyrxCore().getConfig();


        invites.getInvites().forEach((key, value) -> data.add(value));

        Map<String, Long> userTotalValues = new HashMap<>();

        for (Invites.InviteData datum : data) {
            String userId = datum.getUserId();
            long totalValue = datum.getCount();
            userTotalValues.put(userId, userTotalValues.getOrDefault(userId, 0L) + totalValue);
        }

        StringBuilder stringBuilder = new StringBuilder();
        EmbedBuilder builder = new EmbedBuilder();
        AtomicInteger i = new AtomicInteger(0);


        stringBuilder.append("Segue abaixo as ***TOP 5*** pessoas que mais convidaram nesse evento.\n\n");

        userTotalValues.entrySet().stream()
                .filter(entry -> !ignoredIds.contains(entry.getKey()))
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(5)
                .forEach(stringLongEntry ->
                        stringBuilder.append("***").append(ConvertNumbersEnum.values()[i.getAndIncrement()].getDescription()).append("º*** \n").append("<@")
                                .append(stringLongEntry.getKey()).append("> `").append(stringLongEntry.getValue())
                                .append("` ***convidado(s)***").append(i.get() - 1 == 0 ? "\uD83D\uDC51" : "").append("\n\n"));

        builder.setDescription(stringBuilder);
        builder.setTitle("Ranking de convites");
        builder.setThumbnail("https://cdn.discordapp.com/icons/1240266588352024607/a_f37eed73b2585af0d1d2cc14a9446060.gif?size=2048");
        builder.setColor(Color.decode("#D4AF37"));
        builder.setFooter("O ranking atualiza a cada 5 minutos.\n" +
                "Caso você não esteja no ranking e queira saber quantas pessoas você convidou, utilize o comando /convidei, na sala #comandos \n");
        if (invites.getLastMessageId() != null) {
            Main.getSyrxCore().getChannelById(TextChannel.class, config.getInviteChannel()).editMessageEmbedsById(invites.getLastMessageId(), builder.build()).queue();
        } else {
            Main.getSyrxCore().getChannelById(TextChannel.class, config.getInviteChannel()).sendMessageEmbeds(builder.build()).queue(message -> {
                invites.setLastMessageId(message.getId());
                Main.getInvitesDataManager().save(invites);
                Main.reloadConfig();
            });
        }
    }
}

package br.com.syrxmc.bot.utils;

import br.com.syrxmc.bot.domain.guild.GuildConfig;
import br.com.syrxmc.bot.domain.gold.GoldStock;
import br.com.syrxmc.bot.domain.invite.InviteData;
import br.com.syrxmc.bot.domain.ticket.TicketType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SyrxEmbeds {

    private SyrxEmbeds() {}

    public static MessageEmbed ticketWelcome(TicketType type, String userMention, GuildConfig config) {
        EmbedBuilder builder = new EmbedBuilder();
        Color color = parseColor(config != null ? config.getColor() : null);
        builder.setColor(color);

        switch (type) {
            case CASH -> {
                builder.setTitle("Compra de CASH");
                builder.setImage("https://amplologistica.com.br/wp-content/uploads/2018/02/ecommerce-subway-studio-malaysia.gif");
            }
            case GOLD -> {
                builder.setTitle("Compra de GOLD");
                builder.setImage("https://usagif.com/wp-content/uploads/gifs/raining-money-12.gif");
            }
            case INTERMEDIO -> {
                builder.setTitle("Intermédio");
                builder.setImage("https://cdn.discordapp.com/attachments/1169072762002874399/1214925277696888853/imagem_plugin_143833bc-6e72-49e6-a7d1-625ac19c74bc.gif?ex=65fae256&is=65e86d56&hm=9d899f9043e79db963278cb97180e131f1adbaa74232fc4f4d9dbc594e1ca615&");
            }
        }

        builder.setFooter("Clique para expandir a imagem.");
        return builder.build();
    }

    public static String ticketWelcomeMessage(TicketType type, String userMention, GuildConfig config) {
        String staffRoleId = null;
        if (config != null && config.getRoles() != null) {
            staffRoleId = switch (type) {
                case CASH -> config.getRoles().getCash();
                case GOLD -> config.getRoles().getGold();
                case INTERMEDIO -> config.getRoles().getIntermedio();
            };
        }
        String staffMention = staffRoleId != null ? "<@&" + staffRoleId + ">" : "staff";

        String infoChannelId = config != null && config.getChannels() != null ? config.getChannels().getInfo() : null;
        String channelMention = infoChannelId != null ? "<#" + infoChannelId + ">" : "informações";

        return switch (type) {
            case CASH -> "Olá " + userMention + ", você abriu um **pedido de Compra de CASH**, os " + staffMention
                    + " de Cash irão lhe responder em breve, aguarde na sala, caso tenha alguma dúvida sobre o evento acesse a sala "
                    + channelMention + " para visualizar o que vem em cada pacote.";
            case GOLD -> "Olá " + userMention + " , você abriu um pedido de **Compra de GOLD**, os " + staffMention
                    + " de Cash irão lhe responder em breve, aguarde na sala, informe no chat a **quantidade** que você deseja e o **servidor**.";
            case INTERMEDIO -> "Olá " + userMention + ", você abriu um pedido de **INTERMÉDIO**, os " + staffMention
                    + " de Cash irão lhe responder em breve, aguarde na sala, você pode já ir informando o que será realizado no intermédio.";
        };
    }

    public static MessageEmbed ticketClosed(String closedBy, String soldTo, Double saleValue) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#FF004D"));
        builder.setTitle("Ticket Encerrado");

        StringBuilder desc = new StringBuilder();
        desc.append("Ticket fechado por <@").append(closedBy).append(">.");
        desc.append("\nVendido para: <@").append(soldTo).append(">");
        if (saleValue != null && saleValue > 0) {
            desc.append("\nValor da venda: **").append(saleValue).append("**");
        }
        builder.setDescription(desc.toString());
        return builder.build();
    }

    public static MessageEmbed goldStockDisplay(List<GoldStock> stocks, String color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(parseColor(color));
        builder.setTitle("Gold disponível por bloco");

        stocks.stream()
                .sorted((a, b) -> b.getServerName().compareTo(a.getServerName()))
                .forEach(gs -> builder.addField(
                        gs.getServerName(),
                        "Quantidade: **" + Utils.convertToShortScale(gs.getAmount()) + "**",
                        false
                ));

        return builder.build();
    }

    public static MessageEmbed goldStockEmpty(String color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(parseColor(color));
        builder.setTitle("Gold disponível por bloco");
        builder.setDescription("Sem estoque disponível no momento.");
        return builder.build();
    }

    public static MessageEmbed leaderboard(List<InviteData> top, String color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(parseColor(color));
        builder.setTitle("Ranking de convites");
        builder.setThumbnail("https://cdn.discordapp.com/icons/1478199498642685962/4e00c498796e0894efd44a945c2ec561.png");
        builder.setFooter("O ranking atualiza a cada 5 minutos.\nCaso você não esteja no ranking e queira saber quantas pessoas você convidou, utilize o comando /convidei, na sala #comandos");

        StringBuilder sb = new StringBuilder();
        sb.append("Segue abaixo as ***TOP 5*** pessoas que mais convidaram nesse evento.\n\n");

        AtomicInteger i = new AtomicInteger(0);
        top.forEach(entry -> {
            int idx = i.getAndIncrement();
            if (idx < ConvertNumbersEnum.values().length) {
                sb.append("***").append(ConvertNumbersEnum.values()[idx].getDescription()).append("º*** \n")
                        .append("<@").append(entry.getInviterUserId()).append("> `")
                        .append(entry.getCount()).append("` ***convidado(s)***")
                        .append(idx == 0 ? " \uD83D\uDC51" : "")
                        .append("\n\n");
            }
        });

        builder.setDescription(sb.toString());
        return builder.build();
    }

    public static MessageEmbed error(String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setTitle("Erro");
        builder.setDescription(message);
        return builder.build();
    }

    public static MessageEmbed success(String message, String color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(parseColor(color));
        builder.setTitle("Sucesso");
        builder.setDescription(message);
        return builder.build();
    }

    private static Color parseColor(String colorHex) {
        if (colorHex != null && !colorHex.isBlank()) {
            try {
                return Color.decode(colorHex);
            } catch (NumberFormatException e) {
                // fall through to default
            }
        }
        return Color.decode("#FF004D");
    }
}

package br.com.syrxmc.bot.utils;

import br.com.syrxmc.bot.Main;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class Announcement {

    private final String type;

    private final String authorId;

    private final String channelId;

    private final EventWaiter eventWaiter;

    public void startInteraction() {
        String messageId = null;
        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Envie a mensagem que será anunciada:")
                .complete().delete().queueAfter(60, TimeUnit.SECONDS);
        eventWaiter.waitForEvent(MessageReceivedEvent.class,
                messageReceivedEvent ->
                        messageReceivedEvent.getAuthor().getId().equals(authorId),
                event -> {
                    if (event.getMessage().getContentStripped().equalsIgnoreCase("cancelar")) {
                        event.getChannel().sendMessage("Cancelado!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
                        return;
                    }
                    String message = event.getMessage().getContentDisplay();
                    askIfHaveAttachments(message);

                }, 10, TimeUnit.SECONDS, () -> {
                    Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Tempo de espera excedido!")
                            .complete().delete().queueAfter(10, TimeUnit.SECONDS);
                });
    }

    private void askIfHaveAttachments(String message) {
        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("O anuncio vai ter imagens? (sim/não)")
                .complete().delete().queueAfter(10, TimeUnit.SECONDS);
        eventWaiter.waitForEvent(MessageReceivedEvent.class,
                messageReceivedEvent ->
                        messageReceivedEvent.getAuthor().getId().equals(authorId),
                event -> {
                    if (event.getMessage().getContentStripped().equalsIgnoreCase("sim") || event.getMessage().getContentStripped().equalsIgnoreCase("s")) {
                        receiveAttachments(message);
                    } else if (event.getMessage().getContentStripped().equalsIgnoreCase("nao") ||
                            event.getMessage().getContentStripped().equalsIgnoreCase("não") ||
                            event.getMessage().getContentStripped().equalsIgnoreCase("n")) {
                        showMessagePreview(message, new ArrayList<>());
                    }
                }
        );
    }

    private void receiveAttachments(String message) {
        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Mande os arquivos:").queue();
        List<FileUpload> files = new ArrayList<>();
        eventWaiter.waitForEvent(MessageReceivedEvent.class,
                messageReceivedEvent ->
                        messageReceivedEvent.getAuthor().getId().equals(authorId),
                event -> {
                    event.getMessage().getAttachments().forEach(attachment -> {
                        files.add(attachment.getProxy().downloadAsFileUpload(attachment.getFileName()));
                    });
                    event.getMessage().delete().queueAfter(2, TimeUnit.MINUTES);
                    event.getChannel().sendMessage("Arquivos registrados").complete().delete().queueAfter(10, TimeUnit.SECONDS);

                    showMessagePreview(message, files);
                }, 1, TimeUnit.MINUTES, () -> {
                    Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Tempo de espera excedido!")
                            .complete().delete().queueAfter(10, TimeUnit.SECONDS);
                }
        );
    }

    private void showMessagePreview(String message, List<FileUpload> files) {
        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("***Preview:***").queue();
        MessageCreateAction messageCreateAction = Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage(message);
        if (!files.isEmpty()) {
            messageCreateAction.addFiles(files).queue();
        } else {
            messageCreateAction.queue();
        }

        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Mande ***SIM*** para confirmar ou ***NÃO*** para cancelar").queue();
        waitConfirm(message, files);
    }

    private void waitConfirm(String message, List<FileUpload> files) {
        StringBuilder builder = new StringBuilder();
        Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Confirma envio? (sim/não)")
                .complete().delete().queueAfter(10, TimeUnit.SECONDS);
        eventWaiter.waitForEvent(MessageReceivedEvent.class,
                messageReceivedEvent ->
                        messageReceivedEvent.getAuthor().getId().equals(authorId),
                event -> {
                    if (event.getMessage().getContentStripped().equalsIgnoreCase("sim") || event.getMessage().getContentStripped().equalsIgnoreCase("s")) {

                        if(type.equals("server")){
                            builder.append("<@&").append(Main.getSyrxCore().getConfig().getServerRole()).append("> ");
                        }
                        if(type.equals("gold")){
                            builder.append("<@&").append(Main.getSyrxCore().getConfig().getGoldRole()).append("> ");
                        }
                        if(type.equals("normal")){
                            builder.append("<@&").append(Main.getSyrxCore().getConfig().getAnnouncementRole()).append("> ");
                        }

                        builder.append("\n").append(message).append("\n\n")
                                .append("Para não receber mais anúncios ou receber use o botão`").append("\n\n");

                        MessageCreateAction messageCreateAction = Main.getSyrxCore().getChannelById(TextChannel.class, Main.getSyrxCore().getConfig().getAnnouncementChannel()).sendMessage(builder);
                        if (!files.isEmpty()) {
                            messageCreateAction.addFiles(files);
                        }

                        Button button = Button.of(ButtonStyle.PRIMARY, type, null, Emoji.fromUnicode("✅"));
                        messageCreateAction.addActionRow(button).queue();
                    }
                    else {
                        event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                        event.getChannel().sendMessage("Anuncio cancelado!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
                    }
                },
                1, TimeUnit.MINUTES, () -> {
                    Main.getSyrxCore().getChannelById(TextChannel.class, channelId).sendMessage("Tempo de espera excedido!")
                            .complete().delete().queueAfter(10, TimeUnit.SECONDS);
                }
        );

    }
}

package br.com.syrxmc.bot.utils;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
public class WriteChannelBackup {

    private static final String fileSeparator = FileSystems.getDefault().getSeparator();

    private final static Logger logger = LoggerFactory.getLogger(WriteChannelBackup.class);

    public static void writeFile(TextChannel channel, String path) {


        try {
            List<Message> messageStream = channel.getHistory().getRetrievedHistory().stream().filter(message -> !message.getAuthor().isBot()).toList();
            if (messageStream.isEmpty()) {
                return;
            }

            String channelPath = "files" + fileSeparator + String.format(path + fileSeparator + "%1$s", channel.getName() + "-" + instantToString(channel.getTimeCreated().toInstant())) + fileSeparator;

            File f = new File(channelPath);

            if (!f.exists())
                if (f.mkdirs())
                    logger.info("File created...");

            BufferedWriter writer = new BufferedWriter(new FileWriter(String.format(channelPath + "%1$s.txt", channel.getName() + "-" + instantToString(channel.getTimeCreated().toInstant()))));

            channel.getIterableHistory().forEachAsync(message -> {

                if (message.getAuthor().isBot())
                    return false;

                try {

                    writer.write(String.format("[%1$s] - %2$s : %3$s",
                            getFormat(message),
                            getName(message),
                            new String(getMessages(message, channelPath).getBytes(StandardCharsets.UTF_8)))
                    );

                    writer.newLine();

                } catch (IOException e) {
                    logger.error("Falha: ", e);
                }

                return true;

            }).thenRun(() -> {

                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static String getName(Message message) {

        return new String(
                message.getMember() == null ?
                        message.getAuthor().getName().getBytes(StandardCharsets.UTF_8) :
                        message.getMember().getEffectiveName().getBytes(StandardCharsets.UTF_8)
        );

    }

    @NotNull
    private static String getFormat(Message message) {
        return message.getTimeCreated()
                .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @NotNull
    private static String getMessages(Message message, String path) {

        StringBuilder builder = new StringBuilder();

        if (!message.getAttachments().isEmpty()) {
            for (Message.Attachment attachment : message.getAttachments()) {
                builder.append(attachment.getUrl()).append("\n");
                downloadFiles(message, path, attachment);
            }
        } else {
            builder.append(message.getContentDisplay());
        }

        return builder.toString();

    }

    private static void downloadFiles(Message message, String path, Message.Attachment attachment) {

        attachment
                .getProxy()
                .downloadToFile(new File(path + fileSeparator + (System.currentTimeMillis() + 10) + "_" + attachment.getFileName()))
                .thenAcceptAsync(c -> logger.info("O attachment do canal {} foi salvo com sucesso!", message.getChannel().getName()))
                .exceptionally(throwable -> {
                    logger.error("Erro ao baixar attachment do canal {}", message.getChannel().getName(), throwable);
                    return null;
                });

    }

    private static String instantToString(Instant instant) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("America/Sao_Paulo"));

        return formatter.format(localDateTime);

    }

}
package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.ALLOWED_PERMISSIONS;
import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.DENIED_PERMISSIONS;
import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

public class CashButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    private final Config config;

    public CashButtonListener(Config config) {
        super(event -> Objects.equals(event.getButton().getId(), "cashMenu"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {

        Cash cash = Main.getCashManager().get();

        if (!cash.getTickets().isEmpty()) {

            if (cash.getTickets().get(event.getMember().getId()) != null) {

                Optional<Cash.Ticket> ticket = cash.getTickets().get(event.getMember().getId()).stream().filter(ticket1 ->
                        Cash.TicketType.CASH.equals(ticket1.type())
                ).findFirst();

                if (ticket.isPresent()) {
                    event.reply("You already have an open cash room!").setEphemeral(true).queue();
                    return;
                }

            }

        }

        event.getInteraction().deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        TextChannel createdChannel = event.getGuild().getCategoryById(config.getCashCategoryId())
                .createTextChannel("CASH-" + event.getMember().getEffectiveName())
                .addMemberPermissionOverride(event.getMember().getIdLong(), ALLOWED_PERMISSIONS, DENIED_PERMISSIONS)
                .complete();

        try {

            cash.getTickets().computeIfAbsent(event.getMember().getId(), s -> new ArrayList<>())
                    .add(new Cash.Ticket(event.getMember().getId(), createdChannel.getId(), Cash.TicketType.CASH));

            Main.getCashManager().save(cash);
            Main.reloadConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = config.getTicketOpenMessage()
                .replace("{user}", event.getMember().getAsMention())
                .replace("{staff-role}", String.join(", ", config.getCasherIds()))
                .replace("{channel}", "<#" + config.getInfoChannel() + ">");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("CASH Store");
        builder.setColor(PRIMARY_COLOR);
        builder.setFooter("Click to expand the image.");
        builder.setImage("https://cdn.discordapp.com/attachments/337988003161047040/1212432239871270992/image.png?ex=65f1d083&is=65df5b83&hm=0599590b7fd546984b3ebc0dbef4ec9d010920fc9aab38049ea89277479d7f2d&");

        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, createdChannel.getId());

        textChannel.sendMessageEmbeds(builder.build()).queue();
        textChannel.sendMessage(message).queue();
    }
}

package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

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
                    event.reply("Você já tem uma sala de cash aberta!").setEphemeral(true).queue();
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
        builder.setTitle("Compra de CASH");
        builder.setColor(PRIMARY_COLOR);
        builder.setFooter("Clique para expandir a imagem.");
        builder.setImage("https://cdn.discordapp.com/attachments/963521417217986560/1214762517327511552/image.png?ex=65fa4ac1&is=65e7d5c1&hm=cd851a981719c583390539d8bad50f00657ec0f158fb46e37700e1307ae61a22&");

        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, createdChannel.getId());

        textChannel.sendMessageEmbeds(builder.build()).addActionRow(
                Button.danger("closeSelf", "FECHAR TICKET").withEmoji(Emoji.fromUnicode("\u2716"))
        ).queue();

        textChannel.sendMessage(message).queue();
    }
}

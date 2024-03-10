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

public class IntermedioButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    private final Config config;

    public IntermedioButtonListener(Config config) {
        super(event -> Objects.equals(event.getButton().getId(), "intermedio"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {

        Cash cash = Main.getCashManager().get();

        if (!cash.getTickets().isEmpty()) {
            if (cash.getTickets().get(event.getMember().getId()) != null) {

                Optional<Cash.Ticket> ticket = cash.getTickets().get(event.getMember().getId()).stream().filter(ticket1 -> Cash.TicketType.INTERMEDIO.equals(ticket1.type())).findFirst();

                if (ticket.isPresent()) {
                    event.reply("Você já tem uma sala de cash aberta!").setEphemeral(true).queue();
                    return;
                }
            }
        }

        event.getInteraction().deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        TextChannel createdChannel = event.getGuild().getCategoryById(config.getCashCategoryId())
                .createTextChannel("INTERMÉDIO-" + event.getMember().getEffectiveName())
                .addMemberPermissionOverride(event.getMember().getIdLong(), ALLOWED_PERMISSIONS, DENIED_PERMISSIONS)
                .complete();


        try {

            cash.getTickets()
                    .computeIfAbsent(event.getMember().getId(), s -> new ArrayList<>())
                    .add(new Cash.Ticket(
                            event.getMember().getId(),
                            createdChannel.getId(),
                            Cash.TicketType.INTERMEDIO)
                    );

            Main.getCashManager().save(cash);
            Main.reloadConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = config.getIntermedioOpenMessage()
                .replace("{user}", event.getMember().getAsMention())
                .replace("{staff-role}", String.join(", ", config.getCasherIds()));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Intermédio");
        builder.setColor(PRIMARY_COLOR);
        builder.setFooter("Clique para expandir a imagem.");
        builder.setImage("https://cdn.discordapp.com/attachments/1169072762002874399/1214925277696888853/imagem_plugin_143833bc-6e72-49e6-a7d1-625ac19c74bc.gif?ex=65fae256&is=65e86d56&hm=9d899f9043e79db963278cb97180e131f1adbaa74232fc4f4d9dbc594e1ca615&");

        TextChannel textChannel = event.getGuild().getChannelById(TextChannel.class, createdChannel.getId());

        textChannel.sendMessageEmbeds(builder.build()).addActionRow(
                Button.danger("closeSelf","FECHAR TICKET").withEmoji(Emoji.fromUnicode("\u274C"))
        ).queue();
        textChannel.sendMessage(message).queue();

    }

}

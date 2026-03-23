package br.com.syrxmc.bot.listeners.invite;

import br.com.syrxmc.bot.domain.invite.InviteService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InviteListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InviteListener.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final InviteService inviteService;
    private final String botToken;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public InviteListener(InviteService inviteService, String botToken) {
        this.inviteService = inviteService;
        this.botToken = botToken;
    }

    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
        if (event.getInvite().getInviter() == null) return;
        String code = event.getCode();
        String inviterId = event.getInvite().getInviter().getId();
        try {
            inviteService.upsertInviter(event.getGuild().getId(), code, inviterId);
        } catch (Exception e) {
            logger.error("Error recording invite create for code {}", code, e);
        }
    }

    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
        logger.debug("Invite deleted: {}", event.getCode());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final User user = event.getUser();
        final String guildId = event.getGuild().getId();

        if (user.isBot()) return;
        if (!user.getTimeCreated().isBefore(OffsetDateTime.now().minusDays(7))) return;
        if (!inviteService.isEventActive(guildId)) return;

        RequestBody body = RequestBody.create(
                "{\"and_query\":{\"user_id\":{\"or_query\":[\"" + user.getId() + "\"]}},\"limit\":1}",
                JSON
        );
        Request request = new Request.Builder()
                .url("https://discord.com/api/v10/guilds/" + guildId + "/members-search")
                .addHeader("Authorization", "Bot " + botToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        executorService.schedule(() -> {
            String inviteCode = null;
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    DataObject dataObject = DataObject.fromJson(response.body().string());
                    logger.info("Discord member search response: {}", dataObject);
                    inviteCode = dataObject.getArray("members").getObject(0).getString("source_invite_code");
                }
            } catch (Exception e) {
                logger.error("Error fetching invite code for user {}", user.getId(), e);
            }

            logger.info("Invite code for user {}: {}", user.getId(), inviteCode);

            if (inviteCode == null) return;

            try {
                inviteService.recordJoin(guildId, inviteCode, user.getId());
            } catch (Exception e) {
                logger.error("Error recording invite join for user {}", user.getId(), e);
            }
        }, 40, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        String guildId = event.getGuild().getId();
        String userId = event.getUser().getId();

        try {
            inviteService.recordLeave(guildId, userId);
        } catch (Exception e) {
            logger.error("Error recording leave for user {}", userId, e);
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        Member selfMember = guild.getSelfMember();

        if (selfMember.hasPermission(Permission.MANAGE_SERVER)) {
            guild.retrieveInvites().queue(invites -> invites.forEach(invite -> {
                if (invite.getInviter() == null) return;
                String code = invite.getCode();
                String inviterId = invite.getInviter().getId();
                try {
                    inviteService.upsertInviter(guild.getId(), code, inviterId);
                } catch (Exception e) {
                    logger.debug("Could not warm-up invite code {}: {}", code, e.getMessage());
                }
            }));
        }
    }
}

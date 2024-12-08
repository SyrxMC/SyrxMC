package br.com.syrxmc.bot.core.listeners.invite;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.data.Invites;
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
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static br.com.syrxmc.bot.utils.LeadboardScheduler.ignoredIds;
public class InviteListener extends ListenerAdapter {

    private final OkHttpClient client = new OkHttpClient();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public static Map<String, InviteData> inviteCache = new ConcurrentHashMap<>();
    List<InviteUserCache> userCache = new LinkedList<>();

    private final static Logger logger = LoggerFactory.getLogger(InviteListener.class);

    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
        if (!ignoredIds.contains(event.getInvite().getInviter().getId())) {
            Invites invites = Main.getInvites();
            final String code = event.getCode();
            final InviteData inviteData = new InviteData(event.getInvite());
            inviteCache.put(code, inviteData);

            invites.createInvite(event.getInvite().getInviter().getId(), event.getCode());
            Main.getInvitesDataManager().save(invites);
            Main.reloadConfig();
        }
    }

    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
        final String code = event.getCode();
        inviteCache.remove(code);
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final User user = event.getUser();
        RequestBody body = RequestBody.create("{\"and_query\":{\"user_id\":{\"or_query\":[\"" + event.getMember().getId() + "\"]}},\"limit\":1}", JSON);

        Request request = new Request.Builder()
                .url("https://discord.com/api/v10/guilds/" + event.getGuild().getId() + "/members-search")
                .addHeader("Authorization", "Bot " + Main.getSyrxCore().getConfig().getToken())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        if (user.isBot()) return;
        if (!user.getTimeCreated().isBefore(OffsetDateTime.now().minusDays(7))) return;

        executorService.schedule(() -> {

            String inviteCode = null;
            try (Response response = client.newCall(request).execute()) {

                if (response.isSuccessful()) {
                    DataObject dataObject = DataObject.fromJson(response.body().string());

                    logger.info("Objeto recebido do discord: {}", dataObject);

                    inviteCode = dataObject.getArray("members").getObject(0).getString("source_invite_code");
                }
            } catch (Exception exception) {
                logger.error("Exception", exception);
            }

            logger.info("Invite code: {}", inviteCode);
            System.out.println(inviteCode);


            Main.reloadConfig();

            try {

                Invites invitesData = Main.getInvites();
                invitesData.incrementInvite(inviteCode, user.getId());
                invitesData.createUser(new Invites.User(user.getId(), user.getName(), inviteCode));
                Main.getInvitesDataManager().save(invitesData);
            } catch (Exception exception) {
                logger.error("Exception", exception);
            }
        }, 40, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Main.reloadConfig();
        Invites invites = Main.getInvites();
        Optional<Invites.User> optionalUser = invites.getUsers().stream().filter(user -> user.getId().equals(event.getUser().getId())).findFirst();

        if (optionalUser.isPresent()) {
            Invites.User user = optionalUser.get();
            invites.decrementInvite(user.getInvitedCode(), user.getId());
            invites.deleteUser(user);
        }

        Main.getInvitesDataManager().save(invites);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        Guild guild = event.getGuild();
        attemptInviteCaching(guild);
    }


    private void attemptInviteCaching(Guild guild) {
        final Member selfMember = guild.getSelfMember();
        Invites invitesData = Main.getInvites();

        if (selfMember.hasPermission(Permission.MANAGE_SERVER)) {
            guild.retrieveInvites().queue(retrievedInvites ->
                    retrievedInvites.forEach(retrievedInvite -> {
                        if (!ignoredIds.contains(retrievedInvite.getInviter().getId())) {
                            invitesData.createInvite(retrievedInvite.getInviter().getId(), retrievedInvite.getCode());
                            Main.getInvitesDataManager().save(invitesData);
                        }
                    }));
        }
    }
}
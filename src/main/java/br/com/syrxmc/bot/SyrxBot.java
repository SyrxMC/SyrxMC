package br.com.syrxmc.bot;

import br.com.syrxmc.bot.config.BotConfig;
import br.com.syrxmc.bot.core.command.CommandManager;
import br.com.syrxmc.bot.core.command.SlashCommand;
import br.com.syrxmc.bot.core.command.SlashCommandEvent;
import br.com.syrxmc.bot.core.command.SlashSubcommand;
import br.com.syrxmc.bot.core.listeners.events.DynamicEventHandler;
import br.com.syrxmc.bot.database.MongoClientProvider;
import br.com.syrxmc.bot.database.MongoCollections;
import br.com.syrxmc.bot.database.repositories.GoldStockRepository;
import br.com.syrxmc.bot.database.repositories.GuildConfigRepository;
import br.com.syrxmc.bot.database.repositories.InviteRepository;
import br.com.syrxmc.bot.database.repositories.TicketRepository;
import br.com.syrxmc.bot.domain.gold.GoldStockService;
import br.com.syrxmc.bot.domain.guild.GuildConfigService;
import br.com.syrxmc.bot.domain.invite.InviteService;
import br.com.syrxmc.bot.domain.ticket.TicketService;
import br.com.syrxmc.bot.listeners.ChannelDeleteListener;
import br.com.syrxmc.bot.listeners.MemberJoinListener;
import br.com.syrxmc.bot.listeners.buttons.TicketCloseButtonListener;
import br.com.syrxmc.bot.listeners.buttons.TicketOpenButtonListener;
import br.com.syrxmc.bot.listeners.invite.InviteListener;
import br.com.syrxmc.bot.utils.LeadboardScheduler;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.ACTIVITY;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.CLIENT_STATUS;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.EMOJI;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.SCHEDULED_EVENTS;

public class SyrxBot {

    private static final Logger logger = LoggerFactory.getLogger(SyrxBot.class);

    public static void start() {
        try {
            // 1. Load config
            BotConfig.load();
            logger.info("BotConfig loaded.");

            // 2. Init MongoDB
            MongoClientProvider.init(BotConfig.getMongoUri());
            MongoDatabase db = MongoClientProvider.getDatabase();

            // 3. Create repositories
            TicketRepository ticketRepository = new TicketRepository(db.getCollection(MongoCollections.TICKETS));
            GoldStockRepository goldStockRepository = new GoldStockRepository(db.getCollection(MongoCollections.GOLD_STOCK));
            InviteRepository inviteRepository = new InviteRepository(db.getCollection(MongoCollections.INVITES));
            GuildConfigRepository guildConfigRepository = new GuildConfigRepository(db.getCollection(MongoCollections.GUILD_CONFIG));

            // 4. Create services
            TicketService ticketService = new TicketService(ticketRepository);
            GoldStockService goldStockService = new GoldStockService(goldStockRepository, guildConfigRepository);
            InviteService inviteService = new InviteService(inviteRepository, guildConfigRepository);
            GuildConfigService guildConfigService = new GuildConfigService(guildConfigRepository);

            // 5. Register in ServiceRegistry so commands (instantiated via reflection) can access them
            ServiceRegistry.register(ticketService, goldStockService, inviteService, guildConfigService);

            // 6. Build JDA — command dispatch is handled by an inline ListenerAdapter
            RestAction.setPassContext(true);
            RestAction.setDefaultFailure(ErrorResponseException.ignore(
                    RestAction.getDefaultFailure(),
                    ErrorResponse.UNKNOWN_MESSAGE
            ));

            EventWaiter eventWaiter = new EventWaiter();

            // We need to build the CommandManager first (it scans commands via Reflections)
            // but CommandManager requires JDA. We use a two-phase approach:
            // Phase 1: Build JDA without command listener
            // Phase 2: Create CommandManager and add command listener
            JDA jda = JDABuilder.create(
                            BotConfig.getBotToken(),
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MODERATION,
                            GatewayIntent.GUILD_EXPRESSIONS
                    )
                    .setEventPassthrough(true)
                    .addEventListeners(
                            DynamicEventHandler.getInstance(),
                            eventWaiter,
                            new TicketOpenButtonListener(ticketService, guildConfigService),
                            new TicketCloseButtonListener(ticketService, guildConfigService),
                            new ChannelDeleteListener(ticketService),
                            new MemberJoinListener(guildConfigService),
                            new InviteListener(inviteService, BotConfig.getBotToken())
                    )
                    .disableCache(List.of(EMOJI, CLIENT_STATUS, ACTIVITY, SCHEDULED_EVENTS))
                    .setActivity(Activity.customStatus("Dreamscape SHOP"))
                    .build();

            // 7. Register commands via CommandManager (uses Reflections, requires no-arg constructors)
            CommandManager commandManager = new CommandManager(jda);
            commandManager.publicCommands();
            logger.info("Slash commands registered.");

            // 8. Add command dispatch listener to JDA
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                    SlashCommand command = commandManager.getCommand(event.getName());
                    if (command == null) return;

                    String subcommandName = event.getSubcommandName();
                    if (subcommandName != null) {
                        SlashSubcommand subcommand = command.getSubcommands().get(subcommandName);
                        if (subcommand == null) {
                            event.reply("Invalid subcommand").setEphemeral(true).queue();
                            return;
                        }
                        try {
                            subcommand.execute(event);
                        } catch (Exception e) {
                            logger.error("Error executing subcommand {}/{}", command.getName(), subcommandName, e);
                            if (!event.isAcknowledged()) {
                                event.reply("Ocorreu um erro: " + e.getMessage()).setEphemeral(true).queue();
                            }
                        }
                    } else {
                        try {
                            command.execute(new SlashCommandEvent(event));
                        } catch (Exception e) {
                            logger.error("Error executing command {}", command.getName(), e);
                            if (!event.isAcknowledged()) {
                                event.reply("Ocorreu um erro: " + e.getMessage()).setEphemeral(true).queue();
                            }
                        }
                    }
                }
            });

            // 9. Start LeadboardScheduler
            LeadboardScheduler.setServices(guildConfigService, inviteService, jda);

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            JobDetail job = JobBuilder.newJob(LeadboardScheduler.class).build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                    .build();
            scheduler.scheduleJob(job, trigger);

            logger.info("SyrxBot v2 started successfully.");

        } catch (Exception e) {
            logger.error("Failed to start SyrxBot", e);
            throw new RuntimeException("Failed to start SyrxBot", e);
        }
    }
}

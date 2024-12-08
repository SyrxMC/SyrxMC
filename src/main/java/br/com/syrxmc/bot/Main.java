package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.data.GoldStock;
import br.com.syrxmc.bot.data.Invites;
import br.com.syrxmc.bot.utils.DataManager;
import br.com.syrxmc.bot.utils.LeadboardScheduler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


@Getter
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Getter
    private static DataManager<Cash> cashManager;

    @Getter
    private static Cash cash;

    @Getter
    private static SyrxCore syrxCore;

    @Getter
    private static GoldStock goldStock;

    @Getter
    private static Invites invites;

    @Getter
    private static  DataManager<Invites> invitesDataManager;

    @Getter
    private static DataManager<GoldStock> goldStockDataManager;



    public static void main(String[] args) throws IOException, SchedulerException {

        logger.info("Iniciando o bot...");

        DataManager<Config> configDataManager = new DataManager<>("config.json", Config::new).create();

        configs();

        syrxCore = new SyrxCore(configDataManager.get());

        syrxCore.initialize();

        SchedulerFactory shedFact = new StdSchedulerFactory();

        Scheduler scheduler = shedFact.getScheduler();
        scheduler.start();

        JobDetail job = JobBuilder.newJob(LeadboardScheduler.class)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    @SneakyThrows
    public static void reloadConfig() {
        configs();
    }

    public static void configs() throws IOException {
        cashManager = new DataManager<>("cashTickets.json", Cash::new).create();
        cash = cashManager.get();

        goldStockDataManager = new DataManager<>("goldStock.json", GoldStock::new).create();
        goldStock = goldStockDataManager.get();

        invitesDataManager = new DataManager<>("invites.json", Invites::new).create();
        invites = invitesDataManager.get();
    }

}
package br.com.syrxmc.bot;

import br.com.syrxmc.bot.core.SyrxCore;
import br.com.syrxmc.bot.core.scheduler.ClientExpirationScheduler;
import br.com.syrxmc.bot.data.Clients;
import br.com.syrxmc.bot.data.Config;
import br.com.syrxmc.bot.utils.DataManager;
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
    private static SyrxCore syrxCore;

    @Getter
    private static Clients clients;

    @Getter
    private static DataManager<Clients> clientsData;

    public static void main(String[] args) throws IOException, SchedulerException {

        logger.info("Iniciando o bot...");

        DataManager<Config> configDataManager = new DataManager<>("config.json", Config::new).create();

        configs();

        syrxCore = new SyrxCore(configDataManager.get());

        syrxCore.initialize();

        SchedulerFactory shedFact = new StdSchedulerFactory();

        Scheduler scheduler = shedFact.getScheduler();
        scheduler.start();

        JobDetail job = JobBuilder.newJob(ClientExpirationScheduler.class)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?"))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    @SneakyThrows
    public static void reloadConfig() {
        configs();
    }

    public static void configs() throws IOException {
        clientsData = new DataManager<>("client.json", Clients::new).create();
        clients = clientsData.get();
    }

}

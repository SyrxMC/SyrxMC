package br.com.syrxmc.bot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class ConfigLoader<T> {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private final static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    @SneakyThrows
    public T loadFile(String fileName, Class<T> clazz, Supplier<T> constructor) {
        File file = Paths.get(fileName).toFile();

        if(!file.exists()){
            logger.info("Arquivo de configuração {} criada com sucesso", fileName);
            FileIO.write(fileName, gson.toJson(constructor.get()));
            System.exit(1);
        }

        return gson.fromJson(FileIO.read(fileName), clazz);
    }
}

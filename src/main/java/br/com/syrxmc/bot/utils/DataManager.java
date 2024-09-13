package br.com.syrxmc.bot.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DataManager<T> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private final static Logger LOGGER = LoggerFactory.getLogger(DataManager.class);
    private final String fileName;
    private final Supplier<T> supplier;
    private T config;
    private File file;

    public DataManager<T> create() throws IOException {
        File file = new File(this.fileName);

        if (!file.exists()) {
            if (file.createNewFile()) {
                objectMapper.writeValue(file, supplier.get());
                LOGGER.info("[Data] File {} generated.", file.getAbsolutePath());
                System.exit(0);
            }
        }
        this.config = (T) objectMapper.readValue(file, supplier.get().getClass());

        return this;
    }

    public void reload() throws IOException {
        this.config = (T) objectMapper.readValue(file, supplier.get().getClass());
    }

    public void save(T data) {
        try {
            FileIO.write(fileName, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public T get() {

        return config;
    }
}

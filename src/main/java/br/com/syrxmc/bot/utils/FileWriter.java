package br.com.syrxmc.bot.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWriter {

    public static void write(String fileName, String value) throws IOException {
        Files.writeString(Paths.get(fileName), new String(value.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
    }

    public static String read(String fileName) throws IOException {
      return Files.readString(Paths.get(fileName));
    }
}

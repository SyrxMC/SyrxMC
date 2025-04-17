package br.com.syrxmc.bot.data;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class Clients {

    private Map<String, LocalDateTime> clients = new HashMap<>();


    public void addClient(String clientId) {
        clients.put(clientId, LocalDateTime.now().plusMinutes(1));
    }

}

package br.com.syrxmc.bot.data;

import lombok.Data;

@Data
public class CashDaily {

    // Valor textual para flexibilidade (ex.: "R$ 5,20 / 1k")
    private String value = "";

    private String dolar = "";

}

package br.com.syrxmc.bot.core.listeners;

import br.com.syrxmc.bot.Main;
import br.com.syrxmc.bot.core.listeners.events.DynamicEventHandler;
import br.com.syrxmc.bot.core.listeners.events.DynamicHandler;
import br.com.syrxmc.bot.data.Cash;
import br.com.syrxmc.bot.data.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static br.com.syrxmc.bot.core.listeners.PermissionsConstants.*;
import static br.com.syrxmc.bot.utils.UtilsStatics.PRIMARY_COLOR;

public class CashButtonListener extends DynamicHandler<ButtonInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CashButtonListener.class);
    private final Config config;
    // Mapa de sessões ativas por usuário. A versão garante que handlers antigos sejam ignorados.
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public CashButtonListener(Config config) {
        super(event -> Objects.equals(event.getButton().getId(), "cashMenu"));
        this.config = config;
    }

    @Override
    public void onEvent(ButtonInteractionEvent event) {
        Cash cash = Main.getCashManager().get();

        // Verifica se o usuário já tem um ticket de cash aberto
        List<Cash.Ticket> userTickets = cash.getTickets().get(event.getMember().getId());
        if (userTickets != null) {
            boolean hasOpenCash = userTickets.stream()
                    .anyMatch(t -> Cash.TicketType.CASH.equals(t.type()));
            if (hasOpenCash) {
                event.reply("Você já tem uma sala de cash aberta!").setEphemeral(true).queue();
                return;
            }
        }

        String userId = event.getUser().getId();
        // Ao iniciar nova sessão, incrementamos a versão — handlers anteriores ignoram sua sessão stale
        long version = System.currentTimeMillis();
        Session session = new Session(version);
        SESSIONS.put(userId, session);

        StringSelectMenu paymentMenu = StringSelectMenu.create("cash:payment:" + userId)
                .setPlaceholder("Forma de pagamento")
                .addOptions(
                        SelectOption.of("PIX", "PIX"),
                        SelectOption.of("Binance", "BINANCE")
                )
                .build();

        StringSelectMenu pointsMenu = buildPointsMenu("cash:points:" + userId);

        event.reply("🧾 Compra de Cash – Preencha os Dados\n\nSelecione a forma de pagamento e a quantidade de pontos.")
                .setEphemeral(true)
                .addActionRow(paymentMenu)
                .addActionRow(pointsMenu)
                .queue();

        // Handler para seleção de forma de pagamento
        DynamicEventHandler.getInstance().addListener(new DynamicHandler<StringSelectInteractionEvent>(
                e -> Objects.equals(e.getComponentId(), paymentMenu.getId()) && isCurrentSession(userId, version), false) {
            @Override
            public void onEvent(StringSelectInteractionEvent e) {
                Session s = SESSIONS.get(userId);
                if (s == null || s.version != version) return;

                String value = e.getValues().get(0);
                s.payment = "BINANCE".equals(value) ? "Binance" : "PIX";

                if (s.points > 0) {
                    e.replyModal(buildModal(userId)).queue();
                } else {
                    e.reply("Forma de pagamento selecionada: **" + s.payment + "**\nAgora selecione a quantidade de pontos.").setEphemeral(true).queue();
                }
            }
        }, 15, TimeUnit.MINUTES, () -> SESSIONS.remove(userId, session));

        // Handler para seleção de quantidade de pontos
        DynamicEventHandler.getInstance().addListener(new DynamicHandler<StringSelectInteractionEvent>(
                e -> Objects.equals(e.getComponentId(), pointsMenu.getId()) && isCurrentSession(userId, version), false) {
            @Override
            public void onEvent(StringSelectInteractionEvent e) {
                Session s = SESSIONS.get(userId);
                if (s == null || s.version != version) return;

                String selected = e.getValues().get(0);

                if ("other_value".equals(selected)) {
                    e.replyModal(buildModal(userId, "other_value")).queue();
                    return;
                }

                s.points = Integer.parseInt(selected);

                if (s.payment != null) {
                    e.replyModal(buildModal(userId)).queue();
                } else {
                    e.reply("Quantidade selecionada: **" + s.points + " pontos**\nAgora selecione a forma de pagamento.").setEphemeral(true).queue();
                }
            }
        }, 15, TimeUnit.MINUTES, null);

        // Handler para submissão do modal
        DynamicEventHandler.getInstance().addListener(new DynamicHandler<ModalInteractionEvent>(
                e -> e.getModalId().equals("cash:modal:" + userId) && isCurrentSession(userId, version), false) {
            @Override
            public void onEvent(ModalInteractionEvent e) {
                Session s = SESSIONS.get(userId);
                if (s == null || s.version != version) {
                    e.reply("Sessão expirada ou inválida. Por favor, clique novamente em QUERO CASH.").setEphemeral(true).queue();
                    return;
                }
                SESSIONS.remove(userId, s);

                s.payerName = e.getValue("payerName") != null ? e.getValue("payerName").getAsString() : "";
                s.gameLogin = e.getValue("gameLogin") != null ? e.getValue("gameLogin").getAsString() : "";

                // Parse do valor customizado (caso "outro valor" tenha sido selecionado)
                String customValue = e.getValue("value") != null ? e.getValue("value").getAsString() : null;
                if (customValue != null && !customValue.isBlank()) {
                    try {
                        int qty = Integer.parseInt(customValue.trim());
                        if (qty <= 0) {
                            e.reply("Valor inválido. Informe um número inteiro maior que zero.").setEphemeral(true).queue();
                            return;
                        }
                        s.points = qty;
                    } catch (NumberFormatException ex) {
                        e.reply("Valor inválido. Informe um número inteiro válido para a quantidade de pontos.").setEphemeral(true).queue();
                        return;
                    }
                }

                if (s.payment == null) {
                    e.reply("Forma de pagamento não selecionada. Clique novamente em QUERO CASH.").setEphemeral(true).queue();
                    return;
                }
                if (s.points <= 0) {
                    e.reply("Quantidade de pontos não informada. Clique novamente em QUERO CASH.").setEphemeral(true).queue();
                    return;
                }

                // Criar canal do ticket
                long staffRoleId = parseRoleId(config.getStaffRoleId(), 1352639335039762514L);

                TextChannel createdChannel = e.getGuild()
                        .getCategoryById(config.getCashCategoryId())
                        .createTextChannel("ticket-cash-" + sanitize(e.getMember().getEffectiveName()))
                        .addMemberPermissionOverride(e.getMember().getIdLong(), ALLOWED_PERMISSIONS, DENIED_PERMISSIONS)
                        .addRolePermissionOverride(staffRoleId, ALLOWED_STAFF, new ArrayList<>())
                        .complete();

                Cash cash = Main.getCashManager().get();
                cash.getTickets()
                        .computeIfAbsent(e.getMember().getId(), k -> new ArrayList<>())
                        .add(new Cash.Ticket(e.getMember().getId(), createdChannel.getId(), Cash.TicketType.CASH));

                try {
                    Main.getCashManager().save(cash);
                    Main.reloadConfig();
                } catch (Exception ex) {
                    logger.error("Erro ao salvar ticket de cash", ex);
                }

                TextChannel channel = e.getGuild().getChannelById(TextChannel.class, createdChannel.getId());

                // Embed 1: Dados da solicitação
                EmbedBuilder embed1 = new EmbedBuilder();
                embed1.setTitle("🧾 NOVA SOLICITAÇÃO DE CASH");
                embed1.setColor(PRIMARY_COLOR);
                embed1.addField("Cliente", e.getMember().getAsMention(), false);
                embed1.addField("Forma de pagamento", s.payment, true);
                embed1.addField("Quantidade de pontos", s.points + " pontos", true);
                embed1.addField("Nome do pagador", s.payerName, false);
                embed1.addField("Login informado", s.gameLogin, false);
                embed1.setFooter("⏳ Aguarde, um atendente irá prosseguir com o pagamento.");
                channel.sendMessageEmbeds(embed1.build()).queue();

                // Embed 2: Valor da compra
                String dailyValueBRL = Main.getCashDaily() != null ? Main.getCashDaily().getValue() : null;
                String dailyValueUSD = Main.getCashDaily() != null ? Main.getCashDaily().getDolar() : null;
                Double precoPor100BRL = parsePricePer100(dailyValueBRL);
                Double precoPor100USD = parsePricePer100(dailyValueUSD);
                boolean isUsd = isUsdPayment(s.payment);

                String valorPor100BRL = "—", valorPor100USD = "—", totalBRL = "—", totalUSD = "—";

                if (precoPor100BRL != null) {
                    double totalCalcBrl = s.points * (precoPor100BRL / 100.0);
                    valorPor100BRL = formatCurrency(precoPor100BRL) + " a cada 100 pontos";
                    totalBRL = formatCurrency(totalCalcBrl);
                } else if (dailyValueBRL != null && !dailyValueBRL.isBlank()) {
                    valorPor100BRL = dailyValueBRL;
                }

                if (precoPor100USD != null) {
                    double totalCalcUsd = s.points * (precoPor100USD / 100.0);
                    valorPor100USD = formatCurrencyUSD(precoPor100USD) + " per 100 points";
                    totalUSD = formatCurrencyUSD(totalCalcUsd);
                } else if (dailyValueUSD != null && !dailyValueUSD.isBlank()) {
                    valorPor100USD = dailyValueUSD;
                }

                String pixKey;
                EmbedBuilder embed2 = new EmbedBuilder();
                embed2.setTitle("💰 VALOR DA COMPRA");
                embed2.setColor(PRIMARY_COLOR);

                if (isUsd) {
                    embed2.addField("Valor (USD)", valorPor100USD, false);
                    embed2.addField("Total (USD)", totalUSD, true);
                    pixKey = "803810796";
                    embed2.addField("Binance • Código", pixKey, false);
                } else {
                    embed2.addField("Valor (BRL)", valorPor100BRL, false);
                    embed2.addField("Total (BRL)", totalBRL, true);
                    pixKey = "43c94e7d-064f-4bea-af33-56212981d473";
                    embed2.addField("PIX • Nome", "M.S SERVIÇOS DIGITAIS", false);
                    embed2.addField("PIX • Chave", pixKey, true);
                    embed2.setImage("https://cdn.discordapp.com/attachments/1240266591451611149/1475671130600640643/PIX_XP.png");
                }

                embed2.setFooter("📷 QR Code será enviado abaixo");

                String buttonLabel = "Enviar chave %s no chat".formatted(isUsd ? "Binance" : "Pix");
                var pixButton = net.dv8tion.jda.api.interactions.components.buttons.Button
                        .primary("cash:pixcopy:" + pixKey, buttonLabel);
                var loginButton = net.dv8tion.jda.api.interactions.components.buttons.Button
                        .primary("cash:login:" + s.gameLogin, "Enviar login");

                channel.sendMessageEmbeds(embed2.build())
                        .addActionRow(pixButton)
                        .addActionRow(loginButton)
                        .queue();

                e.reply("Seu ticket foi criado: " + channel.getAsMention()).setEphemeral(true).queue();
            }
        }, 15, TimeUnit.MINUTES, () -> SESSIONS.remove(userId, session));
    }

    private static boolean isCurrentSession(String userId, long version) {
        Session s = SESSIONS.get(userId);
        return s != null && s.version == version;
    }

    private static long parseRoleId(String roleId, long fallback) {
        if (roleId == null || roleId.isBlank()) return fallback;
        try {
            return Long.parseLong(roleId);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static StringSelectMenu buildPointsMenu(String id) {
        StringSelectMenu.Builder b = StringSelectMenu.create(id).setPlaceholder("Quantidade de pontos");
        for (int i = 50; i <= 1000; i += 50) {
            b.addOptions(SelectOption.of(String.valueOf(i), String.valueOf(i)));
        }
        b.addOptions(SelectOption.of("Outro valor", "other_value"));
        return b.build();
    }

    private static Modal buildModal(String userId, String mode) {
        TextInput payer = TextInput.create("payerName", "Nome do pagador", TextInputStyle.SHORT)
                .setPlaceholder("Nome de quem vai realizar o pagamento")
                .setRequired(true)
                .build();
        TextInput login = TextInput.create("gameLogin", "Login do jogo (sem senha)", TextInputStyle.SHORT)
                .setPlaceholder("Ex.: igorluiz123")
                .setRequired(true)
                .build();

        Modal.Builder modal = Modal.create("cash:modal:" + userId, "🧾 Compra de Cash – Preencha os Dados")
                .addActionRow(payer)
                .addActionRow(login);

        if ("other_value".equals(mode)) {
            TextInput valor = TextInput.create("value", "Valor desejado de cash", TextInputStyle.SHORT)
                    .setPlaceholder("Ex.: 4500")
                    .setRequired(true)
                    .build();
            modal.addActionRow(valor);
        }

        return modal.build();
    }

    private static Modal buildModal(String userId) {
        return buildModal(userId, "");
    }

    private static String sanitize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9-]", "-").replaceAll("-+", "-");
    }

    private static Double parsePricePer100(String v) {
        if (v == null) return null;
        String cleaned = v.trim().replaceAll("[^0-9.,kK]", "");
        cleaned = cleaned.replaceAll("(?i)([0-9]+)k\\b", "$18000");

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([0-9]{1,3}(?:[\\.,][0-9]{3})+|[0-9]+)(?:[\\.,][0-9]{1,2})?")
                .matcher(cleaned);

        if (!m.find()) return null;

        String candidate = m.group();
        int lastDot = candidate.lastIndexOf('.');
        int lastComma = candidate.lastIndexOf(',');
        String normalized;

        if (lastDot != -1 && lastComma != -1) {
            normalized = lastComma > lastDot
                    ? candidate.replace(".", "").replace(',', '.')
                    : candidate.replace(",", "");
        } else if (lastComma != -1) {
            normalized = candidate.replace(',', '.');
        } else {
            normalized = candidate;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }

    private static String formatCurrencyUSD(double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }

    private static boolean isUsdPayment(String payment) {
        if (payment == null) return false;
        String p = payment.toLowerCase(Locale.ROOT);
        return p.contains("binance") || p.contains("usd") || p.contains("dolar") || p.contains("dólar") || p.contains("dollar");
    }

    private static class Session {
        final long version;
        String payment;
        int points;
        String payerName;
        String gameLogin;

        Session(long version) {
            this.version = version;
        }
    }
}
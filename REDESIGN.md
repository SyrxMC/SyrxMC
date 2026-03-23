# Syrx Bot — Redesign para v2

## Visão Geral

O Syrx é um bot Discord para o servidor Dreamscape SHOP, gerenciando:
- Sistema de tickets (Cash, Gold, Intermédio)
- Estoque de Gold por servidor
- Rastreamento de convites/referrals
- Leaderboard de convites
- Backup de canais de ticket

A nova versão substitui persistência em JSON por MongoDB, melhora consistência de código e torna o sistema mais robusto e escalável.

---

## Problemas Identificados na v1

### Persistência
- Dados salvos em arquivos `.json` na raiz do projeto — frágil, sem atomicidade
- `DataManager<T>` carrega tudo em memória e salva o objeto inteiro a cada mudança
- Sem controle de concorrência real (apenas `synchronized` em poucos pontos)
- Dados de config misturados com dados de estado (ex.: `lastMenuMessage` no `GoldStock`)
- Sem histórico ou auditoria de operações

### Arquitetura
- `SyrxCore` faz tudo: inicializa o bot, registra listeners, carrega dados, cria o scheduler — God Object
- Listeners acessam dados diretamente via `DataManager.instance()`; acoplamento forte
- Sem camada de serviço separando lógica de negócio do Discord
- `DynamicEventHandler` é poderoso mas complexo demais para o uso atual
- `ChannelDeleteListener` e `CashTicketButtonListener` são novos e ainda não integrados completamente

### Dados
- `Cash.java` usa `Map<String, List<Ticket>>` onde `Ticket` é um record simples — sem timestamps, sem status
- `Invites.java` tem estrutura dual (`invites` por código + `users` por userId) que pode dessincronizar
- IDs de usuários ignorados no leaderboard estão hardcoded em `LeadBoardCommand`
- `GoldStock` tem campos de mensagem Discord misturados com dados de negócio

---

## Modelo de Dados MongoDB

### Coleção: `tickets`

```json
{
  "_id": "ObjectId",
  "userId": "String (Discord ID)",
  "channelId": "String (Discord ID)",
  "guildId": "String (Discord ID)",
  "type": "CASH | GOLD | INTERMEDIO",
  "status": "OPEN | CLOSED",
  "openedAt": "ISODate",
  "closedAt": "ISODate | null",
  "closedBy": "String (Discord ID) | null",
  "saleValue": "Double | null",
  "backupPath": "String | null"
}
```

**Índices:**
- `{ userId: 1, type: 1, status: 1 }` — verificar ticket aberto por tipo
- `{ channelId: 1 }` — lookup por canal
- `{ status: 1, closedAt: -1 }` — listagem de fechados

---

### Coleção: `gold_stock`

```json
{
  "_id": "ObjectId",
  "guildId": "String",
  "serverName": "String",
  "amount": "Long",
  "updatedAt": "ISODate",
  "updatedBy": "String (Discord ID)"
}
```

**Índice:**
- `{ guildId: 1, serverName: 1 }` unique — um registro por servidor por guild

---

### Coleção: `invites`

```json
{
  "_id": "ObjectId",
  "guildId": "String",
  "inviterUserId": "String",
  "inviteCode": "String",
  "count": "Integer",
  "invitedUserIds": ["String"],
  "updatedAt": "ISODate"
}
```

**Índice:**
- `{ guildId: 1, inviteCode: 1 }` unique
- `{ guildId: 1, inviterUserId: 1 }` — busca por usuário
- `{ guildId: 1, count: -1 }` — leaderboard

---

### Coleção: `guild_config`

```json
{
  "_id": "ObjectId",
  "guildId": "String",
  "token": "String",
  "channels": {
    "menu": "String",
    "info": "String",
    "greet": "String",
    "invite": "String",
    "logs": "String"
  },
  "roles": {
    "cash": "String",
    "gold": "String",
    "intermedio": "String"
  },
  "categories": {
    "tickets": "String"
  },
  "messages": {
    "lastMenuMessageId": "String | null",
    "lastLeaderboardMessageId": "String | null",
    "lastGoldStockMessageId": "String | null"
  },
  "inviteEventActive": "Boolean",
  "ignoredUserIds": ["String"],
  "greetMessage": "String",
  "greetImageUrl": "String",
  "color": "String (hex)"
}
```

---

### Coleção: `ticket_logs` (auditoria)

```json
{
  "_id": "ObjectId",
  "ticketId": "ObjectId (ref tickets)",
  "action": "OPENED | CLOSED | ADDED_MEMBER | BACKED_UP",
  "performedBy": "String (Discord ID)",
  "timestamp": "ISODate",
  "metadata": {}
}
```

---

## Nova Estrutura de Pacotes

```
src/main/java/br/com/syrxmc/bot/
├── Main.java                          # Apenas bootstrap
├── SyrxBot.java                       # Inicialização do JDA + wiring
│
├── config/
│   └── BotConfig.java                 # Lê variáveis de ambiente / config.yml
│
├── database/
│   ├── MongoClientProvider.java       # Singleton MongoClient
│   ├── MongoCollections.java          # Constantes de nomes de coleção
│   └── repositories/
│       ├── TicketRepository.java
│       ├── GoldStockRepository.java
│       ├── InviteRepository.java
│       └── GuildConfigRepository.java
│
├── domain/
│   ├── ticket/
│   │   ├── Ticket.java                # Modelo de domínio
│   │   ├── TicketType.java            # Enum
│   │   ├── TicketStatus.java          # Enum
│   │   └── TicketService.java         # Lógica de negócio
│   ├── gold/
│   │   ├── GoldStock.java
│   │   └── GoldStockService.java
│   ├── invite/
│   │   ├── InviteData.java
│   │   ├── InviteService.java
│   │   └── InviteCache.java           # Cache em memória dos invite codes
│   └── guild/
│       ├── GuildConfig.java
│       └── GuildConfigService.java
│
├── commands/
│   ├── PingCommand.java
│   ├── ticket/
│   │   ├── CloseCommand.java
│   │   └── AddMemberCommand.java
│   ├── gold/
│   │   ├── GoldAddCommand.java
│   │   ├── GoldRemoveCommand.java
│   │   └── GoldMenuCommand.java
│   ├── invite/
│   │   ├── ConvideiCommand.java
│   │   ├── LeadBoardCommand.java
│   │   └── InviteEventCommand.java
│   └── admin/
│       └── CashMenuCommand.java
│
├── listeners/
│   ├── CommandListener.java
│   ├── MemberJoinListener.java
│   ├── ChannelDeleteListener.java
│   └── buttons/
│       ├── TicketOpenButtonListener.java   # Unifica Cash/Gold/Intermedio
│       ├── TicketCloseButtonListener.java  # Self-close
│       └── CashActionButtonListener.java   # PIX copy, login reveal
│
├── framework/
│   ├── command/
│   │   ├── SlashCommand.java
│   │   ├── SlashSubcommand.java
│   │   ├── CommandManager.java
│   │   └── annotations/RegisterCommand.java
│   └── events/
│       ├── DynamicEventHandler.java
│       └── DynamicHandler.java
│
└── utils/
    ├── EmbedBuilder.java              # Factory de embeds padronizados
    ├── TicketBackupService.java       # Backup de mensagens (extrai de listeners)
    ├── LeadboardScheduler.java
    └── TimeUtils.java
```

---

## Fluxo da Nova Versão

### Inicialização

```
Main.main()
  └── SyrxBot.start()
        ├── BotConfig.load()           # Lê MONGO_URI, BOT_TOKEN do ambiente
        ├── MongoClientProvider.init() # Conecta ao MongoDB
        ├── GuildConfigService.load()  # Carrega configs das guilds
        ├── InviteCache.warm()         # Pré-carrega invite codes do Discord
        ├── JDA.builder()              # Configura JDA
        │     ├── CommandManager.registerAll()
        │     └── EventListeners.*
        └── LeadboardScheduler.start()
```

---

### Fluxo de Criação de Ticket

```
Usuário clica em botão [CASH | GOLD | INTERMÉDIO]
  └── TicketOpenButtonListener.onButtonInteraction()
        ├── Extrai tipo do componentId
        ├── TicketService.hasOpenTicket(userId, type, guildId)
        │     └── TicketRepository.findOpen(userId, type, guildId)
        │           └── MongoDB: { userId, type, status: OPEN }
        ├── [SE JÁ TEM] → Reply efêmera de erro
        └── [SE NÃO TEM]
              ├── Guild.createTextChannel() com permissões
              ├── TicketService.create(userId, channelId, type, guildId)
              │     └── TicketRepository.insert(ticket)
              ├── TicketLogRepository.log(OPENED, userId)
              └── Canal.sendMessage(embed + botões)
```

---

### Fluxo de Fechamento de Ticket

```
Admin usa /fechar cash|gold|intermedio [valor?]
  └── CloseCommand.execute()
        ├── GuildConfigService.getConfig(guildId)
        ├── TicketService.findByChannel(channelId)
        │     └── TicketRepository.findByChannelId(channelId)
        ├── TicketBackupService.backup(channel)
        │     └── Salva mensagens em files/tickets/{TYPE}/{name}_{ts}/
        ├── TicketService.close(ticketId, closedBy, saleValue)
        │     └── TicketRepository.updateOne(
        │           { _id },
        │           { $set: { status: CLOSED, closedAt, closedBy, saleValue } }
        │         )
        ├── TicketLogRepository.log(CLOSED, adminId, { saleValue })
        ├── [SE valor] → logs channel embed com valor da venda
        └── channel.delete()
```

---

### Fluxo de Gold

```
Admin usa /addgold <servidor> <quantidade>
  └── GoldAddCommand.execute()
        ├── GoldStockService.add(guildId, serverName, amount, adminId)
        │     └── GoldStockRepository.upsert(
        │           { guildId, serverName },
        │           { $inc: { amount }, $set: { updatedAt, updatedBy } }
        │         )
        └── GoldStockService.refreshDisplay(guildId)
              ├── GoldStockRepository.findAll(guildId)
              ├── Monta embed com estoque atual
              ├── GuildConfigService.getLastGoldStockMessageId(guildId)
              ├── [SE existe] → message.editMessage(embed)
              └── [SE não] → channel.sendMessage(embed)
                    └── GuildConfigService.setLastGoldStockMessageId(guildId, msgId)
```

---

### Fluxo de Convites

```
Membro entra no servidor
  └── InviteListener.onGuildMemberJoin()
        ├── [SE evento inativo] → return
        ├── Aguarda 40s (API propagation delay)
        ├── Consulta Discord API /guilds/{id}/members-search
        ├── Extrai invite code usado
        ├── InviteService.recordJoin(guildId, inviteCode, newUserId)
        │     └── InviteRepository.updateOne(
        │           { guildId, inviteCode },
        │           { $inc: { count: 1 }, $push: { invitedUserIds: newUserId } }
        │         )
        └── InviteCache.update(inviteCode)

Membro sai do servidor
  └── InviteListener.onGuildMemberRemove()
        ├── InviteService.findInviterOf(guildId, userId)
        │     └── InviteRepository.findByInvitedUser(guildId, userId)
        └── [SE encontrado]
              └── InviteRepository.updateOne(
                    { guildId, inviteCode },
                    { $inc: { count: -1 }, $pull: { invitedUserIds: userId } }
                  )
```

---

## Dependências Novas (build.gradle)

```groovy
// MongoDB Driver
implementation 'org.mongodb:mongodb-driver-sync:4.11.1'

// Remover Jackson standalone (MongoDB BSON já tem serialização)
// Manter para configurações externas se necessário

// Dotenv para variáveis de ambiente
implementation 'io.github.cdimascio:dotenv-java:3.0.0'
```

---

## Configuração de Ambiente (.env)

```env
BOT_TOKEN=seu_token_aqui
MONGO_URI=mongodb://localhost:27017/syrx
# ou Atlas: mongodb+srv://user:pass@cluster.mongodb.net/syrx
```

O arquivo `config.json` atual migra para `guild_config` no MongoDB.
A coleção pode ser populada na primeira execução via comando admin `/setup`.

---

## Melhorias de Qualidade

### 1. Listeners de Botão Unificados
Em vez de `CashButtonListener`, `GoldButtonListener`, `IntermedioButtonListener` separados com código duplicado, um único `TicketOpenButtonListener` que extrai o tipo do `componentId`:

```java
// componentId pattern: "open_ticket:CASH", "open_ticket:GOLD", "open_ticket:INTERMEDIO"
String[] parts = event.getComponentId().split(":");
TicketType type = TicketType.valueOf(parts[1]);
```

### 2. Repositórios Tipados
Cada entidade tem seu repositório com métodos semânticos:

```java
public class TicketRepository {
    public Optional<Ticket> findOpenByUserAndType(String userId, TicketType type, String guildId) { }
    public Optional<Ticket> findByChannelId(String channelId) { }
    public Ticket insert(Ticket ticket) { }
    public void close(ObjectId id, String closedBy, Double saleValue) { }
    public List<Ticket> findClosedBetween(Instant from, Instant to) { }
}
```

### 3. Serviços de Domínio
Lógica de negócio separada dos listeners:

```java
public class TicketService {
    private final TicketRepository repository;
    private final GuildConfigService configService;

    public boolean hasOpenTicket(String userId, TicketType type, String guildId) {
        return repository.findOpenByUserAndType(userId, type, guildId).isPresent();
    }

    public Ticket create(String userId, String channelId, TicketType type, String guildId) {
        Ticket ticket = new Ticket(userId, channelId, type, guildId);
        return repository.insert(ticket);
    }
}
```

### 4. EmbedFactory Centralizado
Em vez de construir embeds espalhados pelos listeners, uma factory:

```java
public class SyrxEmbeds {
    public static MessageEmbed ticketWelcome(TicketType type, Member user, Config config) { }
    public static MessageEmbed ticketClosed(Ticket ticket, Member closedBy) { }
    public static MessageEmbed goldStock(List<GoldStock> stocks) { }
    public static MessageEmbed leaderboard(List<InviteData> top5) { }
    public static MessageEmbed error(String message) { }
}
```

### 5. Tratamento de Erros Consistente
Todos os command handlers e listeners devem capturar exceções e responder adequadamente:

```java
try {
    ticketService.create(...);
    event.reply("Ticket criado!").setEphemeral(true).queue();
} catch (DuplicateTicketException e) {
    event.reply("Você já possui um ticket aberto deste tipo.").setEphemeral(true).queue();
} catch (Exception e) {
    log.error("Erro ao criar ticket", e);
    event.reply("Erro interno. Tente novamente.").setEphemeral(true).queue();
}
```

---

## Ordem de Implementação

1. **Setup MongoDB** — `MongoClientProvider`, `MongoCollections`, `.env` config
2. **Repositórios** — CRUD básico para cada coleção
3. **Migração de dados** — Script ou comando `/migrate` que lê os JSONs e insere no MongoDB
4. **Serviços de domínio** — `TicketService`, `GoldStockService`, `InviteService`
5. **Unificar listeners de botão** — `TicketOpenButtonListener` único
6. **Atualizar commands** — Usar serviços em vez de DataManager direto
7. **EmbedFactory** — Centralizar criação de embeds
8. **Auditoria** — `TicketLogRepository` para rastrear ações
9. **Testes** — Testar fluxos principais com MongoDB embarcado (Flapdoodle)
10. **Cleanup** — Remover JSON files, `DataManager`, `FileIO` legados

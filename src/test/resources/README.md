# Setup ambiente LocalStack + Lambda

## Prerequisiti

- **Docker** e **Docker Compose** installati sul sistema

## Permessi di esecuzione sullo script delle lambda per S.O windows
```bash
dos2unix ./src/test/resources/testcontainers/initsh-for-lambdas.sh
chmod +x ./src/test/resources/testcontainers/initsh-for-lambdas.sh
```
### 2. Avvio dell'ambiente LocalStack
Avviare i container LocalStack e l'applicazione:

```bash
docker-compose up localstack
```

### 3. Verifica dello stato
Controllare che i container siano in esecuzione:

```bash
docker compose ps
```

### 4. Pulizia e ricostruzione
Per fermare tutti i container e rifare il build da zero:

```bash
# Fermare e rimuovere i container
docker compose down

# Rimuovere le immagini (opzionale, per forzare rebuild completo)
docker compose down --rmi all

```

## Comandi utili per il debug

### Visualizzare i log
```bash
# Tutti i servizi
docker compose logs -f

# Solo LocalStack
docker compose logs -f localstack

# Solo l'applicazione
docker compose logs -f app
```

### Accesso ai container
```bash
# Accesso al container LocalStack
docker compose exec localstack bash

# Accesso al container dell'applicazione
docker compose exec app bash
```

### Reset completo dell'ambiente
```bash
# Fermare tutto e pulire volumi e network
docker compose down -v --remove-orphans
```

## Endpoint LocalStack

Una volta avviato, LocalStack sarà disponibile su:
- **API Gateway**: `http://localhost:4566`
- **Lambda**: `http://localhost:4566`
- **S3**: `http://localhost:4566`

## Configurazione

### File JSON di configurazione
È stato aggiunto un nuovo file JSON nella cartella `config` per gestire le variabili d'ambiente delle Lambda. Questo file viene utilizzato per:

- Passare le variabili d'ambiente alle funzioni Lambda di LocalStack
- Essere convertito tramite il comando `jq` negli script di deployment
- Centralizzare la configurazione dell'ambiente di sviluppo

Il file viene processato automaticamente durante il setup delle Lambda per garantire che abbiano accesso alle configurazioni necessarie.

## Note

- I dati di LocalStack vengono persistiti nel volume Docker
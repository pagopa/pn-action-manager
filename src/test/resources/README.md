# Setup ambiente LocalStack e Creazione risorse AWS

## Test in locale con LocalStack

All'avvio del container LocalStack tramite Docker Compose puoi testare le Lambda in locale senza dipendere da AWS. Il setup include:
- Installazione delle dipendenze delle Lambda
- Creazione degli archivi zip e li utilizza per creare le Lambda tramite il servizio
- Configurazioni delle tabelle DynamoDB, stream Kinesis, code SQS
- Deploy delle Lambda collegandole agli stream Kinesis

## Prerequisiti

- **Docker** e **Docker Compose** installati sul sistema:
  - Per installare Docker, segui le istruzioni su [Docker Docs](https://docs.docker.com/get-docker/).
  - Per installare Docker Compose, segui le istruzioni su [Docker Compose Docs](https://docs.docker.com/compose/install/).

## STEP per il test in locale

- Setup dei permessi di esecuzione sullo script delle lambda (vd. paragrafo relativo)
- avvio dell'ambiente localstack  (vd. paragrafo relativo)
- avvio dell'applicativo java 
- inserimento/unschedule delle azioni via http (di default **action-manager** risponde sulla porta **8081**)

[ref01]: ## Permessi di esecuzione sullo script delle Lambda (Windows)

```bash
# Converti lo script in formato Unix e rendilo eseguibile
dos2unix ./src/test/resources/testcontainers/initsh-for-lambdas.sh
chmod +x ./src/test/resources/testcontainers/initsh-for-lambdas.sh
```

### Avvio dell'ambiente LocalStack
Avvia i container LocalStack e l'applicazione:

```bash
# Avvia LocalStack e l'applicazione
 docker compose up localstack
```

### Verifica dello stato
Controlla che i container siano in esecuzione:

```bash
 #Verifica lo stato dei container
docker compose ps
```

### Pulizia e ricostruzione
Per fermare tutti i container e ricostruire da zero:

```bash 
# Ferma e rimuovi i container
docker compose down
# Opzionale, per forzare il rebuild completo
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
# Ferma tutto e pulisci volumi e network
docker compose down -v --remove-orphans
```
## LocalStack Web
Per accedere all'interfaccia web di LocalStack e visualizzare tute le risorse: [LocalStack Web](https://app.localstack.cloud/inst/default/resources)
## Configurazione

### File JSON di configurazione

Nella cartella `config` di ogni lambda è presente un file JSON `local-env-variables.json`  per la gestione delle variabili d'ambiente delle Lambda. Questo file viene utilizzato per:
- Passare le variabili d'ambiente alle funzioni Lambda su LocalStack
- Viene convertito tramite il comando `jq` negli script di deployment

Il file viene processato automaticamente durante il setup delle Lambda per garantire che abbiano accesso alle configurazioni necessarie.

### Note
All'avvio del container di LocalStack verranno installate le dipendenze jq e zip utili per la creazione degli zip delle lambda.

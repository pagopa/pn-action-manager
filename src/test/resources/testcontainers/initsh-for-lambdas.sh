#!/bin/bash

# Mi posiziono nella cartella in cui sono presenti le lambda e la libreria actionCommon
cd ../../lambdas

apt-get update && apt-get install -y jq zip

FORCE_LAMBDA_ZIP=${FORCE_LAMBDA_ZIP}

echo "Valore env FORCE_LAMBDA_ZIP: $FORCE_LAMBDA_ZIP"

echo "Inizio - Fase 1 : Installazione dipendenze actionCommon"
if [ -d ./functionsLib/actionCommon ]; then
  cd ./functionsLib/actionCommon || exit 1
  npm install --production
  cd - || exit 1
  echo "Installazione completata."
else
  echo "Directory ./functionsLib/actionCommon non trovata."
  exit 1
fi
echo "Fine - Fase 1 : Installazione dipendenze actionCommon"

echo "Inizio - Fase 2 : Installazione dipendenze lambdas"
for dir in ./functions/*; do
  if [ -d "$dir" ]; then
    echo "Install in $(basename "$dir")"
    cd "$dir" || exit 1
    npm install --production
    cd - || exit 1
  fi
done
echo "Fine - Fase 2 : Installazione dipendenze lambdas"


echo "Inizio - Fase 3 : Creazione zip lambda"
for dir in ./functions/*; do
  if [ -d "$dir" ]; then
    echo "Inizio zip nella folder $(basename "$dir")"
    cd "$dir" || exit 1
    if [ "FORCE_LAMBDA_ZIP" = "true" ] || [ ! -f ./function.zip ]; then
      [ -f ./function.zip ] && rm -f ./function.zip
      zip -r ./function.zip . -x './src/test/*' -x '*.md' -x './nodejs/*' -x './.nyc_output/*' -x './.scannerwork/*' -x './coverage/*' -x '*.env' -x '*.zip' -x '*.gitignore'
    else
      echo "E' già presente uno zip nella dir $dir e la configurazione FORCE_ZIP non è impostata a true, quindi non verrà ricreato."
    fi
    cd - || exit 1
  fi
done
echo "Fine - Fase 3 : Creazione zip lambda"

echo "Inizio - Fase 4 : Creazione tabelle DynamoDB"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name Action \
    --attribute-definitions \
        AttributeName=actionId,AttributeType=S \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_IMAGE \
    --key-schema \
        AttributeName=actionId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name FutureAction \
    --attribute-definitions \
        AttributeName=timeSlot,AttributeType=S \
        AttributeName=actionId,AttributeType=S \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES \
    --key-schema \
        AttributeName=timeSlot,KeyType=HASH \
        AttributeName=actionId,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name LastPollForFutureAction \
    --attribute-definitions \
        AttributeName=lastPoolKey,AttributeType=N \
    --key-schema \
        AttributeName=lastPoolKey,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

# Inseriamo un record necessario per la lambda future-action-remover
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb put-item \
    --table-name LastPollForFutureAction \
    --item "{\"lastPoolKey\": {\"N\": \"3\"}, \"lastPollExecuted\": {\"S\": \"$(date -u -d '1 minute ago' '+%Y-%m-%dT%H:%M')\"}}"
echo "Fine - Fase 4 : Creazione tabelle DynamoDB"

echo "Inizio - Fase 5 : Creazione stream Kinesis"

aws kinesis create-stream \
    --stream-name action-stream \
    --shard-count 1 \
    --region us-east-1 \
    --endpoint-url=http://localstack:4566

aws kinesis create-stream \
    --stream-name future-action-stream \
    --shard-count 1 \
    --region us-east-1 \
    --endpoint-url=http://localstack:4566

echo "Fine - Fase 5 : Creazione stream Kinesis"

echo "Inizio - Fase 6 : Creazione legame stream Kinesis con stream DynamoDB"
aws dynamodb enable-kinesis-streaming-destination \
  --table-name Action \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/action-stream

# Controlla se la destinazione di streaming Kinesis è abilitata per la tabella Action
aws dynamodb describe-kinesis-streaming-destination \
  --table-name Action \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws dynamodb enable-kinesis-streaming-destination \
  --table-name FutureAction \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/future-action-stream

# Controlla se la destinazione di streaming Kinesis è abilitata per la tabella Action
aws dynamodb describe-kinesis-streaming-destination \
  --table-name FutureAction \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

echo "Fine - Fase 6 : Creazione legame stream Kinesis con stream DynamoDB"

echo "Inizio - Fase 7 : Creazione event bridge"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
      events create-event-bus \
    --name core-bus

echo "Fine - Fase 7 : Creazione event bridge"

# Mi posiziono nella cartella in cui sono presenti le lambda
cd ./functions

echo "Inizio - Fase 8 : Creazione pn-action-router lambda"

# Trasforma il tuo JSON originale convertendo gli oggetti in stringhe
# Estrai le variabili d'ambiente in formato KEY=VALUE
jq '{Variables: (.Variables | to_entries | map({key: .key, value: (if (.value | type) == "object" then (.value | tostring) else .value end)}) | from_entries)}' \
  ./pn-action-router-manager/config/local-env-variables.json > /tmp/action-router-env.json

aws lambda create-function \
  --function-name action-router-lambda \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --environment file:///tmp/action-router-env.json \
  --timeout 30 \
  --region us-east-1 \
  --endpoint-url=http://localhost:4566 \
  --zip-file fileb://./pn-action-router-manager/function.zip

aws logs create-log-group \
  --log-group-name /aws/lambda/action-router-lambda \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws lambda create-event-source-mapping \
  --function-name action-router-lambda \
  --event-source-arn arn:aws:kinesis:us-east-1:000000000000:stream/action-stream \
  --starting-position LATEST \
  --filter-criteria '{"Filters":[{"Pattern":"{\"data\":{\"eventName\":[\"INSERT\"]}}"}]}' \
  --batch-size 100 \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --maximum-retry-attempts 2 \

echo "Fine - Fase 8 : Creazione pn-action-router lambda"

echo "Inizio - Fase 9 : Creazione future-action-remover lambda"
# Estrai le variabili d'ambiente in formato KEY=VALUE
jq '{Variables: (.Variables | to_entries | map({key: .key, value: (if (.value | type) == "object" then (.value | tostring) else .value end)}) | from_entries)}' \
  ./future-action-remover-manager/config/local-env-variables.json > /tmp/future-action-remover-env.json

aws lambda create-function \
  --function-name future-action-remover \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --timeout 30 \
  --environment file:///tmp/future-action-remover-env.json \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --zip-file fileb://./future-action-remover-manager/function.zip

# Crea il log group per future-action-remover
aws logs create-log-group \
  --log-group-name /aws/lambda/future-action-remover \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

echo " - Create EventBridge scheduled rule for future-action-remover"
aws events put-rule \
  --name future-action-remover-schedule \
  --schedule-expression "rate(1 minute)" \
  --description "Scheduled rule to invoke future-action-remover Lambda every hour" \
  --state ENABLED \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws events put-targets \
  --rule future-action-remover-schedule \
  --targets '[
    {
      "Id": "1",
      "Arn": "arn:aws:lambda:us-east-1:000000000000:function:future-action-remover",
      "Input": "{\"source\":\"eventbridge-scheduler\",\"detail-type\":\"Scheduled Event\",\"detail\":{\"action\":\"cleanup\",\"timestamp\":\"scheduled\"}}"
    }
  ]' \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

# Concede permesso a EventBridge di invocare la Lambda
aws lambda add-permission \
  --function-name future-action-remover \
  --statement-id allow-eventbridge \
  --action lambda:InvokeFunction \
  --principal events.amazonaws.com \
  --source-arn arn:aws:events:us-east-1:000000000000:rule/future-action-remover-schedule \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566
echo "Fine - Fase 9 : Creazione future-action-remover lambda"


echo "Inizio - Fase 10 : Creazione pn-action-enqueuer lambda"
# Trasforma il tuo JSON originale convertendo gli oggetti in stringhe
# Estrai le variabili d'ambiente in formato KEY=VALUE
jq '{Variables: (.Variables | to_entries | map({key: .key, value: (if (.value | type) == "object" then (.value | tostring) else .value end)}) | from_entries)}' \
  ./action-enqueuer-manager/config/local-env-variables.json > /tmp/action-enqueuer-env.json

aws lambda create-function \
  --function-name action-enqueuer-lambda \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --environment file:///tmp/action-router-env.json \
  --timeout 30 \
  --region us-east-1 \
  --endpoint-url=http://localhost:4566 \
  --zip-file fileb://./action-enqueuer-manager/function.zip

aws logs create-log-group \
  --log-group-name /aws/lambda/action-enqueuer-lambda \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws lambda create-event-source-mapping \
  --function-name action-enqueuer-lambda \
  --event-source-arn arn:aws:kinesis:us-east-1:000000000000:stream/future-action-stream \
  --starting-position LATEST \
  --filter-criteria '{"Filters":[{"Pattern":"{\"data\":{\"eventName\":[\"REMOVE\"]}}"}]}' \
  --batch-size 100 \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --maximum-retry-attempts 2 \

echo "Fine - Fase 10 : Creazione pn-action-enqueuer lambda"
echo "Inizializzazione terminata"